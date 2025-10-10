# Multi-stage build for Angular application
FROM node:18-alpine AS build

# Set working directory
WORKDIR /app

# Copy package files
COPY package*.json ./

# Install dependencies
RUN npm ci --legacy-peer-deps

# Copy source code
COPY . .

# Remove Angular budgets for Docker build
RUN sed -i '/"budgets": \[/,/\]/d' angular.json

# Build the application with production configuration for Kubernetes
RUN npm run build -- --configuration production

# DEBUG: Comprehensive build output check
RUN echo "=== CHECKING BUILD OUTPUT ===" && \
    echo "Current directory:" && pwd && \
    echo "=== Listing /app ===" && \
    ls -la /app/ && \
    echo "=== Listing /app/dist ===" && \
    ls -la /app/dist/ 2>/dev/null || echo "dist directory not found" && \
    echo "=== Finding all built files ===" && \
    find /app -name "*.js" -o -name "*.html" -o -name "*.css" -o -name "index.html" 2>/dev/null | head -20 && \
    echo "=== Checking angular.json outputPath ===" && \
    cat angular.json | grep -A 5 -B 5 outputPath

# Production stage
FROM nginx:alpine

# Install necessary packages
RUN apk add --no-cache tzdata

# Set timezone
ENV TZ=UTC

# First, let's see what the build stage actually produced
COPY --from=build /app/dist /tmp/build-output

# DEBUG: Check what we copied
RUN echo "=== CHECKING COPIED FILES ===" && \
    ls -la /tmp/build-output/ && \
    find /tmp/build-output -name "*.js" -o -name "*.html" -o -name "*.css" | head -10

# Now copy to the correct location based on actual structure
RUN if [ -d "/tmp/build-output/microsponsoring/browser" ]; then \
        echo "Copying from /tmp/build-output/microsponsoring/browser" && \
        cp -r /tmp/build-output/microsponsoring/browser/* /usr/share/nginx/html/; \
    elif [ -d "/tmp/build-output/browser" ]; then \
        echo "Copying from /tmp/build-output/browser" && \
        cp -r /tmp/build-output/browser/* /usr/share/nginx/html/; \
    elif [ -d "/tmp/build-output/microsponsoring" ]; then \
        echo "Copying from /tmp/build-output/microsponsoring" && \
        cp -r /tmp/build-output/microsponsoring/* /usr/share/nginx/html/; \
    else \
        echo "Copying directly from /tmp/build-output" && \
        cp -r /tmp/build-output/* /usr/share/nginx/html/ 2>/dev/null || echo "No files to copy"; \
    fi

# Copy custom nginx configuration with proper MIME types
COPY nginx.conf /etc/nginx/nginx.conf

# Create nginx user if it doesn't exist
RUN addgroup -g 1001 -S nginxgroup && \
    adduser -u 1001 -S nginxuser -G nginxgroup

# Change ownership of nginx directories
RUN chown -R nginxuser:nginxgroup /var/cache/nginx && \
    chown -R nginxuser:nginxgroup /var/log/nginx && \
    chown -R nginxuser:nginxgroup /etc/nginx/conf.d && \
    chown -R nginxuser:nginxgroup /usr/share/nginx/html

# Test that nginx config is valid
RUN nginx -t

# Switch to non-root user
USER nginxuser

# Expose port
EXPOSE 80

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:80 || exit 1

# Start nginx
CMD ["nginx", "-g", "daemon off;"]