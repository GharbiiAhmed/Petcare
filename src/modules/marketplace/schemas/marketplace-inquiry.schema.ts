// src/modules/marketplace/schemas/marketplace-inquiry.schema.ts

import { Prop, Schema, SchemaFactory } from '@nestjs/mongoose';
import { Document, Types } from 'mongoose';

export type MarketplaceInquiryDocument = MarketplaceInquiry & Document;

export enum InquiryStatus {
  PENDING = 'pending',
  ACCEPTED = 'accepted',
  REJECTED = 'rejected',
  COMPLETED = 'completed',
}

@Schema({ timestamps: true, collection: 'marketplace_inquiries', strict: true })
export class MarketplaceInquiry {
  _id: Types.ObjectId;

  @Prop({ type: Types.ObjectId, ref: 'User', required: true })
  buyer: Types.ObjectId;

  @Prop({
    type: Types.ObjectId,
    ref: 'MarketplaceListing',
    required: true,
  })
  listing: Types.ObjectId;

  @Prop()
  message?: string;

  @Prop()
  phoneNumber?: string;

  @Prop()
  email?: string;

  @Prop({ enum: Object.values(InquiryStatus), default: InquiryStatus.PENDING })
  status: InquiryStatus;

  @Prop()
  rejectionReason?: string;

  @Prop()
  completedAt?: Date;
}

export const MarketplaceInquirySchema =
  SchemaFactory.createForClass(MarketplaceInquiry);
