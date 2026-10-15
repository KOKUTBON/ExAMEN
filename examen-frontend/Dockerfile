# STAGE 1: Build Angular SSR
FROM node:20 AS builder
WORKDIR /app
ARG APP_PUBLIC_URL=http://localhost:4001
ENV APP_PUBLIC_URL=${APP_PUBLIC_URL}

COPY package*.json ./
RUN npm ci

COPY . .
# Builds the browser bundle and the SSR server
RUN npm run build

# STAGE 2: Run Angular SSR
FROM node:20-alpine
WORKDIR /app

COPY --from=builder /app/dist/examen-frontend /app/dist

RUN apk add --no-cache curl
USER node

EXPOSE 4000
HEALTHCHECK --interval=30s --timeout=5s --start-period=20s --retries=3 CMD curl -f http://localhost:4000/ || exit 1
CMD ["node", "dist/server/server.mjs"]
