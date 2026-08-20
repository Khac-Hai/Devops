FROM eclipse-temurin:17-jdk-alpine
WORKDIR /app
COPY UserService.java .
RUN javac UserService.java
EXPOSE 8081
CMD ["java", "UserService"]
