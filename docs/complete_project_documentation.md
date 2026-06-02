# 锄大地 Android 项目完整说明文档

本文档对应当前项目最新代码版本，覆盖代码结构、核心实现、UML 图对应关系、设计模式应用、玩家使用说明、测试与发布说明。项目包名为 `com.example.uml_chudadi`，主入口为 `app/src/main/java/com/example/uml_chudadi/MainActivity.kt`。

## 1. 项目概述

本项目是一个 Kotlin + Jetpack Compose 实现的 Android 锄大地棋牌游戏，已经从课程展示型界面改造成更接近发布测试版的本地棋牌游戏。当前版本支持：

- 开屏页：使用修补后的 CC0 扑克牌素材作为背景，必须点击 `开始游戏` 才进入大厅。
- 应用图标：使用 FreeIcons 的 `Cards` CC0 成品素材作为主体，入口图标直接使用各密度 PNG，避免部分手机桌面 adaptive icon 显示不一致。
- 游戏大厅：显示玩家头像、昵称、等级、金币、战绩入口，以及人机对局、好友蓝牙对局、规则、设置、教程等入口。
- 人机对局：支持简单、普通、困难三种 AI 难度；开局前先入桌，点击 `开始游戏` 后播放发牌动画。
- 规则系统：支持北方规则与南方规则，保存后对后续人机和蓝牙对局生效。
- 蓝牙房间：支持创建房间、加入对局、四座位房间、人机补位、房主权威同步、结算后回原房间再开局。
- 玩家成长：支持金币、经验、等级、称号、战绩、成就、每日奖励、头像商店、自定义头像、改名消费、战绩重置。
- 动画与音效：包含开屏光效、大厅漂浮扑克牌、主视觉光效、按钮按压、牌桌发牌、手牌选中、出牌飞牌、结算动画、背景音乐、出牌音效与震动。

开屏背景素材来自 Wikimedia Commons 的 `The Cloisters Playing Cards` 图片，页面标注为 CC0，可自由复制、修改和分发。项目内对原图右上空位做了补牌修复，保留原素材质感，同时避免竖屏开屏出现右上角缺牌观感：

- <https://commons.wikimedia.org/wiki/File:The_Cloisters_Playing_Cards_MET_DP354608.jpg>

应用图标使用 FreeIcons 的 `Cards` 成品素材作为主体，页面标注 Public Domain / CC0。项目对素材做 Android 图标适配：保留两张现代扑克牌主体，叠加深绿牌桌底、金色边框与阴影，使桌面小尺寸下更醒目。为避免部分手机桌面缓存默认 `ic_launcher` 资源名，正式入口图标使用 `chudadi_launcher` 资源名；入口资源直接使用各密度 PNG，避免部分系统把 adaptive icon 的前景矢量显示成与预览不一致的效果。

- <https://freeicons.co/v/cards-410396/>
- `docs/assets/freeicons_cards_cc0.svg`

## 2. 目录结构

```text
app/src/main/java/com/example/uml_chudadi/
├── MainActivity.kt
├── audio/
│   └── CardRoomMusicPlayer.kt
├── controller/
│   ├── AiStrategy.kt
│   ├── GameController.kt
│   ├── PlayerActionPolicy.kt
│   └── RoomController.kt
├── model/
│   ├── Card.kt
│   ├── Difficulty.kt
│   ├── GameState.kt
│   ├── HandClassifier.kt
│   ├── HandType.kt
│   ├── Player.kt
│   └── RuleSet.kt
├── profile/
│   ├── ProfileController.kt
│   ├── ProfileModels.kt
│   └── ProfileStore.kt
├── transport/
│   ├── BluetoothTransports.kt
│   ├── GameMessage.kt
│   ├── GameSnapshot.kt
│   ├── GameTransport.kt
│   ├── LocalRoomTransport.kt
│   ├── NetworkMoveGuard.kt
│   └── RoomSeat.kt
├── ui/theme/
│   ├── Color.kt
│   ├── Theme.kt
│   └── Type.kt
└── view/
    ├── ChudadiApp.kt
    └── TableAnimationKeys.kt
```

