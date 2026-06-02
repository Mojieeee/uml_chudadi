# 测试安装包、Release 包与验收记录

## Debug 测试包

1. 在 Android Studio 中打开项目并完成 Gradle Sync。
2. 连接模拟器或真机，确认设备栏能看到目标设备。
3. 执行 `Build > Build Bundle(s) / APK(s) > Build APK(s)`。
4. 生成的测试包位于 `app/build/outputs/apk/debug/app-debug.apk`。
5. 发给同学测试前，先在自己的设备上覆盖安装一次，确认大厅、人机对局、规则设置、蓝牙房间、牌桌和结算页都能进入。

## Release 测试包

当前项目已配置 release 签名和一键运行任务。

| 项目 | 当前值 |
| --- | --- |
| 包名 | `com.example.uml_chudadi` |
| 版本 | `versionName=1.3`，`versionCode=4` |
| Release APK | `release/chudadi-v1.3-release.apk` |
| Release AAB | `release/chudadi-v1.3-release.aab` |
| 签名证书 | `keystore/chudadi-release.jks` |
| 签名配置 | `keystore.properties` |
| Android Studio 一键配置 | `.idea/runConfigurations/runRelease.xml` |

常用命令：

```bash
./gradlew :app:assembleRelease --no-daemon
./gradlew :app:bundleRelease --no-daemon
./gradlew :app:runRelease
```

`runRelease` 会执行：构建 release APK、安装到当前连接设备、启动 App。若设备上已有 debug 版，由于签名不同不能覆盖，任务会自动卸载旧包再安装 release；这会清空该 App 的本地数据。

给同学测试优先发送 `release/chudadi-v1.3-release.apk`。应用商店或长期分发保留 `release/chudadi-v1.3-release.aab`。

## 自动化检查

| 检查项 | 命令/位置 | 目标 |
| --- | --- | --- |
| 单元测试 | `./gradlew test --no-daemon` | 规则、AI、消息、房间、动画 key 全通过 |
| Debug 打包 | `./gradlew :app:assembleDebug --no-daemon` | 生成可安装 APK |
| Release 打包 | `./gradlew :app:assembleRelease --no-daemon` | 生成已签名 APK |
| Release Bundle | `./gradlew :app:bundleRelease --no-daemon` | 生成应用商店 AAB |
| Release 签名 | `apksigner verify --verbose --print-certs app/build/outputs/apk/release/app-release.apk` | APK 通过 v2 签名校验 |
| UML 语法 | PlantUML `--check-syntax docs/uml/*.puml` | 7 张图无语法错误 |
| 玩家文案 | `rg` 检查 `app/src/main` | App 内不出现课程/工程说明词 |

## 真机蓝牙验收表

| 场景 | 预期结果 | 记录 |
| --- | --- | --- |
| 房主创建房间 | 显示四个座位，1 号位为房主 | 待截图 |
| 加入者搜索房主 | 展示已配对/附近设备并可连接 | 待截图 |
| 三人同时加入 | 座位不重复，重名自动改名 | 待复测 |
| 房主添加人机 | 空位变为人机，可切换简单/普通/困难 | 待截图 |
| 未满四座开始 | 开始按钮禁用或提示缺少座位 | 待截图 |
| 满四座开局 | 房主广播 START，所有端进入同一牌局 | 待复测 |
| 客户端出牌 | 客户端等待确认，房主广播 MOVE_ACCEPTED/STATE_SNAPSHOT | 待复测 |
| 结算再来一局 | 好友局回原房间，不需要重新加入 | 待复测 |
| 连接失败 | 显示可恢复提示，可重新搜索或返回 | 待截图 |

## Release 发布流程

1. 更新 `app/build.gradle.kts` 中的 `versionCode` 和 `versionName`。
2. 执行 `./gradlew test --no-daemon`。
3. 执行 `./gradlew :app:assembleRelease --no-daemon` 和 `./gradlew :app:bundleRelease --no-daemon`。
4. 使用 `apksigner verify` 验证 APK 签名。
5. 复制 APK/AAB 到 `release/` 目录并按版本命名。
6. 至少完成一轮真机安装、启动、玩家中心、人机对局、蓝牙房间入口烟测。
7. 多机环境可用时，补做创建房间、加入房间、添加人机、满座准备、开局、出牌同步、断线提示验收。

## 回归重点

- 三档人机难度都能开局并自动出牌。
- 已保存规则会影响之后的人机和蓝牙对局。
- 蓝牙房主是权威端：房主校验出牌并广播同步快照。
- 结束后战绩、金币和胜率会保存到本机。
- 玩家界面保持发布版，不展示课程交付文字。
- Release 与 debug 使用不同签名；从 debug 切到 release 时需要卸载旧包或使用 `runRelease` 自动处理。
