// src/modules/salons/dto/create-salon.dto.ts

import {
  IsEmail,
  IsNotEmpty,
  IsString,
  IsOptional,
  IsNumber,
  IsArray,
  MinLength,
  IsObject,
} from 'class-validator';

export class CreateSalonDto {
  // User base fields
  @IsEmail()
  email: string;

  @IsNotEmpty()
  @IsString()
  name: string;

  @IsNotEmpty()
  @IsString()
  @MinLength(6)
  password: string;

  @IsOptional()
  @IsString()
  phoneNumber?: string;

  // Salon-specific required fields
  @IsNotEmpty()
  @IsString()
  salonName: string;

  @IsNotEmpty()
  @IsString()
  salonAddress: string;

  // Salon-specific optional fields
  @IsOptional()
  @IsArray()
  @IsString({ each: true })
  services?: string[]; // Array of services: grooming, nailTrimming, bathing, styling, etc.

  @IsOptional()
  @IsNumber()
  yearsOfExperience?: number;

  @IsOptional()
  @IsNumber()
  latitude?: number;

  @IsOptional()
  @IsNumber()
  longitude?: number;

  @IsOptional()
  @IsString()
  bio?: string;

  @IsOptional()
  @IsObject()
  pricing?: Record<string, number>; // Service pricing map
}