资源与资产目录：

```text
app/src/main/assets/
└── doudizhu_bgm.ogg
```

```text
app/src/main/res/
├── drawable/
│   ├── ic_launcher_background.xml
│   ├── ic_launcher_foreground.xml
│   ├── ic_launcher_monochrome.xml
│   ├── chudadi_launcher_background.xml
│   ├── chudadi_launcher_foreground.xml
│   └── chudadi_launcher_monochrome.xml
├── drawable-nodpi/
│   └── splash_cards.jpg
├── mipmap-*/
│   ├── chudadi_launcher.png
│   └── chudadi_launcher_round.png
├── values/
│   ├── colors.xml
│   ├── strings.xml
│   └── themes.xml
└── xml/
    ├── backup_rules.xml
    └── data_extraction_rules.xml
```

文档与 UML：

```text
docs/
├── requirements.md
├── design.md
├── ai_usage.md
├── release_testing.md
├── test_report.md
├── final_checklist.md
├── complete_project_documentation.md
└── uml/
    ├── use_case.puml / use_case.png
    ├── class_diagram.puml / class_diagram.png
    ├── sequence_game_turn.puml / sequence_game_turn.png
    ├── state_game.puml / state_game.png
    ├── activity_play.puml / activity_play.png
    ├── component_diagram.puml / component_diagram.png
    ├── deployment_diagram.puml / deployment_diagram.png
    └── processon_uml_*.png
```

## 3. MVC 架构说明

项目整体按 MVC 思想组织：

- Model：`model/`、`profile/ProfileModels.kt`、`transport/GameSnapshot.kt`、`transport/RoomSeat.kt`。
- Controller：`controller/`、`profile/ProfileController.kt`、`transport/GameMessageCodec`、`NetworkMoveGuard`。
- View：`view/ChudadiApp.kt` 和 Compose UI 组件。

### 3.1 Model 层

Model 层负责描述“游戏是什么”，不直接依赖 Android UI。

- `Card.kt`
  - `Suit` 定义花色及花色大小。
  - `Rank` 定义牌点及牌点大小。
  - `Card` 是单张牌模型，实现 `Comparable<Card>`，用于排序和比较。
  - `Deck.deal(seed)` 根据可选 seed 洗牌并发给四名玩家，蓝牙开局使用相同 seed 保证各端一致。

- `HandType.kt`
  - `HandCategory` 描述牌型：单张、对子、三张、顺子、同花、葫芦、四带一、同花顺。
  - `HandType` 保存牌型类别、参与牌、主牌点、高牌和显示名称。
  - `Move` 使用 sealed class 表示玩家动作，分为 `Move.Play` 与 `Move.Pass`。
  - `PlayedHand` 记录上一手是谁出的、出牌牌型是什么。

- `HandClassifier.kt`
  - `classify(cards)` 根据牌数分派到单张、对子、三张、五张牌识别逻辑。
  - 五张牌识别包含顺子、同花、葫芦、四带一、同花顺。
  - 该文件是规则判断的基础，被 `RuleSet`、`GameController`、AI 策略和测试复用。

- `RuleSet.kt`
  - `RuleProfile` 把规则差异抽象为可配置数据：首出牌、炸弹增强、五张牌等级、是否同张数压制。
  - `RuleSet` 是规则接口，提供 `classify`、`canLead`、`canBeat`。
  - `NorthRuleSet`：黑桃 3 首出，炸弹增强开启，四带一和同花顺可压普通牌型。
  - `SouthRuleSet`：方块 3 首出，遵循同张数压制，五张牌之间仍按顺子、同花、葫芦、四带一、同花顺等级比较。
  - `ruleSetByIdOrName` 用于从本地设置或网络消息恢复规则。

