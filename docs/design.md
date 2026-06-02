# 锄大地设计说明

## 架构与 MVC 分层

项目采用 MVC 思想组织代码：

- Model：`Card`、`Deck`、`HandType`、`RuleProfile`、`RuleSet`、`GameState`、`Player`、`RoomSeat`、`GameSnapshot`，负责领域数据、规则配置和同步快照。
- Controller：`GameController`、`AiController`、`PlayerActionPolicy`、`RoomController`、`NetworkMoveGuard`，负责回合推进、出牌校验、AI 决策、玩家操作可用性和联机请求保护。
- View：`ChudadiApp` 和 Compose 页面，负责页面状态、动画、音乐反馈和用户点击事件，不直接修改规则模型。

`GameState` 是牌桌唯一状态源。出牌、过牌、AI 行动和蓝牙同步都生成新状态，界面根据状态重组。

## 核心职责

| 模块 | 关键类 | 职责 |
| --- | --- | --- |
| 牌与牌型 | `Card`、`Deck`、`HandClassifier`、`HandType` | 牌面排序、发牌、识别单张/对子/三张/五张牌型 |
| 规则 | `RuleProfile`、`NorthRuleSet`、`SouthRuleSet` | 首出牌、炸弹增强、同张数限制、五张牌等级比较 |
| 回合 | `GameController`、`GameState`、`Move` | 校验出牌、处理不出、三家过牌重置、胜负结算 |
| AI | `AiStrategy`、`GreedyAiStrategy`、`HeuristicAiStrategy`、`MonteCarloRolloutAiStrategy` | 用策略模式切换不同出牌算法 |
| 联机 | `GameTransport`、`BluetoothHostTransport`、`BluetoothClientTransport` | 统一真实蓝牙和模拟传输 |
| 房间 | `RoomSeat`、`RoomSeatKind`、`GameMessageCodec` | 四座位、准备状态、人机补位、消息编解码 |
| 同步 | `GameSnapshot`、`NetworkMoveGuard` | 房主权威校验、重复请求过滤、状态快照恢复 |
| 体验 | `CardRoomMusicPlayer`、`TableAnimationKeys` | 背景音乐、音效、回合动画 key |

## 设计模式与设计原则

1. 策略模式：`AiStrategy` 抽象 AI 算法，简单、普通、困难三种策略可替换，控制器只依赖接口。
2. 接口隔离：`GameTransport` 隔离蓝牙实现、本地模拟和测试替身，View 不直接操作 socket。
3. 主机权威模式：蓝牙房主统一校验出牌并广播快照，避免多端各自结算导致状态分叉。
4. 单一职责：牌型识别、规则判断、回合流转、AI 决策、消息传输、UI 展示分别封装。
5. 状态快照模式：`GameSnapshot` 表示可同步的牌局状态，用于断线恢复、重复消息修正和客户端刷新。

## UML 知识点应用

项目使用 ProcessOn UML 完成 7 类建模，并在 `docs/uml` 保留源文件和导出图片：

- 用例图：描述玩家、房主、加入者、人机与主要用例的关系。
- 类图：描述 Model、Controller、Transport、View/Audio 的类和接口依赖。
- 顺序图：描述本地出牌和蓝牙房主权威同步的调用顺序。
- 状态图：描述大厅、规则、蓝牙房间、牌桌、暂停和结算状态转换。
- 活动图：描述从开局、出牌、过牌到结算/再开局的业务流程。
- 组件图：描述 UI、控制器、模型、消息、蓝牙、音乐和系统能力的组件关系。
- 部署图：描述房主手机、加入者手机、Android Bluetooth Stack 和单机测试环境的部署关系。

## 数据流

本地对局中，View 收集玩家选择后调用 `GameController.play()` 或 `pass()`；控制器通过 `RuleSet` 和 `HandClassifier` 校验合法性，返回新的 `GameState`。轮到人机时，`AiController` 调用当前难度对应的 `AiStrategy` 生成 `Move`。

蓝牙对局中，加入者只发送 `MOVE_REQUEST`。房主使用 `NetworkMoveGuard` 判断请求是否来自当前远端玩家，再调用 `GameController` 结算，最后广播 `MOVE_ACCEPTED` 和 `STATE_SNAPSHOT`。客户端收到后以房主快照为准刷新界面。

## 错误处理

- 非法牌型：保留选择并显示规则提示。
- 无可压牌：禁用出牌和提示，显示“手上没有可以大过人家的牌”。
- 重复或过期蓝牙请求：房主不改变牌局，只回发当前快照。
- 房间未满或未准备：开始按钮禁用并显示缺少座位。
- 蓝牙权限或设备异常：页面展示权限说明、重新搜索和返回入口。
