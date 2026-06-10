# Neural AI Pipeline

本项目的简单、普通、困难三档 AI 共用端侧轻量 MLP 模型 `mlp-selfplay-distilled-v3`。模型只对 `GameController.legalPlays` 产生的候选出牌评分，不直接生成牌，因此规则合法性仍由原有游戏控制器保证。

## Model

- 输入特征：23 维，覆盖出牌张数、牌型强度、主牌点数、剩余手牌、对手压力、过牌轮次、炸弹/五张牌标记、拆牌代价、剩余牌组结构等。
- 网络结构：单隐藏层 MLP，16 个 `tanh` 隐藏单元。
- 参数规模：401 个参数。
- 运行方式：Kotlin 端内置权重，零运行时第三方依赖，不增加蓝牙消息协议字段。
- 难度分层：困难档使用完整模型评分；普通档和简单档在模型分数上加入确定性探索噪声，形成强度梯度，但仍只在合法候选牌内选择。

## Training

训练入口：

```bash
python3 tools/train_neural_ai.py --manifest docs/ai/neural_ai_training_manifest.json
```

当前训练清单：

- 随机合成训练样本：1800
- 自对弈训练样本：4427，来自 32 局自对弈
- 验证样本：1686
- 训练轮数：140
- 验证集 MSE：0.105922
- 验证集 MAE：0.155211

完整机器可读清单见 `docs/ai/neural_ai_training_manifest.json`。

## Benchmark

评估入口：

```bash
python3 tools/benchmark_neural_ai.py --games 512 --states 300 --min-win-rate 0.38 --min-completion-rate 1.0 --min-top3-rate 0.95 --json-out docs/ai/neural_ai_benchmark_report.json
```

当前评估结果：

- 简单档对三名简单基线胜率：168 / 512 = 0.328125
- 普通档对三名简单基线胜率：189 / 512 = 0.369141
- 困难档对三名简单基线胜率：204 / 512 = 0.398438
- 困难档完局率：512 / 512 = 1.000000
- 与教师策略精确一致率：277 / 300 = 0.923333
- 与教师策略 Top 3 一致率：0.996667

完整机器可读评估见 `docs/ai/neural_ai_benchmark_report.json`。

## Artifact Check

校验入口：

```bash
python3 tools/verify_neural_ai_artifacts.py --json-out docs/ai/neural_ai_artifact_check.json
```

该脚本会同时读取 Kotlin 生产模型、训练清单和评估报告，校验模型版本、训练 seed、特征数、隐藏层规模、参数量、自对弈局数以及 benchmark 阈值是否一致。校验报告使用仓库相对路径，便于在其他机器上复查。当前结果为 `artifact_check=passed`，完整机器可读校验见 `docs/ai/neural_ai_artifact_check.json`。

## Bluetooth Boundary

神经网络 AI 复用原有 `Difficulty.Easy`、`Difficulty.Normal`、`Difficulty.Hard` 三档，不新增蓝牙消息类型、协议字段或额外难度枚举。蓝牙消息仍通过已有 `RoomSeat.difficulty` 字段按枚举名传输。相关回归测试覆盖 `GameMessageCodecTest`、`RoomSeatTest` 和 `SimulatedBluetoothHubTest`。
