FROM gradle:7-jdk11 as builder

COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle shadowJar


FROM openjdk:11-jre-slim

COPY --from=builder /home/gradle/src/build/libs .
ENTRYPOINT java -jar *.jar
EXPOSE 80
