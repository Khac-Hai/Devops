# Stage 1: Compile Java Source
FROM eclipse-temurin:17-jdk-alpine AS builder

WORKDIR /app

COPY src ./src

# Biên dịch mã nguồn Java
RUN mkdir -p out && javac -d out src/main/java/com/quickbite/Main.java

# Stage 2: Runtime Image gọn nhẹ
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=builder /app/out ./out

# Chạy class com.quickbite.Main
ENTRYPOINT ["java", "-cp", "out", "com.quickbite.Main"]
