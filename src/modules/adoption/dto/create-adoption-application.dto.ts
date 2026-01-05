// src/modules/adoption/dto/create-adoption-application.dto.ts

import {
  IsString,
  IsOptional,
  IsNotEmpty,
  IsPhoneNumber,
} from 'class-validator';

export class CreateAdoptionApplicationDto {
  @IsOptional()
  @IsPhoneNumber()
  phoneNumber?: string;

  @IsOptional()
  @IsString()
  address?: string;

  @IsOptional()
  @IsString()
  housingType?: string;

  @IsOptional()
  @IsString()
  ownRent?: string;

  @IsOptional()
  @IsString()
  otherPets?: string;

  @IsOptional()
  @IsString()
  familyDescription?: string;

  @IsOptional()
  @IsString()
  workSchedule?: string;

  @IsOptional()
  @IsString()
  motivation?: string;

  @IsOptional()
  @IsString()
  experience?: string;

  @IsOptional()
  @IsString()
  vetReference?: string;

  @IsOptional()
  @IsString()
  additionalInfo?: string;
}
