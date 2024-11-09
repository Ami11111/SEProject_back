# 后端部署流程

原项目地址：https://gitee.com/zehongzhyuan/test-l-i-b-r-a-r-y/tree/dev

1. 安装Mysql 
2. 将sql数据导入Mysql
3. 配置好Maven
4. 修改application.yml文件，将Mysql的用户名和密码设置为自己的
5. 直接用idea打开back或打开SEProject_back然后导入back模块
6. 运行
7. 127.0.0.1:8080查看是否成功启动

# 项目结构

其中主要负责业务逻辑的包为（根据开发的逻辑顺序列举如下）：

1. entity：定义数据库的表对应的数据类。
2. repository：定义查询接口。
3. controller：从前端请求中解析出数据，调用repository的定义查询接口，得到以entity中数据类封装的数据，处理业务逻辑，返回响应体（即数据）和状态码给前端。
4. 其他：工具类，网络配置、服务等一次性配置类，与具体的业务逻辑无关。

# 开发要点

## 控制器中的方法映射到特定的 URL 路径和请求方法

### 1. **`@RequestMapping` 注解**

`@RequestMapping` 是一个通用的注解，它可以映射 HTTP 请求到控制器类或方法上，支持所有类型的 HTTP 请求方法（GET、POST、PUT、DELETE、PATCH 等）。

#### 用法：

- 可以应用在类级别和方法级别。
- 可以通过 `method` 属性指定特定的 HTTP 请求方法。

### 2. **`@PostMapping` 注解**

`@PostMapping` 是 `@RequestMapping` 的简化版本，专门用于处理 HTTP POST 请求。它是 Spring 5 引入的注解，用于处理 POST 类型的表单提交、文件上传等场景。

#### 用法：

- 只能应用在方法级别。
- 自动将 HTTP 方法设置为 POST，因此不需要显式声明 `method` 属性。
- 其他类似的简化注解：
  - `@GetMapping`：处理 GET 请求。
  - `@PutMapping`：处理 PUT 请求。
  - `@DeleteMapping`：处理 DELETE 请求。

在我们的项目中，主要使用的是@RequestMapping和@PostMapping注解。

## 从请求中解析出数据的方法

### 1. **@RequestParam**

**用途**: 用于解析 URL 请求参数中的单个数据（通常是 GET 请求）。

**典型场景**: 查询参数，如 `GET` 请求中的查询字符串。

**使用示例**:

```java
@RequestMapping(value = "/getUser", method = RequestMethod.GET)
public Result<User> getUser(@RequestParam String userId) {
    // userId 从 URL 查询参数中提取出来
    return userService.findUserById(userId);
}
```

**前端请求**:

```bash
/getUser?userId=123
```

### 2. **@PathVariable**

**用途**: 用于从 URL 路径中提取动态路径参数。

**典型场景**: 处理 RESTful 风格的 URL，其中路径的一部分作为参数传递。

**使用示例**:

```java
@RequestMapping(value = "/user/{id}", method = RequestMethod.GET)
public Result<User> getUser(@PathVariable String id) {
    // id 从 URL 路径参数中提取出来
    return userService.findUserById(id);
}
```

**前端请求**:

```bash
/user/123
```

### 3.@RequestBody

**用途**: 用于解析请求体中的 JSON 或 XML 数据，通常用于 `POST`、`PUT` 请求。

**典型场景**: 前端通过发送 JSON 数据提交表单时，将整个请求体解析为 Java 对象。

**使用示例**:

```java
@RequestMapping(value = "/addUser", method = RequestMethod.POST)
public Result<Object> addUser(@RequestBody User user) {
    // user 从请求体中的 JSON 数据解析出来
    return userService.addUser(user);
}
```

**前端请求**:

```json
POST /addUser
{
    "id": "123",
    "username": "john"
}
```

### 4. **@ModelAttribute**

