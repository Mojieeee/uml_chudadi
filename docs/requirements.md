# 锄大地需求分析与评分点映射

## 项目目标

本项目实现一款 Android 锄大地扑克牌游戏。玩家界面按正式棋牌游戏设计，课程交付材料保存在 `docs/` 目录。系统覆盖单机人机对局、好友蓝牙对局、南北玩法规则、多人房间、AI 难度策略、UML 建模、MVC 分层和测试验证。

## 功能需求

1. 玩家进入大厅后可选择人机对局、好友蓝牙对局、规则设置、设置和教程。
2. 人机对局支持简单、普通、困难三档难度，采用不同 AI 策略完成 1 名玩家对 3 名人机的四人对局。
3. 好友蓝牙对局支持创建房间和加入对局，房间固定四个座位，房主可添加/移除人机并选择人机难度。
4. 房间必须四个座位全部有人或人机且准备完成后才能开始；房主负责生成 seed、同步规则和广播开局消息。
5. 游戏支持发牌、首出判定、牌型识别、出牌、过牌、三家过牌重置、胜负结算和再来一局。
6. 游戏支持南北玩法：
   - 北方规则：黑桃 3 首出，四带一和同花顺可作为增强强牌跨牌型压制普通牌。
   - 南方规则：方块 3 首出，同张数压制，五张牌之间按顺子、同花、葫芦、四带一、同花顺等级比较。
7. 联机对局采用房主权威同步：客户端发送 `MOVE_REQUEST`，房主校验后广播 `MOVE_ACCEPTED` 和 `STATE_SNAPSHOT`。
8. 玩家无可压牌时显示“手上没有可以大过人家的牌”，并只允许不出。
9. 背景音乐、震动、音效、金币、战绩和默认设置保存到本机。
10. 项目提供 debug 与 release 两类安装包；release 包使用本地证书签名，并提供 Android Studio 一键 `runRelease` 配置。

## 非功能需求

1. 代码按 MVC 思想分层：Model 保存领域对象和规则，Controller 处理回合、AI 和房间逻辑，View 只负责 Compose 展示和用户意图。
2. AI 使用策略模式，`AiStrategy` 可替换为贪心、启发式和 Monte Carlo Rollout 策略。
3. 蓝牙和本地模拟通过 `GameTransport` 抽象，便于真机联机和自动化模拟测试共用协议。
4. UML 使用 ProcessOn UML 作为正式建模工具；项目内保留 `.puml` 和导出的 `.png` 作为版本管理和交付备份。
5. 核心规则、消息协议、房间座位和蓝牙竞态逻辑必须有单元测试或模拟测试覆盖。

## 评分点映射

| 评分标准 | 当前项目对应内容 |
| --- | --- |
| ProcessOn UML 7 大图建模 | `docs/uml` 提供用例图、类图、顺序图、状态图、活动图、组件图、部署图；正式报告中按这些内容迁移/绘制到 ProcessOn |
| 需求分析和面向对象设计 | `docs/requirements.md`、`docs/design.md`、`docs/final_checklist.md` |
| 设计模式 | AI 策略模式、`GameTransport` 接口隔离、房主权威同步、单一职责拆分 |
| UI 和页面切换 | 大厅、难度、规则、设置、教程、蓝牙房间、牌桌、结算页 |
| 多人蓝牙连接 | `BluetoothHostTransport`、`BluetoothClientTransport`、四座位房间、房主广播同步 |
| 南北规则 | `NorthRuleSet`、`SouthRuleSet`、`RuleProfile`、规则回归测试 |
| 多 AI 策略 | `GreedyAiStrategy`、`HeuristicAiStrategy`、`MonteCarloRolloutAiStrategy` |
| AI 工具使用说明 | `docs/ai_usage.md` 记录 AI 在需求、设计、编码、测试和文档中的使用 |
| 发布与验收 | `release/` 提供签名 APK/AAB，`docs/release_testing.md` 记录 debug/release、runRelease 和验收流程 |
