// src/modules/adoption/schemas/adoption-application.schema.ts

import { Prop, Schema, SchemaFactory } from '@nestjs/mongoose';
import { Document, Types } from 'mongoose';

export type AdoptionApplicationDocument = AdoptionApplication & Document;

export enum ApplicationStatus {
  PENDING = 'pending',
  APPROVED = 'approved',
  REJECTED = 'rejected',
  COMPLETED = 'completed',
}

@Schema({ timestamps: true, collection: 'adoption_applications', strict: true })
export class AdoptionApplication {
  _id: Types.ObjectId;

  @Prop({ type: Types.ObjectId, ref: 'User', required: true })
  applicant: Types.ObjectId;

  @Prop({
    type: Types.ObjectId,
    ref: 'AdoptionListing',
    required: true,
  })
  listing: Types.ObjectId;

  // Applicant information
  @Prop()
  phoneNumber?: string;

  @Prop()
  address?: string;

  @Prop()
  housingType?: string; // "apartment", "house", "farm", etc.

  @Prop()
  ownRent?: string; // "own", "rent"

  @Prop()
  otherPets?: string; // Description of other pets

  @Prop()
  familyDescription?: string; // Number of family members, ages

  @Prop()
  workSchedule?: string; // Work schedule/hours

  @Prop()
  motivation?: string; // Why they want to adopt

  @Prop()
  experience?: string; // Experience with pets

  @Prop()
  vetReference?: string; // Veterinarian reference

  @Prop()
  additionalInfo?: string; // Any additional info

  @Prop({ enum: Object.values(ApplicationStatus), default: ApplicationStatus.PENDING })
  status: ApplicationStatus;

  @Prop()
  rejectionReason?: string;

  @Prop()
  approvedAt?: Date;

  @Prop()
  rejectedAt?: Date;

  @Prop()
  completedAt?: Date;
}

export const AdoptionApplicationSchema = SchemaFactory.createForClass(
  AdoptionApplication,
);
