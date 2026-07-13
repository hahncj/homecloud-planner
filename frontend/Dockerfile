# syntax=docker/dockerfile:1
FROM node:22.20.0-alpine AS build
WORKDIR /workspace

COPY package.json package-lock.json ./
RUN npm ci

COPY . .

# Vite inlines VITE_ variables at build time, so the API base URL must be
# supplied as a build argument rather than a container environment variable.
ARG VITE_API_BASE_URL
ENV VITE_API_BASE_URL=${VITE_API_BASE_URL}
RUN npm run build

FROM nginx:1.27.4-alpine
COPY --from=build /workspace/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

HEALTHCHECK --interval=10s --timeout=5s --start-period=10s --retries=5 \
  CMD wget -q --spider http://localhost:80/healthz || exit 1
