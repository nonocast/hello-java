Java and Gradle
===============
初始化
`gradle init`

注: 最新的版本5.6已经支持交互过程，cool

build.gradle
```
task hello {
  doLast {
    println 'hello world'
  }
}
```
运行
`gradle hello`
`gradle -q hello` 

q means quite

## Apply java plugin
```
└── src
    ├── main
    │   ├── java  
    │   │   └── demo
    │   │       └── App.java
    │   └── resources
    └── test      
        ├── java
        │   └── demo
        │       └── AppTest.java
        └── resources
```
这里把demo改为'cn/nonocast', 根据package name在src/main/java下layout,
./app/src/main/java/cn/nonocast/App.java
```
package cn.nonocast;

public class App {
    public static void main(String[] args) {  
        System.out.println("hello world");
    }
}
```

build.gradle
```
plugins {
    id 'java'
    id 'application'
}

repositories {
    jcenter() 
}

dependencies {

}

mainClassName = 'cn.nonocast.App' 
```

gradle run
```
$ gradle run -q
hello world
```

所有的路径gradle都帮你处理了，你可以理解这是标准的SOP。

直接java运行jar, 则需要在build/libs下
`
java -classpath app.jar cn.nonocast.App
`

可以通过gradle做成可执行jar
```
jar {
    manifest {
        attributes 'Main-Class': 'cn.nonocast.App'
    }
}
```

gradle build后就可以愉快的玩耍了,
```
$ java -jar app.jar 
hello world
```

通过command+.可以快速quickfix,import包名，要是一个个打不得疯吗？
如果在gradle中增加依赖，需要右键build.gradle菜单中选择update project configuration (找的我好苦)

剩下的参考:
[Building Java Applications](https://guides.gradle.org/building-java-applications/)
