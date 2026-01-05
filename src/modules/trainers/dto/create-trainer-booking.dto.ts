// src/modules/trainers/dto/create-trainer-booking.dto.ts

import {
  IsString,
  IsNumber,
  IsOptional,
  IsNotEmpty,
  IsDateString,
} from 'class-validator';

export class CreateTrainerBookingDto {
  @IsNotEmpty()
  @IsString()
  sessionType: string;

  @IsNotEmpty()
  @IsString()
  description: string;

  @IsNotEmpty()
  @IsDateString()
  startDateTime: string; // ISO date string

  @IsNotEmpty()
  @IsDateString()
  endDateTime: string; // ISO date string

  @IsNotEmpty()
  @IsNumber()
  duration: number; // Duration in minutes

  @IsNotEmpty()
  @IsNumber()
  totalPrice: number;

  @IsOptional()
  @IsString()
  petId?: string;

  @IsOptional()
  @IsString()
  notes?: string;

  @IsOptional()
  @IsString()
  location?: string;
}
