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
- 发布与仓库：项目已配置 HTTPS 远程仓库、release 签名、`runRelease` 运行配置和敏感文件忽略规则；签名私钥只保存在本机，不上传 GitHub。

开屏背景素材来自 Wikimedia Commons 的 `The Cloisters Playing Cards` 图片，页面标注为 CC0，可自由复制、修改和分发。项目内对原图右上空位做了补牌修复，保留原素材质感，同时避免竖屏开屏出现右上角缺牌观感：

- <https://commons.wikimedia.org/wiki/File:The_Cloisters_Playing_Cards_MET_DP354608.jpg>

应用图标使用 FreeIcons 的 `Cards` 成品素材作为主体，页面标注 Public Domain / CC0。项目对素材做 Android 图标适配：保留两张现代扑克牌主体，叠加深绿牌桌底、金色边框与阴影，使桌面小尺寸下更醒目。为避免部分手机桌面缓存默认 `ic_launcher` 资源名，正式入口图标使用 `chudadi_launcher` 资源名；入口资源直接使用各密度 PNG，避免部分系统把 adaptive icon 的前景矢量显示成与预览不一致的效果。

- <https://freeicons.co/v/cards-410396/>
- `docs/assets/freeicons_cards_cc0.svg`

### 1.1 实训 8 项要求逐项技术实现说明

本节按实训启动纲要中的 8 个要求逐项说明当前项目如何实现。说明颗粒度细化到技术选型、代码落点、具体实现流程和验证方式，便于按评分标准检查。

#### 要求 1：掌握 ProcessOn UML 工具进行 UML 7 大图建模

技术选型：

- UML 图结构先使用 PlantUML 代码描述，原因是 PlantUML 文本便于版本管理、代码审查和随代码迭代同步。
- 正式图使用 ProcessOn UML。开发者将 PlantUML 图内容导入或参照迁移到 ProcessOn UML 中，再手动调整布局，作为最终报告中的正式 UML 图。
- Android Studio 中已安装 PlantUML 插件，项目内 `.puml` 可在 IDE 中预览；`docs/uml/*.png` 为导出图，用于报告引用和检查。

具体实现：

- 用例图：`docs/uml/use_case.puml` / `docs/uml/use_case.png`
  - 描述玩家、房主、加入者、本地人机四类参与者。
  - 覆盖开屏、进入大厅、规则设置、人机对局、蓝牙房间、添加人机、出牌、不出、结算、玩家中心等用例。
- 类图：`docs/uml/class_diagram.puml` / `docs/uml/class_diagram.png`
  - 按 `model`、`controller`、`profile`、`transport`、`view/audio` 分包展示。
  - 覆盖 `GameController`、`AiStrategy`、`RuleSet`、`GameMessageCodec`、`PlayerProfile`、`ProfileController` 等核心类。
- 顺序图：`docs/uml/sequence_game_turn.puml` / `docs/uml/sequence_game_turn.png`
  - 描述一次本地出牌和蓝牙出牌的调用顺序。
  - 展示 `PlayerActionPolicy`、`GameController`、`RuleSet`、`GameTransport`、`NetworkMoveGuard` 的交互。
- 状态图：`docs/uml/state_game.puml` / `docs/uml/state_game.png`
  - 描述 `Splash`、`Lobby`、`Profile`、`ReadyToStart`、`Dealing`、`Playing`、`Paused`、`Result` 等状态。
- 活动图：`docs/uml/activity_play.puml` / `docs/uml/activity_play.png`
  - 描述从启动、选择对局、创建/加入蓝牙房间、发牌、出牌、结算、再来一局的完整业务流程。
- 组件图：`docs/uml/component_diagram.puml` / `docs/uml/component_diagram.png`
  - 描述 Compose UI、控制器、规则模型、消息编解码、蓝牙传输、玩家成长、音乐反馈和 SharedPreferences 的依赖关系。
- 部署图：`docs/uml/deployment_diagram.puml` / `docs/uml/deployment_diagram.png`
  - 描述房主手机、加入者手机、本地测试环境、Android Bluetooth Stack、开发发布环境和 GitHub HTTPS 仓库。