- `GameState.kt`
  - 作为游戏唯一状态源，包含玩家列表、规则、当前玩家、上一手牌、过牌次数、首轮标记、赢家和消息。
  - `currentPlayer`、`isFinished`、`player(id)`、`nextPlayerId(id)`、`updatePlayer(player)` 都是状态读取和复制辅助。
  - Controller 每次操作生成新的 `GameState`，Compose 观察状态自动重组。

- `Player.kt`
  - `PlayerKind` 区分真人、本地 AI、远端玩家。
  - `Player` 保存玩家 id、昵称、类型和手牌。
  - `remove(cards)` 负责从手牌中移除已经出的牌。

- `Difficulty.kt`
  - `Difficulty.Easy`、`Normal`、`Hard` 对应简单、普通、困难。
  - UI 显示难度标题与说明，控制器根据难度选择不同 AI 策略。

### 3.2 Controller 层

Controller 层负责“游戏怎么推进”和“操作是否合法”。

- `GameController.kt`
  - `newGame` 创建四人新局、发牌、定位首出玩家。
  - `play` 校验回合、手牌归属、牌型是否合法、首出是否包含首牌、是否能压过上一手，并更新胜负。
  - `pass` 处理过牌；当三家都过时，上一手出牌者重新获得先手。
  - `applyMove` 统一执行 `Move.Play` 或 `Move.Pass`。
  - `legalPlays` 枚举 1、2、3、5 张组合，过滤合法可出牌，是提示按钮、无可压牌提示、AI 决策的共同来源。

- `AiStrategy.kt`
  - `AiStrategy` 是策略模式接口，控制器只依赖接口。
  - `GreedyAiStrategy`：简单难度，优先出最小合法牌。
  - `HeuristicAiStrategy`：普通难度，使用手牌结构、对手剩余牌数、拆牌损失、炸弹保留等启发式评分。
  - `MonteCarloRolloutAiStrategy`：困难难度，参考 ISMCTS / rollout 思路，使用固定预算模拟评估候选动作，不引入第三方模型。
  - `AiController` 判断当前是否本地 AI，并把策略返回的 `Move` 交给 `GameController` 执行。

- `PlayerActionPolicy.kt`
  - 把 UI 层按钮状态抽象出来，输出 `canAct`、`canPass`、`hasLegalPlay`、`waitingForNetwork`、`message`。
  - 牌桌页根据该策略显示“手上没有可以大过人家的牌”、禁用出牌/提示、允许不出。
  - 避免 UI 自己散落复杂规则判断。

- `RoomController.kt`
  - 保存房间状态：座位、规则、房主身份、错误信息。
  - 作为蓝牙房间控制逻辑的补充，使房间状态与游戏状态分离。

### 3.3 View 层

View 层在 `view/ChudadiApp.kt` 中实现，主要职责是展示状态、播放动画、把用户意图传给 Controller。

核心 UI 状态：

- `screen`：当前页面，包含 `Splash`、`Lobby`、`Profile`、`DifficultySelect`、`Rules`、`Settings`、`Tutorial`、`Nearby`、`Game`、`Result`。
- `profile`：玩家档案，从 `ProfileStore` 读取并保存。
- `gameState`：当前对局状态。
- `selectedRule`：当前保存规则。
- `difficulty`：人机默认难度。
- `networkTransport`、`networkSeats`、`networkIsHost`、`waitingForNetworkMove`：蓝牙联机状态。
- `gameStartPhase`：人机对局开局阶段，区分待开始、发牌中、对局中。

主要 Compose 组件：

- `SplashScreen`
  - App 开屏页。
  - 使用 `R.drawable.splash_cards` 作为背景图；该图基于 CC0 原素材修补右上缺牌区域生成。
  - 不再叠加顶部悬挂牌组，避免多余装饰破坏原素材质感。
  - 有主标题、光效、扑克牌标记和 `开始游戏` 按钮。

