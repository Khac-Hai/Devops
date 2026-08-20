FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY ProductService.java .
RUN javac ProductService.java
EXPOSE 8082
CMD ["java", "ProductService"]
