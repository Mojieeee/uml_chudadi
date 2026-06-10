# SCRUM 迭代与团队协作记录

## 迭代目标

| Sprint | 目标 | 主要产出 |
| --- | --- | --- |
| Sprint 1 | 需求分析和 UML 建模 | 需求说明、用例图、类图、状态图 |
| Sprint 2 | 核心规则和人机对局 | 牌型识别、南北规则、GameController、AI 策略 |
| Sprint 3 | 蓝牙联机和房间 | 创建/加入房间、四座位、消息协议、房主权威同步 |
| Sprint 4 | 发布版 UI 和动画 | 大厅、牌桌、结算、音乐、动效和设置 |
| Sprint 5 | 测试、文档和交付 | 单元测试、模拟蓝牙测试、UML 更新、测试报告 |
| Sprint 6 | 细化迭代 3：AI、玩家中心和体验完善 | 三档 AI、状态建模、成长系统、头像商店、金币消费确认 |
| Sprint 7 | 发布收尾和蓝牙稳定性增强 | TransportEvent、客户端断线托管、房主断线退出提示、release 包、GitHub 上传准备 |


## 风险处理

- 真机数量不足：使用 `SimulatedBluetoothHubTest`、`RoomSeatTest`、`GameMessageCodecTest` 覆盖三人同时加入、房满、移动请求、房主广播、客户端断线托管和房主断线退出提示。
- 规则存在地区差异：使用 `RuleProfile` 封装南北规则，便于按老师要求调整。
- 动画卡顿：参考 Android 官方 Compose 性能建议，减少每帧布局和对象分配。
- 文档与代码不同步：最终交付前用 `docs/final_checklist.md` 逐项核查。
- 签名证书丢失：`keystore/chudadi-release.jks` 和 `keystore.properties` 只本机备份，不上传 GitHub。
