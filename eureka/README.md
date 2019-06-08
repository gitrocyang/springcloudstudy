# Spring Cloud 2.0（一）：Eureka
Eureka：基于REST的服务，主要负责实现微服务架构中的服务治理功能（管理所有服务的信息与状态）
以下代码基于：

```xml
<properties>
	<java.version>1.8</java.version>
	<spring-cloud.version>Greenwich.SR1</spring-cloud.version>
</properties>
```

### 搭建Eurek

#### 1. Server：注册中心服务
依赖如下：
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
</dependency>
```

Application如下:
```java
@EnableEurekaServer
@SpringBootApplication
public class EurekaStartApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaStartApplication.class, args);
    }

}
```
比平常的Spring Boot多了`@EnableEurekaServer`

application.yml
```yml
spring:
  application:
    name: eureka-start-server
server:
  port: 8000
eureka:
  instance:
    hostname: localhost
```

如在页面上显示
>**EMERGENCY! EUREKA MAY BE INCORRECTLY CLAIMING INSTANCES ARE UP WHEN THEY'RE NOT. RENEWALS ARE LESSER THAN THRESHOLD AND HENCE THE INSTANCES ARE NOT BEING EXPIRED JUST TO BE SAFE.**

表示安全模式启动

关闭安全模式：
```yml
eureka:
  server:
    enable-self-preservation: false
```

#### 2. Client: 服务提供者

```xml
<dependency>
      <groupId>org.springframework.cloud</groupId>
      <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
<dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```
没有服务，是不会注册成功的（至少有一个controller）

Application
```java
@EnableDiscoveryClient
@SpringBootApplication
public class EurekaClientStartApplication {
    public static void main(String[] args) {
        SpringApplication.run(EurekaClientStartApplication.class, args);
    }
}
```
关键在：`@EnableDiscoveryClient`
也可使用`@EnableEurekaClient`(只能用于Eureka服务)

配置文件：
```yml   
spring:
  application:
    name: eureka-client-provider
server:
  port: 8001

eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8000/eureka/ #定义注册中心
  instance:
    status-page-url-path: ${management.endpoints.web.base-path}/info #定义健康检查的页面地址
    health-check-url-path: ${management.endpoints.web.base-path}/health #定义健康检查的页面地址
management:
  server:
    port: 9001
  endpoints:
    web:
      base-path: /status #变更默认的actuator页面目录
```
实际生产中需要注意actuator的暴露的断点

#### 3. Client: 消费者

依赖：同服务提供者
Application同服务提供者

配置文件(基本相同)：
```yml
spring:
  application:
    name: eureka-client-consumer
server:
  port: 10001

eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8000/eureka/ #定义注册中心
  instance:
    status-page-url-path: ${management.endpoints.web.base-path}/info #定义健康检查的页面地址
    health-check-url-path: ${management.endpoints.web.base-path}/health #定义健康检查的页面地址
management:
  server:
    port: 11001 #actuator的端口
  endpoints:
    web:
      base-path: /status #定义actuator的路径
```

需要configuration
configuration:
```java
@Configuration
public class BeanConfiguration {
    @Bean
    @LoadBalanced
    public RestTemplate getRestTemplate(){
        return new RestTemplate();
    }
}
```
controller中，访问服务时，可用服务提供者注册的Application.name来访问
如下所示：
```java
@RestController
@RequestMapping("/index")
public class IndexController {

    @Autowired
    private RestTemplate restTemplate;

    @RequestMapping("/provider")
    public JSONObject callProvider(){
        //直接访问提供者，绕开了eureka服务中心
        /*return restTemplate.getForObject(
                "http://localhost:8001/index/name",
                JSONObject.class
        );*/
        //通过eureka服务中心访问微服务
        return restTemplate.getForObject(
                "http://eureka-client-provider/index/name",
                JSONObject.class
        );
    }

}
```

至此，一套简单的Eureka搭建完成。

继续，需要添加更多的特性。

### 开启安全验证
##### Server 改造
Server项目增加依赖：
```xml
<dependency>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-security</artifactId>
</dependency>
```
application.yml变更如下：
```yml
spring:
  application:
    name: eureka-start-server
  security:
    user:
      name: rocyang
      password: 12378456
server:
  port: 8000
eureka:
  instance:
    hostname: localhost
    #prefer-ip-address: true
  client:
    register-with-eureka: false #本实例为中心，不向注册中心注册自己
    fetch-registry: false #本实例不做检索服务
    service-url:
      defaultZone: http://${spring.security.user.name}:${spring.security.user.password}@${eureka.instance.hostname}:${server.port}/eureka/
  server:
    enable-self-preservation: true #关闭自保护
```

需要增加安全配置
```java
@EnableWebSecurity
public class WebSecurityConfigurer extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().ignoringAntMatchers("/eureka/**");//对eureka部分关闭csrf验证
        super.configure(http);
    }
}
```

##### Client改造
提供者与消费者改造相同
application.yml文件中
```yml
eureka:
  client:
    serviceUrl:
      defaultZone: http://localhost:8000/eureka/ #定义注册中心
```
变更为：
```yml
eureka:
  client:
    serviceUrl:
      defaultZone: http://rocyang:12378456@localhost:8000/eureka/ #定义注册中心
