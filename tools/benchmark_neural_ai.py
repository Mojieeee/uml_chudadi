#!/usr/bin/env python3
"""Evaluate the production NeuralMoveModel weights against simple baselines."""

from __future__ import annotations

import argparse
from dataclasses import dataclass
import json
import random
import re
import sys
from pathlib import Path
from typing import Callable, List, Optional, Sequence, Tuple

from train_neural_ai import (
    HandInfo,
    Model,
    card_sort_key,
    classify,
    deal,
    encode_self_play_features,
    legal_plays,
    remove_cards,
    teacher_score,
)


ROOT = Path(__file__).resolve().parents[1]
DEFAULT_MODEL_PATH = ROOT / "app/src/main/java/com/example/uml_chudadi/controller/NeuralAiStrategy.kt"
Policy = Callable[[Sequence[Sequence[int]], int, Optional[HandInfo], int, bool, Sequence[Sequence[int]]], List[int]]


@dataclass(frozen=True)
class DifficultyProfile:
    name: str
    noise: float


DIFFICULTY_PROFILES = [
    DifficultyProfile("Easy", 3.0),
    DifficultyProfile("Normal", 0.80),
    DifficultyProfile("Hard", 0.0),
]


def parse_double_array(raw: str) -> List[float]:
    return [float(value.strip()) for value in raw.split(",") if value.strip()]


def load_production_model(path: Path) -> Tuple[str, Model]:
    text = path.read_text(encoding="utf-8")
    version = re.search(r'const val VERSION = "([^"]+)"', text)
    section = text.split("object NeuralMoveModel", 1)[1]
    hidden_units_match = re.search(r"const val HIDDEN_UNITS = ([0-9]+)", section)
    if hidden_units_match is None:
        raise ValueError(f"Missing HIDDEN_UNITS in {path}")
    hidden_units = int(hidden_units_match.group(1))
    arrays = re.findall(r"doubleArrayOf\(([^)]*)\)", section)
    expected_arrays = hidden_units + 2
    if len(arrays) < expected_arrays:
        raise ValueError(f"Expected at least {expected_arrays} numeric arrays in {path}, got {len(arrays)}")
    hidden_weights = [parse_double_array(raw) for raw in arrays[:hidden_units]]
    hidden_bias = parse_double_array(arrays[hidden_units])
    output_weights = parse_double_array(arrays[hidden_units + 1])
    output_bias_match = re.search(r"OUTPUT_BIAS = ([\-0-9.]+)", section)
    if output_bias_match is None:
        raise ValueError(f"Missing OUTPUT_BIAS in {path}")
    return (
        version.group(1) if version else "unknown",
        Model(hidden_weights, hidden_bias, output_weights, float(output_bias_match.group(1))),
    )


def int32(value: int) -> int:
    value &= 0xFFFFFFFF
    return value - 0x1_0000_0000 if value >= 0x8000_0000 else value


def stable_noise(
    hands: Sequence[Sequence[int]],
    current_player: int,
    last_hand: Optional[HandInfo],
    pass_count: int,
    first_turn: bool,
    cards: Sequence[int],
) -> float:
    value = 17
    value = int32(value * 31 + current_player)
    value = int32(value * 31 + current_player)
    value = int32(value * 31 + pass_count)
    value = int32(value * 31 + (1 if first_turn else 0))
    value = int32(value * 31 + (last_hand[0] if last_hand is not None else 0))
    for hand in hands:
        value = int32(value * 31 + len(hand))
    for card in sorted(cards, key=card_sort_key):
        value = int32(value * 31 + card_rank(card) * 4 + card_suit(card))
    mixed = int32(value ^ ((value & 0xFFFFFFFF) >> 16))
    return ((mixed & 0xFFFF) / 32767.5) - 1.0


def card_rank(card: int) -> int:
    return card % 13


def card_suit(card: int) -> int:
    return card // 13


def neural_policy(model: Model, profile: DifficultyProfile = DIFFICULTY_PROFILES[-1]) -> Policy:
    def choose(
        hands: Sequence[Sequence[int]],
        current_player: int,
        last_hand: Optional[HandInfo],
        pass_count: int,
        first_turn: bool,
        legal: Sequence[Sequence[int]],
    ) -> List[int]:
        hand_size = len(hands[current_player])
        for cards in legal:
            if len(cards) == hand_size:
                return list(cards)

        def key(cards: Sequence[int]) -> Tuple[float, int, int, Tuple[int, int]]:
            features = encode_self_play_features(hands, current_player, last_hand, pass_count, first_turn, cards)
            info = classify(cards)
            high = card_sort_key(max(cards, key=card_sort_key))
            score = model.predict(features)
            if profile.noise:
                score += profile.noise * stable_noise(hands, current_player, last_hand, pass_count, first_turn, cards)
            return (
                score,
                len(cards),
                info[0] if info is not None else 0,
                (-high[0], -high[1]),
            )

        return list(max(legal, key=key))

    return choose


