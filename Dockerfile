FROM fedora:41
WORKDIR /app
COPY target/oda-actions-service /app

CMD ["./oda-actions-service"]
