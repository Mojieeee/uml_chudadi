# 测试报告

## 自动化测试范围

| 测试文件 | 覆盖内容 |
| --- | --- |
| `HandClassifierTest` | 单张、对子、三张、顺子、同花、葫芦、四带一、同花顺、南北规则差异 |
| `GameControllerTest` | 发牌、首出、过牌重置、AI 合法动作、随机多局完成 |
| `PlayerActionPolicyTest` | 无可压牌提示、按钮可用性、锁定状态 |
| `GameMessageCodecTest` | ROOM、START、MOVE、SNAPSHOT 等消息往返 |
| `RoomSeatTest` | 四座位、添加人机、准备、断线、再开局重置 |
| `NetworkMoveGuardTest` | 当前远端玩家校验、重复/非法请求过滤 |
| `SimulatedBluetoothHubTest` | 三人加入、房满拒绝、房主广播和移动请求 |
| `MusicAssetTest` | 背景音乐资源存在和 0:00-1:33 循环配置 |

## 最近验证命令

- `./gradlew test --no-daemon`
- `./gradlew :app:assembleDebug --no-daemon`
- `./gradlew :app:assembleRelease --no-daemon`
- `./gradlew :app:bundleRelease --no-daemon`
- `apksigner verify --verbose --print-certs app/build/outputs/apk/release/app-release.apk`

以上命令均应在最终提交前重新执行并截图归档。当前 release APK 使用 `keystore/chudadi-release.jks` 签名，输出包见 `release/chudadi-v1.3-release.apk` 和 `release/chudadi-v1.3-release.aab`。

## 手动测试建议

1. 大厅进入人机对局，分别选择简单、普通、困难并完成一局。
2. 在规则设置保存北方玩法，确认黑桃 3 首出；保存南方玩法，确认方块 3 首出。
3. 蓝牙房主创建房间，加入者连接后准备；房主添加人机补满四座并开局。
4. 客户端连续快速点击出牌/不出，确认只接受一次有效行动。
5. 对局结束后点击再来一局，好友局回原房间，人机局直接重新发牌。
6. 拒绝蓝牙权限、关闭蓝牙或连接失败时，确认页面给出恢复提示。

## 遗留风险

- 蓝牙真实多机环境受手机品牌、系统权限、配对状态和距离影响，仍需最终真机复测。
- ProcessOn UML 图片需要在最终报告中单独截图或导出，保证与 `docs/uml` 内容一致。
- Debug 模式动画性能低于 release 模式，正式演示建议使用 release 或至少关闭 Android Studio 调试器。
- `runRelease` 若遇到 debug/release 签名不一致，会卸载旧包后安装 release，因此演示前应确认是否需要保留本地玩家数据。
