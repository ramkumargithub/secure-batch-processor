FROM openjdk:14-jdk-alpine

WORKDIR /opt/batch-processor

COPY target/batch-processor-1.0-SNAPSHOT-jar-with-dependencies.jar app.jar

# Create data folders
RUN mkdir -p data/in data/out data/archive data/error

VOLUME ["/opt/batch-processor/data"]

CMD ["java", "-jar", "app.jar"]
