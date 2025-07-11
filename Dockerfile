FROM openjdk:17
COPY ./build/libs/goodluckynews-0.0.1-SNAPSHOT.jar goodluckynews.jar
ENTRYPOINT ["java", "-jar", "goodluckynews.jar"]