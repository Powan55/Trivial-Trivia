# Builds the WAR and runs it on its own, without an external Tomcat.
#
# Boot's repackage keeps the provided-scope Tomcat and Jasper in WEB-INF/lib-provided, and
# `java -jar` on a WAR puts those back on the classpath. That is what makes the JSP views render
# here. Jasper ships its own compiler, so a JRE base is enough.
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /src
COPY pom.xml .
COPY src ./src
RUN mvn -B -DskipTests package

FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /src/target/trivial-trivia-*.war app.war
# The JSPs the build already compiled, in the layout Jasper uses for its own work directory.
COPY --from=build /src/target/classes/org/apache/jsp /app/jsp-work/org/apache/jsp
EXPOSE 8080

# Shell form on purpose: $PORT is set by the host and has to expand.
#
# A free instance is a tenth of a CPU, and every visit starts by waking the container, so all of
# this is about the cold path. Capping the JIT at tier one gets Tomcat up sooner. Handing Jasper
# the precompiled classes as its scratch directory is what makes it use them -- it finds each page
# already built and newer than the .jsp and compiles nothing; without it the first hit on a page
# costs a minute or more of the visitor's time. development=false stops it checking, and lives
# here rather than in application.properties so that a JSP edited locally still reloads.
CMD exec java -XX:MaxRAMPercentage=75 -XX:TieredStopAtLevel=1 -jar app.war --server.port=${PORT:-8080} --server.servlet.jsp.init-parameters.scratchdir=/app/jsp-work --server.servlet.jsp.init-parameters.development=false
