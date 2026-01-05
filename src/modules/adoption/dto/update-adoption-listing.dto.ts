// src/modules/adoption/dto/update-adoption-listing.dto.ts

import { PartialType } from '@nestjs/mapped-types';
import { CreateAdoptionListingDto } from './create-adoption-listing.dto';
import { IsEnum, IsOptional } from 'class-validator';
import { AdoptionListingStatus } from '../schemas/adoption-listing.schema';

export class UpdateAdoptionListingDto extends PartialType(
  CreateAdoptionListingDto,
) {
  @IsOptional()
  @IsEnum(AdoptionListingStatus)
  status?: AdoptionListingStatus;
}
