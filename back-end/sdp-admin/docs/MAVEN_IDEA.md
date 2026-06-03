# 使用 IntelliJ IDEA 捆绑 Maven 构建 sdp-admin

本机若未单独安装 Maven，可使用 IDEA 自带的 Maven（Bundled Maven）。下文说明如何在 Windows 上定位 `mvn.cmd`，并在终端或 IDEA 内编译本项目。


## 常用命令

在 **`sdp-admin` 根目录**（含根 `pom.xml`）执行：

```bat
"%MVN%" -v
"%MVN%" -pl sdp-server -am clean compile
"%MVN%" -pl sdp-server -am test
```

说明：`-pl sdp-server -am` 会连带编译 `sdp-server` 所依赖的模块。打包可用：

`"%MVN%" -pl sdp-server -am clean package -DskipTests`

## IDEA 图形界面与系统 Maven

在 IDEA 中可在 **Maven** 工具窗口选择 `sdp-admin` 的 **Lifecycle → clean / compile / test**。若使用捆绑 Maven，一般无需再单独配置 PATH；若使用本机安装的 Maven，在 **Settings → Build, Execution, Deployment → Build Tools → Maven** 中指定 **Maven home** 即可。