验证方式：

- 检查 `docs/uml` 下 7 个 `.puml` 和 7 个 `.png` 是否同时存在。
- 使用 Android Studio PlantUML 插件预览 `.puml`，确认图能打开。
- 将 PlantUML 内容导入或参照迁移到 ProcessOn UML 后，核对类名、状态名、消息名和流程是否与当前代码一致。

#### 要求 2：进行需求分析和面向对象设计，并进行分析建模和设计建模

技术选型：

- 需求分析采用 Markdown 文档记录，便于与源码一起提交、审查和更新。
- 面向对象设计采用 MVC 思想：领域对象放在 Model，业务控制放在 Controller，Compose 页面放在 View。
- 分析建模通过用例图、活动图、状态图描述“系统要做什么”；设计建模通过类图、顺序图、组件图、部署图描述“系统如何实现”。

具体实现：

- 需求分析文档：`docs/requirements.md`
  - 把功能需求拆成开屏、大厅、人机、蓝牙、规则、玩家成长、音乐反馈、发布包等模块。
  - 把非功能需求拆成 MVC 分层、策略模式、蓝牙抽象、UML、测试覆盖和敏感文件保护。
  - 用评分点映射表说明每个评分点对应的代码和文档。
- 设计说明文档：`docs/design.md`
  - 说明 Model、Controller、View 的边界。
  - 说明 AI 策略模式、传输接口隔离、主机权威同步、状态快照、仓储模式、状态模式等设计。
- 完整说明文档：`docs/complete_project_documentation.md`
  - 从代码结构、规则实现、蓝牙实现、玩家成长、UML、测试、发布完整说明项目。

面向对象落点：

- `model/Card.kt`
  - `Suit`、`Rank`、`Card`、`Deck` 表示扑克牌和发牌。
- `model/HandType.kt` 与 `model/HandClassifier.kt`
  - 把牌型识别独立为模型层能力，供规则、控制器、AI 和测试复用。
- `model/RuleSet.kt`
  - 用 `RuleProfile`、`NorthRuleSet`、`SouthRuleSet` 表示不同规则配置。
- `model/GameState.kt` 与 `model/Player.kt`
  - 表示四名玩家、当前回合、上一手、过牌次数、胜者和提示消息。
- `controller/GameController.kt`
  - 实现出牌、过牌、合法动作枚举和胜负推进。
- `transport/GameMessage.kt`、`transport/GameSnapshot.kt`、`transport/RoomSeat.kt`
  - 表示联机消息、同步快照和四座位房间模型。
- `profile/ProfileModels.kt`
  - 表示玩家档案、战绩、成就、头像和成长数据。

验证方式：

- 用 `docs/uml/use_case.puml`、`activity_play.puml`、`state_game.puml` 验证分析建模。
- 用 `docs/uml/class_diagram.puml`、`sequence_game_turn.puml`、`component_diagram.puml`、`deployment_diagram.puml` 验证设计建模。
- 用 `./gradlew test --no-daemon` 验证规则、控制器、消息、蓝牙模拟和玩家成长逻辑。

#### 要求 3：详细设计时引入必要的设计模式，优化设计

技术选型：

- 只使用 Kotlin、Android SDK、Jetpack Compose 和 JUnit，不新增第三方运行依赖。
- 设计模式优先服务真实复杂度，不为了文档堆砌模式。
- 每个模式都对应明确代码点和实际问题。

具体设计模式：

1. 策略模式
   - 代码位置：`controller/AiStrategy.kt`
   - 抽象接口：`AiStrategy`
   - 实现类：`GreedyAiStrategy`、`HeuristicAiStrategy`、`MonteCarloRolloutAiStrategy`
   - 解决问题：同一套牌局控制器可以适配不同 AI 算法，不需要在 `GameController` 内写大量难度分支。
   - UI 对应：人机对局选择简单、普通、困难；蓝牙房间中房主给 AI 座位选择难度。

2. MVC 模式
   - Model：`model/`、`profile/ProfileModels.kt`、`transport/GameSnapshot.kt`
   - Controller：`controller/`、`profile/ProfileController.kt`、`transport/NetworkMoveGuard.kt`
   - View：`view/ChudadiApp.kt`
   - 解决问题：规则、AI、蓝牙和 UI 分离，降低 Compose 页面直接写业务规则导致的维护成本。

