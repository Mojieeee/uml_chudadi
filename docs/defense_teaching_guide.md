# 锄大地 Android 实训答辩速成教学文档

这份文档是给答辩前快速理解项目用的。目标不是把你变成 Android 高手，而是让你能把项目讲清楚、能回答老师围绕实训要求提出的问题，并且知道每个功能在代码里大概在哪里。

项目名称：锄大地 Android 棋牌游戏  
项目包名：`com.example.uml_chudadi`  
主入口：`app/src/main/java/com/example/uml_chudadi/MainActivity.kt`  
主界面文件：`app/src/main/java/com/example/uml_chudadi/view/ChudadiApp.kt`  
完整说明文档：`docs/complete_project_documentation.md`

## 1. 答辩时先记住的核心说法

如果只能记一段话，可以这样说：

> 本项目是一个 Kotlin + Jetpack Compose 实现的 Android 锄大地棋牌游戏。整体采用 MVC 思想分层，Model 层负责扑克牌、牌型、规则和状态，Controller 层负责回合推进、AI 决策、蓝牙同步和玩家成长，View 层用 Compose 实现大厅、牌桌、蓝牙房间、玩家中心和结算页。项目实现了人机对局、多人蓝牙房间、南北规则切换、三种 AI 策略、玩家成长系统、音乐动画、release 签名包和 UML 7 大图。开发过程中我使用 OpenAI Codex / GPT-5 辅助需求分析、代码实现、测试和文档整理，但最终规则、代码结构、UML、测试和运行效果都在 Android Studio 中进行了人工确认。

答辩时不要说“我完全不会，都是 AI 做的”。更稳的说法是：

> 我是借助 Codex 完成了需求拆解、代码辅助和文档整理，但我现在理解项目的主要结构：Activity 作为入口，Compose 管理界面，GameController 管游戏流程，RuleSet 管南北规则，AiStrategy 管人机策略，BluetoothTransport 管联机，ProfileController 管玩家成长。

## 2. Android 开发最基础概念

### 2.1 Android Studio 是什么

Android Studio 是 Android 官方 IDE，主要负责：

- 打开项目。
- 编辑 Kotlin 代码。
- 下载和同步 Gradle 依赖。
- 连接模拟器或真机。
- 构建 APK / AAB。
- 运行、调试、查看日志。
- 管理 Git 和 GitHub。

本项目在 Android Studio 中打开后，常见操作是：

```bash
./gradlew test --no-daemon
./gradlew :app:assembleDebug --no-daemon
./gradlew :app:assembleRelease --no-daemon
./gradlew :app:runRelease
```

### 2.2 Gradle 是什么

Gradle 是 Android 项目的构建工具。它负责：

- 编译 Kotlin 代码。
- 打包资源文件。
- 生成 APK / AAB。
- 运行单元测试。
- 处理 debug / release 两种构建类型。
- 读取签名配置生成 release 包。

本项目里常见 Gradle 文件：

- `settings.gradle.kts`
- `build.gradle.kts`
- `app/build.gradle.kts`

答辩时可以说：

> Gradle 相当于项目的自动化构建脚本，它把 Kotlin 代码、Compose UI、资源文件、Manifest、签名配置整合起来，最后生成可安装的 APK。

### 2.3 Manifest 是什么

Manifest 文件路径：

```text
app/src/main/AndroidManifest.xml
```

它的作用是告诉 Android 系统：

- App 的包名。
- App 入口 Activity。
- 需要哪些权限。
- 蓝牙、定位、震动等能力声明。
- 应用图标和主题。

本项目需要蓝牙相关权限，因为实现了好友蓝牙对局。Android 12 以上需要 `BLUETOOTH_CONNECT`、`BLUETOOTH_SCAN` 等权限；旧系统还需要定位权限辅助蓝牙发现。

### 2.4 Activity 是什么

Activity 可以理解为 Android App 的一个页面入口。本项目只有一个主要 Activity：