- `AnimatedScreenHost`
  - 统一页面淡入、缩放过渡。
  - 不引入 Navigation 依赖，使用 sealed/enum screen 状态管理。

- `GameScaffold`
  - 全局游戏背景容器，提供深绿渐变牌桌、纹理、漂浮扑克牌。

- `LobbyScreen`
  - 大厅页面，显示顶部玩家条、主视觉、战绩条、人机/蓝牙入口、规则/设置/教程入口。

- `ProfileScreen`
  - 玩家中心。
  - 展示资料、昵称设置、头像商店、自定义头像、每日奖励、战绩管理、成就、历史战绩。
  - 金币消费操作会先走 `ConfirmProfileSpendDialog`，操作结果通过 `ProfileActionResultDialog` 弹窗显示，不再显示在用户名字下方。

- `RulesScreen`
  - 规则设置页。
  - 切换北方/南方玩法，点击保存后写入 `SharedPreferences`。
  - 包含当前规则的详细说明。

- `NearbyScreen`
  - 蓝牙入口、创建房间、加入对局、搜索设备、房间座位管理。
  - 房主可添加 AI、切换 AI 难度、移除 AI；未满四座不能开始。

- `GameScreen`
  - 牌桌页。
  - 显示对手座位、中央出牌区、倒计时、手牌、出牌/不出/提示/重选按钮、设置菜单。
  - 支持出牌飞牌动画、AI 思考、暂停菜单、无可压牌提示。

- `ResultScreen`
  - 结算页。
  - 展示胜负、排名、金币/经验奖励、升级、成就、再来一局、返回大厅。

## 4. 蓝牙联机实现

蓝牙联机采用 Android Classic Bluetooth RFCOMM 方式，遵循主机权威房间模型。

### 4.1 抽象接口

`transport/GameTransport.kt`：

- `TransportRole.Host`：创建房间。
- `TransportRole.Client`：加入房间。
- `TransportRole.Local`：本地模拟。
- `GameTransport` 统一 `start`、`send`、`observe`、`close`。

这样 UI 和房间控制逻辑不直接绑定具体蓝牙 Socket，便于模拟测试。

### 4.2 真实蓝牙

`transport/BluetoothTransports.kt`：

- `BluetoothHostTransport` 使用 `BluetoothServerSocket` 等待客户端连接。
- `BluetoothClientTransport` 使用设备地址创建 `BluetoothSocket` 加入房间。
- 搜索前取消旧 discovery，连接后关闭 discovery，退出时关闭 socket 和线程。
- 权限通过 `hasBluetoothPermissions` 和 `requiredBluetoothPermissions` 判断。
- 设备列表优先显示已配对设备，再合并附近搜索结果。

### 4.3 房间座位

`transport/RoomSeat.kt`：

- `RoomSeatKind.Empty`：空位。
- `Host`：房主。
- `Human`：真实加入者。
- `Ai`：房主添加的人机。
- `RoomSeat` 保存座位号、昵称、类型、难度、准备状态、连接状态。
- `canStartRoom` 确保四个座位全部占满后才允许开始。

### 4.4 消息协议

`transport/GameMessage.kt`：

- `HELLO`：客户端问候并提交昵称。
- `ROOM`：房主广播房间座位快照。
- `START`：房主广播 seed、规则、座位列表。
- `ROOM_READY`：准备状态。
- `MOVE_REQUEST`：客户端请求出牌/不出。
- `MOVE_ACCEPTED`：房主确认动作。
- `STATE_SNAPSHOT`：完整状态快照同步。
- `SYNC_REQUEST`：客户端请求重同步。
- `LEAVE` / `KICK`：离开与移出。
- `ERROR`：错误提示。

编码使用 URL encode，能安全传输中文名和分隔符。

### 4.5 主机权威同步

蓝牙对局中只有房主负责：

- 校验所有出牌是否合法。
- 驱动 AI 行动。
- 生成并广播 `STATE_SNAPSHOT`。
- 决定对局结束和再来一局。