3. 接口隔离 / 适配器思想
   - 代码位置：`transport/GameTransport.kt`
   - 接口：`GameTransport`
   - 实现：`BluetoothHostTransport`、`BluetoothClientTransport`、`LocalRoomTransport`
   - 解决问题：UI 和房间逻辑不直接依赖 BluetoothSocket；测试可以使用本地模拟传输。

4. 主机权威同步模式
   - 代码位置：`transport/NetworkMoveGuard.kt`、`transport/GameSnapshot.kt`、`transport/GameMessage.kt`
   - 解决问题：蓝牙多端联机时，只有房主校验出牌和推进状态，加入者以房主快照为准，避免多台手机各自计算导致状态分裂。

5. 状态模式
   - 代码位置：`view/ChudadiApp.kt`
   - 状态类型：`Screen`、`BluetoothEntryMode`、`GameStartPhase`
   - 解决问题：页面切换、蓝牙入口和人机开局阶段有明确状态，不用多个布尔值互相组合。

6. 命令对象思想
   - 代码位置：`model/GameState.kt` 中的 `Move`，`transport/GameMessage.kt` 中的 `MoveRequest`
   - 解决问题：玩家、AI、蓝牙远端都统一用 `Move.Play` / `Move.Pass` 表达动作，便于控制器和测试复用。

7. 编码器 / 解码器模式
   - 代码位置：`transport/GameMessage.kt` 中的 `GameMessageCodec`
   - 解决问题：蓝牙传输只处理字符串帧，模型对象和协议转换集中管理，避免 UI 或蓝牙线程手写解析。

8. 仓储模式
   - 代码位置：`profile/ProfileStore.kt`
   - 解决问题：玩家昵称、金币、头像、战绩、成就、每日奖励和设置统一持久化，UI 不直接处理序列化细节。

验证方式：

- `AiStrategy` 通过 `GameControllerTest` 和 AI 相关断言验证返回合法动作。
- `GameTransport` 和主机权威同步通过 `GameMessageCodecTest`、`NetworkMoveGuardTest`、`SimulatedBluetoothHubTest` 验证。
- 玩家成长仓储和控制逻辑通过 `ProfileControllerTest` 验证。
- 页面状态通过手动验收和 `TableAnimationKeysTest` 辅助验证。

#### 要求 4：完成 UI 界面设计，实现不同页面切换

技术选型：

- UI 使用 Jetpack Compose，实现声明式界面和状态驱动重组。
- 页面切换不引入 Navigation 依赖，而是使用项目内 `Screen` 枚举和 `AnimatedScreenHost` 管理。
- 动画使用 Compose 内置 API，如 `AnimatedVisibility`、`animateFloatAsState`、`rememberInfiniteTransition`、`Canvas`、`graphicsLayer`。
- 视觉风格使用深绿牌桌、金色按钮、暗色玻璃面板、扑克牌元素和轻量光效，定位为发布版棋牌游戏界面。

页面实现：

- `SplashScreen`
  - 开屏页，必须点击 `开始游戏` 才进入大厅。
  - 使用 `drawable-nodpi/splash_cards.jpg` 作为背景。
  - 包含标题、光效和开始按钮。
- `LobbyScreen`
  - 游戏大厅。
  - 顶部 `PlayerBar` 显示玩家头像、昵称、等级、经验进度和金币。
  - 中部提供人机对局和好友蓝牙对局两个主要入口。
  - 底部提供规则、设置、教程快捷入口。
- `DifficultySelectScreen`
  - 人机对局前选择简单、普通、困难。
  - 选择后进入牌桌预备页，不立即发牌。
- `RulesScreen`
  - 北方规则和南方规则选择。
  - 保存后写入 `SharedPreferences`，后续所有对局使用已保存规则。
  - 页面内展示当前规则详细说明。
- `SettingsScreen`
  - 背景音乐、音效、震动、默认难度、退出游戏等设置。
  - 退出游戏必须二次确认。
