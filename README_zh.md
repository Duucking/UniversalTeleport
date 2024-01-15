# UniversalTeleport
***为多设备之间提供一个“传送门”***
## 当前具有的功能：
1. 设备发现
2. 剪切板共享
3. 文件共享
4. 数据加密
## 准备阶段:
1. 安装Python环境。[下载链接](https://www.python.org/downloads/)
2. 安装依赖项，运行脚本"installRequirements.bat"。
## 如何使用:
### 快速启动APP.

|安卓客户端|Python客户端|
|:----:|:----:|
|添加这个应用的快捷开关到控制中心<br>![tutieshi_320x711_13s](https://github.com/Duucking/UniversalTeleport/assets/68683037/d00f91f9-0322-4b49-b8e0-f823dbe2bebe)|双击"UniversalTeleport.pyw"或者"startAPP.bat"文件|


### 设置密钥和设备名

*Python客户端需要先在config.ini中设置一个目标IP地址才能发送文件(还没有整设备发现)*

|安卓客户端|Python客户端|
|:----:|:----:|
|![tutieshi_320x711_20s](https://github.com/Duucking/UniversalTeleport/assets/68683037/4cf0c10e-5566-4494-a10e-d57c3d43cb3b)|![tutieshi_320x166_18s](https://github.com/Duucking/UniversalTeleport/assets/68683037/77571cab-f0b6-4b1d-9c34-be8e03b6b939)|

### 分享文本到其他设备

*文本会发送到其他设备的剪切板(目标设备必须后台运行着这个应用)*

|安卓客户端|Python客户端|
|:----:|:----:|
|![tutieshi_320x711_10s](https://github.com/Duucking/UniversalTeleport/assets/68683037/a6f3f7fa-1cab-423f-b1d1-0984ae6f238a)|直接复制就行 ~|

### 分享文件到其他设备

*文件会保存的用户的默认系统下载目录下的UniversalTeleport文件夹中(目标设备必须后台运行着这个应用)*

|安卓客户端|Python客户端|
|:----:|:----:|
|![tutieshi_320x711_10s-](https://github.com/Duucking/UniversalTeleport/assets/68683037/e06c9d5a-eee4-425a-90a1-242e9fce462f)|步骤1 为"sendFile(Put file here).bat"创建一个快捷方式到桌面<br>步骤2 拖动文件到这个快捷方式上打开<br>![tutieshi_320x416_5s](https://github.com/Duucking/UniversalTeleport/assets/68683037/ebb8d2e6-c87a-4161-8186-e91bb3d0d9b3)|

## 注意:
1. 你需要给安卓客户端设置一些权限(大部分设备不用设置，点开即用)。
2. 如果App不能运行闪退啥的，重启可能会恢复正常，然后提交个issue我看看(重启解决99%的问题)。
