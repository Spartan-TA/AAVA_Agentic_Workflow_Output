import express from 'express';
import { PrismaClient } from '@prisma/client';
import { z } from 'zod';
import jwt from 'jsonwebtoken';

const router = express.Router();
const prisma = new PrismaClient();

const bookingSchema = z.object({
  room: z.string().min(1),
  guestName: z.string().min(1),
  checkIn: z.string().refine(d => !isNaN(Date.parse(d))),
  checkOut: z.string().refine(d => !isNaN(Date.parse(d))),
  status: z.string().min(1),
});

// Middleware for JWT and RBAC
function auth(requiredRoles: string[] = []) {
  return (req, res, next) => {
    const header = req.headers['authorization'];
    if (!header) return res.status(401).json({ error: 'Missing token' });
    const token = header.split(' ')[1];
    try {
      const payload = jwt.verify(token, process.env.JWT_SECRET || 'secret') as any;
      if (requiredRoles.length && !requiredRoles.includes(payload.role)) {
        return res.status(403).json({ error: 'Forbidden' });
      }
      req.user = payload;
      next();
    } catch {
      return res.status(401).json({ error: 'Invalid token' });
    }
  };
}

// List bookings (RBAC: user or admin)
router.get('/', auth(['user', 'admin']), async (req, res) => {
  const bookings = await prisma.booking.findMany();
  res.json(bookings);
});

// Create booking (RBAC: user)
router.post('/', auth(['user']), async (req, res) => {
  try {
    const data = bookingSchema.parse(req.body);
    const booking = await prisma.booking.create({
      data: {
        ...data,
        userId: req.user.userId,
      },
    });
    await prisma.auditLog.create({
      data: {
        action: 'CREATE_BOOKING',
        userId: req.user.userId,
        details: `Booking for room ${data.room}`,
      },
    });
    res.json(booking);
  } catch (e: any) {
    res.status(400).json({ error: e.errors ? e.errors : 'Invalid request' });
  }
});

export default router;