```text
app/src/main/java/com/example/uml_chudadi/MainActivity.kt
```

它做的事情很简单：

```kotlin
setContent {
    Uml_chudadiTheme {
        ChudadiApp()
    }
}
```

意思是：

- App 启动后进入 `MainActivity`。
- `MainActivity` 使用 Compose 的 `setContent` 加载界面。
- 真正的页面逻辑都在 `ChudadiApp()` 里。

答辩时可以说：

> MainActivity 是 Android 的入口，项目采用单 Activity + Compose 多页面状态切换的方式，避免引入额外 Navigation 依赖。

### 2.5 Jetpack Compose 是什么

Jetpack Compose 是 Android 新式 UI 框架。传统 Android 常用 XML 写布局，而 Compose 用 Kotlin 函数直接写界面。

例如一个按钮、文本、卡片都可以写成 Kotlin 函数：

```kotlin
@Composable
fun LobbyScreen(...) {
    Text("锄大地")
    Button(onClick = { ... }) {
        Text("人机对局")
    }
}
```

Compose 的核心思想是：

```text
状态变了 -> 界面自动重组
```

本项目的页面状态主要在：

```text
app/src/main/java/com/example/uml_chudadi/view/ChudadiApp.kt
```

例如：

- 当前页面：`screen`
- 当前规则：`selectedRule`
- 当前对局：`gameState`
- 当前玩家档案：`profile`
- 当前人机难度：`difficulty`
- 当前蓝牙房间座位：`networkSeats`

### 2.6 资源文件是什么

Android 资源放在：

```text
app/src/main/res/
```

本项目常见资源：

- 图标：`mipmap-*`
- 开屏背景：`drawable-nodpi/splash_cards.jpg`
- 颜色：`values/colors.xml`
- 字符串：`values/strings.xml`
- 主题：`values/themes.xml`

音乐资源放在：

```text
app/src/main/assets/doudizhu_bgm.ogg
```

答辩时可以说：

> Android 的资源和代码分离，图片、图标、主题、音频等放在 res 或 assets 中，代码通过资源 id 或 asset 名称读取。

### 2.7 SharedPreferences 是什么

SharedPreferences 是 Android 本地轻量存储，用来保存简单数据。

本项目用它保存：

- 规则选择。
- 音乐/音效/震动设置。
- 默认难度。
- 玩家昵称。
- 玩家金币。
- 经验和等级。
- 战绩和成就。
- 头像解锁状态。

代码位置：

```text
app/src/main/java/com/example/uml_chudadi/profile/ProfileStore.kt
```

答辩时可以说：

> 因为本项目不接服务器，所以玩家成长数据和设置都使用 SharedPreferences 做本地持久化。

## 3. 项目整体架构怎么理解

### 3.1 一句话理解架构

本项目按 MVC 思想组织：

```text
Model：游戏数据和规则
Controller：业务逻辑和状态推进
View：Compose 页面展示和用户交互
```

### 3.2 Model 层

Model 层回答：“游戏世界里有什么？”

代码位置：

```text
app/src/main/java/com/example/uml_chudadi/model/
app/src/main/java/com/example/uml_chudadi/profile/ProfileModels.kt
app/src/main/java/com/example/uml_chudadi/transport/RoomSeat.kt
app/src/main/java/com/example/uml_chudadi/transport/GameSnapshot.kt
```

关键类：

- `Card`：一张牌。
- `Suit`：花色。
- `Rank`：牌点。
- `Deck`：牌堆和发牌。
- `HandType`：识别出来的牌型。
- `HandCategory`：牌型类别。
- `GameState`：当前牌局状态。
- `Player`：玩家。
- `RuleSet`：规则接口。
- `NorthRuleSet`：北方规则。
- `SouthRuleSet`：南方规则。
- `PlayerProfile`：玩家档案。
- `RoomSeat`：蓝牙房间座位。
- `GameSnapshot`：蓝牙同步快照。

答辩说法：

