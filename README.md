# GomokuUltra

五子棋 Ultra 版，支持多种特殊棋子和策略点玩法。

## 玩法简介
- 普通棋子：每落一子获得1点策略点。
- 障碍棋、冻结棋、炸弹棋、覆盖棋等特殊棋子，详见游戏内规则说明。

## 运行方法
```bash
# 编译
javac -d out src/*.java
# 运行（假设主类为 Main）
java -cp out Main
```

## 依赖环境
- JDK 8 及以上
- Java Swing（标准库）

## 项目结构示例
```
GomokuUltra/
├── src/                # 源代码目录，存放 .java 文件
├── out/                # 编译输出目录，存放 .class 文件（不上传）
├── dist/               # 发布目录，存放 jar 文件
├── assets/             # 资源文件（如图片、音效等，若有）
├── .gitignore
├── README.md
├── LICENSE
```

## 作者
- [你的名字](https://github.com/你的GitHub用户名) 