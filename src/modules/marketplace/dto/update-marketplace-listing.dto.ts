// src/modules/marketplace/dto/update-marketplace-listing.dto.ts

import { PartialType } from '@nestjs/mapped-types';
import { CreateMarketplaceListingDto } from './create-marketplace-listing.dto';
import { IsEnum, IsOptional } from 'class-validator';
import { ListingStatus } from '../schemas/marketplace-listing.schema';

export class UpdateMarketplaceListingDto extends PartialType(
  CreateMarketplaceListingDto,
) {
  @IsOptional()
  @IsEnum(ListingStatus)
  status?: ListingStatus;
}
