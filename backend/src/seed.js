import 'dotenv/config';
import bcrypt from 'bcryptjs';
import mysql from 'mysql2/promise';

const pool = await mysql.createPool({
  host: process.env.DB_HOST,
  port: process.env.DB_PORT || 3306,
  database: process.env.DB_NAME,
  user: process.env.DB_USER,
  password: process.env.DB_PASSWORD
});

const email = process.env.ADMIN_EMAIL || 'admin@jejaksehat.local';
const password = process.env.ADMIN_PASSWORD || 'Admin12345';
const hash = await bcrypt.hash(password, 10);
await pool.query(
  `INSERT INTO users (nama, email, password_hash, role) VALUES (?, ?, ?, 'ADMIN')
   ON DUPLICATE KEY UPDATE nama=VALUES(nama), password_hash=VALUES(password_hash), role='ADMIN'`,
  ['Administrator JejakSehat', email, hash]
);
console.log(`Admin siap: ${email}`);
await pool.end();
