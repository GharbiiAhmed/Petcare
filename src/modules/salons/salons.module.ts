// src/modules/salons/salons.module.ts

import { Module } from '@nestjs/common';
import { MongooseModule } from '@nestjs/mongoose';
import { SalonsService } from './salons.service';
import { SalonsController } from './salons.controller';
import { User, UserSchema } from '../users/schemas/user.schema';
import { Salon, SalonSchema } from './schemas/salon.schema';
import { UsersModule } from '../users/users.module';
import { MailModule } from '../mail/mail.module';

@Module({
  imports: [
    MongooseModule.forFeature([
      { name: User.name, schema: UserSchema },
      { name: Salon.name, schema: SalonSchema },
    ]),
    UsersModule,
    MailModule,
  ],
  controllers: [SalonsController],
  providers: [SalonsService],
  exports: [SalonsService],
})
export class SalonsModule {}


