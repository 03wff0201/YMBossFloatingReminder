圆梦三职业 Boss 悬浮提醒 - GitHub 云打包说明

一、这个包是什么？
这是安卓悬浮窗提醒工具的 GitHub 云打包版本。
你不用在电脑上安装 Android Studio，也不用本机配置 Gradle。
只需要把整个项目上传到 GitHub，GitHub Actions 会自动打包 APK。

二、准备工作
你需要：
1. 一个 GitHub 账号
2. 能访问 GitHub 的浏览器
3. 这个项目文件夹：YMBossFloatingReminder_GitHubBuild

三、上传到 GitHub

方法一：网页上传，适合新手

1. 打开 GitHub
2. 点右上角 “+”
3. 选择 “New repository”
4. Repository name 可以填：

   YMBossFloatingReminder

5. 选择 Public 或 Private 都可以
6. 点 “Create repository”
7. 进入新仓库后，点 “uploading an existing file”
8. 把本项目文件夹里的所有内容上传进去

注意：
要上传的是文件夹里面的内容，不是只上传 zip。
仓库根目录里应该能看到：

- settings.gradle
- build.gradle
- app/
- .github/

四、启动云打包

1. 进入你的 GitHub 仓库
2. 点击上方 “Actions”
3. 左侧选择 “Build Android APK”
4. 点击 “Run workflow”
5. 再点绿色的 “Run workflow”
6. 等待几分钟

如果你是第一次使用 GitHub Actions，GitHub 可能会提示启用 Actions，按页面提示启用即可。

五、下载 APK

打包完成后：

1. 打开刚才完成的 workflow 记录
2. 往下找到 “Artifacts”
3. 下载：

   YMBossFloatingReminder-debug-apk

4. 解压后会得到：

   app-debug.apk

这个 app-debug.apk 就可以安装到安卓手机。

六、安装到手机

1. 把 app-debug.apk 发到手机
2. 在手机上打开 APK 安装
3. 如果提示“不允许安装未知应用”，按系统提示开启权限
4. 安装后打开 App
5. 点击“授权悬浮窗权限”
6. 允许“显示在其他应用上层”
7. 回到 App，点击“启动 Boss 悬浮窗”
8. 切回游戏使用

七、注意事项

1. 这个工具只做 Boss 刷新提醒和手动记录。
2. 它不会自动点击游戏。
3. 它不会自动跑图。
4. 它不会自动打怪。
5. 不读取账号密码，不修改游戏数据。

八、如果 GitHub 打包失败

常见原因：
1. 文件没有上传完整；
2. 仓库根目录不是项目根目录；
3. 没有上传 .github/workflows/build-apk.yml；
4. GitHub Actions 没有启用。

正确的仓库根目录应该包含：

settings.gradle
build.gradle
app/
.github/workflows/build-apk.yml

九、后续可以升级

可以继续升级：
1. 手机端添加 Boss 页面；
2. 导入/导出 JSON 配置；
3. 地图快捷记录；
4. 声音提醒；
5. 红怪重点提醒；
6. 手机端备份和恢复。