> Model 层不关心界面怎么显示，只描述牌、玩家、规则、房间、快照和玩家档案这些领域对象。

### 3.3 Controller 层

Controller 层回答：“玩家操作后游戏怎么变化？”

代码位置：

```text
app/src/main/java/com/example/uml_chudadi/controller/
app/src/main/java/com/example/uml_chudadi/profile/ProfileController.kt
app/src/main/java/com/example/uml_chudadi/transport/NetworkMoveGuard.kt
```

关键类：

- `GameController`
  - 新建牌局。
  - 出牌。
  - 不出。
  - 判断合法出牌。
  - 判断胜利。
- `AiController`
  - 判断当前是否轮到本地 AI。
  - 调用对应 AI 策略。
- `PlayerActionPolicy`
  - 判断按钮能不能点。
  - 无可压牌时显示提示。
- `RoomController`
  - 管理房间状态。
- `NetworkMoveGuard`
  - 防止蓝牙重复/非法请求。
- `ProfileController`
  - 结算金币和经验。
  - 改名扣金币。
  - 解锁头像。
  - 重置战绩。

答辩说法：

> Controller 层是业务核心。比如出牌时 UI 不直接修改手牌，而是把用户选择传给 GameController，由 GameController 根据 RuleSet 判断是否合法，再返回新的 GameState。

### 3.4 View 层

View 层回答：“玩家看到什么、点什么？”

代码位置：

```text
app/src/main/java/com/example/uml_chudadi/view/ChudadiApp.kt
```

主要页面：

- `SplashScreen`：开屏页。
- `LobbyScreen`：大厅。
- `DifficultySelectScreen`：人机难度选择。
- `RulesScreen`：规则设置。
- `SettingsScreen`：设置。
- `TutorialScreen`：教程。
- `ProfileScreen`：玩家中心。
- `NearbyScreen`：好友蓝牙。
- `GameScreen`：牌桌。
- `ResultScreen`：结算。

答辩说法：

> View 层只负责展示和收集用户意图，比如玩家点击“出牌”，View 会把选择的牌交给 Controller，而不是自己判断所有规则。

## 4. 实训 8 个评分点怎么讲

### 4.1 ProcessOn UML 7 大图

老师可能问：

> 你们 UML 是怎么做的？

你可以答：

> 我先用 PlantUML 代码描述 7 类 UML 图，因为文本格式方便版本管理和跟代码同步。然后把 PlantUML 图内容导入或参照迁移到 ProcessOn UML 中，调整布局后作为正式图。项目里 `docs/uml` 保留了 `.puml` 源文件和导出的 `.png`，包括用例图、类图、顺序图、状态图、活动图、组件图和部署图。

7 张图怎么讲：

- 用例图：讲玩家、房主、加入者、人机能做什么。
- 类图：讲 Model、Controller、Transport、Profile、View 的类关系。
- 顺序图：讲一次出牌如何从 UI 到 Controller，再到蓝牙同步。
- 状态图：讲开屏、大厅、房间、发牌、对局、暂停、结算的状态切换。
- 活动图：讲完整游戏流程。
- 组件图：讲 UI、控制器、模型、蓝牙、存储、音乐的组件依赖。
- 部署图：讲房主手机、加入者手机、蓝牙栈、开发环境、GitHub。

对应文件：

```text
docs/uml/use_case.puml
docs/uml/class_diagram.puml
docs/uml/sequence_game_turn.puml
docs/uml/state_game.puml
docs/uml/activity_play.puml
docs/uml/component_diagram.puml
docs/uml/deployment_diagram.puml
```

### 4.2 需求分析和面向对象设计

老师可能问：

> 你的需求分析体现在哪里？

你可以答：

> 需求分析放在 `docs/requirements.md`，里面把功能需求、非功能需求和评分点映射都列出来了。功能上包括人机对局、好友蓝牙、南北规则、AI 策略、玩家中心、音乐动画和 release 包。面向对象设计主要体现在 MVC 分层和类图中。

