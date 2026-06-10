#!/usr/bin/env python3
"""Verify Neural AI model metadata, training manifest, and benchmark report."""

from __future__ import annotations

import argparse
import hashlib
import json
import re
import sys
from pathlib import Path
from typing import Any, Dict, List


ROOT = Path(__file__).resolve().parents[1]
DEFAULT_MODEL_PATH = ROOT / "app/src/main/java/com/example/uml_chudadi/controller/NeuralAiStrategy.kt"
DEFAULT_TRAINING_MANIFEST = ROOT / "docs/ai/neural_ai_training_manifest.json"
DEFAULT_BENCHMARK_REPORT = ROOT / "docs/ai/neural_ai_benchmark_report.json"


def require_match(pattern: str, text: str, label: str) -> str:
    match = re.search(pattern, text)
    if match is None:
        raise ValueError(f"Missing {label}")
    return match.group(1)


def parse_model(path: Path) -> Dict[str, Any]:
    text = path.read_text(encoding="utf-8")
    section = text.split("object NeuralMoveModel", 1)[1]
    hidden_units = int(require_match(r"const val HIDDEN_UNITS = ([0-9]+)", section, "hidden units"))
    arrays = re.findall(r"doubleArrayOf\(([^)]*)\)", section)
    hidden_weights = [parse_array(raw) for raw in arrays[:hidden_units]]
    hidden_bias = parse_array(arrays[hidden_units]) if len(arrays) > hidden_units else []
    output_weights = parse_array(arrays[hidden_units + 1]) if len(arrays) > hidden_units + 1 else []
    output_bias = float(require_match(r"OUTPUT_BIAS = (-?[0-9.]+)", section, "output bias"))
    weights = {
        "hiddenWeights": hidden_weights,
        "hiddenBias": hidden_bias,
        "outputWeights": output_weights,
        "outputBias": output_bias,
    }
    return {
        "version": require_match(r'const val VERSION = "([^"]+)"', section, "model version"),
        "weightChecksum": require_match(r'const val WEIGHT_CHECKSUM = "([a-f0-9]+)"', section, "weight checksum"),
        "computedWeightChecksum": weight_checksum(weights),
        "trainingSeed": int(require_match(r"const val TRAINING_SEED = ([0-9]+)", section, "training seed")),
        "selfPlayGames": int(require_match(r"const val SELF_PLAY_GAMES = ([0-9]+)", section, "self-play games")),
        "hiddenUnits": hidden_units,
        "featureCount": int(require_match(r"const val FEATURE_COUNT = ([0-9]+)", text, "feature count")),
        "hiddenWeightRows": len(hidden_weights),
        "hiddenWeightColumns": len(hidden_weights[0]) if hidden_weights else 0,
        "hiddenBiasSize": len(hidden_bias),
        "outputWeightSize": len(output_weights),
        "parameterCount": sum(len(row) for row in hidden_weights) + len(hidden_bias) + len(output_weights) + 1,
    }


def parse_array(raw: str) -> List[float]:
    return [float(value.strip()) for value in raw.split(",") if value.strip()]


def weight_checksum(weights: Dict[str, Any]) -> str:
    payload = {
        "hiddenWeights": [[f"{value:.6f}" for value in row] for row in weights["hiddenWeights"]],
        "hiddenBias": [f"{value:.6f}" for value in weights["hiddenBias"]],
        "outputWeights": [f"{value:.6f}" for value in weights["outputWeights"]],
        "outputBias": f"{weights['outputBias']:.6f}",
    }
    encoded = json.dumps(payload, separators=(",", ":"), ensure_ascii=True).encode("utf-8")
    return hashlib.sha256(encoded).hexdigest()


def read_json(path: Path) -> Dict[str, Any]:
    return json.loads(path.read_text(encoding="utf-8"))


def repo_relative(path: Path) -> str:
    resolved = path.resolve()
    try:
        return resolved.relative_to(ROOT).as_posix()
    except ValueError:
        return path.as_posix()


def verify(model: Dict[str, Any], training: Dict[str, Any], benchmark: Dict[str, Any]) -> List[str]:
    failures = []
    checks = [
        ("model version matches training manifest", model["version"], training["modelVersion"]),
        ("model version matches benchmark report", model["version"], benchmark["modelVersion"]),
        ("declared checksum matches computed weights", model["weightChecksum"], model["computedWeightChecksum"]),
        ("model checksum matches training manifest", model["weightChecksum"], training["weightChecksum"]),
        ("training seed matches manifest", model["trainingSeed"], training["seed"]),
        ("benchmark seed matches manifest", benchmark["seed"], training["seed"]),
        ("feature count matches manifest", model["featureCount"], training["featureCount"]),
        ("hidden units matches manifest", model["hiddenUnits"], training["hiddenUnits"]),
        ("self-play games match manifest", model["selfPlayGames"], training["training"]["selfPlayGames"]),
        ("parameter count matches manifest", model["parameterCount"], training["parameterCount"]),
        ("hidden weight row count matches metadata", model["hiddenWeightRows"], model["hiddenUnits"]),
        ("hidden weight column count matches feature count", model["hiddenWeightColumns"], model["featureCount"]),
        ("hidden bias size matches hidden units", model["hiddenBiasSize"], model["hiddenUnits"]),
        ("output weight size matches hidden units", model["outputWeightSize"], model["hiddenUnits"]),
    ]
    for label, left, right in checks:
        if left != right:
            failures.append(f"{label}: {left!r} != {right!r}")

    thresholds = benchmark.get("thresholds", {})
    win_rate = benchmark["neuralVsGreedy"]["winRate"]
    completion_rate = benchmark["neuralVsGreedy"]["completionRate"]
    top3_rate = benchmark["teacherAgreement"]["top3Rate"]
    threshold_checks = [
        ("win rate threshold", win_rate, thresholds.get("minWinRate")),
        ("completion rate threshold", completion_rate, thresholds.get("minCompletionRate")),
        ("top3 agreement threshold", top3_rate, thresholds.get("minTop3Rate")),
    ]
    for label, value, threshold in threshold_checks:
        if threshold is not None and value < threshold:
            failures.append(f"{label}: {value:.6f} < {threshold:.6f}")
    return failures


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--model-path", type=Path, default=DEFAULT_MODEL_PATH)
    parser.add_argument("--training-manifest", type=Path, default=DEFAULT_TRAINING_MANIFEST)
    parser.add_argument("--benchmark-report", type=Path, default=DEFAULT_BENCHMARK_REPORT)
    parser.add_argument("--json-out", type=Path)
    args = parser.parse_args()

    model = parse_model(args.model_path)
    training = read_json(args.training_manifest)
    benchmark = read_json(args.benchmark_report)
    failures = verify(model, training, benchmark)
    report = {
        "modelVersion": model["version"],
        "artifactCheck": "failed" if failures else "passed",
        "model": model,
        "modelPath": repo_relative(args.model_path),
        "trainingManifest": repo_relative(args.training_manifest),
        "benchmarkReport": repo_relative(args.benchmark_report),
        "failures": failures,
    }
    if args.json_out is not None:
        args.json_out.parent.mkdir(parents=True, exist_ok=True)
        args.json_out.write_text(json.dumps(report, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")
    if failures:
        print("artifact_check=failed")
        for failure in failures:
            print(f"- {failure}", file=sys.stderr)
        raise SystemExit(1)
    print(
        "artifact_check=passed "
        f"model_version={model['version']} feature_count={model['featureCount']} "
        f"hidden_units={model['hiddenUnits']} parameter_count={model['parameterCount']}"
    )


if __name__ == "__main__":
    main()
