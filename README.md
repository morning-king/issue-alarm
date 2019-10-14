# 报警系统

## 使用手册

在业务系统下需要配置一个文件名为`alarm.yml`的配置文件，里面必须包含 appenders，filters 属性

**appender属性**：主要处理报警信息的方式，必须继承Appender接口
    
    工程内包含：
        * EmailAlarmAppender: 发送邮件

**Filter属性**：主要是过滤报警信息，必须继承Filter接口

    工程内包含：
        * FilterByMsg: 使用报错的message做模糊匹配过滤
 
 **自定义的Appender | Filter**: 配置文件中必须包含path包路径
         
例：
```yaml
appenders:
  EmailAlarmAppender:
    recipientCcs: '243717042@qq.com'
    recipientTo: '814694804@qq.com'
    subject: 'order-api邮件报警'
    username: 'someone@osp.com'
    password: 'egP8~@Q9'
    bottom: 'order-api'
    env: dev
filters:
  FilterByMsg:
    rule: '.*Broken pipe.*、.*getWriter\\\\(\\\\)\\\\s+has.*'

```

## dubbo工程引入时需要增加的配置
> 1、在resources目录下新建`META-INF`目录，在`META-INF`目录下新建`dubbo`目录，在dubbo目录下新建
>   `com.alibaba.dubbo.rpc.Filter`文本文件，在该文件中配置`exceptionFilter=com.osp.issue.dubbo.AlarmExceptionFilter`  
> 2、在dubbo-service.xml配置文件中增加 `<dubbo:provider filter="exceptionFilter"/>`配置




