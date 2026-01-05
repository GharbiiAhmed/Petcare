// src/modules/trainers/dto/update-trainer-booking.dto.ts

import {
  IsString,
  IsOptional,
  IsEnum,
  IsDateString,
} from 'class-validator';
import { BookingStatus } from '../schemas/trainer-booking.schema';

export class UpdateTrainerBookingDto {
  @IsOptional()
  @IsEnum(BookingStatus)
  status?: BookingStatus;

  @IsOptional()
  @IsString()
  rejectionReason?: string;

  @IsOptional()
  @IsDateString()
  cancelledAt?: string;

  @IsOptional()
  @IsString()
  cancellationReason?: string;
}
