// src/modules/trainers/schemas/trainer.schema.ts

import { Prop, Schema, SchemaFactory } from '@nestjs/mongoose';
import { Document, Types } from 'mongoose';

export type TrainerDocument = Trainer & Document;

@Schema({ timestamps: true, collection: 'trainers', strict: true })
export class Trainer {
  _id: Types.ObjectId;

  // Reference to User (trainer profile)
  @Prop({ type: Types.ObjectId, ref: 'User', required: true, unique: true })
  user: Types.ObjectId;

  // Training-specific fields
  @Prop({ required: true })
  specialization: string; // e.g., "Obedience", "Agility", "Behavioral"

  @Prop({ required: true })
  hourlyRate: number; // Price per hour

  @Prop()
  yearsOfExperience: number; // Years of experience

  @Prop({ type: [String], default: [] })
  certifications?: string[]; // List of certifications

  @Prop()
  bio?: string; // Bio/description

  @Prop()
  location?: string; // Training location

  @Prop()
  latitude?: number;

  @Prop()
  longitude?: number;

  @Prop({ default: true })
  isAvailable?: boolean; // Whether accepting new clients

  @Prop({ type: [String], default: [] })
  trainingMethods?: string[]; // e.g., "Positive reinforcement", "Clicker training"

  @Prop()
  profileImage?: string; // Cloudinary image URL

  @Prop({ type: [Types.ObjectId], ref: 'TrainerBooking', default: [] })
  bookings?: Types.ObjectId[]; // List of booking IDs

  @Prop({ default: 0 })
  averageRating?: number; // Average rating from reviews

  @Prop({ default: 0 })
  totalReviews?: number; // Total number of reviews

  // Contact information
  @Prop()
  phoneNumber?: string;

  @Prop()
  email?: string; // Can be different from user email if trainer operates from business email

  // Rating & Reviews will be stored in separate schema
}

export const TrainerSchema = SchemaFactory.createForClass(Trainer);
