FROM alpine:3.21.3

RUN apk add --no-cache libc6-compat libstdc++

WORKDIR /app
RUN addgroup -S appgroup && \
    adduser -S appuser -G appgroup -h /app && \
    chown -R appuser:appgroup /app

COPY --chown=appuser:appgroup ./build/native/nativeCompile/ /app/

USER appuser

EXPOSE 8080

ENTRYPOINT ["/app/searchengine"]