面向对象怎么讲：

- `Card` 表示牌。
- `Player` 表示玩家。
- `GameState` 表示牌局状态。
- `RuleSet` 表示规则接口。
- `GameController` 表示游戏控制器。
- `AiStrategy` 表示 AI 策略接口。
- `GameTransport` 表示传输接口。
- `PlayerProfile` 表示玩家档案。

答辩重点：

> 每个对象都有明确职责，不把所有逻辑堆在一个 Activity 里。

### 4.3 设计模式

老师可能问：

> 项目用了哪些设计模式？

你可以答：

> 主要用了策略模式、MVC、接口隔离、主机权威同步、状态模式、命令对象思想、编码器/解码器和仓储模式。

最重要要会讲这几个：

1. 策略模式

代码：

```text
controller/AiStrategy.kt
```

说法：

> AiStrategy 是 AI 算法接口，简单、普通、困难三种 AI 是三个实现类。GameController 不关心具体 AI 算法，只接收 Move，这样以后新增 AI 不需要改游戏主流程。

2. MVC

说法：

> Model 存数据，Controller 管逻辑，View 展示界面。这样 UI 和规则解耦。

3. GameTransport 接口隔离

代码：

```text
transport/GameTransport.kt
```

说法：

> 蓝牙传输和本地模拟都实现 GameTransport，UI 不直接操作 BluetoothSocket，便于测试和替换。

4. 主机权威同步

代码：

```text
transport/NetworkMoveGuard.kt
transport/GameSnapshot.kt
```

说法：

> 蓝牙对局里只有房主真正推进牌局，客户端只发送请求，房主校验后广播快照，避免多台设备状态不一致。

5. 仓储模式

代码：

```text
profile/ProfileStore.kt
```

说法：

> 玩家档案统一由 ProfileStore 保存到 SharedPreferences，UI 不直接处理存储细节。

### 4.4 UI 页面和页面切换

老师可能问：

> 你怎么实现不同页面切换？

你可以答：

> 项目没有引入 Navigation 依赖，而是在 `ChudadiApp.kt` 中使用 `Screen` 枚举保存当前页面，再用 `AnimatedScreenHost` 做页面切换动画。点击不同按钮时修改 `screen` 状态，Compose 会自动重组界面。

页面列表：

```text
Splash
Lobby
Profile
DifficultySelect
Rules
Settings
Tutorial
Nearby
Game
Result
```

技术点：

- Compose 声明式 UI。
- `remember` 保存状态。
- `mutableStateOf` 触发界面重组。
- Canvas 绘制扑克牌、头像、光效。
- 动画使用 Compose 内置动画 API。

答辩时可以演示：

1. 开屏页点击开始。
2. 大厅进入人机对局。
3. 选择难度。
4. 点击开始游戏看发牌动画。
5. 返回大厅。
6. 进入规则设置。
7. 进入玩家中心。
8. 进入蓝牙房间。

### 4.5 多人蓝牙连接

老师可能问：

> 蓝牙不是一对一吗？你怎么实现多人？

你可以答：

> 项目采用 C/S 房间模型。一台手机作为房主，使用 BluetoothServerSocket 监听连接；其他手机作为客户端，通过 BluetoothSocket 连接房主。不是客户端两两互连，而是所有客户端都连房主，由房主广播 ROOM、START、MOVE_ACCEPTED、STATE_SNAPSHOT 等消息，实现多人同步。

关键代码：

```text
transport/BluetoothTransports.kt
transport/GameTransport.kt
transport/GameMessage.kt
transport/RoomSeat.kt
transport/NetworkMoveGuard.kt
```

蓝牙流程：

1. 房主创建房间。
2. 房主打开 RFCOMM 监听。
3. 加入者搜索设备。
4. 加入者连接房主。
5. 加入者发送 `HELLO`。
6. 房主把加入者放进空座位。
7. 房主广播 `ROOM`。
8. 四座位满且准备后，房主广播 `START`。
9. 客户端出牌发送 `MOVE_REQUEST`。
10. 房主校验后广播 `MOVE_ACCEPTED` 和 `STATE_SNAPSHOT`。

