FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

FROM amazoncorretto:17-alpine
ARG VERSION=1.0.0
ARG PROFILE=local

ENV JAR_VERSION=${VERSION}
ENV JAR_PROFILE=${PROFILE}
ENV DB_URL=jdbc:postgresql://postgres-stsh:5432/stsh
WORKDIR /app
COPY --from=build /build/target/*.jar /app/
EXPOSE 8443
CMD java -jar steam-shop-backend-${JAR_VERSION}.jar --spring.profiles.active=${JAR_PROFILE} --spring.datasource.url=${DB_URL}