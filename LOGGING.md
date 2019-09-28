LOGGING
=======
抽象接口+实现: slf4j+logback, spring boot默认采用的就是这个套餐，您就别挑了。
logback是slf4j的native implementations,就是亲儿子。

build.gradle

`implementation 'org.slf4j:slf4j-api:2.0.0-alpha0'`


app.java
```
public static void main(String[] args) {
  Logger logger = LoggerFactory.getLogger(App.class);
  logger.info("[1 + 2 = {}]", calc(1, 2));
}
```
logger中可以直接用{}表示place holder，这个很方便。

```
$ gradle run -q
SLF4J: No SLF4J providers were found.
SLF4J: Defaulting to no-operation (NOP) logger implementation
SLF4J: See http://www.slf4j.org/codes.html#noProviders for further details.
```

需要搭配一个最简单的实现，
```
dependencies {
    implementation group: 'org.slf4j', name: 'slf4j-api', version: '2.0.0-alpha0'
    implementation group: 'org.slf4j', name: 'slf4j-simple', version: '2.0.0-alpha0'
}
```

Run
```
$ gradle run -q
[main] INFO cn.nonocast.App - [1 + 2 = 3]
```

导入logback
```
dependencies {
    implementation 'org.slf4j:slf4j-api:2.0.0-alpha0'
    implementation 'ch.qos.logback:logback-classic:1.3.0-alpha4'
}
```
不用改任何client代码, 直接运行就已经采用logback, 然后就是通过配置文件来调校logback,
src/main/resources/logback.xml (取决于sourceSet, 一般都是这个)
```
<configuration>
  <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
    <encoder>
      <pattern>%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
    </encoder>
  </appender>
  <logger name="deng" level="DEBUG" />
  <root level="INFO">
    <appender-ref ref="STDOUT" />
  </root>
</configuration>
```

run again
```
$ gradle run -q
23:57:41.922 [main] INFO  cn.nonocast.App - [1 + 2 = 3]
```

如果在class中申明static logger
```
public class App {
  private static final Logger logger = LoggerFactory.getLogger(App.class);

}
```

差不多就行了。
