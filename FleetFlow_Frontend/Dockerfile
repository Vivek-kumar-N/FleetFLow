# ─────────────────────────────────────────────
# Stage 1 – Build the Angular app
# ─────────────────────────────────────────────
FROM node:18-alpine AS builder

WORKDIR /app

# Copy lockfiles first for layer caching — npm install only re-runs when
# package.json or package-lock.json changes.
COPY package.json package-lock.json ./
RUN npm ci --legacy-peer-deps

# Copy the rest of the source and compile for production.
# Output lands in dist/fleetflow-frontend (as declared in angular.json).
COPY . .
RUN npm run build -- --configuration production

# ─────────────────────────────────────────────
# Stage 2 – Serve with Nginx
# ─────────────────────────────────────────────
FROM nginx:alpine

# Replace the default Nginx config with our custom one.
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Copy the compiled Angular bundle into Nginx's web root.
COPY --from=builder /app/dist/fleetflow-frontend /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