def teacher_policy() -> Policy:
    def choose(
        hands: Sequence[Sequence[int]],
        current_player: int,
        last_hand: Optional[HandInfo],
        pass_count: int,
        first_turn: bool,
        legal: Sequence[Sequence[int]],
    ) -> List[int]:
        def key(cards: Sequence[int]) -> Tuple[float, int, int, Tuple[int, int]]:
            features = encode_self_play_features(hands, current_player, last_hand, pass_count, first_turn, cards)
            info = classify(cards)
            high = card_sort_key(max(cards, key=card_sort_key))
            return (
                teacher_score(features),
                len(cards),
                info[0] if info is not None else 0,
                (-high[0], -high[1]),
            )

        return list(max(legal, key=key))

    return choose


def greedy_policy() -> Policy:
    def choose(
        hands: Sequence[Sequence[int]],
        current_player: int,
        last_hand: Optional[HandInfo],
        pass_count: int,
        first_turn: bool,
        legal: Sequence[Sequence[int]],
    ) -> List[int]:
        def key(cards: Sequence[int]) -> Tuple[int, int, Tuple[int, int]]:
            info = classify(cards)
            return (
                len(cards),
                info[0] if info is not None else 99,
                card_sort_key(max(cards, key=card_sort_key)),
            )

        return list(min(legal, key=key))

    return choose


def play_game(seed: int, policies: Sequence[Policy], max_turns: int = 260) -> Tuple[Optional[int], int]:
    rng = random.Random(seed)
    hands = deal(rng)
    current_player = next(index for index, hand in enumerate(hands) if 3 * 13 in hand)
    last_hand: Optional[HandInfo] = None
    last_player: Optional[int] = None
    pass_count = 0
    first_turn = True

    for turn in range(1, max_turns + 1):
        legal = legal_plays(hands, current_player, last_hand, first_turn)
        if not legal:
            if last_player is None:
                return None, turn
            pass_count += 1
            if pass_count >= 3:
                current_player = last_player
                last_hand = None
                pass_count = 0
                first_turn = False
            else:
                current_player = (current_player + 1) % 4
            continue

        cards = policies[current_player](hands, current_player, last_hand, pass_count, first_turn, legal)
        hand_info = classify(cards)
        hands[current_player] = remove_cards(hands[current_player], cards)
        if not hands[current_player]:
            return current_player, turn
        last_hand = hand_info
        last_player = current_player
        pass_count = 0
        first_turn = False
        current_player = (current_player + 1) % 4
    return None, max_turns


def evaluate_vs_greedy(
    model: Model,
    games: int,
    seed: int,
    profile: DifficultyProfile = DIFFICULTY_PROFILES[-1],
) -> Tuple[int, int, float, int, float, float]:
    wins = 0
    completed = 0
    total_turns = 0
    policy = neural_policy(model, profile)
    greedy = greedy_policy()
    for index in range(games):
        neural_seat = index % 4
        policies = [greedy, greedy, greedy, greedy]
        policies[neural_seat] = policy
        winner, turns = play_game(seed + index, policies)
        total_turns += turns
        if winner is not None:
            completed += 1
        if winner == neural_seat:
            wins += 1
    win_rate = wins / games if games else 0.0
    completion_rate = completed / games if games else 0.0
    average_turns = total_turns / games if games else 0.0
    return wins, games, win_rate, completed, completion_rate, average_turns


