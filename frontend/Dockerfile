# syntax=docker/dockerfile:1

# ---------- Build ----------
FROM node:24-alpine AS build
WORKDIR /app

# Dependencias primeiro: so sao reinstaladas quando o package-lock.json muda
COPY package.json package-lock.json ./
RUN --mount=type=cache,target=/root/.npm npm ci --no-audit --no-fund

COPY . .
RUN npm run build

# ---------- Runtime ----------
# Imagem oficial do Nginx que roda sem root (porta 8080)
FROM nginxinc/nginx-unprivileged:1.29-alpine

COPY nginx/default.conf /etc/nginx/conf.d/default.conf
COPY nginx/security-headers.conf /etc/nginx/snippets/security-headers.conf
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html

EXPOSE 8080

HEALTHCHECK --interval=10s --timeout=3s --start-period=5s --retries=3 \
  CMD wget -q --spider http://127.0.0.1:8080/ || exit 1
