# Frontend Dockerfile
FROM nginx:alpine

# Copy built Angular app
COPY dist/your-app-name /usr/share/nginx/html

# Copy custom nginx config
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Add proper MIME types (backup solution)
RUN echo "types { application/javascript js; }" >> /etc/nginx/nginx.conf

EXPOSE 80