- `TutorialScreen`
  - 展示牌型、首出、五张牌等级、南北差异和蓝牙连接提示。
- `ProfileScreen`
  - 玩家中心，包含资料卡、昵称修改、头像商店、自定义头像、每日奖励、成就、历史战绩和战绩管理。
  - 消耗金币的操作通过弹窗确认。
- `NearbyScreen`
  - 好友蓝牙入口、创建房间、加入对局、搜索设备、四座位房间管理。
- `GameScreen`
  - 牌桌页，包含三名对手座位、中央出牌区、底部手牌、倒计时、设置菜单、出牌/不出/提示/重选按钮。
  - 支持人机预备阶段、发牌动画、AI 思考、无可压牌提示和出牌飞牌动画。
- `ResultScreen`
  - 结算页，展示胜负、排名、金币/经验变化、升级、成就、再来一局和返回大厅。

页面切换实现：

- `Screen` 枚举位于 `view/ChudadiApp.kt`，包含 `Splash`、`Lobby`、`Profile`、`DifficultySelect`、`Rules`、`Settings`、`Tutorial`、`Nearby`、`Game`、`Result`。
- `screen` 使用 `remember { mutableStateOf(...) }` 保存当前页面。
- 用户点击按钮时修改 `screen`，`AnimatedScreenHost` 根据状态切换页面。
- 由于不使用 Navigation 依赖，项目依赖更少，Gradle 风险更低，也更适合实训项目展示。

验证方式：

- 手动从开屏进入大厅，再依次进入人机、规则、设置、教程、玩家中心、蓝牙、牌桌、结算。
- 检查按钮文字不截断、页面可滚动、弹窗不遮挡关键内容。
- `TableAnimationKeysTest` 验证出牌动画 key 稳定性，避免重复播放或漏播。

#### 要求 5：实现多人的蓝牙连接，提供多名玩家联机对战功能

技术选型：

- 使用 Android Classic Bluetooth RFCOMM，不新增 Google Play services 或 Nearby Connections 依赖。
- 房主端使用 `BluetoothServerSocket` 监听连接。
- 加入端使用 `BluetoothSocket` 连接房主设备。
- 联机拓扑采用房主权威 C/S 模式，不采用所有客户端互相连接的网状拓扑。
- 设备发现遵循 Android 官方建议：优先展示已配对设备，再搜索附近设备；连接前取消旧 discovery，连接后关闭 discovery。

具体实现：

- 抽象接口：`transport/GameTransport.kt`
  - `start(role)`：启动主机或客户端。
  - `send(message)`：发送协议文本。
  - `observe(listener)`：监听收到的消息。
  - `close()`：关闭连接和线程。
- 真实蓝牙：`transport/BluetoothTransports.kt`
  - `BluetoothHostTransport`：房主端监听 socket，接收多个客户端连接。
  - `BluetoothClientTransport`：加入端根据设备地址连接房主。
  - 内部使用读写线程处理 socket 输入输出，异常时通过消息反馈给 UI。
- 房间模型：`transport/RoomSeat.kt`
  - 固定四个座位：`Host`、`Human`、`Ai`、`Empty`。
  - 房主固定在 1 号位。
  - 空位可以添加 AI，AI 可选择简单、普通、困难。
  - `canStartRoom()` 要求四个座位都 occupied、ready、connected 才允许开始。
- 消息协议：`transport/GameMessage.kt`
  - `HELLO`：加入者向房主报到。
  - `ROOM`：房主广播完整座位快照。
  - `ROOM_READY`：加入者准备状态。
  - `START`：房主广播 seed、规则、座位列表。
  - `MOVE_REQUEST`：加入者请求出牌或不出。
  - `MOVE_ACCEPTED`：房主确认行动。
  - `STATE_SNAPSHOT`：房主广播权威状态快照。
  - `SYNC_REQUEST`、`LEAVE`、`KICK`、`ERROR`：同步、离开、踢出和错误恢复。
- 主机权威：`transport/NetworkMoveGuard.kt`
  - 房主只接受当前回合远端玩家的请求。
  - 重复、过期、非当前玩家请求不会推进牌局，只回发快照。
  - 蓝牙房间中的 AI 只由房主运行，加入者把 AI 当作远端同步状态。

