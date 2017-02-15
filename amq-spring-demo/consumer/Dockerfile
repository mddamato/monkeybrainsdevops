FROM 10.103.137.89:4443/openjdk:8-jdk-alpine
VOLUME /tmp
ENV SLEEP_TIME 3
ADD *.jar /app.jar
EXPOSE 8080
EXPOSE 61616
EXPOSE 1883
EXPOSE 8778
EXPOSE 61613
RUN sh -c 'touch /app.jar'
ENTRYPOINT ["/usr/bin/java"]
CMD ["-Djava.security.egd=file:/dev/./urandom", "-jar", "/app.jar"]
