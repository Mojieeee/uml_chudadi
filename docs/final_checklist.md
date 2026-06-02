# 最终评分检查表

| 评分点 | 完成情况 | 对应材料 |
| --- | --- | --- |
| ProcessOn UML 7 大图 | 已有 7 类图内容，需最终在 ProcessOn 中确认/导出 | `docs/uml` |
| 需求分析和面向对象设计 | 已完成当前版本需求和 MVC 设计说明 | `docs/requirements.md`、`docs/design.md` |
| 设计模式 | 策略模式、接口隔离、主机权威同步、状态快照、单一职责 | `docs/design.md` |
| UI 页面切换 | 大厅、难度、规则、设置、教程、蓝牙、牌桌、结算 | `ChudadiApp.kt` |
| 多人蓝牙连接 | 创建房间、加入对局、四座位、房主广播、模拟测试 | `transport/`、`SimulatedBluetoothHubTest` |
| 南北规则 | 北方黑桃 3 + 炸弹增强，南方方块 3 + 同张数限制 | `RuleSet.kt`、`HandClassifierTest` |
| 多 AI 策略 | 简单、普通、困难三种策略，使用 `AiStrategy` | `AiStrategy.kt`、`GameControllerTest` |
| AI 工具说明 | 已按阶段说明 AI 使用和效果评价 | `docs/ai_usage.md` |
| Release 交付 | 已生成签名 APK/AAB，并提供一键 `runRelease` | `release/`、`app/build.gradle.kts` |

## 提交前必须完成

1. 重新运行 `./gradlew test --no-daemon`。
2. 重新运行 `./gradlew :app:assembleDebug --no-daemon`。
3. 重新运行 `./gradlew :app:assembleRelease --no-daemon`。
4. 使用 `apksigner verify` 检查 release APK 签名。
5. 使用 ProcessOn 检查并导出 7 张 UML 图片。
6. 准备 Android Studio 运行截图、真机或模拟器运行截图。
7. 准备至少一组蓝牙创建/加入/房间/牌桌截图。
8. 检查玩家 App 内不出现课程说明文字。
9. 确认 `keystore/chudadi-release.jks` 已备份；后续升级必须使用同一证书。