玩家可见流程：

1. 大厅点击 `好友蓝牙对局`。
2. 选择 `创建房间` 或 `加入对局`。
3. 房主创建房间后显示四个座位。
4. 加入者搜索已配对设备和附近设备，选择房主设备连接。
5. 房主可添加 AI 补满四座，或等待好友加入。
6. 四座位全部有人或 AI 且准备后，房主点击开始。
7. 房主广播 `START`，各端进入同一对局。
8. 加入者出牌时发送 `MOVE_REQUEST`，等待房主确认。
9. 房主广播 `MOVE_ACCEPTED` 和 `STATE_SNAPSHOT`，所有端同步刷新。

验证方式：

- `SimulatedBluetoothHubTest` 模拟 1 房主 + 3 客户端同时加入、房满拒绝、广播和移动请求。
- `RoomSeatTest` 验证添加 AI、准备、断线和再开局重置。
- `GameMessageCodecTest` 验证中文名、分隔符、座位、START 和 SNAPSHOT 编解码。
- `NetworkMoveGuardTest` 验证房主只接受当前远端玩家请求。
- 真机验收需要至少两台手机，测试创建房间、加入、添加 AI、满座开局、出牌同步、结算再来一局。

#### 要求 6：适配玩家自由选择南北规则的不同玩法

技术选型：

- 使用规则接口 `RuleSet` 抽象规则差异。
- 使用 `RuleProfile` 保存规则元数据，便于 UI 展示和蓝牙同步。
- 使用 `SharedPreferences` 保存玩家选择，保证重启 App 后仍然保留。
- 蓝牙房间规则由房主决定，`START` 消息同步给加入者，避免各端规则不一致。

具体实现：

- 代码位置：`model/RuleSet.kt`
- 规则抽象：
  - `RuleSet.profile`
  - `RuleSet.classify(cards)`
  - `RuleSet.canLead(cards, isFirstTurn)`
  - `RuleSet.canBeat(previous, next)`
- 北方规则：`NorthRuleSet`
  - `firstCard = Card(Suit.Spades, Rank.Three)`
  - 黑桃 3 首出。
  - `bombMode = BombMode.Enhanced`
  - 四带一和同花顺作为增强强牌，可以跨牌型压制普通牌。
  - `sameSizeOnly = false`
- 南方规则：`SouthRuleSet`
  - `firstCard = Card(Suit.Diamonds, Rank.Three)`
  - 方块 3 首出。
  - `bombMode = BombMode.None`
  - `sameSizeOnly = true`
  - 同张数压制，五张牌之间按顺子、同花、葫芦、四带一、同花顺等级比较。
- 规则选择：
  - `RulesScreen` 中选择北方/南方玩法。
  - 点击保存后写入 `SharedPreferences`。
  - 人机和蓝牙开局都读取 `selectedRule`。
- 联机同步：
  - 房主开局时通过 `GameMessage.Start(seed, ruleName, seats)` 发送规则名。
  - 客户端收到 `START` 后用 `ruleSetByIdOrName(ruleName)` 转换为本地规则对象。

规则比较流程：

1. 玩家选择牌后，`GameController.play()` 先判断是否轮到该玩家。
2. 调用 `state.ruleSet.classify(cards)` 识别牌型。
3. 首出时调用 `state.ruleSet.canLead(cards, state.firstTurn)`，检查是否包含首牌。
4. 跟牌时调用 `state.ruleSet.canBeat(previous, next)`，根据南北规则判断能否压制。
5. 合法则更新 `GameState`；不合法则保留选择并显示提示。

验证方式：

- `HandClassifierTest` 覆盖单张、对子、三张、顺子、同花、葫芦、四带一、同花顺。
- 规则测试覆盖黑桃 3 / 方块 3 首出差异。
- 测试北方炸弹增强和南方同张数压制差异。
- 手动在规则设置页切换规则，保存后进入人机和蓝牙房间确认生效。

#### 要求 7：对扑克牌博弈 AI 模块提供不同 AI 算法实现策略，至少 2 种 AI 算法策略，需要使用策略模式

