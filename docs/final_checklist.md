# 最终评分检查表

| 评分点 | 完成情况 | 对应材料 |
| --- | --- | --- |
| ProcessOn UML 7 大图 | 已有 7 类 PlantUML 图内容，正式图按 PlantUML 代码导入/参照迁移到 ProcessOn UML 后确认导出 | `docs/uml` |
| 需求分析和面向对象设计 | 已完成当前版本需求和 MVC 设计说明 | `docs/requirements.md`、`docs/design.md` |
| 设计模式 | 策略模式、接口隔离、主机权威同步、状态快照、单一职责 | `docs/design.md` |
| UI 页面切换 | 大厅、难度、规则、设置、教程、蓝牙、牌桌、结算 | `ChudadiApp.kt` |
| 多人蓝牙连接 | 创建房间、加入对局、四座位、房主广播、客户端断线托管、房主断线退出提示、模拟测试 | `transport/`、`SimulatedBluetoothHubTest`、`RoomSeatTest` |
| 南北规则 | 北方黑桃 3 + 炸弹增强，南方方块 3 + 同张数限制 | `RuleSet.kt`、`HandClassifierTest` |
| 多 AI 策略 | 简单、普通、困难三种策略，使用 `AiStrategy` | `AiStrategy.kt`、`GameControllerTest` |
| AI 工具说明 | 已按阶段说明 AI 使用和效果评价 | `docs/ai_usage.md` |
| Release 交付 | 已生成签名 APK/AAB，并提供一键 `runRelease` | `release/`、`app/build.gradle.kts` |
| 玩家成长系统 | 已实现等级、金币、战绩、成就、头像商店、自定义头像和消费确认 | `profile/`、`ChudadiApp.kt`、`ProfileControllerTest` |
| 发布版体验 | 已实现开屏页、大厅动画、发牌动画、音乐、音效、震动和结算奖励 | `ChudadiApp.kt`、`CardRoomMusicPlayer.kt` |

## 提交前必须完成

1. 重新运行 `./gradlew test --no-daemon`。
2. 重新运行 `./gradlew :app:assembleDebug --no-daemon`。
3. 重新运行 `./gradlew :app:assembleRelease --no-daemon`。
4. 使用 `apksigner verify` 检查 release APK 签名。
5. 使用 PlantUML 代码导入或参照迁移到 ProcessOn UML，检查并导出 7 张正式 UML 图片。
6. 准备 Android Studio 运行截图、真机或模拟器运行截图。
7. 准备至少一组蓝牙创建/加入/房间/牌桌截图；多机可用时补充客户端断线托管和房主断线退出提示截图。
8. 检查玩家 App 内不出现课程说明文字。
9. 确认 `keystore/chudadi-release.jks` 已备份；后续升级必须使用同一证书。
10. 确认 GitHub 仓库不包含 `keystore.properties`、`.jks`、`local.properties`、`.gradle`、`.kotlin` 和 `build/`。
11. 如需开源，补充根目录 `LICENSE`；当前项目文档只说明素材来源和签名安全，不等于已授予开源许可。
