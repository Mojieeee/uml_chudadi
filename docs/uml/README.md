# UML 建模说明

本项目正式交付以 ProcessOn UML 图为准。开发者先用 PlantUML 代码描述 7 类 UML 图，再将 PlantUML 图内容导入或参照迁移到 ProcessOn UML 中整理为正式图。`docs/uml` 中的 `.puml` 文件用于版本管理和快速语法检查，`.png` 文件用于报告引用和对照。

当前 `.puml` 已按最新代码同步：包含开屏页、玩家中心、人机预备/发牌阶段、三档 AI、蓝牙四座位、房主权威同步、玩家成长系统、release 签名和 GitHub HTTPS 上传注意事项。

## 七大图对应关系

| UML 图 | 文件 | 说明 |
| --- | --- | --- |
| 用例图 | `use_case.puml` / `use_case.png` | 玩家、房主、加入者、人机与核心用例 |
| 类图 | `class_diagram.puml` / `class_diagram.png` | Model、Controller、Transport、View/Audio 类关系 |
| 顺序图 | `sequence_game_turn.puml` / `sequence_game_turn.png` | 本地出牌和蓝牙房主权威同步顺序 |
| 状态图 | `state_game.puml` / `state_game.png` | 大厅、房间、牌桌、暂停、结算状态转换 |
| 活动图 | `activity_play.puml` / `activity_play.png` | 开局、出牌、不出、结算和再开局流程 |
| 组件图 | `component_diagram.puml` / `component_diagram.png` | UI、控制器、模型、消息、蓝牙和系统能力 |
| 部署图 | `deployment_diagram.puml` / `deployment_diagram.png` | 房主手机、加入者手机、蓝牙栈和测试环境 |

## ProcessOn 使用建议

1. 在 ProcessOn 中创建 UML 图，按上述 7 类图分别命名。
2. 将 `.puml` 的图结构导入 ProcessOn，或参照 `.puml` 中的类名、参与者、状态、组件和消息关系手动绘制。
3. 在 ProcessOn 中调整布局，使图形适合报告展示。
4. 导出 PNG 放回本目录，保持文件名与表格一致。
5. 最终报告中注明“UML 图由 PlantUML 代码导入/参照迁移到 ProcessOn UML 绘制，项目内保留 PlantUML 文本作为版本管理备份”。
6. 如果 ProcessOn 图已手动调整，请以本目录 `.puml` 的类名、状态名和消息名为准做最后核对。
