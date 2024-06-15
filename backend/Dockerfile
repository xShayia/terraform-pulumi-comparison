#Stage 1
# initialize build and set base image for first stage
FROM gradle:8.8.0-jdk17 as stage1
# set working directory
WORKDIR /demo
COPY build.gradle .
COPY settings.gradle .
COPY ./src ./src
RUN gradle build -x test
#Stage 2
# set base image for second stage
FROM amazoncorretto:17-alpine3.19
# set deployment directory
WORKDIR /demo
# copy over the built artifact from the maven image
COPY --from=stage1 /demo/build/libs/demo-0.0.1-SNAPSHOT.jar /demo/app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/demo/app.jar"]