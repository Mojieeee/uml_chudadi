# 锄大地 Release 包

- 测试安装包：`chudadi-v1.3-release.apk`
- 应用商店包：`chudadi-v1.3-release.aab`
- 包名：`com.example.uml_chudadi`
- 版本：`versionName=1.3`，`versionCode=4`
- 签名证书：`keystore/chudadi-release.jks`

## 安装说明

如果手机上已经安装过 Android Studio 运行的 debug 版本，需要先卸载旧版本，再安装 release APK。debug 和 release 使用不同签名，Android 不允许直接覆盖安装。

也可以在 Android Studio 顶部运行配置中选择 `runRelease`，或使用命令：

```bash
./gradlew :app:runRelease
```

该任务会自动构建 release、安装并启动 App；如果遇到 debug/release 签名不一致，会先卸载旧包再安装 release。

## 后续升级

后续版本必须继续使用同一个 `keystore/chudadi-release.jks` 签名，否则已安装 release 版的手机无法直接升级。
