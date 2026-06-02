# 锄大地 Android

Kotlin + Jetpack Compose 实现的四人锄大地棋牌游戏，支持人机对局、好友蓝牙房间、南北规则、玩家成长系统、动画音效、Release 签名包和完整 UML/文档交付。

## 功能概览

- 人机对局：简单、普通、困难三档 AI。
- 好友蓝牙：创建房间、加入对局、四座位、人机补位、房主权威同步。
- 规则系统：北方规则与南方规则，可保存并应用到后续对局。
- 玩家中心：昵称、头像、金币、等级、经验、战绩、成就、每日奖励。
- 发布包：`release/chudadi-v1.3-release.apk` 和 `release/chudadi-v1.3-release.aab`。
- 文档：需求、设计、测试、发布说明与 UML 图见 `docs/`。

## 常用命令

```bash
./gradlew test --no-daemon
./gradlew :app:assembleDebug --no-daemon
./gradlew :app:assembleRelease --no-daemon
./gradlew :app:runRelease
```

## 入口文档

- 完整项目说明：`docs/complete_project_documentation.md`
- 需求分析：`docs/requirements.md`
- 设计说明：`docs/design.md`
- 发布测试：`docs/release_testing.md`
- UML：`docs/uml/`

## 注意

Release 升级必须使用同一签名证书。仓库不会上传 `keystore.properties` 和 `.jks` 私钥文件，请在本机妥善备份。
