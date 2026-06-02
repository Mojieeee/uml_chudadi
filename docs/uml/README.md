# UML 建模说明

本项目正式交付以 ProcessOn UML 图为准。`docs/uml` 中的 `.puml` 文件用于版本管理和快速语法检查，`.png` 文件用于报告引用；最终报告可按同样结构在 ProcessOn 中绘制或导入后导出。

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
2. 参考 `.puml` 中的类名、参与者、状态和组件关系绘制图形。
3. 导出 PNG 放回本目录，保持文件名与表格一致。
4. 最终报告中注明“UML 图使用 ProcessOn 绘制，项目内保留 PlantUML 文本备份用于版本管理”。
