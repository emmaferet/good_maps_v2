# ==============================================================================
# Good Maps Frontend - Multi-stage Production Dockerfile
# Stage 1: Build Angular 19 Production Bundle
# ==============================================================================
FROM node:22-alpine AS builder

WORKDIR /app

# Cache package dependencies
COPY package*.json ./
RUN npm ci

# Copy source code and build production distribution
COPY . .
RUN npm run build -- --configuration production

# ==============================================================================
# Stage 2: Serve with Lightweight & Secure Nginx Alpine
# ==============================================================================
FROM nginx:1.27-alpine AS runner

# Remove default Nginx website
RUN rm -rf /usr/share/nginx/html/*

# Copy compiled Angular distribution from builder stage
COPY --from=builder /app/dist/good-maps-frontend/browser /usr/share/nginx/html

# Copy custom Nginx configuration
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

# Healthcheck testing HTTP response on root
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:80/ || exit 1

CMD ["nginx", "-g", "daemon off;"]
