# Build stage
FROM maven:4.0.0-rc-4-amazoncorretto-17 AS build
COPY src /home/app/src
COPY pom.xml /home/app
COPY settings.xml /home/app
RUN mvn --settings /home/app/settings.xml -f /home/app/pom.xml clean package

# Package stage
FROM buddy/ubuntu-java:20.04-openjdk-17
COPY --from=build /home/app/target/*.jar app.jar
# Add MuseScore
RUN apt-get update && apt-get install -y xvfb wget libfuse2 freeglut3-dev ffmpeg lame
RUN mkdir /opt/musescore
RUN wget -q -O /opt/musescore/MuseScore-3.6.2.548021370-x86_64.AppImage https://github.com/geniot/octavian-data/releases/download/0.1/MuseScore-3.6.2.548021370-x86_64.AppImage
RUN chmod 777 /opt/musescore/MuseScore-3.6.2.548021370-x86_64.AppImage
EXPOSE 8080
ENTRYPOINT ["java","-jar","app.jar"]