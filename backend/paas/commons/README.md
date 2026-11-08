# UniMall Commons Library

## 简介

Commons 是 UniMall 项目的通用工具库，包含基础工具类、DTO、VO 等核心功能。该库为 UniMall 生态系统中的所有微服务提供统一的基础功能支持。

## 功能特性

- **通用工具类**：提供常用的工具方法，如日期处理、字符串处理、加密解密等
- **数据传输对象**：包含各种业务 DTO 和 VO 类
- **响应封装**：统一的 API 响应格式封装
- **异常处理**：统一的异常处理机制
- **Spring Boot 集成**：与 Spring Boot 生态完美集成
- **Spring Cloud 支持**：支持 Nacos、Sentinel、Seata 等 Spring Cloud 组件

## 版本信息

- **当前版本**：1.0.0
- **Java 版本**：21
- **Spring Boot 版本**：3.2.4
- **Spring Cloud 版本**：2023.0.1

## 快速开始

### 1. 添加依赖

在您的 `pom.xml` 文件中添加以下依赖：

```xml
<dependency>
    <groupId>com.houjiang</groupId>
    <artifactId>commons</artifactId>
    <version>1.0.0</version>
</dependency>
```

### 2. 使用示例

#### 响应结果封装

```java
import com.houjiang.commons.util.ResponseResult;

@RestController
public class UserController {
    
    @GetMapping("/users")
    public ResponseResult<List<User>> getUsers() {
        List<User> users = userService.findAll();
        return ResponseResult.success(users);
    }
    
    @PostMapping("/users")
    public ResponseResult<User> createUser(@RequestBody User user) {
        User savedUser = userService.save(user);
        return ResponseResult.success(savedUser);
    }
}
```

#### 工具类使用

```java
import com.houjiang.commons.util.DateUtil;
import com.houjiang.commons.util.StringUtil;

public class ExampleService {
    
    public void processData() {
        // 日期处理
        String dateStr = DateUtil.format(new Date(), "yyyy-MM-dd HH:mm:ss");
        
        // 字符串处理
        boolean isEmpty = StringUtil.isEmpty("test");
    }
}
```

## 核心组件

### 工具类 (util)

- `ResponseResult`：统一响应结果封装
- `DateUtil`：日期时间处理工具
- `StringUtil`：字符串处理工具
- `RedisUtil`：Redis 操作工具
- `JsonUtil`：JSON 处理工具

### 数据传输对象 (dto/vo)

- `UserInfoVO`：用户信息传输对象
- `OrderDTO`：订单数据传输对象
- `ProductVO`：商品信息传输对象

### 异常处理 (exception)

- `BusinessException`：业务异常
- `SystemException`：系统异常
- `GlobalExceptionHandler`：全局异常处理器

## 构建和安装

### 本地安装

```bash
# 编译并安装到本地 Maven 仓库
mvn clean install

# 跳过测试安装
mvn clean install -DskipTests
```

### 生成源码和文档

```bash
# 生成源码 JAR
mvn source:jar

# 生成 Javadoc
mvn javadoc:jar

# 生成所有构件（包括源码和文档）
mvn clean package
```

## 测试

```bash
# 运行所有测试
mvn test

# 运行特定测试类
mvn test -Dtest=ResponseResultTest

# 生成测试报告
mvn surefire-report:report
```

## 贡献指南

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

## 许可证

本项目采用 Apache License 2.0 - 查看仓库根目录 [LICENSE](../../../../LICENSE) 文件了解详情。

## 联系方式

- **项目主页**：https://github.com/houcloud/commons
- **问题反馈**：https://github.com/houcloud/commons/issues
- **邮箱**：support@houcloud.com

## 更新日志

### 1.0.0 (2025-09-05)

- 初始版本发布
- 提供基础工具类和 DTO/VO
- 集成 Spring Boot 和 Spring Cloud 组件
- 支持统一响应格式和异常处理
