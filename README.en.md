# LocalChatApp

The second small project of the "Brick and Tile" team: A local LAN chat application based on Java Swing and Socket.

## Introduction

This project is a C/S architecture instant messaging software that supports user registration, login, group chat, group management, and other functions.
The project is developed in Java, using Swing as the GUI framework, and the underlying layer communicates in full duplex via TCP Socket.

## Software Architecture

The project is mainly divided into three core packages: `client`, `server`, and `global`/`util` (common components).

### Directory Structure

- **Client**
    - Entry file: [`src/client/Client.java`](src/client/Client.java)
    - Business Logic: [`src/client/service/`](src/client/service/)
        - `ChatReceiver.java`: Responsible for receiving server messages and driving UI updates
        - `ChatSender.java`: Responsible for sending messages to the server
        - `LocalData.java`: Client local data cache
    - Interface Display: [`src/client/view/`](src/client/view/)
        - `LoginPage.java`: Login/Registration main container
        - `MainPage.java`: Chat main interface container
        - `login/`: Specific view components for login and registration
        - `main/`: Specific view components such as chat windows and group lists

- **Server**
    - Entry file: [`src/server/Server.java`](src/server/Server.java)
    - Business Service: [`src/server/serveice/`](src/server/serveice/)
        - `ClientChatThread.java`: Connection thread handling a single client
        - `Wrapper.java`: Communication protocol data packet definition
    - Data Model: [`src/server/data/`](src/server/data/)

- **Common Components**
    - Global Constants: [`src/global/global.java`](src/global/global.java)
    - Utilities: [`src/util/`](src/util/)
        - `MsgUtil.java`: Message processing utility
        - `SocketUtil.java`: Socket related utility
        - `FileUtil.java`: File operation utility

## Core Features

1.  **User System**
    - Login and Registration
    - Personal Information Maintenance
2.  **Instant Messaging**
    - Real-time Group Chat
    - Message Receiving and Sending
3.  **Group Management**
    - Group List Display
    - Group Information Update and Maintenance

## UI Component Introduction

This project adopts a design pattern of dynamically replacing UI components. For example, on the login interface, when the user clicks the "Register" button, the login panel (SignIn) will be replaced by the registration panel (SignUp).

- **Login Page (`LoginPage`)**
    - Contains `SignInView`: Login form component
    - Contains `SignUpView`: Registration form component
    - *Replacement Logic*: Implement panel switching through `CardLayout` or removing/adding components.

- **Main Interface (`MainPage`)**
    - **Left Navigation (`SideOptionView`)**: Function menu and group list
    - **Middle Content (`ContentView`)**: Chat window and message display
    - **Secondary Options (`SecondaryOptionView`)**: Auxiliary function entry. *(Update: Added clear top and bottom dividers to the top title bar to enhance the visual hierarchy with the chat list and window title)*

- **UI Toolkit (`util`)**
    - Contains custom UI elements (such as `BlueOutlineWhiteBtn`, `CircleCharIcon2`)
    - `DesignToken`: Uniformly defines design specifications such as colors and fonts to ensure UI style consistency.

## Project Packaging

This project supports generating independent client installation packages.

1.  **Compile and Package**:
    Use Maven to compile. `pom.xml` is configured with `maven-resources-plugin` to automatically copy dependencies to `target/lib`.
    ```bash
    mvn clean package
    ```

2.  **Generate Client Image**:
    Use `jpackage` to generate an independent client containing JRE.
    ```bash
    jpackage --type app-image --input target --main-jar LocalChatRoom-1.0-SNAPSHOT.jar --main-class client.Client --dest dist --name LocalChatClient --java-options "-Dfile.encoding=UTF-8"
    ```

3.  **Generate Installer**:
    Use Inno Setup to compile the `dist/setup.iss` script, finally generating `dist/Output/LocalChatApp_Setup.exe`.

## Extended Features

This project reserves relevant interfaces and logic for the friend system. Although the current version mainly focuses on group chat, the following functions can be expanded in the future:

- **Friend Management**: Add and delete friends
- **Private Chat Function**: Establish point-to-point private chat sessions
- **Status Synchronization**: Friend online/offline status display

*Note: At this stage, these functions are optional items and are not forcibly required to be implemented.*

## Development Environment

- **JDK**: Java 11 (Recommended) / Java 8
- **Build Tool**: Maven
- **UI Framework**: Java Swing / JavaFX
- **Dependencies**: The project has built-in required dependency libraries (located in the `lib/` directory), and can be built without additional Maven repository configuration.

## How to Run

1.  **Start Server**
    Run the `main` method in [`src/server/Server.java`](src/server/Server.java).

2.  **Start Client**
    Run the `main` method in [`src/client/Client.java`](src/client/Client.java).
    *Note: Please ensure the server is started, otherwise the client cannot connect.*

## Contribution Guide

1.  Fork this repository
2.  Create `feature/xxx` branch
3.  Commit your code
4.  Create Pull Request
