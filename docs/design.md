# 锄大地设计说明

## 架构与 MVC 分层

项目采用 MVC 思想组织代码：

- Model：`Card`、`Deck`、`HandType`、`RuleProfile`、`RuleSet`、`GameState`、`Player`、`RoomSeat`、`GameSnapshot`、`PlayerProfile`，负责领域数据、规则配置、同步快照和玩家档案数据。
- Controller：`GameController`、`AiController`、`PlayerActionPolicy`、`RoomController`、`NetworkMoveGuard`、`ProfileController`，负责回合推进、出牌校验、AI 决策、玩家操作可用性、联机请求保护和成长结算。
- View：`ChudadiApp` 和 Compose 页面，负责页面状态、动画、音乐反馈、弹窗确认和用户点击事件，不直接修改规则模型。

`GameState` 是牌桌唯一状态源。出牌、过牌、AI 行动和蓝牙同步都生成新状态，界面根据状态重组。

## 核心职责

| 模块 | 关键类 | 职责 |
| --- | --- | --- |
| 牌与牌型 | `Card`、`Deck`、`HandClassifier`、`HandType` | 牌面排序、发牌、识别单张/对子/三张/五张牌型 |
| 规则 | `RuleProfile`、`NorthRuleSet`、`SouthRuleSet` | 首出牌、炸弹增强、同张数限制、五张牌等级比较 |
| 回合 | `GameController`、`GameState`、`Move` | 校验出牌、处理不出、三家过牌重置、胜负结算 |
| AI | `AiStrategy`、`GreedyAiStrategy`、`HeuristicAiStrategy`、`MonteCarloRolloutAiStrategy` | 用策略模式切换不同出牌算法 |
| 联机 | `GameTransport`、`TransportEvent`、`BluetoothHostTransport`、`BluetoothClientTransport` | 统一真实蓝牙和模拟传输，区分消息、连接、断开和错误事件 |
| 房间 | `RoomSeat`、`RoomSeatKind`、`SeatConnectionState`、`GameMessageCodec` | 四座位、准备状态、人机补位、断线托管、消息编解码 |
| 同步 | `GameSnapshot`、`NetworkMoveGuard` | 房主权威校验、重复请求过滤、状态快照同步、`roomId` 旧消息过滤 |
| 玩家成长 | `PlayerProfile`、`ProfileController`、`ProfileStore` | 等级经验、金币消费、头像解锁、成就、战绩和每日奖励 |
| 体验 | `CardRoomMusicPlayer`、`TableAnimationKeys` | 背景音乐、音效、回合动画 key |

## 设计模式与设计原则

1. 策略模式：`AiStrategy` 抽象 AI 算法，简单、普通、困难三种策略可替换，控制器只依赖接口。
2. 接口隔离：`GameTransport` 隔离蓝牙实现、本地模拟和测试替身，View 不直接操作 socket。
3. 主机权威模式：蓝牙房主统一校验出牌并广播快照，避免多端各自结算导致状态分叉。
4. 单一职责：牌型识别、规则判断、回合流转、AI 决策、消息传输、UI 展示分别封装。
5. 状态快照模式：`GameSnapshot` 表示可同步的牌局状态，用于重复消息修正、客户端刷新和断线后的房主端托管继续。
6. 命令对象思想：`Move.Play`、`Move.Pass`、`GameMessage.MoveRequest` 将玩家意图包装成可验证、可传输的对象。
7. 仓储模式：`ProfileStore` 将玩家档案、头像、自定义头像路径、规则和设置持久化到本机存储，UI 不直接处理序列化细节。
8. 状态模式：`Screen`、`BluetoothEntryMode`、`GameStartPhase` 分别描述页面、蓝牙入口和人机开局阶段，避免 UI 用大量布尔值交叉控制。
9. 事件驱动：`TransportEvent` 把 socket 读写线程产生的连接、断开和错误转化为 UI 可处理的业务事件。
10. 断线托管模式：客户端断线后真人座位不清空，而是标记 `takeoverByAi`，由房主端 AI 继续托管；当前版本没有实现自动断线重连。