def evaluate_teacher_agreement(model: Model, states: int, seed: int) -> Tuple[int, int, float, float]:
    rng = random.Random(seed)
    exact = 0
    top_three = 0
    visited = 0
    neural = neural_policy(model, DIFFICULTY_PROFILES[-1])
    teacher = teacher_policy()
    while visited < states:
        hands = deal(rng)
        current_player = next(index for index, hand in enumerate(hands) if 3 * 13 in hand)
        last_hand: Optional[HandInfo] = None
        last_player: Optional[int] = None
        pass_count = 0
        first_turn = True
        for _turn in range(240):
            legal = legal_plays(hands, current_player, last_hand, first_turn)
            if not legal:
                if last_player is None:
                    break
                pass_count += 1
                if pass_count >= 3:
                    current_player = last_player
                    last_hand = None
                    pass_count = 0
                    first_turn = False
                else:
                    current_player = (current_player + 1) % 4
                continue

            teacher_cards = teacher(hands, current_player, last_hand, pass_count, first_turn, legal)
            neural_cards = neural(hands, current_player, last_hand, pass_count, first_turn, legal)
            teacher_ranked = sorted(
                legal,
                key=lambda cards: teacher_score(encode_self_play_features(hands, current_player, last_hand, pass_count, first_turn, cards)),
                reverse=True,
            )
            if set(neural_cards) == set(teacher_cards):
                exact += 1
            if any(set(neural_cards) == set(cards) for cards in teacher_ranked[:3]):
                top_three += 1
            visited += 1
            if visited >= states:
                break

            cards = teacher_cards
            hand_info = classify(cards)
            hands[current_player] = remove_cards(hands[current_player], cards)
            if not hands[current_player]:
                break
            last_hand = hand_info
            last_player = current_player
            pass_count = 0
            first_turn = False
            current_player = (current_player + 1) % 4
    exact_rate = exact / visited if visited else 0.0
    top_three_rate = top_three / visited if visited else 0.0
    return exact, visited, exact_rate, top_three_rate


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--model-path", type=Path, default=DEFAULT_MODEL_PATH)
    parser.add_argument("--games", type=int, default=64)
    parser.add_argument("--states", type=int, default=240)
    parser.add_argument("--seed", type=int, default=20260610)
    parser.add_argument("--min-win-rate", type=float, default=None)
    parser.add_argument("--min-completion-rate", type=float, default=None)
    parser.add_argument("--min-top3-rate", type=float, default=None)
    parser.add_argument("--json-out", type=Path)
    args = parser.parse_args()

    version, model = load_production_model(args.model_path)
    profile_results = {}
    for profile in DIFFICULTY_PROFILES:
        wins, games, win_rate, completed, completion_rate, average_turns = evaluate_vs_greedy(
            model,
            args.games,
            args.seed,
            profile,
        )
        profile_results[profile.name] = {
            "games": games,
            "wins": wins,
            "winRate": round(win_rate, 6),
            "completed": completed,
            "completionRate": round(completion_rate, 6),
            "averageTurns": round(average_turns, 3),
            "scoreNoise": profile.noise,
        }
    primary = profile_results["Hard"]
    exact, states, exact_rate, top_three_rate = evaluate_teacher_agreement(model, args.states, args.seed + 10_000)

    print(f"model_version={version}")
    for profile in DIFFICULTY_PROFILES:
        result = profile_results[profile.name]
        print(
            f"{profile.name.lower()}_vs_greedy "
            f"games={result['games']} wins={result['wins']} win_rate={result['winRate']:.3f} "
            f"completed={result['completed']} completion_rate={result['completionRate']:.3f} "
            f"avg_turns={result['averageTurns']:.1f}"
        )
    print(
        "teacher_agreement "
        f"states={states} exact={exact} exact_rate={exact_rate:.3f} top3_rate={top_three_rate:.3f}"
    )
    if args.json_out is not None:
        report = {
            "modelVersion": version,
            "seed": args.seed,
            "neuralVsGreedy": primary,
            "difficultyProfiles": profile_results,
            "teacherAgreement": {
                "states": states,
                "exact": exact,
                "exactRate": round(exact_rate, 6),
                "top3Rate": round(top_three_rate, 6),
            },
            "thresholds": {
                "minWinRate": args.min_win_rate,
                "minCompletionRate": args.min_completion_rate,
                "minTop3Rate": args.min_top3_rate,
            },
        }
        args.json_out.parent.mkdir(parents=True, exist_ok=True)
        args.json_out.write_text(json.dumps(report, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")

    failures = []
    if args.min_win_rate is not None and primary["winRate"] < args.min_win_rate:
        failures.append(f"hard_win_rate {primary['winRate']:.3f} < {args.min_win_rate:.3f}")
    if args.min_completion_rate is not None and primary["completionRate"] < args.min_completion_rate:
        failures.append(f"hard_completion_rate {primary['completionRate']:.3f} < {args.min_completion_rate:.3f}")
    if args.min_top3_rate is not None and top_three_rate < args.min_top3_rate:
        failures.append(f"top3_rate {top_three_rate:.3f} < {args.min_top3_rate:.3f}")
    if failures:
        print("benchmark_failed=" + "; ".join(failures), file=sys.stderr)
        raise SystemExit(1)


if __name__ == "__main__":
    main()