为什么要房主权威：

> 如果每台手机都自己计算，可能因为 AI、延迟或重复点击导致状态不同。房主权威可以保证所有端以同一个快照为准。

### 4.6 南北规则

老师可能问：

> 南北规则区别是什么？怎么实现自由切换？

你可以答：

> 项目用 RuleSet 接口抽象规则差异。北方规则是黑桃 3 首出，开启炸弹增强，四带一和同花顺可以作为强牌压制普通牌型。南方规则是方块 3 首出，更强调同张数压制，五张牌之间按顺子、同花、葫芦、四带一、同花顺比较。规则选择保存在 SharedPreferences，后续人机和蓝牙对局都会使用保存后的规则。蓝牙开局时房主通过 START 消息同步规则给客户端。

关键代码：

```text
model/RuleSet.kt
```

核心接口：

```kotlin
interface RuleSet {
    val profile: RuleProfile
    fun classify(cards: List<Card>): HandType?
    fun canLead(cards: List<Card>, isFirstTurn: Boolean): Boolean
    fun canBeat(previous: HandType, next: HandType): Boolean
}
```

出牌判断流程：

1. `GameController.play()` 收到玩家选择的牌。
2. 调用 `RuleSet.classify()` 识别牌型。
3. 首出调用 `canLead()` 判断是否包含首牌。
4. 跟牌调用 `canBeat()` 判断能否压过上一手。
5. 合法就更新 `GameState`，不合法就显示提示。

### 4.7 AI 策略模块

老师可能问：

> 你这个 AI 是真正的 AI 吗？用了什么算法？

你可以答：

> 本项目的人机模块是传统游戏 AI，不是接入大模型，也不是深度学习训练模型。它属于可解释的本地博弈 AI，采用策略模式、合法动作枚举、启发式评分和 Monte Carlo rollout。项目参考了 RLCard、OpenSpiel、DouZero 和 ISMCTS 的思想，但没有直接复制开源代码，也没有加载外部模型。

关键代码：

```text
controller/AiStrategy.kt
```

接口：

```kotlin
interface AiStrategy {
    val name: String
    fun chooseMove(state: GameState, playerId: Int): Move
}
```

三种 AI：

1. 简单：`GreedyAiStrategy`

说法：

> 简单 AI 是贪心策略。它先调用 `legalPlays` 找出所有合法出牌，再选最小、最简单的一手牌。如果没有能出的牌就不出。

2. 普通：`HeuristicAiStrategy`

说法：

> 普通 AI 是启发式评分策略。它会考虑出掉牌的数量、对手剩余牌数、拆对子/三张的损失、是否过早消耗四带一或同花顺等因素。

3. 困难：`MonteCarloRolloutAiStrategy`

说法：

> 困难 AI 参考 ISMCTS 的 rollout 思想，对每个候选动作做有限次数模拟，估计打完这手后剩余手牌结构和后续收官能力，再选择平均评分最高的动作。

为什么不接真正训练模型：

> 锄大地没有成熟可直接部署到 Android 的预训练模型。如果自己训练强化学习模型，需要实现训练环境、动作编码、自博弈训练和 TFLite/ONNX 移动端部署，成本较高。实训项目选择本地可解释 AI，更稳定也更容易测试。

### 4.8 AI 工具使用说明

老师可能问：

> 你怎么使用大语言模型和 AI 工具？

你可以答：

> 开发过程中我使用 OpenAI Codex / GPT-5 辅助需求拆解、架构设计、代码实现、测试补全、UML 初稿和文档整理。比如根据实训要求拆出 8 个评分点，根据 MVC 设计生成模块结构，根据蓝牙要求梳理房主权威同步方案，根据 AI 要求补充三种策略，并用它辅助检查文档和代码是否一致。但规则确认、代码运行、真机测试、release 签名和最终文档都由我在 Android Studio 中人工确认。

