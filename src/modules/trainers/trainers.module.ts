// src/modules/trainers/trainers.module.ts

import { Module } from '@nestjs/common';
import { MongooseModule } from '@nestjs/mongoose';
import { TrainersService } from './trainers.service';
import { TrainersController } from './trainers.controller';
import { Trainer, TrainerSchema } from './schemas/trainer.schema';
import {
  TrainerBooking,
  TrainerBookingSchema,
} from './schemas/trainer-booking.schema';
import { User, UserSchema } from '../users/schemas/user.schema';
import { UsersModule } from '../users/users.module';
import { MailModule } from '../mail/mail.module';

@Module({
  imports: [
    MongooseModule.forFeature([
      { name: Trainer.name, schema: TrainerSchema },
      { name: TrainerBooking.name, schema: TrainerBookingSchema },
      { name: User.name, schema: UserSchema },
    ]),
    UsersModule,
    MailModule,
  ],
  controllers: [TrainersController],
  providers: [TrainersService],
  exports: [TrainersService],
})
export class TrainersModule {}