```

### 构建高可用性
假设有3台服务器：

* 192.168.2.204
* 192.168.2.205
* 192.168.2.206
我们将其作为Eureka的注册中心服务器。

##### 1. Server改造
新增：application-master.yml
```yml
server:
  port: 8000

eureka:
  client:
    service-url:
      defaultZone: http://${spring.security.user.name}:${spring.security.user.password}@${192.168.2.205}:${8000}/eureka/,http://${spring.security.user.name}:${spring.security.user.password}@${192.168.2.206}:${8000}/eureka/
```
将其他注册中心服务的地址填写进去，多个地址用英文逗号分隔。
再建一个application-slave1.yml
```yml
server:
  port: 8000

eureka:
  client:
    service-url:
      defaultZone: http://${spring.security.user.name}:${spring.security.user.password}@192.168.2.204:8000/eureka/,http://${spring.security.user.name}:${spring.security.user.password}@192.168.2.206:8000/eureka/
```
再建一个application-slave2.yml
```yml
server:
  port: 8000

eureka:
  client:
    service-url:
      defaultZone: http://${spring.security.user.name}:${spring.security.user.password}@192.168.2.204:8000/eureka/,http://${spring.security.user.name}:${spring.security.user.password}@192.168.2.205:8000/eureka/
```
即各个服务器均向其他服务器进行注册。
application.yml变更如下：
```yml
spring:
  profiles:
    active: master #定义生效的配置文件
  application:
    name: eureka-start-server
  security:
    user:
      name: rocyang
      password: 12378456

eureka:
  instance:
    hostname: localhost
  client:
    register-with-eureka: false #本实例为中心，不向注册中心注册自己
    fetch-registry: false #本实例不做检索服务
  server:
    enable-self-preservation: true #关闭自保护
```
可以在启动jar的时候加上--spring.profiles.active=slave1 或者slave2即可启动其他配置
##### 2. Client改造
提供者与消费者改造相同
application.yml文件中
```yml
eureka:
  client:
    service-url:
      defaultZone: http://rocyang:12378456@192.168.2.204:8000/eureka/,http://rocyang:12378456@192.168.2.205:8000/eureka/,http://rocyang:12378456@192.168.2.204:8000/eureka/
```
配置上所有的注册中心服务即可。
### 自定义Eureka的Instance ID
在Server的配置文件中增加以下内容
```yml
eureka:
  instance:
    instance-id: ${spring.application.name}:${spring.cloud.client.ip-address}:${spring.application.instance_id:${server.port}}
    prefer-ip-address: true
```
Client中增加：
```yml
eureka:
  instance:
    instance-id: ${spring.application.name}:${spring.cloud.client.ip-address}:${spring.application.instance_id:${server.port}}
```
完成。
### 健康检查-心跳
在Client的配置文件中增加：
```yml
eureka:
  client:
    healthcheck:
      enabled: true
```

### 服务上下线监控
##### Server增加代码
```java
@Component
public class EurekaStateChangeListner {

    private Logger logger = LoggerFactory.getLogger(EurekaStateChangeListner.class);

    @EventListener
    public void listen(EurekaInstanceCanceledEvent event){
        logger.info("{} \t {} 服务下线",event.getServerId(),event.getAppName());
    }

    @EventListener
    public void listen(EurekaInstanceRegisteredEvent event){
        InstanceInfo info = event.getInstanceInfo();
        logger.info("{}:{} \t {} 服务上线",info.getIPAddr(),info.getPort(),info.getAppName());
    }

    @EventListener
    public void listen(EurekaInstanceRenewedEvent event){
        logger.info("{} \t {} 服务续约",event.getServerId(),event.getAppName());
    }

    @EventListener
    public void listen(EurekaRegistryAvailableEvent event){
        logger.info("注册中心 启动");
    }

    @EventListener
    public void listen(EurekaServerStartedEvent event){
        logger.info("Eureka Server 启动");
    }
}
```
高可用方案下（集群模式），每个节点均会触发事件，注意控制

### Eureka REST
详见：[
https://github.com/Netflix/eureka/wiki/Eureka-REST-operations](https://github.com/Netflix/eureka/wiki/Eureka-REST-operations)

例子：
前面的服务提供者的应用信息，可通过以下链接查看：
`http://localhost:8000/eureka/apps/eureka-client-provider`

### 自定义元数据
在Client 服务提供者的配置文件中，增加：
```yml
eureka:
  instance:
    metadata-map:
      author: rocyang
```
查看链接：[http://localhost:8000/eureka/apps/eureka-client-provider](http://localhost:8000/eureka/apps/eureka-client-provider`)
也可在消费者Client中编码：
使用EurekaClient或者DiscoveryClient
```java
@Autowired
private EurekaClient eurekaClient;
@Autowired
private DiscoveryClient discoveryClient;

@RequestMapping("/appinfo/eureka")
public Object getAppInfoByEurekaClient(){
	return eurekaClient.getInstancesByVipAddress(App_Name,false);

}

@RequestMapping("/appinfo/discovery")
public Object getAppInfoByDiscoveryClient(){
	return discoveryClient.getInstances(App_Name);
}
```
二取一即可，结果是一样的。