// src/modules/marketplace/dto/update-marketplace-inquiry.dto.ts

import { IsEnum, IsOptional, IsString } from 'class-validator';
import { InquiryStatus } from '../schemas/marketplace-inquiry.schema';

export class UpdateMarketplaceInquiryDto {
  @IsOptional()
  @IsEnum(InquiryStatus)
  status?: InquiryStatus;

  @IsOptional()
  @IsString()
  rejectionReason?: string;
}
