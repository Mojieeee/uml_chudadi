# UML 建模说明

本项目正式交付以 ProcessOn UML 图为准。开发者先用 PlantUML 代码描述 7 类 UML 图，再将 PlantUML 图内容导入或参照迁移到 ProcessOn UML 中整理为正式图。`docs/uml` 中的 `.puml` 文件用于版本管理和快速语法检查，`.png` 文件用于报告引用和对照。

当前 `.puml` 已按最新代码同步：包含开屏页、玩家中心、人机预备/发牌阶段、三档 AI、蓝牙四座位、房主权威同步、`TransportEvent` 事件流、客户端断线托管、房主断线提示退出、玩家成长系统、release 签名和 GitHub HTTPS 上传注意事项。

## 七大图对应关系

| UML 图 | 文件 | 说明 |
| --- | --- | --- |
| 用例图 | `use_case.puml` / `use_case.png` | 玩家、房主、加入者、人机与断线处理用例 |
| 类图 | `class_diagram.puml` / `class_diagram.png` | MVC 分层下的 Model、Controller、Transport、Profile、View/Audio 核心类关系 |
| 顺序图 | `sequence_game_turn.puml` / `sequence_game_turn.png` | 本地出牌、蓝牙房主权威同步、客户端断线托管和房主断线退出顺序 |
| 状态图 | `state_game.puml` / `state_game.png` | 大厅、房间、牌桌、暂停、断线托管、房主断线退出、结算状态转换 |
| 活动图 | `activity_play.puml` / `activity_play.png` | 开局、出牌、不出、断线处理、结算和再开局流程 |
| 组件图 | `component_diagram.puml` / `component_diagram.png` | UI、控制器、模型、消息、蓝牙、断线提示和系统能力 |
| 部署图 | `deployment_diagram.puml` / `deployment_diagram.png` | 房主手机、加入者手机、蓝牙栈、断线提示和测试环境 |

## 与阶段产物的对应

| 阶段 | 阶段产物 | UML 图 | 在实验报告中的展示位置 |
| --- | --- | --- | --- |
| 阶段 1：开始（Inception） | Use-Case Model、Vision、Supplementary Specification、Glossary、Iteration Plan | `use_case.png` | 第 4.4 节阶段图片成果 |
| 阶段 2：细化迭代 1 / 构造 | Domain Model、System Sequence Diagram、Operation Contract、Package/Interaction/Class Diagram | `class_diagram.png`、`sequence_game_turn.png`、`component_diagram.png` | 第 5.5 节阶段图片成果 |
| 阶段 3：细化迭代 2 / 构造 | 出牌规则模块、蓝牙通信模块、设计模式应用、联机部署说明 | `sequence_game_turn.png`、`activity_play.png`、`deployment_diagram.png` | 第 6.5 节阶段图片成果 |
| 阶段 4：细化迭代 3 / 构造 | AI 算法模块、状态建模、UI 牌桌阶段控制、玩家成长系统完善 | `state_game.png` | 第 7.6 节阶段图片成果 |
| 阶段 5：交付（Transition） | 测试验收、文档归档、Release 包、GitHub 托管、UML 最终一致性检查 | `processon_uml_collection.png`、`processon_uml_board.png`、全部 7 张 UML 图 | 第 8.2 节阶段图片成果 |

## ProcessOn 使用建议

1. 在 ProcessOn 中创建 UML 图，按上述 7 类图分别命名。
2. 将 `.puml` 的图结构导入 ProcessOn，或参照 `.puml` 中的类名、参与者、状态、组件和消息关系手动绘制。
3. 在 ProcessOn 中调整布局，使图形适合报告展示。
4. 导出 PNG 放回本目录，保持文件名与表格一致。
5. 最终报告中注明“UML 图由 PlantUML 代码导入/参照迁移到 ProcessOn UML 绘制，项目内保留 PlantUML 文本作为版本管理备份”。
6. 如果 ProcessOn 图已手动调整，请以本目录 `.puml` 的类名、状态名和消息名为准做最后核对。
