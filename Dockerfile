FROM amazoncorretto:21
EXPOSE 8080
COPY ./build/libs/*.jar ./app.jar
COPY ./application.yml ./
ENTRYPOINT ["java", "-jar", "app.jar"]