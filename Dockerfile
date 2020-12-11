FROM openjdk:11
WORKDIR /app/
COPY ./src/* ./
RUN javac -encoding UTF-8 *.java
