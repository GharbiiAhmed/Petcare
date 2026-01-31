// src/modules/marketplace/schemas/marketplace-listing.schema.ts

import { Prop, Schema, SchemaFactory } from '@nestjs/mongoose';
import { Document, Types } from 'mongoose';

export type MarketplaceListingDocument = MarketplaceListing & Document;

export enum ListingStatus {
  ACTIVE = 'active',
  SOLD = 'sold',
  ARCHIVED = 'archived',
  PENDING = 'pending',
}

@Schema({ timestamps: true, collection: 'marketplace_listings', strict: true })
export class MarketplaceListing {
  _id: Types.ObjectId;

  // Seller information
  @Prop({ type: Types.ObjectId, ref: 'User', required: true })
  seller: Types.ObjectId;

  /** Contact phone for this listing (for WhatsApp); overrides seller profile phone when set */
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

  @Prop({ required: true })
  price: number; // Price in currency units

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

  // Status and metadata
  @Prop({ enum: Object.values(ListingStatus), default: ListingStatus.ACTIVE })
  status: ListingStatus;

  @Prop()
  soldDate?: Date;

  @Prop({ type: Types.ObjectId, ref: 'User' })
  buyer?: Types.ObjectId; // Reference to buyer when sold

  @Prop({ default: 0 })
  views?: number;

  @Prop({ default: 0 })
  likes?: number;

  @Prop({ type: [Types.ObjectId], default: [] })
  likedBy?: Types.ObjectId[];
}

export const MarketplaceListingSchema =
  SchemaFactory.createForClass(MarketplaceListing);
