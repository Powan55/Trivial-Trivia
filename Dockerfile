# Builds the WAR and runs it on its own, without an external Tomcat.
#
# Boot's repackage keeps the provided-scope Tomcat and Jasper in WEB-INF/lib-provided, and
# `java -jar` on a WAR puts those back on the classpath. That is what makes the JSP views render
# here. Jasper compiles them at request time with the ECJ it ships, so a JRE base is enough.
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /src
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /src/target/trivial-trivia-*.war app.war
EXPOSE 8080
# Shell form on purpose: $PORT is set by the host and has to expand. The JIT is capped at the
# first tier because a free instance gets a tenth of a CPU and every visit starts by waking the
# thing up -- startup is what hurts here, not steady-state throughput.
CMD exec java -XX:MaxRAMPercentage=75 -XX:TieredStopAtLevel=1 -jar app.war --server.port=${PORT:-8080}
