# Multi-Stage Dockerfile für LivePoll
# Stage 1: Maven Build
FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -B

COPY src ./src

RUN mvn clean package -DskipTests

FROM tomcat:10.1-jdk21-temurin

ENV CATALINA_HOME=/usr/local/tomcat
ENV PATH=$CATALINA_HOME/bin:$PATH

RUN mkdir -p /app/db

COPY --from=build /app/target/*.war $CATALINA_HOME/webapps/ROOT.war

COPY docker/tomcat-users.xml $CATALINA_HOME/conf/
COPY docker/server.xml $CATALINA_HOME/conf/

COPY docker/entrypoint.sh /entrypoint.sh
RUN sed -i 's/\r$//' /entrypoint.sh && chmod +x /entrypoint.sh

EXPOSE 8080

ENTRYPOINT ["/entrypoint.sh"]
