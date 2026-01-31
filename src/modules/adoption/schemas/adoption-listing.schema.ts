// src/modules/adoption/schemas/adoption-listing.schema.ts

import { Prop, Schema, SchemaFactory } from '@nestjs/mongoose';
import { Document, Types } from 'mongoose';

export type AdoptionListingDocument = AdoptionListing & Document;

export enum AdoptionListingStatus {
  AVAILABLE = 'available',
  ADOPTED = 'adopted',
  ARCHIVED = 'archived',
  PENDING = 'pending',
}

@Schema({ timestamps: true, collection: 'adoption_listings', strict: true })
export class AdoptionListing {
  _id: Types.ObjectId;

  // Rescuer/Shelter information
  @Prop({ type: Types.ObjectId, ref: 'User', required: true })
  rescuer: Types.ObjectId;

  /** Contact phone for this listing (for WhatsApp); overrides rescuer profile phone when set */
  @Prop()
  contactPhone?: string;

  // Pet information
  @Prop({ required: true })
  petName: string;

  @Prop({ required: true })
  species: string; // e.g., "Dog", "Cat"

  @Prop()
  breed?: string;

  @Prop()
  age?: number; // Age in months

  @Prop()
  color?: string;

  @Prop()
  weight?: number; // Weight in kg

  @Prop()
  gender?: string; // "male", "female"

  @Prop()
  description?: string; // Detailed description

  @Prop({ type: [String], default: [] })
  images?: string[]; // Cloudinary image URLs

  @Prop()
  location?: string;

  @Prop()
  latitude?: number;

  @Prop()
  longitude?: number;

  // Medical & behavior info
  @Prop()
  vaccinated?: boolean;

  @Prop()
  neutered?: boolean;

  @Prop()
  medicalHistory?: string;

  @Prop({ type: [String], default: [] })
  traits?: string[]; // e.g., "friendly", "energetic", "calm"

  @Prop()
  adoptionRequirements?: string; // Special requirements for adoption

  // Status and metadata
  @Prop({
    enum: Object.values(AdoptionListingStatus),
    default: AdoptionListingStatus.AVAILABLE,
  })
  status: AdoptionListingStatus;

  @Prop()
  adoptedDate?: Date;

  @Prop({ type: Types.ObjectId, ref: 'User' })
  adoptedBy?: Types.ObjectId; // Reference to adopter

  @Prop({ default: 0 })
  views?: number;

  @Prop({ default: 0 })
  likes?: number;

  @Prop({ type: [Types.ObjectId], default: [] })
  likedBy?: Types.ObjectId[];

  @Prop()
  adoptionAgreement?: string; // URL to adoption agreement document
}

export const AdoptionListingSchema =
  SchemaFactory.createForClass(AdoptionListing);
