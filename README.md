# 单词记忆游戏程序设计

## 任务描述
用 Java 编程开发“单词记忆游戏”程序，主要考核字符串处理、IO 操作、网络编程等。

## 功能要求

### （0）基础设置
- 在网上下载词汇表，中英文对应，并保存在服务器端。
- 服务器允许 1 个客户端连入。
- 客户端初始分数为 10 分，以下功能 1、功能 2、功能 3 全做。

### （1）功能 1：根据中文补齐英文
- 客户端界面打开，开始游戏。
- 服务器随机选出一个英文单词的中文单词描述，发给客户端显示。
- 界面出现一个限时 10 秒的计数器。
- 界面底端出现该英文单词的首尾字母，其他字母空着。
- 客户端补齐其他字母，按回车提交。
- **规则**：
  - 10 秒之内提交正确，客户端加 1 分，进入下一局。
  - 提交错误，客户端扣 2 分，进入下一局。
  - 不提交，扣 1 分，进入下一局。
- 进入下一局之前，若回答正确，客户端显示“恭喜回答正确”；回答错误，客户端显示“回答错误，答案是 XXX”；没回答，客户端显示“您没有回答，答案是 XXX”。
- 用户分数扣到 0 分，则游戏输掉，退出。

### （2）功能 2：根据英文选择中文
- 服务器随机选出一个英文单词，发给客户端显示。
- 界面出现一个限时 10 秒的计数器。
- 界面底端出现 ABCD 四个中文选项，其中一个正确。
- 客户端输入选项，提交。
- **规则**：
  - 10 秒之内提交正确，该客户端增加 1 分，进入下一局。
  - 提交错误，该客户端扣 2 分，进入下一局。
  - 不回答，扣 1 分，进入下一局。
- 进入下一局之前，若回答正确，客户端显示“恭喜回答正确”；回答错误，客户端显示“回答错误，答案是 XXX”；没回答，客户端显示“您没有回答，正确答案是 XXX”。
- 用户分数扣到 0 分，则游戏输掉，退出。

### （3）功能 3：错词保存
- 如果一个单词，在功能 1 或 2 中，被用户答对，将其保存在“已掌握单词.txt”中。
- 如果一个单词，在功能 1 或 2 中，答错或没有答，则保存在“未掌握单词.txt”中（标注是答错还是没答）。
- 用户可以打开复习。
- 对于客户端，“已掌握单词.txt”和“未掌握单词.txt”保存在本地。
