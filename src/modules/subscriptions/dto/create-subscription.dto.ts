// src/modules/subscriptions/dto/create-subscription.dto.ts

import { IsString, IsEnum, IsOptional } from 'class-validator';

export class CreateSubscriptionDto {
  @IsOptional()
  @IsEnum(['vet', 'sitter', 'trainer', 'premium'])
  role?: 'vet' | 'sitter' | 'trainer' | 'premium'; // Optional - user chooses after payment

  @IsOptional()
  @IsString()
  paymentMethodId?: string;
}

