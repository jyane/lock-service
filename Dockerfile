FROM gcr.io/distroless/java

ADD app-0.1-SNAPSHOT /app
ADD conf /app/conf
WORKDIR /app

EXPOSE 10080

ENTRYPOINT ["/usr/bin/java", "-classpath", "/app/lib/*", "jp.jyane.lock.LockMain"]
