# Team Task Manager

Full-stack project/task tracker with role-based access control.
Backend: Spring Boot REST API + JWT + two-tier RBAC. Frontend: React (Vite).
Built for two-deployment hosting: **Railway** (backend + MySQL) and **Vercel** (frontend).

---

## 1. How the RBAC model works

There are two role layers, and they answer two different questions:

| Layer | Where it lives | Question it answers |
|---|---|---|
| Global `Role` (ADMIN/MEMBER) | `User.role`, inside the JWT | Who is this person, account-wide? (currently informational; every signup defaults to MEMBER) |
| Project `ProjectRole` (ADMIN/MEMBER) | `ProjectMember.role`, looked up fresh from the DB on every request | Can this person manage *this specific* project? |

Whoever creates a project automatically becomes that project's **ADMIN**. Project admins can add members and create/assign tasks. Any project member can update the status of a task **if they're the assignee or a project admin** — that specific check happens in the service layer (`TaskService.updateStatus`), not in `@PreAuthorize`, since "is this your task" needs a data lookup, not just a role check.

Project-level role is deliberately **not** baked into the JWT — it's looked up on every request via `ProjectMemberRepository`, so removing someone from a project takes effect immediately instead of waiting for their token to expire.

---

## 2. Folder structure

```
team-task-manager/
├── backend/                 Spring Boot REST API
│   ├── pom.xml
│   └── src/main/java/com/sanskar/taskmanager/
│       ├── entity/           User, Project, ProjectMember, Task + enums
│       ├── repository/       Spring Data JPA interfaces
│       ├── dto/request/      Request bodies
│       ├── dto/response/     Response shapes
│       ├── security/         JwtUtil, JwtAuthFilter, SecurityConfig, ProjectSecurityService
│       ├── config/           CorsConfig
│       ├── service/          Business logic + RBAC checks
│       ├── controller/       REST endpoints
│       └── exception/        Global error handling
│
└── frontend/                 React (Vite) — plain fetch, no axios
    └── src/
        ├── api/apiClient.js   fetch wrapper: attaches JWT, redirects on 401
        ├── context/           AuthContext (login/signup/logout)
        ├── components/        Navbar, ProjectCard, TaskCard, ProtectedRoute
        └── pages/              LoginPage, SignupPage, Dashboard, ProjectView
```

---

## 3. Running it locally

### Backend

You need Java 17+, Maven, and a MySQL instance (local, or just point at your Railway MySQL credentials directly for local dev too).

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

By default it reads `localhost:3306/taskmanager` with `root/root` (see `application.properties`). Override any of these with environment variables — `MYSQLHOST`, `MYSQLPORT`, `MYSQLDATABASE`, `MYSQLUSER`, `MYSQLPASSWORD`, `JWT_SECRET`, `CORS_ALLOWED_ORIGINS` — instead of editing the file.

**Note:** I wrote and manually reviewed every file (checked imports, DTO constructor argument order, cross-file method references, brace balance) but could not run `mvn compile` in my own sandbox — Maven Central is network-blocked there. Run `mvn clean install` yourself as the first real compile check; if anything surfaces, it'll almost certainly be a small import/typo fix.

### Frontend

Needs Node 18+.

```bash
cd frontend
npm install
npm run dev
```

Opens at `http://localhost:5173`, already pointed at `http://localhost:8080/api` via `.env`. This part **is** verified — `npm run build` and `npm run lint` both ran clean in my sandbox.

---

## 4. API reference

| Method | Endpoint | Access | Purpose |
|---|---|---|---|
| POST | `/api/auth/signup` | public | Create account, returns JWT |
| POST | `/api/auth/login` | public | Returns JWT |
| GET | `/api/projects` | authenticated | List projects you're a member of |
| POST | `/api/projects` | authenticated | Create project (you become its ADMIN) |
| GET | `/api/projects/{id}` | project member | Project detail |
| GET | `/api/projects/{id}/members` | project member | List members |
| POST | `/api/projects/{id}/members` | project ADMIN | Add a member by email |
| GET | `/api/projects/{id}/tasks` | project member | List tasks |
| POST | `/api/projects/{id}/tasks` | project ADMIN | Create + assign a task |
| PATCH | `/api/projects/{id}/tasks/{taskId}/status` | assignee or project ADMIN | Update status |

All authenticated requests need `Authorization: Bearer <token>`.

---

## 5. Deploying (2-deployment setup)

### Backend → Railway

1. Push `backend/` to GitHub, create a new Railway project from that repo.
2. Add a MySQL plugin in the same Railway project.
3. Set these variables on the **backend service** (not the MySQL one): `JWT_SECRET` (long random string, 32+ chars), `CORS_ALLOWED_ORIGINS` (your Vercel URL, added after step 4). Railway auto-injects the MySQL host/port/user/password/database vars — check the MySQL plugin's "Variables" tab and confirm the names match `application.properties` (`MYSQLHOST`, `MYSQLPORT`, etc.); rename in the properties file if Railway's current plugin uses different names.
4. Deploy. Note the generated public URL (e.g. `https://xxxx.up.railway.app`).

### Frontend → Vercel

1. Push `frontend/` to GitHub (or a subfolder of the same repo — set Vercel's root directory accordingly).
2. Import into Vercel.
3. Set environment variable `VITE_API_BASE_URL` = `https://xxxx.up.railway.app/api` (your Railway URL from above).
4. Deploy. Go back to Railway and set `CORS_ALLOWED_ORIGINS` to this Vercel URL, then redeploy the backend.

---

## 6. Demo flow (for your submission video)

1. Sign up as two different users (e.g. you as admin, a teammate email as member).
2. Log in as user 1 → create a project → you're now its admin.
3. Add user 2 as a member.
4. Create a task, assign it to user 2, set a due date in the past (to show the overdue flag).
5. Log out, log in as user 2 → open the project → update the task's status.
6. Log back in as user 1 to show the dashboard task/member counts updated.