客户端负责：

- 展示房主广播的状态。
- 当前轮到自己时发送 `MOVE_REQUEST`。
- 发送后进入等待确认状态，收到 `MOVE_ACCEPTED` 或 `STATE_SNAPSHOT` 后解锁。

`transport/NetworkMoveGuard.kt` 用于防止重复、过期、非当前玩家的移动请求污染状态。

## 5. 玩家成长系统

成长系统由 `profile/` 包实现。

### 5.1 数据模型

`ProfileModels.kt`：

- `PlayerProfile`
  - 昵称、金币、经验、头像、战绩、成就、已解锁头像、自定义头像开关、历史记录、每日奖励日期。
- `ProfileStats`
  - 总局、胜场、蓝牙局、蓝牙胜场、困难胜场、连胜、最佳胜利剩余手牌。
- `MatchRecord`
  - 最近对局历史，最多保存 50 条。
- `AchievementDefinition`
  - 成就 id、名称、描述、金币奖励、经验奖励。
- `BuiltInAvatar`
  - 头像 id、名称、标记、价格、稀有度、开放等级。
- `AvatarProfile`
  - 当前内置头像 id、自定义头像路径、边框 id。

### 5.2 业务控制

`ProfileController.kt`：

- `settleMatch`：按名次、模式、难度结算金币和经验，记录历史，触发成就。
- `claimDailyReward`：每日奖励，一天只能领取一次。
- `renameWithCost`：改名消耗 500 金币，昵称没变不扣费。
- `unlockAvatar`：解锁内置头像，普通 800，稀有 1800，传说 3200，等级不足或金币不足时失败。
- `unlockCustomAvatar`：一次性 4000 金币解锁自定义头像功能。
- `resetStats`：消耗 2500 金币清空战绩与历史，保留金币、经验、等级、头像、成就。
- `normalizedUnlockedAvatars`：兼容旧数据，确保免费头像和当前头像始终可用。

### 5.3 本地存储

`ProfileStore.kt`：

- 使用 Android `SharedPreferences` 保存玩家档案。
- 历史记录使用 `ProfileCodec` 编码，支持中文和特殊分隔符。
- 启动时自动补齐旧档案缺失字段。

## 6. 音乐与反馈

`audio/CardRoomMusicPlayer.kt`：

- 管理大厅和牌桌背景音乐。
- 背景音乐资源来自用户提供的本地 OGG 文件，并已按 0:00-1:33 片段处理为循环播放版本。
- `MusicScene.Lobby` 与 `MusicScene.Game` 可区分场景。

`ChudadiApp.kt` 中还包含：

- `playCardSound`：出牌音效。
- `vibrate`：震动反馈。
- 设置页可开关背景音乐与震动。

## 7. 设计模式对应代码

### 7.1 MVC 模式

- Model：`model/`、`profile/ProfileModels.kt`。
- View：`view/ChudadiApp.kt` 的 Compose 页面和组件。
- Controller：`controller/`、`profile/ProfileController.kt`、`transport/GameMessageCodec`。

体现方式：

- UI 不直接修改手牌，而是调用 `GameController.play/pass`。
- `GameState` 是唯一游戏状态源。
- `ProfileController` 统一处理金币、经验、成就、战绩变化。

### 7.2 策略模式

接口：`controller/AiStrategy.kt`

- `AiStrategy` 定义 `chooseMove`。
- `GreedyAiStrategy`、`HeuristicAiStrategy`、`MonteCarloRolloutAiStrategy` 是不同策略实现。
- UI 选择难度后，`ChudadiApp.strategy` 根据难度创建对应策略。

优点：

- 新增 AI 不需要修改 `GameController`。
- 蓝牙房间中的 AI 座位可独立配置难度。

### 7.3 状态模式

位置：`view/ChudadiApp.kt`