技术选型：

- 使用策略模式抽象 AI 算法：`AiStrategy`。
- 当前实现 3 种 AI，超过“至少 2 种”的要求。
- AI 算法不引入外部模型或第三方库，全部用 Kotlin 实现，保证 Android 端性能和构建稳定。
- AI 设计参考了公开扑克/博弈 AI 项目的算法思想，但没有直接复制开源代码，也没有在 Android 端加载神经网络模型。

参考来源：

- RLCard：一个面向纸牌游戏的强化学习工具包，支持 Blackjack、Leduc Hold'em、Texas Hold'em、UNO、Dou Dizhu、Mahjong 等环境，项目参考其“游戏环境 + Agent 策略接口 + 合法动作集合”的组织方式。
  - <https://github.com/datamllab/rlcard>
  - <https://arxiv.org/abs/1910.04376>
- OpenSpiel：DeepMind 开源的多智能体博弈研究框架，支持完美/不完美信息、回合制/同步行动、搜索和强化学习算法。项目参考其“游戏状态、合法动作、策略/Agent 解耦”的通用博弈建模方式。
  - <https://github.com/google-deepmind/open_spiel>
- ISMCTS：Cowling、Powley、Whitehouse 的 Information Set Monte Carlo Tree Search 论文，核心思想是把 Monte Carlo 搜索用于隐藏信息和不确定性游戏。项目困难 AI 没有完整实现树搜索，而是参考“对候选动作进行有限随机模拟和期望评分”的 rollout 思路。
  - <https://pure.york.ac.uk/portal/en/publications/information-set-monte-carlo-tree-search/>
- DouZero：快手开源的斗地主 AI，使用自博弈深度强化学习解决斗地主。项目没有使用 DouZero 的神经网络和训练模型，只参考其对斗地主这类出牌游戏的动作候选、残局压力和多玩家不完美信息问题的处理方向。
  - <https://github.com/kwai/DouZero>
  - <https://arxiv.org/abs/2106.06135>

代码实现：

- 代码位置：`controller/AiStrategy.kt`
- 策略接口：
  - `val name: String`
  - `fun chooseMove(state: GameState, playerId: Int): Move`
- AI 控制器：
  - `AiController.playTurnIfNeeded(state)`
  - 判断当前玩家是否 `PlayerKind.LocalAi`。
  - 调用当前策略生成 `Move`。
  - 交给 `GameController.applyMove()` 执行。

三种策略：

1. 简单难度：`GreedyAiStrategy`
   - 参考来源：
     - 参考 RLCard 和 OpenSpiel 中常见的 baseline agent 设计思路：先由环境给出合法动作集合，再由 Agent 选择一个动作。
     - 简单 AI 没有使用搜索或学习模型，而是实现最基础的规则型贪心策略，用作低难度基线。
   - 算法思想：
     - “先合法，再最小”。
     - AI 不预测后续局面，只在当前 `GameState` 中枚举合法出牌。
     - 在所有能出的牌里选择消耗最小、牌力较低的牌，模拟新手玩家的保守出牌。
   - 实现方式：
     - 调用 `gameController.legalPlays(state, playerId)` 获取所有合法候选。
     - 按牌张数、牌型强度、最大牌大小排序。
     - 选择最小候选。
     - 如果没有合法候选，返回 `Move.Pass(playerId)`。
   - 代码细节：
     - `legalPlays` 只枚举 1、2、3、5 张组合，保证候选牌已经经过 `RuleSet.classify()` 和 `RuleSet.canBeat()` 过滤。
     - `minWithOrNull(compareBy<List<Card>> { it.size }...)` 控制它优先选择“小动作”。
   - 适用场景：新手练习，出牌速度较快，强度低。

