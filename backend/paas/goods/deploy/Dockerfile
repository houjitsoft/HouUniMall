# 基础镜像
FROM  openjdk:21
MAINTAINER fzh

EXPOSE 8083
# 设置时区
RUN /bin/cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo 'Asia/Shanghai' >/etc/timezone
# 创建目录
RUN mkdir -p /app
# 指定路径
WORKDIR /app
# 复制jar文件到路径
COPY goods.jar /app/goods.jar
COPY model.pt /app/model.pt
# 启动认证服务
ENTRYPOINT ["java","-jar","-Xms512m -Xmx1024m","goods.jar"]
