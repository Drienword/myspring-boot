# Springboot + Mybatis

## 准备

先将Spring Bott工程搭建出来

## 起步

### 项目依赖

依赖文件查看仓库：myspring-boot/pom.xml

### 初始化数据库

表的设计 商品和用户

### Springboot整合Mybatis

- 1.Mapper接口的XML配置文件变化。之前我们使用Mybatis接口代理开发，规定Mapper映射文件要和接口在一个目录下；而这里Mapper映射文件在resources/mapper/下，在src/main/java/下的Mapper接口需要用@Mapper，注解标识，不然映射文件与接口无法匹配。

- 2.SpringBoot建议使用YAML作为配置文件，它有更简便的配置方式。所以整合Mybatis在配置文件上有一定的区别，但最终都是那几个参数的配置。

  #### 整合配置文件

  在Spring阶段用XML配置mybatis无非就是配置：1.连接池；2.数据库url链接；3.mysql驱动；4.其他初始化配置

  myspring-boot/resources/application.yml

  ##### 解释

  1. 我们实现的是spring-mybatis的整合，包含mybatis的配置以及datasource数据源的配置是属于spring配置中的一部分，所以需要在spring：下。

  2. mapper-locations相当于XML中的<property name="mapperLocations">用来扫描Mapper曾的配置文件，由于我们的配置文件在resources下，所以需要指定class path：。

  3. type-aliases-package相当于XML中<[rpoperty name="typeAliasesPackase">别名配置，一般取其下实体类类名作为别名。

  4. datasource数据源的配置，name表示数据源的名称，类似于之前的<bean id="dataSource">id属性，这里可以任意指定，因为我们无需关注Spring是怎么注入这个Bean对象的。

  5. druid代表本项目中使用了阿里的druid连接池，driver-class-name：相当于XML中的<property name ="username">;password代表XML中的<property name= "password">; 

     ## 实现查询

     1. 在src/main/java/cn.drien/entity/下新建User.java实体类

        ```java
        public class User implements Serializable {
            private Long id; //编号
            private String username; //用户名
            private String password; //密码
            //getter/setter
        }
        ```

     2. 在src/main/java/cn/drien/service/下创建BaseService.java通用接口，目的是简化service层接口基本FRUD方法的编写

        ```java
        public interface BaseService<T> {
            
            //查询所有
            List<T> findAll();
            
            //根据ID查询
            List<T> findById(Long id);
            
            //添加
            void create(T t);
            
            //删除 （批量）
            void delete(Long... ids);
            
            //修改
            void update(T t);
        }
        ```

        以上就是对Service层基本CRUD接口的简易封装，使用了泛型类，其继承接口指定了什么泛型，T就代表什么类。

     3. 在src/main/java/cn/drien/service/下创建UserService.java接口：

        ```java
        public interface UserService extends BaseService<User> {}
        ```

     4. 在src/main/java/cn/drien/service/impl/下创建UserServiceImpl.java 实体类：

        ```java
        @Servie
        public class UserServiceImpl implements UserService {
            
            @Autowired
            private UserMapper userMapper;
            
            @Override
            public List<User> findAll() {
                return userMapper.findAll();
            }
            
            //其他方法省略
        }
        ```

     5. 在src/main/java/cn/drien/mapper/下创建UserMapper.java Mapper接口类：

        ```java
        @Mapper
        public interface UserMapper {
            List<User> findAll();
        }
        ```

        如上，使用@Mapper接口标识这个接口，不然Mybatis找不到其对应的XML映射文件。

     6. 在src/main/resources/mapper/下创建UserMapper.xml映射文件：

        ```java
        <?xml version="1.0" encoding="UTF-8" ?>
        <!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd" >
        <mapper namespace="cn.drien.mapper.UserMapper">
        
            <!-- 查询所有 -->
            <select id="findAll" resultType="cn.drien.entity.User">
                SELECT * FROM tb_user
            </select>
        </mapper>
        ```

     7. 在src/main/java/cn/drien/controller/admin/下创建UserController.java

        ```java
        @RestController
        public class UserController {
            @Autowired
            private UserService userService;
            
            @RequestMapping("/findAll")
            public List<User> findAll() {
                return userService.findAll();
            }
        }
        ```

     8. 运行src/main/java/cn/drien/SpringbootApplication.java的main方法，启动springboot

        在浏览器上访问localhost:8080/findAll 即可得到一串JSON数据

        ### 思考

        明白了，其实和SSM阶段的CRUD基本相同

        ## 实现页面跳转

因为Thymeleaf指定的目录 src/main/resources/templates/ 是受保护的目录，其下的资源不能直接通过浏览器访问，可以使用Controller映射的方式访问，怎么映射呢？

1. 在application.yml中添加配置

   ```java
   spring:
     thymeleaf:
   	prefix: classpath:/templates/
       check0template-location: true
       suffix: .html
       encoding: UTF-8
       mode: LEGACYHTML5
       cache: false
   ```

   指定Thymeleaf模板引擎扫描resources下的templates文件夹中已 .html结尾的文件。这样就实现了MVC中关于视图解析器的配置：

   ```java
   <!-- 配置视图解析器 -->
   <bean class="org.springframework.web.servlet.view.InternalResourceViewResolver">
       <property name="perfix" value="/">
       <property name="suffix" value=".jsp" />
   </bean>
   ```

   注意的是：classpath: 后的目录地址一定要先加/，比如目前的 classpath:/templates/ 。

2. 在Controller添加映射方法

   ```java
   @GetMapping(value = {"/", "/index"})
   public String index() {
       return "home/index";
   }
   ```

   这样，访问localhost:8080/index将直接跳转到 resources/templates/home/index.html 页面。

   ## 实现分页查询

   首先我们需要在application.yml中配置pageHelper插件

   ```java
   pagehelper:
   	pagehelperDialect: mysql
       resonable: true
       support-methods-arguments: true
   ```

   这里使用了Mybatis的PageHelper分页插件，前端使用了ElementUI自带的分页插件

   ### 核心配置

   UserServiceImp.java

   ```java
   public PageBean findByPage(Goods goods, int pageCode, int pageSize) {
       //使用Mybatis分页插件
       PageHelper.startPage(pageCode, pageSize);
       
       //调用分页查询方法，其实就是查询所有数据，mybatis自动帮我们进行分页计算
       Page<Goods> page = goodsMapper.findByPage(goods);
       
       return new PageBean(page.getTotal(), page.getResult());
   }
   ```

   ## 实现文件上传

   这里涉及的是SpringMVC的文件上传

   前端使用了ElementUI+Vue.JS技术

   除了代码的编写，这里在application.yml中进行配置：

   ```java
   Spring:
   	servlet:
   		multipart:
   		  max-file-size: 10Mb
   		  max-request-size: 100Mb
   ```

   这就相当于SpringMVC的XML配置：

   ```java
   <bean id="multipartResolver" class="org.springframework.web.multipart.commons.CommonsMultipartResolver">
       <property name="maxUploadSize" value="500000"
   </bean>
   ```

   ## 使用Spring AOP切面编程实现简单的登录拦截器

   使用Spring AOP切面编程思想实现简单的登录拦截：

   ```java
   @Component
   @Aspect
   public class MyInterceptor {
       @Pointcut("within (cn.diren.controller..*) && !within(cn.drien.controller.admin,LoginController)")
       public void pointCut() {
           
       }
       @Around("pointCut()")
       public Object trackInfo(ProceedingJoinPoint joinPoint) throws Throwable {
           ServletRequestAttributes attributes = attributes.getRequest();
           User user = (User) request.getSession().getAttribute("user");
           if (user == null) {
               attributes.getResponse().sendRedirect("/login"); //手动转发到/login映射路径
           }
           return joinPoint.proceed();
       }
   }
   ```

   #### 解释

   注意一下几点

   1. 一定要熟悉AspectJ的切点表达式，在这里：..*标识其目录下的所有方法和子目录方法。
   2. 如果进行了登录拦截，即在session中没有获取到用户的登录信息，我们可能需要手动转发到login 页面，这里访问的是 login 隐射。
   3. 基于2，一定要指定Object返回值，若AOP拦截的Controller return了一个视图地址，那么本来Controller应该跳转到这个视图地址的，但是被AOP拦截了，那么原来Controller仍会执行return, 但是视图地址却找不到404了。
   4. 切记一定要调用proceed()方法，proceed(): 执行被通知的方法，如不调用将会组织被通知的方法的调用，也就导致Controller中的return会404.