文档位置：

```text
docs/ai_usage.md
docs/complete_project_documentation.md
```

重点态度：

> AI 是辅助工具，不是替代开发者。项目最终结果要能运行、能测试、能解释。

## 5. 项目运行时一局游戏怎么走

### 5.1 人机对局流程

1. App 启动进入 `MainActivity`。
2. `MainActivity` 加载 `ChudadiApp()`。
3. 默认显示 `SplashScreen`。
4. 点击开始进入 `LobbyScreen`。
5. 点击人机对局进入 `DifficultySelectScreen`。
6. 选择简单/普通/困难。
7. 进入 `GameScreen` 的 `ReadyToStart` 阶段。
8. 点击开始游戏。
9. 创建 `GameController`。
10. 调用 `newGame()` 发牌。
11. 进入 `Dealing` 阶段播放发牌动画。
12. 动画结束进入 `Playing`。
13. 玩家选择手牌。
14. 点击出牌。
15. `PlayerActionPolicy` 判断按钮是否可用。
16. `GameController.play()` 校验规则。
17. 如果合法，更新 `GameState`。
18. 如果轮到 AI，`AiController` 调用 AI 策略。
19. 有玩家手牌为空，进入 `ResultScreen`。
20. `ProfileController.settleMatch()` 更新金币、经验、战绩。

### 5.2 蓝牙对局流程

1. 大厅点击好友蓝牙对局。
2. 进入 `NearbyScreen`。
3. 房主点击创建房间。
4. 房主进入四座位房间。
5. 加入者点击加入对局。
6. 加入者搜索设备并连接房主。
7. 加入者发送 `HELLO`。
8. 房主更新座位并广播 `ROOM`。
9. 房主添加 AI 或等待好友补满四座。
10. 满座且准备后房主点击开始。
11. 房主生成 seed 和规则，广播 `START`。
12. 各端根据相同 seed 进入同一牌局。
13. 客户端出牌发送 `MOVE_REQUEST`。
14. 房主用 `NetworkMoveGuard` 校验。
15. 房主调用 `GameController` 更新状态。
16. 房主广播 `MOVE_ACCEPTED` 和 `STATE_SNAPSHOT`。
17. 客户端按快照刷新界面。

## 6. 老师可能问的问题和回答

### Q1：为什么选择 Compose，而不是 XML？

答：

> Compose 是 Android 新式声明式 UI，更适合用状态驱动游戏界面。项目里页面状态、手牌选择、倒计时、动画都可以通过 Kotlin 状态控制，减少 XML 和 Activity 之间的来回绑定。

### Q2：为什么没有用 Navigation？

答：

> 项目页面数量虽然多，但逻辑集中在一个游戏应用里。我使用 `Screen` 枚举和 `AnimatedScreenHost` 管理页面切换，减少第三方依赖，也方便实训中解释页面状态。

### Q3：蓝牙怎么保证多人同步？

答：

> 使用房主权威模型。客户端只发请求，房主校验并广播快照。所有设备以房主 `STATE_SNAPSHOT` 为准。

### Q4：AI 是不是大模型？

答：

> 运行时不是大模型，而是本地游戏 AI。它用策略模式实现三种算法：贪心、启发式评分和 Monte Carlo rollout。大模型主要用于开发过程辅助。

### Q5：南北规则怎么切换？

答：

> `RuleSet` 是规则接口，`NorthRuleSet` 和 `SouthRuleSet` 是两个实现。规则设置页保存选择后，开局时读取当前规则。蓝牙房主通过 START 消息同步规则。

### Q6：为什么要测试模块？

答：

> 因为规则、AI、蓝牙消息和玩家成长都容易出 bug。单元测试能验证牌型、规则、消息编解码、房间座位和 AI 合法动作；蓝牙多机不方便时，用模拟测试覆盖并发加入和同步逻辑。