- `Screen` 管理页面状态。
- `GameStartPhase` 管理人机对局开局状态：待开始、发牌中、对局中。
- `ConnectionGuidePhase` 管理蓝牙连接引导状态。

优点：

- 页面切换和开局流程清晰。
- 发牌动画期间不会触发 AI 或倒计时。

### 7.4 观察者思想

Compose 的 `mutableStateOf`、`mutableIntStateOf` 和重组机制充当状态观察：

- `profile` 改变后，玩家中心和大厅自动刷新。
- `gameState` 改变后，牌桌自动刷新。
- `screen` 改变后，`AnimatedScreenHost` 自动切换页面。

### 7.5 适配器模式

`GameTransport` 把不同传输方式适配成统一接口：

- `BluetoothHostTransport`
- `BluetoothClientTransport`
- `LocalRoomTransport`
- 测试中的 Fake/Simulated transport

UI 只关心 `send/observe/close`，不关心底层是蓝牙 Socket 还是模拟总线。

### 7.6 单例 / 对象模式

Kotlin `object` 用于无状态工具和目录：

- `Deck`
- `HandClassifier`
- `NorthRuleSet` / `SouthRuleSet`
- `AvatarCatalog`
- `AchievementCatalog`
- `ProfileController`
- `GameMessageCodec`
- `NetworkMoveGuard`

这些对象全局唯一，避免重复创建无状态工具。

### 7.7 命令模式

`Move` sealed class 表示玩家动作命令：

- `Move.Play(playerId, cards)`
- `Move.Pass(playerId)`

`GameController.applyMove` 根据命令类型执行逻辑。AI、玩家、网络都可以用同一种 Move 语义表达操作。

### 7.8 编码器 / 解码器模式

`GameMessageCodec` 负责网络消息和字符串协议之间的转换。

好处：

- 蓝牙线程不直接操作 UI。
- 消息格式集中管理，便于测试和扩展。

## 8. UML 图对应说明

UML 文件位于 `docs/uml/`，PlantUML 源文件和导出的 PNG 同时保留，另外有 ProcessOn UML 画板截图用于展示建模过程。

### 8.1 用例图 `use_case.puml`

对应用户角色：

- 玩家
- 房主
- 加入者

主要用例：

- 人机对局
- 创建蓝牙房间
- 加入蓝牙对局
- 添加人机
- 设置规则
- 修改资料
- 查看战绩
- 领取奖励

代码对应：

- `LobbyScreen` 提供入口。
- `NearbyScreen` 管理蓝牙房间。
- `ProfileScreen` 管理玩家资料。
- `GameController` 执行对局。

### 8.2 类图 `class_diagram.puml`

核心类：

- `Card`、`Player`、`GameState`、`HandType`
- `RuleSet`、`NorthRuleSet`、`SouthRuleSet`
- `GameController`
- `AiStrategy` 及其实现
- `GameTransport` 及蓝牙实现
- `PlayerProfile`、`ProfileController`

代码对应：

- `model/` 是主要实体。
- `controller/` 是主要服务类。
- `transport/` 是网络抽象。
- `profile/` 是成长系统。

### 8.3 顺序图 `sequence_game_turn.puml`

描述一次出牌回合：

1. 玩家在 UI 选择手牌。
2. UI 调用 `PlayerActionPolicy` 判断按钮可用。
3. 点击出牌后播放飞牌动画。
4. 本地对局调用 `GameController.play`；蓝牙对局发送 `MOVE_REQUEST`。
5. 房主校验后广播 `MOVE_ACCEPTED` 和 `STATE_SNAPSHOT`。
6. UI 根据新 `GameState` 重组。

### 8.4 状态图 `state_game.puml`

描述对局状态：

- 待开始
- 发牌中
- 玩家行动
- AI 思考
- 等待网络确认
- 结算
- 回房间 / 再来一局

代码对应：

- `GameStartPhase`
- `waitingForNetworkMove`
- `gameState.winnerId`
- `Screen.Result`

### 8.5 活动图 `activity_play.puml`

