// src/modules/marketplace/marketplace.module.ts

import { Module } from '@nestjs/common';
import { MongooseModule } from '@nestjs/mongoose';
import { MarketplaceService } from './marketplace.service';
import { MarketplaceController } from './marketplace.controller';
import {
  MarketplaceListing,
  MarketplaceListingSchema,
} from './schemas/marketplace-listing.schema';
import {
  MarketplaceInquiry,
  MarketplaceInquirySchema,
} from './schemas/marketplace-inquiry.schema';
import { User, UserSchema } from '../users/schemas/user.schema';

@Module({
  imports: [
    MongooseModule.forFeature([
      { name: MarketplaceListing.name, schema: MarketplaceListingSchema },
      { name: MarketplaceInquiry.name, schema: MarketplaceInquirySchema },
      { name: User.name, schema: UserSchema },
    ]),
  ],
  controllers: [MarketplaceController],
  providers: [MarketplaceService],
  exports: [MarketplaceService],
})
export class MarketplaceModule {}
