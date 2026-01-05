// src/app.module.ts
import { Module } from '@nestjs/common';
import { AppController } from './app.controller';
import { AppService } from './app.service';
import { UsersModule } from './modules/users/users.module';
import { AuthModule } from './modules/auth/auth.module';
import { PetsModule } from './modules/pets/pets.module';
import { VeterinariansModule } from './modules/veterinarians/veterinarians.module';
import { PetSittersModule } from './modules/pet-sitters/pet-sitters.module';
import { MongooseModule } from '@nestjs/mongoose';
import { ConfigModule } from '@nestjs/config';
import { MailModule } from './modules/mail/mail.module';
import { MessagesModule } from './modules/messages/messages.module';
import { BookingsModule } from './modules/bookings/bookings.module';
import { NotificationsModule } from './modules/notifications/notifications.module';
import { AiModule } from './modules/ai/ai.module';
import { FcmModule } from './modules/fcm/fcm.module';
import { ChatbotModule } from './modules/chatbot/chatbot.module';
import { CommunityModule } from './modules/community/community.module';
import { TrainersModule } from './modules/trainers/trainers.module';
import { MarketplaceModule } from './modules/marketplace/marketplace.module';
import { AdoptionModule } from './modules/adoption/adoption.module';
import { AdminModule } from './modules/admin/admin.module';
import { SubscriptionsModule } from './modules/subscriptions/subscriptions.module';

@Module({
  imports: [
    ConfigModule.forRoot({ isGlobal: true }),
    MongooseModule.forRoot(process.env.MONGO_URI || 'mongodb://localhost/petcare'),
    UsersModule,
    AuthModule,
    PetsModule,
    VeterinariansModule,
    PetSittersModule,
    MailModule,
    MessagesModule,
    NotificationsModule,
    BookingsModule,
    AiModule,
    FcmModule,
    ChatbotModule,
    CommunityModule,
    TrainersModule,
    MarketplaceModule,
    AdoptionModule,
    SubscriptionsModule,
    AdminModule,
  ],
  controllers: [AppController],
  providers: [AppService],
})
export class AppModule {}
