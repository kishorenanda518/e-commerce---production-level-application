# royal-mart/Dockerfile
# ─────────────────────────────────────────────────────────────
# Multi-stage build:
#   Stage 1 — Node.js builds the React app
#   Stage 2 — Nginx serves the built files
# ─────────────────────────────────────────────────────────────

# ── Stage 1: Build ────────────────────────────────────────────
FROM node:20-alpine AS build

WORKDIR /app

# Copy package files first (layer caching)
COPY package.json package-lock.json ./
RUN npm install --legacy-peer-deps

# Copy source and build
COPY . .
RUN npm run build

# ── Stage 2: Serve with Nginx ─────────────────────────────────
FROM nginx:alpine

# Copy built React app to nginx
COPY --from=build /app/build /usr/share/nginx/html

# Copy nginx config
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
