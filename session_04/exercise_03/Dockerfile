FROM alpine:latest

RUN apk add --no-cache postgresql-client bash

COPY app.sh /app.sh
RUN chmod +x /app.sh

EXPOSE 8080

CMD ["/app.sh"]
