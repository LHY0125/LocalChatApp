# LocalChatApp (本地网络聊天室)

一个基于 Java Swing 和 Socket 的本地局域网聊天应用。

## 简介

本项目是一个 C/S 架构的即时通讯软件，支持用户注册、登录、群组聊天、群组管理等功能。
项目采用 Java 语言开发，使用 Swing 作为 GUI 框架，底层通过 TCP Socket 进行全双工通信。

## 软件架构

项目主要分为三个核心包：`client`（客户端）、`server`（服务端）和 `global`/`util`（公共组件）。

### 目录结构说明

- **客户端 (Client)**
    - 入口文件: [`src/client/Client.java`](src/client/Client.java)
    - 业务逻辑: [`src/client/service/`](src/client/service/)
        - `ChatReceiver.java`: 负责接收服务端消息，驱动 UI 更新
        - `ChatSender.java`: 负责发送消息给服务端
        - `LocalData.java`: 客户端本地数据缓存
    - 界面展示: [`src/client/view/`](src/client/view/)
        - `LoginPage.java`: 登录/注册主容器
        - `MainPage.java`: 聊天主界面容器
        - `login/`: 登录与注册的具体视图组件
        - `main/`: 聊天窗口、群组列表等具体视图组件

- **服务端 (Server)**
    - 入口文件: [`src/server/Server.java`](src/server/Server.java)
    - 业务服务: [`src/server/serveice/`](src/server/serveice/)
        - `ClientChatThread.java`: 处理单个客户端的连接线程
        - `Wrapper.java`: 通信协议数据包定义
    - 数据模型: [`src/server/data/`](src/server/data/)

- **公共组件**
    - 全局常量: [`src/global/global.java`](src/global/global.java)
    - 工具类: [`src/util/`](src/util/)
        - `MsgUtil.java`: 消息处理工具
        - `SocketUtil.java`: Socket 相关工具
        - `FileUtil.java`: 文件操作工具

## 核心功能

1.  **用户系统**
    - 登录与注册：支持新用户注册和账号登录验证
    - 个人信息维护：支持修改头像、签名、生日等个人信息
2.  **即时通讯**
    - **群组聊天**：支持创建群组、加入群组、群内实时消息广播
    - **私聊功能**：支持好友间点对点私聊，消息实时推送
    - **混合消息列表**：统一展示群聊和私聊会话，直观管理所有聊天
3.  **好友与关系链**
    - **好友管理**：支持查找用户、发送好友请求、同意/拒绝好友申请
    - **状态同步**：实时感知好友在线/离线状态
4.  **数据持久化**
    - 服务端自动保存群组信息、用户信息及聊天记录
    - 客户端本地缓存最近的聊天数据和用户配置

## 拓展功能

*注：以下功能为项目进阶特性，已在当前版本中完全实现。*

- **好友系统深度集成**：不仅支持基础的增删好友，还实现了好友状态的实时同步和私聊消息的独立存储。
- **动态 UI 交互**：
    - 登录/注册面板平滑切换
    - 消息列表根据消息类型（群聊/私聊）自动展示不同图标和状态
    - 聊天窗口自适应调整，支持显示历史消息记录

## UI 组件介绍

本项目存在动态替换 UI 组件的设计模式。例如在登录界面，当用户点击“注册”按钮时，登录面板（SignIn）会被替换为注册面板（SignUp）。

- **登录页面 (`LoginPage`)**
    - 包含 `SignInView`: 登录表单组件
    - 包含 `SignUpView`: 注册表单组件
    - *替换逻辑*: 通过 `CardLayout` 或移除/添加组件的方式实现面板切换。

- **主界面 (`MainPage`)**
    - **左侧导航 (`SideOptionView`)**: 功能菜单与群组列表
    - **中间内容 (`ContentView`)**: 聊天窗口与消息展示
    - **二级选项 (`SecondaryOptionView`)**: 辅助功能入口。*（更新：顶部标题栏添加了明显的上下分割线，增强了与聊天列表及窗口标题的视觉层次感）*

- **UI 工具包 (`util`)**
    - 包含自定义 UI 元素（如 `BlueOutlineWhiteBtn`、`CircleCharIcon2`）
    - `DesignToken`: 统一定义颜色、字体等设计规范，确保 UI 风格一致性。

## 项目打包

本项目支持生成包含客户端和服务端的独立安装包。

1.  **编译打包**:
    使用 Maven 进行编译，`pom.xml` 已配置 `maven-resources-plugin` 自动复制依赖到 `target/lib`。
    ```bash
    mvn clean package
    ```

2.  **生成客户端和服务端映像**:
    使用 `jpackage` 生成包含 JRE 的独立可执行程序。
    
    *   **生成客户端**:
        ```bash
        jpackage --type app-image --input target --main-jar LocalChatRoom-1.0-SNAPSHOT.jar --main-class client.Client --dest dist --name LocalChatClient --java-options "-Dfile.encoding=UTF-8"
        ```

    *   **生成服务端**:
        ```bash
        jpackage --type app-image --input target --main-jar LocalChatRoom-1.0-SNAPSHOT.jar --main-class server.Server --dest dist --name LocalChatServer --java-options "-Dfile.encoding=UTF-8"
        ```

3.  **生成安装包**:
    使用 Inno Setup 编译 `dist/setup.iss` 脚本，最终生成 `dist/Output/LocalChatApp_Setup.exe`。
    安装包将同时包含客户端和服务端程序。


## 开发环境

- **JDK**: Java 11 (推荐) / Java 8
- **Build Tool**: Maven
- **UI Framework**: Java Swing / JavaFX
- **Dependencies**: 项目已内置所需依赖库（位于 `lib/` 目录），无需额外配置 Maven 仓库即可构建。

## 如何运行

1.  **启动服务端**
    运行 [`src/server/Server.java`](src/server/Server.java) 中的 `main` 方法。

2.  **启动客户端**
    运行 [`src/client/Client.java`](src/client/Client.java) 中的 `main` 方法。
    *注意：请确保服务端已启动，否则客户端无法连接。*

## 贡献指南

1.  Fork 本仓库
2.  新建 `feature/xxx` 分支
3.  提交代码
4.  新建 Pull Request
