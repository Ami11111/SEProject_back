# 后端部署流程
原项目地址：https://gitee.com/zehongzhyuan/test-l-i-b-r-a-r-y/tree/dev

1. 安装Mysql 
2. 将topic.sql导入Mysql
3. 配置好Maven
4. 启动redis
5. 修改application.yml文件，将Mysql的用户名和密码设置为自己的
6. 直接用idea打开back或打开SEProject_back然后导入back模块
7. 运行ZwzApplication.java
8. 127.0.0.1:8081查看是否成功启动