2. 普通难度：`HeuristicAiStrategy`
   - 参考来源：
     - 参考 RLCard/OpenSpiel 的“策略只依赖当前状态和合法动作”结构，不把规则判断写进 UI。
     - 参考 DouZero 对斗地主动作空间的处理方向：出牌游戏的 AI 不应随意拆坏组合，候选动作需要考虑剩余牌结构和对手压力。
     - 该策略是项目内为锄大地重写的启发式评分策略，不是某个开源项目的原样算法。
   - 算法思想：
     - 在“尽快减少手牌”和“保留组合/强牌”之间做权衡。
     - 当对手剩余牌少时，提高主动出牌和减少手牌数量的权重。
     - 当候选动作会拆对子、三张、炸弹或同花顺时增加惩罚，避免不必要地破坏手牌结构。
   - 评分因素：
     - 打出的牌越多，得分越高。
     - 对手最少手牌越少，压力越高，AI 更倾向于主动出更多牌。
     - 拆对子、三张等结构会扣分。
     - 非必要时打出四带一或同花顺会扣分，避免过早浪费强牌。
     - 牌型强度会加分，但高牌消耗会适度扣分。
   - 代码细节：
     - `pressure = state.players.filterNot { it.id == playerId }.minOf { it.hand.size }` 计算对手压力。
     - `breakPenalty(hand, cards)` 统计候选牌是否拆掉同点数组合。
     - 四带一和同花顺会被额外扣分，除非确实有必要。
     - `legal.firstOrNull { it.size == hand.size }` 保证如果能一手走完，普通 AI 也会优先获胜。
   - 适用场景：中等难度，能体现保留组合和强牌的策略。

3. 困难难度：`MonteCarloRolloutAiStrategy`
   - 参考来源：
     - 主要参考 ISMCTS 论文中“隐藏信息游戏使用 Monte Carlo 模拟评估候选行动”的思想。
     - 参考 OpenSpiel 对搜索/规划和博弈状态建模的通用结构，即策略根据状态和合法动作进行评估。
     - 参考 DouZero 对斗地主这种不完美信息出牌游戏的启发：残局压力、动作编码、并行候选评估和一手走完优先很重要。
     - 项目没有完整实现 ISMCTS 树结构，也没有使用 DouZero 深度强化学习模型；Android 端采用固定预算 rollout，是为了在手机性能、依赖风险和实训可解释性之间折中。
   - 算法思想：
     - 对每个合法候选动作做若干次轻量模拟。
     - 模拟后评估剩余手牌数量、剩余结构、牌型强度、拆牌损失和强牌消耗。
     - 选择平均评分最高的动作。
   - 实现方式：
     - 获取所有合法候选。
     - 如果存在一手出完，直接选择获胜动作。
     - 对每个候选动作调用 `rolloutScore()`。
     - `rolloutScore()` 结合剩余手牌数量、牌型强度、拆牌损失、炸弹消耗、剩余结构价值和模拟收官能力评分。
     - 使用固定 seed 的 `Random`，避免每次重组造成不可预测抖动。
   - 代码细节：
     - 默认 `rollouts = 24`，避免 Android 端计算量过大。
     - `gameController.play(state, playerId, cards)` 先模拟当前候选动作执行后的局面。
     - `remainingStructureValue(remaining)` 给对子、三张、炸弹、高牌等剩余结构加分。
     - `bombSpendPenalty(state, cards, remaining)` 惩罚非必要消耗四带一或同花顺。
     - `simulatedFinishScore(state, remaining, random)` 在有限轮数内模拟继续出牌，估计收官能力。
     - `playableGroups(state, simulated)` 复用 `RuleSet.classify()`，保证模拟过程仍遵守锄大地牌型。
   - 适用场景：更强人机，对残局和保留强牌更敏感。

UI 与房间适配：

- 人机对局前进入 `DifficultySelectScreen` 选择难度。
- 蓝牙房间中，房主给每个 AI 座位选择简单、普通或困难。
- 蓝牙对局中只有房主运行 AI，避免多端各自运行 AI 导致状态不同步。
- AI 出牌延迟根据难度不同设置：简单较快，普通中等，困难更慢，模拟思考时间。

验证方式：

- `GameControllerTest` 验证 AI 返回合法动作。
- 困难策略在可一手走完时优先获胜动作。
- 固定 seed 随机多局测试不死循环、不崩溃。
- 手动分别选择三档难度，观察出牌倾向差异。

#### 要求 8：关键软件工程阶段尽量使用大语言模型和 AI 工具支持，并详细说明使用情况和效果评价

