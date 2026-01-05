// src/modules/adoption/dto/update-adoption-application.dto.ts

import { IsEnum, IsOptional, IsString } from 'class-validator';
import { ApplicationStatus } from '../schemas/adoption-application.schema';

export class UpdateAdoptionApplicationDto {
  @IsOptional()
  @IsEnum(ApplicationStatus)
  status?: ApplicationStatus;

  @IsOptional()
  @IsString()
  rejectionReason?: string;
}
