import express from 'express';
import { PrismaClient } from '@prisma/client';
import bcrypt from 'bcrypt';
import jwt from 'jsonwebtoken';
import { z } from 'zod';

const router = express.Router();
const prisma = new PrismaClient();

const loginSchema = z.object({
  email: z.string().email(),
  password: z.string().min(8),
});

router.post('/login', async (req, res) => {
  try {
    const { email, password } = loginSchema.parse(req.body);
    const user = await prisma.user.findUnique({ where: { email }, include: { role: true } });
    if (!user) return res.status(401).json({ error: 'Invalid credentials' });
    const valid = await bcrypt.compare(password, user.password);
    if (!valid) return res.status(401).json({ error: 'Invalid credentials' });
    const token = jwt.sign(
      { userId: user.id, role: user.role.name },
      process.env.JWT_SECRET || 'secret',
      { expiresIn: '8h' }
    );
    // Audit log
    await prisma.auditLog.create({
      data: { action: 'LOGIN', userId: user.id, details: `User ${user.email} logged in` },
    });
    res.json({ token, user: { id: user.id, email: user.email, role: user.role.name, name: user.name } });
  } catch (e: any) {
    res.status(400).json({ error: e.errors ? e.errors : 'Invalid request' });
  }
});

export default router;