**用途**: 用于将请求参数自动绑定到 Java 对象的字段上，适合复杂表单数据的绑定，通常用于 `GET` 或 `POST` 请求。

**典型场景**: 将多个请求参数绑定到一个复杂的对象（如表单数据）。

**使用示例**:

```java
@RequestMapping(value = "/updateUser", method = RequestMethod.POST)
public Result<Object> updateUser(@ModelAttribute User user) {
    // 将请求参数中的字段映射到 User 对象中
    return userService.updateUser(user);
}
```

**前端请求**:

```bash
POST /updateUser
username=john&age=30
```

### 5.@RequestHeader

**用途**: 用于从请求头中提取信息。

**典型场景**: 获取认证信息、API token、或者客户端传递的自定义头部信息。

**使用示例**:

```java
@RequestMapping(value = "/getUserAgent", method = RequestMethod.GET)
public String getUserAgent(@RequestHeader("User-Agent") String userAgent) {
    // 从请求头中提取 User-Agent 信息
    return userAgent;
}
```

**前端请求**:

```less
GET /getUserAgent
(Request header: User-Agent: Mozilla/5.0)
```

### 6. **@CookieValue**

- **用途**: 用于从请求中的 Cookie 提取数据。

- **典型场景**: 提取会话信息或用户跟踪信息。

- **使用示例**:

  ```java
  @RequestMapping(value = "/getSessionId", method = RequestMethod.GET)
  public String getSessionId(@CookieValue(value = "JSESSIONID") String sessionId) {
      // 从 Cookie 中提取 JSESSIONID
      return sessionId;
  }
  ```

### 7. **MultipartFile (文件上传)**

- **用途**: 用于处理文件上传请求。

- **典型场景**: 处理用户通过表单上传文件。

- **使用示例:**

  ```java
  @RequestMapping(value = "/upload", method = RequestMethod.POST)
  public Result<Object> handleFileUpload(@RequestParam("file") MultipartFile file) {
      // file 从请求的 multipart/form-data 中提取出来
      return fileService.storeFile(file);
  }
  ```

- **前端请求:**

  ```html
  <form method="POST" enctype="multipart/form-data" action="/upload">
      <input type="file" name="file">
      <input type="submit">
  </form>
  ```

### 8.@RequestHeader

从Header中解析出Token:

```java
public ResponseEntity<Object> adminAddUser(@RequestBody PM_User user, @RequestHeader("Authorization") String token)
```

可以通过@RequestHeader("Authorization") String token解析。

## 在Repository接口中定义SQL

- 在 Spring MVC 中，`Repository` 接口的主要作用是提供与数据库交互的抽象层，简化对数据的持久化操作。它属于 Spring Data JPA 的一部分，帮助开发者减少繁琐的 SQL 查询语句编写，并通过面向对象的方式与数据库进行交互。

### `Repository` 接口的核心作用

1. **数据访问抽象层：** `Repository` 接口是 Spring Data 提供的通用数据访问接口，它通过定义方法为开发者提供了一种标准的方式来访问底层数据库。通过继承不同的 `Repository` 子接口，开发者可以直接使用 CRUD 操作方法。
2. **简化数据操作：** 通过继承 `Repository` 接口，Spring Data JPA 可以自动生成常见的数据操作方法，比如增删改查，无需开发者自己编写 SQL 或 HQL。
3. **提供自定义查询：** 除了内置的 CRUD 操作，开发者还可以通过定义方法名称或自定义查询注解（如 `@Query`）来实现更加复杂的查询。

示例：根据用户名和密码精确查找用户并返回结果的数目，可以在 `Repository` 接口中自定义一个查询方法。Spring Data JPA 允许通过方法命名约定自动生成查询，也可以使用 `@Query` 注解自定义查询。

### 方法一：使用命名查询

你可以通过 `findByUsernameAndPassword` 来精确查找，并使用 `countBy` 方法返回结果数目。