技术选型：

- 使用 OpenAI Codex / GPT-5 作为主要 AI 辅助工具。
- 使用 Android Studio 作为编码、构建、真机运行和 GitHub HTTPS 上传工具。
- 使用 PlantUML 和 ProcessOn UML 完成建模工具链。
- 使用 Gradle、JUnit、Android Instrumented Test 做验证。

开发者实际使用方式：

- 需求阶段：
  - 使用 AI 辅助阅读实训要求，拆分 8 个评分点。
  - 形成 `docs/requirements.md` 中的需求和评分点映射。
- 设计阶段：
  - 使用 AI 辅助比较 MVC、策略模式、蓝牙 C/S 房间、房主权威同步、SharedPreferences 本地持久化等方案。
  - 最终由开发者确定技术选型并落地到代码。
- UML 阶段：
  - 使用 AI 辅助生成 PlantUML 初稿。
  - 开发者根据当前代码调整 `.puml`。
  - 再将 PlantUML 内容导入或参照迁移到 ProcessOn UML，整理正式图。
- 编码阶段：
  - 使用 AI 辅助生成 Kotlin/Compose 页面、规则模型、AI 策略、蓝牙协议和测试结构。
  - 开发者在 Android Studio 中运行、修改和确认。
- 测试阶段：
  - 使用 AI 辅助补充规则、AI、蓝牙模拟、玩家成长、消息协议和动画 key 测试用例。
  - 使用 `./gradlew test --no-daemon` 运行本地单元测试。
- 文档阶段：
  - 使用 AI 辅助检查文档是否跟当前代码一致。
  - 开发者按实训评分点补充完整说明、测试报告、发布说明和最终检查表。
- 发布阶段：
  - 使用 AI 辅助检查 debug/release 区别、签名证书、GitHub 敏感文件排除和 HTTPS remote。
  - release 签名私钥由开发者本机保管，不上传仓库。

效果评价：

- 效率提升：
  - AI 能快速把评分点转化为模块清单、测试清单和文档结构。
  - 对蓝牙消息协议、AI 策略、UML 类关系梳理帮助明显。
- 质量提升：
  - AI 辅助发现了旧文档和当前代码不一致的问题。
  - AI 辅助补齐了蓝牙模拟测试、玩家成长测试、无可压牌提示和金币消费确认说明。
- 局限性：
  - AI 不能替代真实蓝牙多机测试。
  - AI 生成的规则说明必须由开发者按老师要求确认。
  - AI 生成的 UML 和文档必须跟最终代码逐项核对。
- 最终确认：
  - 当前项目的代码、测试、UML、release 包和文档均由开发者在本机项目中检查。
  - AI 是辅助工具，不是项目运行结果和答辩材料的最终责任主体。

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

UI 中的改名、头像解锁、自定义头像和战绩重置不会直接扣费，而是先进入 `ConfirmProfileSpendDialog` 二次确认；确认后才调用 `ProfileController`，结果通过 `ProfileActionResultDialog` 弹窗反馈，用户名下方不再显示临时操作提示。

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

UML 文件位于 `docs/uml/`。开发者先用 PlantUML 代码描述 7 类图，再将 PlantUML 图内容导入或参照迁移到 ProcessOn UML 中完成正式绘制；项目内同时保留 `.puml` 源文件、导出的 PNG 和 ProcessOn UML 画板截图用于展示建模过程。

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

GitHub 与开源许可检查：

- 当前远程地址：`https://github.com/Mojieeee/uml_chudadi.git`。
- 当前项目未在根目录加入 `LICENSE` 文件；如果需要公开开源，建议补充 MIT 或 Apache-2.0 许可证。
- 代码仓库不应包含 `keystore.properties`、`.jks`、`local.properties`、`.gradle/`、`.kotlin/`、`build/`。
- release 证书为自签名证书，所有者和发布者均为 `CN=Chudadi, OU=Game, O=Chudadi, L=Guangzhou, ST=Guangdong, C=CN`。
- 后续 release 升级必须继续使用同一 `keystore/chudadi-release.jks` 和别名 `chudadi`。

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