描述玩家完整流程：

1. 开屏页点击开始游戏。
2. 进入大厅。
3. 选择人机或蓝牙。
4. 保存规则或选择难度。
5. 开始对局。
6. 出牌/不出/提示。
7. 结算并写入战绩。

### 8.6 组件图 `component_diagram.puml`

组件关系：

- Compose UI
- Game Controller
- Rule Model
- AI Strategy
- Profile System
- Bluetooth Transport
- Local Storage

### 8.7 部署图 `deployment_diagram.puml`

部署结构：

- Android 手机 A：房主端，运行 HostTransport 和权威 GameController。
- Android 手机 B/C/D：客户端，运行 ClientTransport。
- 本地 SharedPreferences：保存规则、档案、设置。
- 蓝牙 RFCOMM：传输房间和对局消息。

## 9. App 使用说明

### 9.1 启动

1. 安装并打开 App。
2. 进入开屏页。
3. 点击 `开始游戏`。
4. 进入游戏大厅。

### 9.2 人机对局

1. 在大厅点击 `人机对局`。
2. 选择难度：简单、普通、困难。
3. 进入牌桌预备页。
4. 点击 `开始游戏`。
5. 等待发牌动画结束。
6. 轮到自己时选择手牌。
7. 点击 `出牌`、`不出`、`提示` 或 `重选`。
8. 对局结束后进入结算页。
9. 点击 `再来一局` 回到预备页，或 `返回大厅`。

### 9.3 好友蓝牙对局

房主：

1. 大厅点击 `好友蓝牙对局`。
2. 选择 `创建房间`。
3. 按系统提示开启蓝牙和附近设备权限。
4. 等待好友加入。
5. 空位可添加简单、普通、困难人机。
6. 四个座位满后点击 `开始对局`。

加入者：

1. 大厅点击 `好友蓝牙对局`。
2. 选择 `加入对局`。
3. 授权附近设备权限。
4. 先确认两台手机在系统蓝牙里可见或已配对。
5. 点击搜索到的房主设备。
6. 等待房主开始。

连接失败处理：

- 确认两台手机距离较近。
- 关闭再打开蓝牙。
- 先在系统蓝牙中配对。
- 让房主重新创建房间。
- 加入者重新搜索。

### 9.4 规则设置

1. 大厅点击 `规则`。
2. 选择北方规则或南方规则。
3. 阅读详细规则说明。
4. 点击 `保存设置`。
5. 后续人机和蓝牙房间会使用保存后的规则。

### 9.5 玩家中心

1. 点击大厅顶部玩家区域。
2. 查看等级、经验、金币、总局、胜率。
3. 修改昵称：输入新昵称，点击确认改名，弹出消费确认后支付 500 金币。
4. 头像商店：点击可解锁头像，确认支付金币后使用。
5. 自定义头像：支付 4000 金币解锁后，可从相册选择图片。
6. 每日奖励：每天可领取一次金币和经验。
7. 战绩管理：支付 2500 金币可重置战绩。
8. 成就墙：展示已解锁和未解锁成就。
9. 历史战绩：展示最近 50 局。

### 9.6 设置

1. 大厅点击 `设置`。
2. 可开关背景音乐与音效。
3. 可开关震动反馈。
4. 点击 `退出游戏` 会先弹出二次确认，确认后关闭 App。

### 9.7 教程

1. 大厅点击 `教程`。
2. 查看牌型、首出、压制、蓝牙连接等说明。

## 10. 测试说明

项目已有测试覆盖：

- 牌型识别：`HandClassifierTest`
- 南北规则差异：`HandClassifierTest`、`GameControllerTest`
- 游戏流程：`GameControllerTest`
- 操作按钮策略：`PlayerActionPolicyTest`
- AI 策略合法性：控制器测试
- 消息协议：`GameMessageCodecTest`
- 蓝牙模拟：`SimulatedBluetoothHubTest`
- 移动请求去重：`NetworkMoveGuardTest`
- 房间座位：`RoomSeatTest`
- 成长系统：`ProfileControllerTest`
- 音乐资源：`MusicAssetTest`
- 动画 key：`TableAnimationKeysTest`

