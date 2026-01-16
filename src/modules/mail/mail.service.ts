// src/mail/mail.service.ts
import { Injectable, InternalServerErrorException } from '@nestjs/common';
import * as nodemailer from 'nodemailer';

@Injectable()
export class MailService {
  private transporter: nodemailer.Transporter;

  constructor() {
    // SMTP Configuration
    const smtpHost = process.env.SMTP_HOST;
    const smtpPort = parseInt(process.env.SMTP_PORT || '587', 10);
    const smtpUser = process.env.SMTP_USER;
    const smtpPassword = process.env.SMTP_PASSWORD;
    const smtpSecure = process.env.SMTP_SECURE === 'true'; // true for 465, false for other ports

    if (!smtpHost || !smtpUser || !smtpPassword) {
      throw new Error(
        'SMTP configuration is incomplete. Please set SMTP_HOST, SMTP_USER, and SMTP_PASSWORD environment variables.',
      );
    }

    this.transporter = nodemailer.createTransport({
      host: smtpHost,
      port: smtpPort,
      secure: smtpSecure, // true for 465, false for other ports
      auth: {
        user: smtpUser,
        pass: smtpPassword,
      },
      // For Gmail and some providers, you may need to set:
      // tls: {
      //   rejectUnauthorized: false
      // }
    });
  }

  async sendVerificationCode(email: string, code: string): Promise<void> {
    const from = process.env.MAIL_FROM;
    if (!from) {
      throw new Error('MAIL_FROM is not defined');
    }

    const mailOptions = {
      from: from,
      to: email,
      subject: 'Verify your email address',
      html: `
        <h2>Your verification code</h2>
        <p>Use this code to verify your email:</p>
        <h1 style="font-size: 32px; letter-spacing: 4px;">${code}</h1>
        <p>This code will expire in 10 minutes.</p>
      `,
    };

    try {
      const info = await this.transporter.sendMail(mailOptions);
      console.log(`Verification email sent to ${email}`, info.messageId);
    } catch (err) {
      console.error('Failed to send verification email:', err);
      throw new InternalServerErrorException(
        'Could not send verification email. Please try again later.',
      );
    }
  }
}
