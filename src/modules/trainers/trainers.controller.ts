// src/modules/trainers/trainers.controller.ts

import {
  Controller,
  Get,
  Post,
  Body,
  Param,
  Put,
  Delete,
  UseGuards,
  HttpCode,
  HttpStatus,
  Request,
} from '@nestjs/common';
import { TrainersService } from './trainers.service';
import { CreateTrainerDto } from './dto/create-trainer.dto';
import { UpdateTrainerDto } from './dto/update-trainer.dto';
import { CreateTrainerBookingDto } from './dto/create-trainer-booking.dto';
import { UpdateTrainerBookingDto } from './dto/update-trainer-booking.dto';
import { JwtAuthGuard } from '../auth/guards/jwt-auth.guard';

@Controller('trainers')
export class TrainersController {
  constructor(private readonly trainersService: TrainersService) {}

  // Trainer Profile Management
  @Post()
  @HttpCode(HttpStatus.CREATED)
  async createTrainer(@Body() createTrainerDto: CreateTrainerDto) {
    return this.trainersService.create(createTrainerDto);
  }

  @Get()
  async getAllTrainers() {
    return this.trainersService.findAll();
  }

  @Get(':id')
  async getTrainer(@Param('id') id: string) {
    return this.trainersService.findOne(id);
  }

  @Put(':id')
  @UseGuards(JwtAuthGuard)
  async updateTrainer(
    @Param('id') id: string,
    @Body() updateTrainerDto: UpdateTrainerDto,
  ) {
    return this.trainersService.update(id, updateTrainerDto);
  }

  @Delete(':id')
  @HttpCode(HttpStatus.NO_CONTENT)
  @UseGuards(JwtAuthGuard)
  async deleteTrainer(@Param('id') id: string) {
    return this.trainersService.remove(id);
  }

  // Convert existing user to trainer
  @Post('convert/:userId')
  @UseGuards(JwtAuthGuard)
  async convertUserToTrainer(
    @Param('userId') userId: string,
    @Body()
    trainerData: Omit<CreateTrainerDto, 'email' | 'name' | 'password'>,
  ) {
    return this.trainersService.convertUserToTrainer(userId, trainerData);
  }

  // Trainer Booking Management
  @Post(':trainerId/bookings')
  @UseGuards(JwtAuthGuard)
  async createBooking(
    @Param('trainerId') trainerId: string,
    @Body() createBookingDto: CreateTrainerBookingDto,
    @Request() req: any,
  ) {
    return this.trainersService.createBooking(
      req.user.sub,
      trainerId,
      createBookingDto,
    );
  }

  @Get(':trainerId/bookings')
  @UseGuards(JwtAuthGuard)
  async getTrainerBookings(@Param('trainerId') trainerId: string) {
    return this.trainersService.findBookingsByTrainer(trainerId);
  }

  @Get('bookings/owner/:ownerId')
  @UseGuards(JwtAuthGuard)
  async getOwnerBookings(@Param('ownerId') ownerId: string) {
    return this.trainersService.findBookingsByOwner(ownerId);
  }

  @Get('bookings/:bookingId')
  @UseGuards(JwtAuthGuard)
  async getBooking(@Param('bookingId') bookingId: string) {
    return this.trainersService.findBooking(bookingId);
  }

  @Put('bookings/:bookingId')
  @UseGuards(JwtAuthGuard)
  async updateBooking(
    @Param('bookingId') bookingId: string,
    @Body() updateBookingDto: UpdateTrainerBookingDto,
  ) {
    return this.trainersService.updateBooking(bookingId, updateBookingDto);
  }

  @Put('bookings/:bookingId/confirm')
  @UseGuards(JwtAuthGuard)
  async confirmBooking(@Param('bookingId') bookingId: string) {
    return this.trainersService.confirmBooking(bookingId);
  }

  @Put('bookings/:bookingId/reject')
  @UseGuards(JwtAuthGuard)
  async rejectBooking(
    @Param('bookingId') bookingId: string,
    @Body() body: { rejectionReason: string },
  ) {
    return this.trainersService.rejectBooking(
      bookingId,
      body.rejectionReason,
    );
  }

  @Put('bookings/:bookingId/cancel')
  @UseGuards(JwtAuthGuard)
  async cancelBooking(
    @Param('bookingId') bookingId: string,
    @Body() body: { cancellationReason: string },
  ) {
    return this.trainersService.cancelBooking(
      bookingId,
      body.cancellationReason,
    );
  }

  @Put('bookings/:bookingId/complete')
  @UseGuards(JwtAuthGuard)
  async completeBooking(@Param('bookingId') bookingId: string) {
    return this.trainersService.completeBooking(bookingId);
  }
}
