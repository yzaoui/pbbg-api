FROM gradle:7.6.0-jdk19 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle app:buildFatJar --no-daemon

FROM openjdk:19
EXPOSE 8080:80
RUN mkdir /app
COPY --from=build /home/gradle/src/app/build/libs/app.jar /app/app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]
