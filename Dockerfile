FROM openjdk:17
COPY ./build/libs/winnercat-0.0.1-SNAPSHOT.jar winnercat.jar
ENTRYPOINT ["java", "-jar", "winnercat.jar"]