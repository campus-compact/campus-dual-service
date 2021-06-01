FROM gradle:latest as builder
COPY build.gradle.kts .
COPY gradle.properties .
COPY settings.gradle.kts .
COPY src ./src
RUN gradle shadowJar

FROM openjdk:11-oraclelinux8
COPY cert.der .
RUN keytool -import -alias campusdualcert -keystore $JAVA_HOME/lib/security/cacerts -storepass changeit -noprompt -file cert.der

COPY --from=builder /home/gradle/build/libs/campus-dual-service-0.0.1-all.jar /app.jar
CMD [ "java", "-jar", "-Djava.security.egd=file:/dev/./urandom", "/app.jar" ]