常用命令：

```bash
./gradlew test --no-daemon
./gradlew :app:assembleDebug --no-daemon
./gradlew :app:runDebug --no-daemon
./gradlew :app:assembleRelease --no-daemon
./gradlew :app:bundleRelease --no-daemon
./gradlew :app:runRelease
```

真机测试建议：

- 至少一台手机测试安装、启动、开屏、玩家中心、人机完整对局。
- 两台及以上手机测试蓝牙创建、加入、AI 补位、开局、出牌、结算、再来一局。
- 权限拒绝、蓝牙关闭、找不到设备、房满、断开重连都需要覆盖。

## 11. 发布测试包说明

Debug APK 路径：

```text
app/build/outputs/apk/debug/app-debug.apk
```

Release APK / AAB 路径：

```text
app/build/outputs/apk/release/app-release.apk
app/build/outputs/bundle/release/app-release.aab
release/chudadi-v1.3-release.apk
release/chudadi-v1.3-release.aab
```

当前 release 信息：

- 包名：`com.example.uml_chudadi`
- 版本：`versionName=1.3`，`versionCode=4`
- 签名文件：`keystore/chudadi-release.jks`
- 签名配置：`keystore.properties`
- Android Studio 一键运行配置：`.idea/runConfigurations/runRelease.xml`

发布/测试命令：

```bash
./gradlew :app:assembleRelease --no-daemon
./gradlew :app:bundleRelease --no-daemon
./gradlew :app:runRelease
```

`runRelease` 与 `runDebug` 类似，都会安装并启动 App；区别是 `runRelease` 使用签名后的 release APK。若手机上已安装 debug 版，因为 debug/release 签名不同，Android 不允许直接覆盖，`runRelease` 会先卸载旧包再安装 release，因此本地 App 数据可能被清空。

给同学测试时：

1. 优先发送 `release/chudadi-v1.3-release.apk`。
2. Android 手机上允许安装未知来源应用。
3. 首次进入蓝牙功能时允许附近设备权限。
4. 如果安装失败，先卸载同包名旧版本再重新安装。
5. 后续版本必须继续使用同一个 `keystore/chudadi-release.jks` 签名，否则手机无法覆盖升级已安装的 release 版。

素材与隐私发布检查：

- 开屏图：Wikimedia Commons `The Cloisters Playing Cards`，CC0。
- 应用图标：FreeIcons `Cards`，Public Domain / CC0。
- 背景音乐：用户提供本地 OGG，项目内使用 0:00-1:33 片段循环。
- 权限：蓝牙、附近设备、定位兼容旧系统、震动。

## 12. 当前实现亮点

- 使用 MVC 分层，核心规则与 UI 分离。
- 使用策略模式实现三档 AI。
- 使用主机权威模型增强蓝牙一致性。
- 使用 URL 编码的文本协议降低网络消息解析风险。
- 使用 SharedPreferences 完成本地档案、规则、设置持久化。
- 使用 Compose 内置动画实现完整游戏体验，不新增第三方依赖。
- 玩家消费操作加入确认弹窗，避免误扣金币。
- 开屏页有明确开始按钮，符合成熟手游先进入封面再进入大厅的流程。

## 13. 后续可优化方向

- 完善应用商店素材：隐私说明、截图、版本更新日志和测试账号说明。
- 增加横屏适配。
- 增加更完整的新手引导步骤。
- 增加蓝牙断线重连和房间恢复。
- 增加更多规则变体，但需要确保 UI 说明、测试和规则实现同步。
- 增加截图回归测试，固定检查大厅、牌桌、玩家中心、结算页。
- 若后续允许服务端，可扩展在线匹配、云战绩、排行榜。
