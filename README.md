# 后端部署流程
原项目地址：https://gitcode.com/open-source-toolkit/623e8/overview?utm_source=highlight_word_gitcode&word=%E5%89%8D%E5%90%8E%E7%AB%AF%E5%88%86%E7%A6%BB&isLogin=1  
参考视频：https://live.csdn.net/v/266171?spm=1001.2014.3001.5501
1. 安装Mysql & redis
    - redis下载地址：https://github.com/tporadowski/redis/releases
2. 将topic.sql导入Mysql
3. 配置好Maven
4. 启动redis
5. 修改application.yml文件，将Mysql的用户名和密码设置为自己的
6. 直接用idea打开back或打开SEProject_back然后导入back模块
7. 运行ZwzApplication.java
8. 127.0.0.1:8081查看是否成功启动