```java
public interface UserRepository extends JpaRepository<User, Long> {

    // 精确查找用户的数目
    long countByUsernameAndPassword(String username, String password);
}
```

- **`countByUsernameAndPassword`**：通过用户名和密码查找，并返回匹配的记录数。

### 方法二：使用 @Query 注解自定义查询

如果你想更灵活地定义查询逻辑，也可以使用 `@Query` 注解实现：

```java
public interface UserRepository extends JpaRepository<User, Long> {

    @Query("SELECT COUNT(u) FROM User u WHERE u.username = :username AND u.password = :password")
    long findUserCountByUsernameAndPassword(@Param("username") String username, @Param("password") String password);
}
```

- 这里使用了 JPQL（Java Persistence Query Language）通过 `@Query` 注解自定义查询语句，返回符合条件的用户数目。

### 使用方法

在控制器调用这个查询方法：

```java
public class UserController {

    @Autowired
    private UserRepository userRepository;

    public long getUserCount(String username, String password) {
        return userRepository.countByUsernameAndPassword(username, password);
    }
}
```

## 将响应体与状态码组装发回前端

### 手动构建返回对象

如果你的返回数据格式比较复杂，无法直接通过实体类进行灵活调整，手动构建一个返回的对象是个不错的选择。你可以直接使用`Map`或者`JSONObject`来构建灵活的JSON结构。

#### 使用 `Map`

```
java复制代码@PostMapping("/login")
public Map<String, Object> login(@RequestBody PM_User user) {
    Map<String, Object> response = new HashMap<>();
    try {
        int cnt = userRepository.countByUsernameAndPassword(user.getUsername(), user.getPassword());
        if (cnt == 0) {
            response.put("status", 400);
            response.put("msg", "用户名或密码错误");
            return response;
        }
        String jwtToken = jwtUtil.generateToken(user.getUsername());
        response.put("status", 200);
        response.put("msg", "验证成功");
        response.put("userInfo", userRepository.findByUsername(user.getUsername()));
        response.put("token", jwtToken);
        return response;
    } catch (Exception e) {
        response.put("status", 500);
        response.put("msg", "系统错误");
        return response;
    }
}
```

在这种情况下，返回的JSON格式可能会是这样的：

```
json复制代码{
  "status": 200,
  "msg": "验证成功",
  "userInfo": {
    "id": 1,
    "username": "john_doe"
  },
  "token": "some-jwt-token"
}
```

### 使用 `ResponseEntity`

`ResponseEntity` 是一个 Spring 提供的类，它可以用来灵活设置 HTTP 状态码、头部信息和返回的消息体。

#### 示例：

```
java复制代码@PostMapping("/login")
public ResponseEntity<Object> login(@RequestBody PM_User user) {
    int cnt = userRepository.countByUsernameAndPassword(user.getUsername(), user.getPassword());
    if (cnt == 0) {
        return new ResponseEntity<>("用户名或密码错误", HttpStatus.NOT_FOUND); // 404 状态码
    }
    
    String jwtToken = jwtUtil.generateToken(user.getUsername());
    Map<String, Object> response = new HashMap<>();
    response.put("message", "登录成功");
    response.put("token", jwtToken);
    
    return new ResponseEntity<>(response, HttpStatus.OK); // 200 状态码
}
```

在这个例子中，我们返回了一个`ResponseEntity`对象，并指定了不同的HTTP状态码，例如`HttpStatus.NOT_FOUND`（404）和`HttpStatus.OK`（200）。

## Postman使用方法

[接口调试神器：Postman 从入门到进阶教程（万字长文）！-腾讯云开发者社区-腾讯云](https://cloud.tencent.com/developer/article/2021399)

Token的添加方法：

在请求的Headers中添加如下键值对：

```js
Authorization: Bearer jwt
```

其中`Authorization`为键，`Bearer jwt`为值,jwt可以向`/api/login`或者`/api/register`发送请求获取。注意中间的空格不要遗漏。
