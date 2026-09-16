# FleetFlow – Deployment Guide

## Architecture Overview

```
Browser → Nginx (port 80)
              ├─ /* → Angular SPA (static files)
              └─ /api/* → Spring Boot (port 8080) → MySQL (port 3306)
```

---

## 1. Local Development with Docker Compose

### Prerequisites
- [Docker Desktop](https://www.docker.com/products/docker-desktop/) (includes Docker Compose v2)
- Git

### Steps

```bash
# 1. Clone the repo
git clone <your-repo-url>
cd FleetFlow

# 2. Create your local env file
cp .env.example .env
# Edit .env and set a secure MYSQL_ROOT_PASSWORD (and mail creds if needed)

# 3. Build images and start all three services
docker compose up --build

# The first run downloads base images and compiles both apps — allow ~5 minutes.
# Subsequent starts are much faster because Docker caches layers.
```

### Verify everything is running

| What | URL |
|------|-----|
| Angular frontend | http://localhost |
| Spring Boot API  | http://localhost:8080/api |
| MySQL (via tool) | localhost:3306, DB: fleetflow_db |

### Useful commands

```bash
# Run in background (detached)
docker compose up --build -d

# Stream logs for a single service
docker compose logs -f backend

# Stop and remove containers (keeps the DB volume)
docker compose down

# Nuke everything including the DB volume (fresh start)
docker compose down -v
```

---

## 2. Cloud Deployment

### Option A – Render (Backend + Database, free tier)

#### 2a. MySQL on Render (free managed PostgreSQL is free; MySQL requires a paid plan)
> **Note:** Render's free database tier is PostgreSQL only.  
> For free MySQL, use **Railway** (see Option B) or **PlanetScale** (serverless MySQL).

#### 2b. Backend on Render (Web Service)

1. Push your repository to GitHub.
2. Go to [render.com](https://render.com) → **New → Web Service**.
3. Connect your GitHub repo.
4. Set the following:
   - **Root Directory:** `FleetFlow_Backend/FleetFlow`
   - **Dockerfile path:** `Dockerfile` (auto-detected)
   - **Instance type:** Free
5. Add **Environment Variables** in the Render dashboard:
   ```
   SPRING_DATASOURCE_URL      = jdbc:mysql://<db-host>:3306/fleetflow_db?useSSL=true&allowPublicKeyRetrieval=true
   SPRING_DATASOURCE_USERNAME = <db-user>
   SPRING_DATASOURCE_PASSWORD = <db-password>
   SPRING_MAIL_USERNAME       = your-email@gmail.com
   SPRING_MAIL_PASSWORD       = your-gmail-app-password
   ```
6. Deploy. Render builds the Docker image from your Dockerfile automatically.
7. Note your backend's public URL (e.g. `https://fleetflow-backend.onrender.com`).

---

### Option B – Railway (Backend + MySQL, free starter plan)

Railway gives you a free MySQL instance and free app hosting in the same project.

1. Go to [railway.app](https://railway.app) → **New Project → Deploy from GitHub repo**.
2. Select your repository.
3. Railway auto-detects the `Dockerfile` — set the **Root Directory** to `FleetFlow_Backend/FleetFlow`.
4. Add a **MySQL** plugin inside the same Railway project (one click).
5. Railway injects `MYSQLHOST`, `MYSQLPORT`, `MYSQLUSER`, `MYSQLPASSWORD`, `MYSQLDATABASE` automatically. Override them to match your Spring Boot variable names:
   ```
   SPRING_DATASOURCE_URL      = jdbc:mysql://${{MYSQLHOST}}:${{MYSQLPORT}}/${{MYSQLDATABASE}}?useSSL=false&allowPublicKeyRetrieval=true
   SPRING_DATASOURCE_USERNAME = ${{MYSQLUSER}}
   SPRING_DATASOURCE_PASSWORD = ${{MYSQLPASSWORD}}
   ```
6. Deploy and grab the public backend URL from the Railway dashboard.

---

### Option C – Angular Frontend on Vercel (free)

Vercel builds and serves the Angular app as a static site.

1. Go to [vercel.com](https://vercel.com) → **Add New → Project** → import your GitHub repo.
2. Configure the project:
   - **Root Directory:** `FleetFlow_Frontend`
   - **Build Command:** `npm run build -- --configuration production`
   - **Output Directory:** `dist/fleetflow-frontend`
   - **Install Command:** `npm ci --legacy-peer-deps`
3. Add an **Environment Variable** (if your Angular code reads the API URL from the environment):
   ```
   BACKEND_URL = https://fleetflow-backend.onrender.com
   ```
   > If you hard-code `environment.prod.ts`, update `apiUrl` there before deploying.
4. Deploy. Vercel gives you a free HTTPS URL instantly.

> **CORS:** Make sure your Spring Boot `SecurityConfig` / CORS config allows requests from the Vercel domain.

---

### Option D – Angular Frontend on Render (Static Site, free)

1. **New → Static Site** in Render.
2. Connect the repo, set:
   - **Root Directory:** `FleetFlow_Frontend`
   - **Build Command:** `npm ci --legacy-peer-deps && npm run build -- --configuration production`
   - **Publish Directory:** `dist/fleetflow-frontend`
3. Add a **Rewrite Rule** for Angular routing:
   - Source: `/*`
   - Destination: `/index.html`
4. Deploy.

---

## 3. Environment Variables Reference

| Variable | Used by | Description |
|----------|---------|-------------|
| `MYSQL_ROOT_PASSWORD` | Compose only | MySQL root password |
| `SPRING_DATASOURCE_URL` | Backend | Full JDBC connection string |
| `SPRING_DATASOURCE_USERNAME` | Backend | DB username |
| `SPRING_DATASOURCE_PASSWORD` | Backend | DB password |
| `SPRING_MAIL_USERNAME` | Backend | Gmail address for notifications |
| `SPRING_MAIL_PASSWORD` | Backend | Gmail App Password (not your login password) |

---

## 4. Production Checklist

- [ ] Change `spring.jpa.hibernate.ddl-auto` from `update` to `validate` after the schema stabilises.
- [ ] Set `spring.jpa.show-sql=false` in production.
- [ ] Store secrets in Render/Railway/Vercel environment variables — never commit `.env`.
- [ ] Add `SPRING_PROFILES_ACTIVE=prod` and create `application-prod.properties` for production overrides.
- [ ] Enable HTTPS on all services (Render, Railway, and Vercel provide free TLS automatically).
- [ ] Update Angular `environment.prod.ts` with the real backend URL before building for Vercel/Render static.
- [ ] Restrict CORS in `SecurityConfig` to your specific frontend domain(s).