### Q7：release 和 debug 有什么区别？

答：

> debug 主要用于开发调试，签名是默认 debug 签名；release 用正式 keystore 签名，适合给别人安装测试。后续升级 release 版必须使用同一个 `.jks` 证书，否则手机不能覆盖安装。

### Q8：GitHub 为什么不上传 keystore？

答：

> keystore 是 App 的发布私钥，泄露后别人可以伪造升级包，所以 `.jks` 和 `keystore.properties` 都在 `.gitignore` 中，只本机备份。

### Q9：如果老师问“这个项目是不是你写的？”

建议答：

> 项目开发过程中我确实大量借助了 Codex / GPT-5 做需求拆解、代码辅助和文档整理，但我现在理解项目的主要模块、设计模式和运行流程，也能说明代码结构、规则逻辑、蓝牙同步和 AI 策略。AI 是开发辅助工具，最终代码运行、测试和答辩说明由我确认。

## 7. 答辩演示顺序

建议按这个顺序演示，不容易乱：

1. 打开 App。
2. 开屏页点击开始游戏。
3. 大厅介绍：玩家信息、人机、蓝牙、规则、设置、教程。
4. 进入规则设置，讲南北规则。
5. 回大厅，进入人机对局。
6. 选择困难难度。
7. 点击开始游戏，看发牌动画。
8. 出一手牌，讲 `GameController.play()`。
9. 点击提示或不出，讲 `legalPlays`。
10. 打开设置菜单，讲暂停。
11. 返回大厅。
12. 进入玩家中心，讲金币、等级、头像、战绩。
13. 进入蓝牙页面，讲创建房间/加入对局/四座位。
14. 打开 docs/uml，展示 7 张 UML 图。
15. 打开测试文件，展示测试覆盖。

## 8. 你最应该熟悉的 10 个文件

1. `MainActivity.kt`
   - App 入口。
2. `ChudadiApp.kt`
   - 所有主要 Compose 页面和页面切换。
3. `Card.kt`
   - 扑克牌模型。
4. `HandClassifier.kt`
   - 牌型识别。
5. `RuleSet.kt`
   - 南北规则。
6. `GameState.kt`
   - 牌局状态。
7. `GameController.kt`
   - 出牌、不出、胜负。
8. `AiStrategy.kt`
   - 三种 AI 策略。
9. `BluetoothTransports.kt`
   - 真实蓝牙传输。
10. `GameMessage.kt`
   - 蓝牙消息协议。

## 9. 代码理解路线

如果你有半天时间，按这个顺序看：

1. 看 `MainActivity.kt`
   - 明白入口是 `ChudadiApp()`。
2. 看 `ChudadiApp.kt` 里的 `Screen`
   - 明白页面有哪些。
3. 看 `model/Card.kt`
   - 明白牌怎么表示。
4. 看 `model/HandClassifier.kt`
   - 明白牌型怎么识别。
5. 看 `model/RuleSet.kt`
   - 明白南北规则差异。
6. 看 `controller/GameController.kt`
   - 明白出牌流程。
7. 看 `controller/AiStrategy.kt`
   - 明白 AI 三种算法。
8. 看 `transport/GameMessage.kt`
   - 明白蓝牙消息。
9. 看 `transport/RoomSeat.kt`
   - 明白四座位。
10. 看 `profile/ProfileController.kt`
   - 明白金币经验战绩。

## 10. 测试怎么讲

测试文件在：

```text
app/src/test/java/com/example/uml_chudadi/
app/src/androidTest/java/com/example/uml_chudadi/
```

重点测试：

- `HandClassifierTest`
  - 测牌型。
- `GameControllerTest`
  - 测发牌、首出、过牌、胜利。
- `PlayerActionPolicyTest`
  - 测无可压牌和按钮状态。
- `GameMessageCodecTest`
  - 测蓝牙消息编解码。
- `RoomSeatTest`
  - 测四座位和 AI 补位。
