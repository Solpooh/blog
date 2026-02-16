# ARM64 JDK 17
FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

# Timezone 설정
ENV TZ=Asia/Seoul
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# yt-dlp standalone binary + Node.js (JS runtime for YouTube n challenge) 설치
RUN apt-get update && \
    apt-get install -y wget && \
    wget -q https://github.com/yt-dlp/yt-dlp/releases/latest/download/yt-dlp_linux_aarch64 -O /usr/local/bin/yt-dlp && \
    chmod +x /usr/local/bin/yt-dlp && \
    wget -q https://nodejs.org/dist/v20.11.0/node-v20.11.0-linux-arm64.tar.gz && \
    tar -xzf node-v20.11.0-linux-arm64.tar.gz && \
    cp node-v20.11.0-linux-arm64/bin/node /usr/local/bin/ && \
    rm -rf node-v20.11.0-linux-arm64* && \
    yt-dlp --version && \
    node --version && \
    apt-get remove -y wget && \
    apt-get autoremove -y && \
    rm -rf /var/lib/apt/lists/*

# transcript 임시 디렉토리 생성
RUN mkdir -p /app/transcript

# 빌드된 JAR 파일 복사
COPY build/libs/*.jar app.jar

# 포트 노출
EXPOSE 4000

# JVM 메모리 설정 (t4g.micro 1.8GB, 다중 프로세스 공존 환경)
ENV JAVA_OPTS="-Xms256m -Xmx512m \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:MaxMetaspaceSize=128m \
  -XX:ReservedCodeCacheSize=64m \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=/var/log/app/"

# 프로파일 설정
ENV SPRING_PROFILES_ACTIVE=prod

# 실행
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

