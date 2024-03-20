# 后端业务

## 依赖库
- [MyBatis Flex](https://mybatis-flex.com/): 数据库工具
- [MapStruct](https://mapstruct.org/): Java类转换工具
- [Lombok](https://projectlombok.org/): 通过注解的方式，在编译时自动为属性生成构造器
- [FastJSON](https://github.com/alibaba/fastjson): Json工具
- [Log4j2](https://logging.apache.org/log4j/2.x/): 日志工具
- [Passay](https://www.passay.org/): 密码工具
- [Spring-Security](https://spring.io/projects/spring-security): 安全工具
- [MinIO](https://min.io/): 文件服务器
- [Redis](): 内存数据库, 存储验证码
- [Fabric-SDK-Java](https://github.com/hyperledger/fabric-gateway-java): Fabric的Java SDK工具
- [Sa-Token](https://sa-token.cc/): 登录, 权限认证工具
- [Tika](https://tika.apache.org/): 文件分析工具

## TODO
- 对接长安链

## 部署注意事项
- 安装Mariadb并修改application.yaml中的字段
  - spring.datasouce.url
  - spring.datasouce.username
  - spring.datasouce.password
- 安装Minio并修改application.yaml中的字段
  - minio.endpoint
  - minio.accessKey
  - minio.secretkey
- 安装Redis并修改application.yaml中的字段
  - spring.redis.host
  - spring.redis.port
  - spring.redis.password