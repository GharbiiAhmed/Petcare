// src/modules/marketplace/dto/create-marketplace-inquiry.dto.ts

import {
  IsString,
  IsOptional,
  IsNotEmpty,
  IsEmail,
  IsPhoneNumber,
} from 'class-validator';

export class CreateMarketplaceInquiryDto {
  @IsOptional()
  @IsString()
  message?: string;

  @IsOptional()
  @IsPhoneNumber()
  phoneNumber?: string;

  @IsOptional()
  @IsEmail()
  email?: string;
}
