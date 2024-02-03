# MiFitness-Uploader-XPosed
An XPosed plugin for "MiFitness(小米健康)", allowing you to fetch the data using socket.

## WARNING
目前仍为不稳定开发版，不提供相关的 release 及文档。代码结构及调用方法等都有可能出现破坏性变更，仅为参考学习。

思路：[我的博客](https://n.ova.moe/blog/MiBand-8-Pro-Data-to-Obsidian) 或 [XLOG](https://x.ouo.sh/MiBand-8-Pro-Data-to-Obsidianmd)

目前会在手机的 23235 端口下监听 GET 请求，路由 `/getDailyReport`，接受两个参数 `type`：可选值 `STEP` 和 `SLEEP`，`date`：时间的格式化字符，如 `2024-02-02`，返回一个 json，包含状态和数据。

![](docs/1.png)

## Feature Plan
秉持着能用就好的心态，其实不太想继续搓这个项目，但是还是画一些饼

- [ ] 增加更多类型的导出
- [ ] 支持自定义时间间隔范围导出
- [ ] 支持对称性密钥及 HMAC 验证等密码学措施保护安全性
- [ ] 添加插件设置界面，允许自定义端口及密钥
- [ ] 实时心率上传（Websocket）