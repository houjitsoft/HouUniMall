# =====================================================================
# HouCloud 后端「构建器」镜像：一次性编译全部服务，产物 jar 全部归集到 /jars。
# 各服务镜像（Dockerfile.service）只做一件事：从这个镜像里 COPY 自己那一个 jar，
# 因此 14 个服务只编译一次，而不是每个镜像各编译一遍。
#
# 构建上下文 = 仓库根目录：
#   docker build -f deploy/Dockerfile.builder -t unimall/builder:latest .
# 首次编译会下载全部依赖（约 5-15 分钟），之后走 /root/.m2 缓存会快很多。
# =====================================================================
FROM maven:3.9.9-eclipse-temurin-21

# build-jars.sh 依赖 python3（解析 architecture/services.json）与 unzip（校验 jar
# 的 MANIFEST 是否有 Start-Class）。maven 基础镜像两者都没有，缺失时脚本会在第一步
# 就以 exit 127 失败，且报错只显示 "did not complete successfully" 很难定位。
# （2026-10-19 构建 base 时踩到：exit 127 ← python3 not found）
RUN apt-get update \
    && apt-get install -y --no-install-recommends python3 unzip \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /src
COPY . /src
RUN chmod +x /src/deploy/build-jars.sh

# 依赖缓存挂载：重复构建不需要重新下载 Maven 依赖
RUN --mount=type=cache,target=/root/.m2 \
    JARS_OUT_DIR=/jars /src/deploy/build-jars.sh

# 说明：不要在这里声明 VOLUME /jars——声明后 Dockerfile.service 的
#       COPY --from=builder /jars/xxx.jar 会拿不到文件。
CMD ["ls", "-lh", "/jars"]
