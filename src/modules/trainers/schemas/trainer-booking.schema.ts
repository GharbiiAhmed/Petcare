// src/modules/trainers/schemas/trainer-booking.schema.ts

import { Prop, Schema, SchemaFactory } from '@nestjs/mongoose';
import { Document, Types } from 'mongoose';

export type TrainerBookingDocument = TrainerBooking & Document;

export enum BookingStatus {
  PENDING = 'pending',
  CONFIRMED = 'confirmed',
  COMPLETED = 'completed',
  CANCELLED = 'cancelled',
  REJECTED = 'rejected',
}

@Schema({ timestamps: true, collection: 'trainer_bookings', strict: true })
export class TrainerBooking {
  _id: Types.ObjectId;

  @Prop({ type: Types.ObjectId, ref: 'User', required: true })
  owner: Types.ObjectId; // Pet owner

  @Prop({ type: Types.ObjectId, ref: 'Trainer', required: true })
  trainer: Types.ObjectId; // The trainer being booked

  @Prop({ type: Types.ObjectId, ref: 'Pet' })
  pet?: Types.ObjectId; // Specific pet for training

  @Prop({ required: true })
  sessionType: string; // e.g., "One-on-one", "Group"

  @Prop({ required: true })
  description: string; // Description of training needs

  @Prop({ required: true })
  startDateTime: Date; // Session start date/time

  @Prop({ required: true })
  endDateTime: Date; // Session end date/time

  @Prop({ required: true })
  duration: number; // Duration in minutes

  @Prop({ required: true })
  totalPrice: number; // Total price for the session

  @Prop({ enum: Object.values(BookingStatus), default: BookingStatus.PENDING })
  status: BookingStatus;

  @Prop()
  notes?: string; // Additional notes from owner

  @Prop()
  location?: string; // Session location

  @Prop()
  rejectionReason?: string; // If rejected, reason for rejection

  @Prop()
  cancelledAt?: Date;

  @Prop()
  completedAt?: Date;

  @Prop()
  cancellationReason?: string;
}

export const TrainerBookingSchema = SchemaFactory.createForClass(TrainerBooking);
