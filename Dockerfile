FROM openjdk:17.0.1-jdk-slim

RUN useradd -ms /bin/bash appuser

COPY sb-workflow-handler-1.0.0.jar /opt/
EXPOSE 9060

RUN chown -R appuser:appuser /opt
USER appuser
WORKDIR /opt

CMD ["/bin/bash", "-c", "java -XX:+PrintFlagsFinal $JAVA_OPTIONS -XX:+UnlockExperimentalVMOptions -jar /opt/sb-workflow-handler-1.0.0.jar"]
