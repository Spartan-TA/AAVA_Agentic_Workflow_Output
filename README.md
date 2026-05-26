# dog.rc â Secure Hotel Room Booking Portal

## Overview
A secure, scalable, and compliant full-stack web application for hotel room bookings. Built with Next.js, Vite, TypeScript, shadcn UI, TailwindCSS, Zustand, React Router DOM, Node.js/Express, PostgreSQL, and Prisma ORM.

---

## Features
- User Authentication (JWT, bcrypt, RBAC)
- Room Booking CRUD
- Role-Based Access Control (RBAC)
- Audit Logging
- Input Validation (zod)
- Responsive UI (shadcn UI, TailwindCSS)
- State Management (Zustand)
- RESTful API
- Secrets Management and Environment Variables
- Docker Compose for deployment

---

## Setup & Deployment

### Prerequisites
- Docker & Docker Compose
- Node.js >= 18 (for local dev)

### Quickstart (Docker Compose)
1. Clone repository
2. Run: `docker-compose up --build`
3. Backend: http://localhost:4000
4. Frontend: http://localhost:3000
5. PostgreSQL: localhost:5432 (user: dogrc, pass: supersecurepassword, db: dogrcdb)

### Local Development
- `cd backend && npm install && npx prisma migrate dev`
- `cd frontend && npm install && npm run dev`

---

## Security & Compliance
- **Authentication:** JWT, bcrypt for password hashing
- **RBAC:** Enforced in backend routes
- **Input Validation:** zod for all endpoints
- **Audit Logging:** All auth and booking actions are logged
- **Secrets:** Use `.env` files and Docker secrets for production
- **CORS & Helmet:** Enabled by default
- **Database:** Prisma migrations ensure referential integrity
- **Encryption:** Passwords are always hashed
- **Compliance:** Designed for GDPR/PCI-DSS readiness (no plaintext sensitive data)

---

## Project Structure

- `frontend/`
  - `src/components/Login.tsx`
  - `src/components/BookingDashboard.tsx`
  - `src/store/bookingStore.ts`
- `backend/`
  - `src/index.ts`
  - `src/routes/auth.ts`
  - `src/routes/booking.ts`
  - `prisma/schema.prisma`
- `docker-compose.yml`
- `README.md`

---

## Extending & Customizing
- Add more roles (e.g., manager) in `Role` model
- Extend booking logic for room availability
- Add user registration and password reset
- Integrate email/SMS notifications

---

## License
MIT
