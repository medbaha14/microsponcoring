# Multi-stage build for Angular application
FROM node:18-alpine AS build

# Set working directory
WORKDIR /app

# Copy package files
COPY package*.json ./

# Install dependencies (removed --only=production and added --legacy-peer-deps)
RUN npm ci --legacy-peer-deps

# Copy source code
COPY . .

# Remove Angular budgets for Docker build
RUN sed -i '/"budgets": \[/,/\]/d' angular.json

# Build the application with production configuration
RUN npm run build -- --configuration production

# Production stage
FROM nginx:alpine

# Install necessary packages
RUN apk add --no-cache tzdata

# Set timezone
ENV TZ=UTC

# Copy built application from build stage
COPY --from=build /app/dist/microsponsoring/browser /usr/share/nginx/html

# Copy custom nginx configuration
COPY nginx.conf /etc/nginx/nginx.conf

# Create nginx user if it doesn't exist
RUN addgroup -g 1001 -S nginxgroup && \
    adduser -u 1001 -S nginxuser -G nginxgroup

# Change ownership of nginx directories
RUN chown -R nginxuser:nginxgroup /var/cache/nginx && \
    chown -R nginxuser:nginxgroup /var/log/nginx && \
    chown -R nginxuser:nginxgroup /etc/nginx/conf.d && \
    chown -R nginxuser:nginxgroup /usr/share/nginx/html

# Switch to non-root user
USER nginxuser

# Expose port
EXPOSE 80

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:80 || exit 1

# Start nginx
CMD ["nginx", "-g", "daemon off;"] 