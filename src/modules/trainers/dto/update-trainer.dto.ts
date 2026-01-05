// src/modules/trainers/dto/update-trainer.dto.ts

import { PartialType } from '@nestjs/mapped-types';
import { CreateTrainerDto } from './create-trainer.dto';
import { IsOptional, IsBoolean } from 'class-validator';

export class UpdateTrainerDto extends PartialType(CreateTrainerDto) {
  @IsOptional()
  @IsBoolean()
  isAvailable?: boolean;
}
