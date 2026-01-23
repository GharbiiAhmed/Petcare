// src/modules/salons/schemas/salon.schema.ts

import { Prop, Schema, SchemaFactory } from '@nestjs/mongoose';
import { Document, Types } from 'mongoose';

export type SalonDocument = Salon & Document;

@Schema({ timestamps: true, collection: 'salons', strict: true })
export class Salon {
  _id: Types.ObjectId;

  // Reference to User
  @Prop({ type: Types.ObjectId, ref: 'User', required: true, unique: true })
  user: Types.ObjectId;

  // Salon-specific required fields
  @Prop({ required: true })
  salonName: string;

  @Prop({ required: true })
  salonAddress: string;

  // Salon-specific optional fields
  @Prop({ type: [String], default: [] })
  services?: string[]; // Array of services: grooming, nailTrimming, bathing, styling, etc.

  @Prop()
  yearsOfExperience?: number;

  @Prop()
  latitude?: number;

  @Prop()
  longitude?: number;

  @Prop()
  bio?: string;

  @Prop({ type: Map, of: Number })
  pricing?: Map<string, number>; // Service pricing map, e.g., { "grooming": 50, "nailTrimming": 20 }
}

export const SalonSchema = SchemaFactory.createForClass(Salon);

// Prevent email field from being added
SalonSchema.pre('save', function (next) {
  if (this.isNew && (this as any).email !== undefined) {
    delete (this as any).email;
  }
  next();
});


