FROM eclipse-temurin:17-jdk

WORKDIR /app

ARG JAR_FILE=build/libs/*.jar
COPY ${JAR_FILE} app.jar

ENTRYPOINT ["java","-Xms128m","-Xmx512m","-XX:+UseG1GC","-jar","app.jar"]