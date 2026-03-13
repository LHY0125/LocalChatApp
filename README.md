# LocalChatApp (本地网络聊天室)

一个基于 **JavaFX** 和 Socket 的本地局域网聊天应用。

## 简介

本项目是一个 C/S 架构的即时通讯软件，支持用户注册、登录、群组聊天、群组管理等功能。
项目采用 Java 语言开发，使用 **JavaFX** 作为 GUI 框架，底层通过 TCP Socket 进行全双工通信。

## 软件架构

项目主要分为三个核心包：`client`（客户端）、`server`（服务端）和 `global`/`util`（公共组件）。

### 目录结构说明

- **客户端 (Client)**
  - 入口文件: [`src/client/Client.java`](src/client/Client.java) (继承自 `Application`)
  - 业务逻辑: [`src/client/service/`](src/client/service/)
    - `ChatReceiver.java`: 负责接收服务端消息，更新本地 Observable 数据
    - `ChatSender.java`: 负责发送消息给服务端
    - `LocalData.java`: 客户端本地数据缓存，使用 `ObservableList/Map` 实现数据绑定
  - 界面展示: [`src/client/view/`](src/client/view/)
    - `LoginPage.java`: 登录界面控制器
    - `MainPage.java`: 主界面控制器
    - `login/`: 登录界面 FXML 资源
    - `main/`: 主界面 FXML 资源
    - `css/`: 界面样式表

- **服务端 (Server)**
  - 入口文件: [`src/server/Server.java`](src/server/Server.java)
  - 业务服务: [`src/server/serveice/`](src/server/serveice/)
    - `ClientChatThread.java`: 处理单个客户端的连接线程
    - `Wrapper.java`: 通信协议数据包定义
  - 数据模型: [`src/server/data/`](src/server/data/)

- **公共组件**
  - 全局常量: [`src/global/Global.java`](src/global/Global.java)
  - 工具类: [`src/util/`](src/util/)
    - `MsgUtil.java`: 消息处理工具
    - `SocketUtil.java`: Socket 相关工具
    - `FileUtil.java`: 文件操作工具

## 核心功能

1. **用户系统**
   - 登录与注册：支持新用户注册和账号登录验证
   - 个人信息维护：支持修改头像、签名、生日等个人信息
2. **即时通讯**
   - **群组聊天**：支持创建群组、加入群组、群内实时消息广播
   - **私聊功能**：支持好友间点对点私聊，消息实时推送
   - **数据绑定**：UI 与数据层解耦，通过 Observable 自动更新界面
3. **好友与关系链**
   - **好友管理**：支持查找用户、发送好友请求、同意/拒绝好友申请
   - **状态同步**：实时感知好友在线/离线状态
4. **数据持久化**
   - 服务端自动保存群组信息、用户信息及聊天记录
   - 客户端本地缓存最近的聊天数据和用户配置

## 开发环境

- **JDK**: Java 11+ (推荐使用 JDK 17)
- **Build Tool**: Maven
- **UI Framework**: JavaFX
- **Dependencies**: 通过 Maven 管理 (在 `pom.xml` 中定义)

## 如何运行

1. **环境准备**
   - 确保已安装 JDK 11+ (推荐 JDK 17)。
   - 确保项目根目录下存在 `lib` 文件夹，并包含所有依赖 jar 包（如 JavaFX 等）。

2. **启动服务端**

   使用提供的脚本（Windows）：

   ```cmd
   run_server.bat
   ```

   或者手动运行：

   ```cmd
   java -Djava.net.preferIPv4Stack=true -cp "target\classes;lib\*" server.Server
   ```

3. **启动客户端**

   使用提供的脚本（Windows）：

   ```cmd
   run_client.bat
   ```

   或者手动运行：

   ```cmd
   java -Djava.net.preferIPv4Stack=true -cp "target\classes;lib\*" client.App
   ```

## 贡献指南

1. Fork 本仓库
2. 新建 `feature/xxx` 分支
3. 提交代码
4. 新建 Pull Request
