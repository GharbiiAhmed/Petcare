// src/modules/salons/dto/update-salon.dto.ts

import {
  IsOptional,
  IsString,
  IsNumber,
  IsArray,
  IsEmail,
  IsObject,
} from 'class-validator';

export class UpdateSalonDto {
  @IsOptional()
  @IsEmail()
  email?: string;

  @IsOptional()
  @IsString()
  name?: string;

  @IsOptional()
  @IsString()
  phoneNumber?: string;

  @IsOptional()
  @IsString()
  country?: string;

  @IsOptional()
  @IsString()
  city?: string;

  @IsOptional()
  @IsString()
  salonName?: string;

  @IsOptional()
  @IsString()
  salonAddress?: string;

  @IsOptional()
  @IsArray()
  @IsString({ each: true })
  services?: string[];

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
  pricing?: Record<string, number>;
}