## UML 知识点应用

项目 UML 建模流程为：先用 PlantUML 代码描述 7 类图的元素、关系和流程，再将 PlantUML 图内容导入或参照迁移到 ProcessOn UML 中整理为正式图。`docs/uml` 保留 `.puml` 源文件和导出图片：

- 用例图：描述玩家、房主、加入者、人机与主要用例的关系。
- 类图：描述 Model、Controller、Transport、View/Audio 的类和接口依赖。
- 顺序图：描述本地出牌和蓝牙房主权威同步的调用顺序。
- 状态图：描述大厅、规则、蓝牙房间、牌桌、暂停和结算状态转换。
- 活动图：描述从开局、出牌、过牌到结算/再开局的业务流程。
- 组件图：描述 UI、控制器、模型、消息、蓝牙、音乐和系统能力的组件关系。
- 部署图：描述房主手机、加入者手机、Android Bluetooth Stack 和单机测试环境的部署关系。

## 数据流

本地对局中，人机入口先进入 `GameStartPhase.ReadyToStart`，点击开始后进入 `Dealing` 并播放中心牌堆发牌动画，动画结束后进入 `Playing`。View 收集玩家选择后调用 `GameController.play()` 或 `pass()`；控制器通过 `RuleSet` 和 `HandClassifier` 校验合法性，返回新的 `GameState`。轮到人机时，`AiController` 调用当前难度对应的 `AiStrategy` 生成 `Move`。

蓝牙对局中，加入者只发送 `MOVE_REQUEST`。房主使用 `NetworkMoveGuard` 判断请求是否来自当前远端玩家，再调用 `GameController` 结算，最后广播 `MOVE_ACCEPTED` 和 `STATE_SNAPSHOT`。客户端收到后以房主快照为准刷新界面。快照携带 `roomId`，用于避免旧房间消息混入当前牌局。

客户端断线时，`SocketGameTransport` 发出 `TransportEvent.PeerDisconnected`。房主通过 `peerKey` 找到座位，调用 `markHumanDisconnected(takeoverByAi=true)` 保留座位并广播 `DISCONNECT_NOTICE`、`ROOM` 和 `STATE_SNAPSHOT`。如果轮到该座位，房主端把该玩家视为 `LocalAi` 并继续出牌。当前版本未实现客户端自动断线重连，断线客户端需要重新进入蓝牙流程。

房主断线时，客户端通过 socket 断开或错误事件弹窗提示“房主连接已断开”，关闭当前传输并退出房间或返回大厅。由于房主是权威端，当前版本不做新房主迁移，避免多端状态恢复不一致。

玩家成长中，结算页根据排名、对局模式、难度和规则生成 `MatchSettlement`，交给 `ProfileController.settleMatch()` 计算金币、经验、成就和历史记录。消耗金币的改名、头像、自定义头像和战绩重置操作先进入 `ConfirmProfileSpendDialog`，确认后才调用控制器并通过 `ProfileActionResultDialog` 显示结果。

## 错误处理

- 非法牌型：保留选择并显示规则提示。
- 无可压牌：禁用出牌和提示，显示“手上没有可以大过人家的牌”。
- 重复或过期蓝牙请求：房主不改变牌局，只回发当前快照。
- 客户端断线：房主提示“断线，已由人机托管”，座位保留并继续对局。
- 客户端重连：当前版本未实现自动恢复原座位；需要重新进入蓝牙流程。
- 房主断线：客户端弹窗提示连接断开，退出当前对局并提示重新创建或重新加入。
- 房间未满或未准备：开始按钮禁用并显示缺少座位。
- 蓝牙权限或设备异常：页面展示权限说明、重新搜索和返回入口。
- 金币不足或头像等级不足：消费操作不改变 `PlayerProfile`，弹窗显示失败原因。
- Debug/release 签名不一致：`runRelease` 会先卸载旧包再安装 release，文档提醒可能清空本地数据。
