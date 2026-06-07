FROM docker.m.daocloud.io/library/eclipse-temurin:17-jre
WORKDIR /app

COPY lib/ /app/lib/
COPY app.jar /app/

ENV JAVA_OPTS="-Dspring.profiles.active=prod -Duser.timezone=Asia/Shanghai"
EXPOSE 8070
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -cp 'app.jar:lib/*' org.xcyms.ApiApplication"]