- `NetworkMoveGuardTest`
  - 测房主权威校验。
- `SimulatedBluetoothHubTest`
  - 模拟多人蓝牙加入和广播。
- `ProfileControllerTest`
  - 测金币、经验、头像、战绩和成就。

答辩说法：

> 蓝牙真实多机测试受设备数量和权限影响，所以项目同时加入了模拟蓝牙测试，用 Fake/Simulated transport 模拟房主和多个客户端，覆盖并发加入、房满、广播和移动请求。

## 11. 如果你被问到“Codex 做了什么”

可以这样说，诚实但不失分：

> Codex 主要帮助我完成了需求拆解、代码生成建议、错误定位、UML 初稿、测试场景和文档整理。我不是直接把生成结果当成最终交付，而是在 Android Studio 中运行、修改、测试和确认。尤其是实训要求的 8 个点，我都在文档和代码中做了对应：UML、MVC、设计模式、UI、蓝牙、南北规则、AI 策略和 AI 工具使用说明。

不要这样说：

```text
都是 AI 做的，我不知道。
代码我没看过。
老师你看文档就行。
```

更好的说法：

```text
我借助 AI 工具完成开发，但我理解项目结构，能说明核心代码和设计原因。
```

## 12. 常见术语速查

| 术语 | 你可以怎么理解 |
| --- | --- |
| Activity | Android 页面入口 |
| Compose | 用 Kotlin 写 UI 的框架 |
| Composable | Compose 里的界面函数 |
| State | 控制界面变化的数据 |
| Recomposition | 状态变化后界面自动刷新 |
| Gradle | 构建和打包工具 |
| Manifest | App 权限、入口、图标配置 |
| APK | Android 安装包 |
| AAB | 应用商店发布包 |
| debug | 开发调试版本 |
| release | 正式签名版本 |
| keystore | release 签名证书 |
| MVC | Model、View、Controller 分层 |
| Strategy | 策略模式 |
| RFCOMM | Android Classic Bluetooth socket 通信 |
| Snapshot | 用于同步的状态快照 |
| SharedPreferences | Android 本地轻量存储 |

## 13. 最后背下来的 3 段话

### 13.1 项目介绍

> 我的项目是一个 Android 锄大地棋牌游戏，使用 Kotlin 和 Jetpack Compose 开发。功能包括人机对局、好友蓝牙房间、南北规则、三档 AI、玩家成长、音乐动画、release 包和 UML 文档。架构上采用 MVC 思想，规则和 UI 分离，蓝牙采用房主权威同步。

### 13.2 蓝牙介绍

> 蓝牙部分使用 Classic Bluetooth RFCOMM。房主通过 BluetoothServerSocket 创建房间，加入者通过 BluetoothSocket 连接房主。项目不是一对一匹配，而是房主维护四个座位并广播房间和牌局状态，客户端只发送出牌请求，房主校验后广播快照。

### 13.3 AI 介绍

> AI 部分使用策略模式。AiStrategy 是统一接口，简单 AI 是贪心策略，普通 AI 是启发式评分策略，困难 AI 是 Monte Carlo rollout 策略。它不是大模型 AI，而是本地可解释的传统游戏 AI。项目开发过程中借助了 Codex / GPT-5，但运行时 AI 是 Kotlin 算法实现。

## 14. 答辩前检查清单

- 能打开 App。
- 能进入大厅。
- 能进入人机对局。
- 能选择难度。
- 能点击开始游戏并看到发牌动画。
- 能出牌、不出、提示、重选。
- 能进入规则设置并说明南北规则。
- 能进入蓝牙页面并说明创建/加入/四座位。
- 能打开玩家中心并说明金币、等级、头像、战绩。
- 能展示 `docs/uml` 的 7 张图。
- 能说出 `GameController`、`RuleSet`、`AiStrategy`、`GameTransport` 分别干什么。
- 能解释 Codex / GPT-5 在项目中是辅助工具。

