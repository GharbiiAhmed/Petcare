// src/modules/adoption/adoption.module.ts

import { Module } from '@nestjs/common';
import { MongooseModule } from '@nestjs/mongoose';
import { AdoptionService } from './adoption.service';
import { AdoptionController } from './adoption.controller';
import {
  AdoptionListing,
  AdoptionListingSchema,
} from './schemas/adoption-listing.schema';
import {
  AdoptionApplication,
  AdoptionApplicationSchema,
} from './schemas/adoption-application.schema';
import { User, UserSchema } from '../users/schemas/user.schema';

@Module({
  imports: [
    MongooseModule.forFeature([
      { name: AdoptionListing.name, schema: AdoptionListingSchema },
      { name: AdoptionApplication.name, schema: AdoptionApplicationSchema },
      { name: User.name, schema: UserSchema },
    ]),
  ],
  controllers: [AdoptionController],
  providers: [AdoptionService],
  exports: [AdoptionService],
})
export class AdoptionModule {}
