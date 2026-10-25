# ─────────────────────────────────────────────────────────────────────────────
# Stage 1 – Build Angular application
# ─────────────────────────────────────────────────────────────────────────────
FROM node:22-alpine AS build

WORKDIR /app

# Install dependencies first (cached unless package-lock.json changes)
COPY package.json package-lock.json ./
RUN npm ci --prefer-offline

# Copy source and build production bundle
COPY . .
RUN npm run build

# ─────────────────────────────────────────────────────────────────────────────
# Stage 2 – Serve with nginx
# ─────────────────────────────────────────────────────────────────────────────
FROM nginx:1.27-alpine

# Remove the default nginx config
RUN rm /etc/nginx/conf.d/default.conf

# Copy custom nginx config
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy built Angular files
COPY --from=build /app/dist/frontend/browser /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
