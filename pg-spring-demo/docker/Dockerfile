FROM openjdk:8-jdk-alpine
VOLUME /tmp
ENV SLEEP_TIME 3
ADD *.jar /app.jar
RUN sh -c 'touch /app.jar'
ENTRYPOINT ["/usr/bin/java"]
CMD ["-Djava.security.egd=file:/dev/./urandom", "-jar", "/app.jar"]
