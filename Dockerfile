FROM gradle:8.5.0-jdk21-alpine AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src

RUN gradle build --scan --no-daemon -x test

FROM openjdk:21-jdk-slim

EXPOSE 8080

RUN mkdir /app

COPY --from=build /home/gradle/src/build/libs/*.jar /app/seed.jar

ENTRYPOINT ["java","-jar","/app/seed.jar"]