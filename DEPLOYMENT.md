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
git clone https://github.com/Vivek-kumar-N/FleetFLow.git
cd FleetFlow

# 2. Create your local env file
cp .env.example .env
# Edit .env and set a secure MYSQL_ROOT_PASSWORD and JWT_SECRET

# 3. Build images and start all three services
docker compose up --build
```

### Verify everything is running

| What | URL |
|------|-----|
| Angular frontend | http://localhost |
| Spring Boot API  | http://localhost:8080/api |
| MySQL (via tool) | localhost:3307, DB: fleetflow_db |

### Useful commands

```bash
docker compose up --build -d        # detached mode
docker compose logs -f backend      # stream backend logs
docker compose down                 # stop (keeps DB volume)
docker compose down -v              # stop + wipe DB volume
```

---

## 2. Cloud Deployment — Free Tier

### Architecture

```
Vercel (Angular SPA)  ──→  Render Web Service (Spring Boot)  ──→  Aiven / Railway (MySQL)
```

---

### Step 1 — Free MySQL Database (Aiven)

Aiven offers a free MySQL instance (no credit card required for the free plan).

1. Sign up at [aiven.io](https://aiven.io).
2. Create a new **MySQL** service → choose the **Free** plan.
3. Once provisioned, go to **Connection Information** and note:
   - **Host** (e.g. `mysql-xxxx.aivencloud.com`)
   - **Port** (e.g. `12345`)
   - **Database** (`defaultdb` — you can rename to `fleetflow_db`)
   - **Username** (`avnadmin`)
   - **Password**
4. Build your JDBC URL:
   ```
   jdbc:mysql://<host>:<port>/fleetflow_db?useSSL=true&allowPublicKeyRetrieval=true
   ```

> **Railway alternative:** Go to [railway.app](https://railway.app) → New Project → Add MySQL plugin. Railway auto-injects connection variables into the same project.

---

### Step 2 — Backend on Render (Web Service)

1. Go to [render.com](https://render.com) → **New → Web Service**.
2. Connect your GitHub repo: `Vivek-kumar-N/FleetFLow`.
3. Configure:
   - **Root Directory:** `FleetFlow_Backend/FleetFlow`
   - **Runtime:** Docker (Render auto-detects the `Dockerfile`)
   - **Instance Type:** Free
4. Add the following **Environment Variables** in the Render dashboard:

| Variable | Value |
|----------|-------|
| `SPRING_DATASOURCE_URL` | `jdbc:mysql://<aiven-host>:<port>/fleetflow_db?useSSL=true&allowPublicKeyRetrieval=true` |
| `SPRING_DATASOURCE_USERNAME` | `avnadmin` (or your DB user) |
| `SPRING_DATASOURCE_PASSWORD` | your DB password |
| `JWT_SECRET` | a strong random 32+ char string |
| `ALLOWED_ORIGINS` | `https://fleetflow.vercel.app,https://fleetflow-frontend.onrender.com` |
| `SPRING_MAIL_USERNAME` | your Gmail address |
| `SPRING_MAIL_PASSWORD` | your Gmail App Password |

5. Click **Deploy**. Render builds the Docker image and starts the container.
6. Once live, note your backend URL: `https://fleetflow-backend.onrender.com`

> **Important:** Free Render instances spin down after 15 minutes of inactivity. The first request after sleep takes ~30 seconds to wake up. This is a free-tier limitation.

---

### Step 3 — Update Frontend Production URL

Before deploying the frontend, update the backend URL if it differs from the default:

Edit `FleetFlow_Frontend/src/environments/environment.prod.ts`:
```typescript
export const environment = {
  production: true,
  apiUrl: 'https://YOUR-ACTUAL-RENDER-URL.onrender.com/api'
};
```

Commit and push the change.

---

### Step 4 — Frontend on Vercel (Angular SPA)

1. Go to [vercel.com](https://vercel.com) → **Add New → Project** → import `Vivek-kumar-N/FleetFLow`.
2. Configure:
   - **Root Directory:** `FleetFlow_Frontend`
   - **Framework Preset:** Angular (or Other)
   - **Build Command:** `npm run build -- --configuration production`
   - **Output Directory:** `dist/fleetflow-frontend`
   - **Install Command:** `npm ci --legacy-peer-deps`
3. Deploy. The `vercel.json` in `FleetFlow_Frontend/` handles Angular HTML5 routing automatically — deep links won't 404.
4. Note your Vercel URL (e.g. `https://fleetflow.vercel.app`).

---

### Step 5 — Update CORS on Render

Go back to the Render dashboard → your backend service → **Environment** and update:

```
ALLOWED_ORIGINS=https://fleetflow.vercel.app,https://fleetflow-frontend.onrender.com
```

Trigger a redeploy for the change to take effect.

---

## 3. Environment Variables Reference

### Backend (Render / Railway / Docker)

| Variable | Required | Description |
|----------|----------|-------------|
| `SPRING_DATASOURCE_URL` | ✅ | Full JDBC connection string |
| `SPRING_DATASOURCE_USERNAME` | ✅ | DB username |
| `SPRING_DATASOURCE_PASSWORD` | ✅ | DB password |
| `JWT_SECRET` | ✅ | Random 32+ char string for signing JWTs |
| `ALLOWED_ORIGINS` | ✅ | Comma-separated frontend URLs for CORS |
| `SPRING_MAIL_USERNAME` | Optional | Gmail address for email notifications |
| `SPRING_MAIL_PASSWORD` | Optional | Gmail App Password |

### Docker Compose only (local)

| Variable | Description |
|----------|-------------|
| `MYSQL_ROOT_PASSWORD` | MySQL root password for the local container |

---

## 4. Production Checklist

- [ ] `environment.prod.ts` points to the live Render backend URL.
- [ ] `JWT_SECRET` is a strong, unique random string — never the default.
- [ ] `ALLOWED_ORIGINS` includes your exact Vercel and/or Render frontend URL.
- [ ] `SPRING_DATASOURCE_*` variables all set correctly in Render dashboard.
- [ ] `spring.jpa.hibernate.ddl-auto` changed from `update` to `validate` after schema stabilises.
- [ ] `spring.jpa.show-sql=false` set for production (avoids log noise).
- [ ] `.env` is gitignored — confirmed by `git status`.
- [ ] All services using HTTPS (Render, Railway, Aiven, Vercel all provide free TLS).
