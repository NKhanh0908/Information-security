# JDK 21 và Maven
# dùng image đã có sẵn cả hai, và đặt tên stage là builder.
FROM maven:3.9.9-eclipse-temurin-21 AS builder
LABEL authors="khanh"

# Thiết lập nơi làm việc cố định để các lệnh sau dễ dàng tham chiếu.
WORKDIR /app

# Copy pom.xml để cache dependency
COPY pom.xml ./

# Tải dependency
RUN mvn dependency:go-offline -B

# Copy source
COPY src ./src

# Build jar
# làm sạch (clean), đóng gói thành file JAR (package), Bỏ qua các bài kiểm tra (-DskipTests),
# sử dụng cấu hình sản xuất (-Pprod)
RUN mvn clean package -DskipTests -Pprod -Dspring-boot.build-image=false -Dpackaging=jar

FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

WORKDIR /app

# ARG để tránh hardcode version JAR
ARG JAR_FILE=target/*.jar
COPY --from=builder /app/${JAR_FILE} app.jar

# Chạy ứng dụng
ENTRYPOINT ["java","-jar","app.jar"]