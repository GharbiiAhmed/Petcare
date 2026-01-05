// src/modules/trainers/trainers.service.ts

import {
  Injectable,
  NotFoundException,
  ConflictException,
} from '@nestjs/common';
import { InjectModel } from '@nestjs/mongoose';
import { Model, Types } from 'mongoose';
import { Trainer, TrainerDocument } from './schemas/trainer.schema';
import {
  TrainerBooking,
  TrainerBookingDocument,
  BookingStatus,
} from './schemas/trainer-booking.schema';
import { User, UserDocument } from '../users/schemas/user.schema';
import { CreateTrainerDto } from './dto/create-trainer.dto';
import { UpdateTrainerDto } from './dto/update-trainer.dto';
import { CreateTrainerBookingDto } from './dto/create-trainer-booking.dto';
import { UpdateTrainerBookingDto } from './dto/update-trainer-booking.dto';
import { UsersService } from '../users/users.service';
import { MailService } from '../mail/mail.service';
import * as bcrypt from 'bcrypt';

@Injectable()
export class TrainersService {
  constructor(
    @InjectModel(User.name)
    private readonly userModel: Model<UserDocument>,
    @InjectModel(Trainer.name)
    private readonly trainerModel: Model<TrainerDocument>,
    @InjectModel(TrainerBooking.name)
    private readonly trainerBookingModel: Model<TrainerBookingDocument>,
    private readonly usersService: UsersService,
    private readonly mailService: MailService,
  ) {}

  // Trainer management
  async create(createTrainerDto: CreateTrainerDto): Promise<UserDocument> {
    const existingUser = await this.usersService.findByEmail(
      createTrainerDto.email,
    );
    if (existingUser) {
      throw new ConflictException('User with this email already exists');
    }

    const hashedPassword = await bcrypt.hash('defaultPassword123', 10);

    const trainerData = {
      email: createTrainerDto.email,
      name: createTrainerDto.specialization + ' Trainer',
      password: hashedPassword,
      role: 'trainer',
      isVerified: false,
      balance: 0,
    };

    const createdUser = await new this.userModel(trainerData).save();

    // Create trainer record
    const trainer = new this.trainerModel({
      user: createdUser._id,
      specialization: createTrainerDto.specialization,
      hourlyRate: createTrainerDto.hourlyRate,
      yearsOfExperience: createTrainerDto.yearsOfExperience,
      certifications: createTrainerDto.certifications || [],
      bio: createTrainerDto.bio,
      location: createTrainerDto.location,
      latitude: createTrainerDto.latitude,
      longitude: createTrainerDto.longitude,
      trainingMethods: createTrainerDto.trainingMethods || [],
      phoneNumber: createTrainerDto.phoneNumber,
      email: createTrainerDto.email,
      isAvailable: true,
    });

    await trainer.save();

    return createdUser;
  }

  async findAll(): Promise<any[]> {
    const trainers = await this.trainerModel
      .find()
      .populate('user')
      .exec();

    return trainers.map((trainer) => {
      const user = trainer.user as unknown as UserDocument;
      if (!user || !('_id' in user)) {
        throw new NotFoundException('User not populated correctly');
      }
      // Merge trainer fields into user document
      return {
        ...user.toObject(),
        ...trainer.toObject(),
      };
    });
  }

  async findOne(id: string): Promise<any> {
    const trainer = await this.trainerModel
      .findById(id)
      .populate('user')
      .exec();

    if (!trainer) {
      throw new NotFoundException(`Trainer with ID ${id} not found`);
    }

    const user = trainer.user as unknown as UserDocument;
    return {
      ...user.toObject(),
      ...trainer.toObject(),
    };
  }

  async findByUserId(userId: string): Promise<TrainerDocument> {
    const trainer = await this.trainerModel
      .findOne({ user: new Types.ObjectId(userId) })
      .exec();

    if (!trainer) {
      throw new NotFoundException(`Trainer for user ${userId} not found`);
    }

    return trainer;
  }

  async update(
    id: string,
    updateTrainerDto: UpdateTrainerDto,
  ): Promise<TrainerDocument> {
    const trainer = await this.trainerModel
      .findByIdAndUpdate(id, updateTrainerDto, { new: true })
      .exec();

    if (!trainer) {
      throw new NotFoundException(`Trainer with ID ${id} not found`);
    }

    return trainer;
  }

  async remove(id: string): Promise<void> {
    const trainer = await this.trainerModel.findByIdAndDelete(id).exec();

    if (!trainer) {
      throw new NotFoundException(`Trainer with ID ${id} not found`);
    }

    // Delete associated user
    await this.userModel.findByIdAndDelete(trainer.user).exec();
  }

  async convertUserToTrainer(
    userId: string,
    trainerData: Omit<CreateTrainerDto, 'email' | 'name' | 'password'>,
  ): Promise<UserDocument> {
    const user = await this.usersService.findOne(userId);
    if (!user) {
      throw new NotFoundException(`User with ID ${userId} not found`);
    }

    let trainer = await this.trainerModel
      .findOne({ user: userId })
      .exec();

    if (trainer) {
      // Update existing trainer record
      trainer = await this.trainerModel
        .findOneAndUpdate(
          { user: userId },
          {
            $set: {
              specialization: trainerData.specialization,
              hourlyRate: trainerData.hourlyRate,
              yearsOfExperience: trainerData.yearsOfExperience,
              certifications: trainerData.certifications,
              bio: trainerData.bio,
              location: trainerData.location,
              latitude: trainerData.latitude,
              longitude: trainerData.longitude,
              trainingMethods: trainerData.trainingMethods,
              phoneNumber: trainerData.phoneNumber,
              isAvailable: true,
            },
          },
          { new: true },
        )
        .exec();
    } else {
      // Create new trainer record
      trainer = new this.trainerModel({
        user: userId,
        specialization: trainerData.specialization,
        hourlyRate: trainerData.hourlyRate,
        yearsOfExperience: trainerData.yearsOfExperience,
        certifications: trainerData.certifications || [],
        bio: trainerData.bio,
        location: trainerData.location,
        latitude: trainerData.latitude,
        longitude: trainerData.longitude,
        trainingMethods: trainerData.trainingMethods || [],
        phoneNumber: trainerData.phoneNumber,
        isAvailable: true,
      });
      await trainer.save();
    }

    // Update user role to trainer
    user.role = 'trainer';
    await user.save();

    return user;
  }

  // Trainer Booking management
  async createBooking(
    ownerId: string,
    trainerId: string,
    createBookingDto: CreateTrainerBookingDto,
  ): Promise<TrainerBookingDocument> {
    const trainer = await this.trainerModel.findById(trainerId).exec();
    if (!trainer) {
      throw new NotFoundException(`Trainer with ID ${trainerId} not found`);
    }

    const booking = new this.trainerBookingModel({
      owner: new Types.ObjectId(ownerId),
      trainer: new Types.ObjectId(trainerId),
      pet: createBookingDto.petId
        ? new Types.ObjectId(createBookingDto.petId)
        : undefined,
      sessionType: createBookingDto.sessionType,
      description: createBookingDto.description,
      startDateTime: new Date(createBookingDto.startDateTime),
      endDateTime: new Date(createBookingDto.endDateTime),
      duration: createBookingDto.duration,
      totalPrice: createBookingDto.totalPrice,
      notes: createBookingDto.notes,
      location: createBookingDto.location,
      status: BookingStatus.PENDING,
    });

    const savedBooking = await booking.save();

    // Add booking to trainer's bookings list
    await this.trainerModel
      .findByIdAndUpdate(
        trainerId,
        { $push: { bookings: savedBooking._id } },
        { new: true },
      )
      .exec();

    return savedBooking;
  }

  async findBooking(bookingId: string): Promise<TrainerBookingDocument> {
    const booking = await this.trainerBookingModel
      .findById(bookingId)
      .populate([
        { path: 'owner', select: 'name email phoneNumber profileImage' },
        { path: 'trainer', populate: { path: 'user' } },
        { path: 'pet', select: 'name breed species' },
      ])
      .exec();

    if (!booking) {
      throw new NotFoundException(`Booking with ID ${bookingId} not found`);
    }

    return booking;
  }

  async findBookingsByTrainer(
    trainerId: string,
  ): Promise<TrainerBookingDocument[]> {
    return await this.trainerBookingModel
      .find({ trainer: new Types.ObjectId(trainerId) })
      .populate([
        { path: 'owner', select: 'name email phoneNumber profileImage' },
        { path: 'pet', select: 'name breed species' },
      ])
      .sort({ createdAt: -1 })
      .exec();
  }

  async findBookingsByOwner(
    ownerId: string,
  ): Promise<TrainerBookingDocument[]> {
    return await this.trainerBookingModel
      .find({ owner: new Types.ObjectId(ownerId) })
      .populate([
        { path: 'trainer', populate: { path: 'user' } },
        { path: 'pet', select: 'name breed species' },
      ])
      .sort({ createdAt: -1 })
      .exec();
  }

  async updateBooking(
    bookingId: string,
    updateBookingDto: UpdateTrainerBookingDto,
  ): Promise<TrainerBookingDocument> {
    const booking = await this.trainerBookingModel
      .findByIdAndUpdate(bookingId, updateBookingDto, { new: true })
      .populate([
        { path: 'owner', select: 'name email phoneNumber profileImage' },
        { path: 'trainer', populate: { path: 'user' } },
        { path: 'pet', select: 'name breed species' },
      ])
      .exec();

    if (!booking) {
      throw new NotFoundException(`Booking with ID ${bookingId} not found`);
    }

    return booking;
  }

  async cancelBooking(
    bookingId: string,
    cancellationReason: string,
  ): Promise<TrainerBookingDocument> {
    const booking = await this.trainerBookingModel
      .findByIdAndUpdate(
        bookingId,
        {
          status: BookingStatus.CANCELLED,
          cancelledAt: new Date(),
          cancellationReason,
        },
        { new: true },
      )
      .exec();

    if (!booking) {
      throw new NotFoundException(`Booking with ID ${bookingId} not found`);
    }

    return booking;
  }

  async completeBooking(bookingId: string): Promise<TrainerBookingDocument> {
    const booking = await this.trainerBookingModel
      .findByIdAndUpdate(
        bookingId,
        {
          status: BookingStatus.COMPLETED,
          completedAt: new Date(),
        },
        { new: true },
      )
      .exec();

    if (!booking) {
      throw new NotFoundException(`Booking with ID ${bookingId} not found`);
    }

    return booking;
  }

  async rejectBooking(
    bookingId: string,
    rejectionReason: string,
  ): Promise<TrainerBookingDocument> {
    const booking = await this.trainerBookingModel
      .findByIdAndUpdate(
        bookingId,
        {
          status: BookingStatus.REJECTED,
          rejectionReason,
        },
        { new: true },
      )
      .exec();

    if (!booking) {
      throw new NotFoundException(`Booking with ID ${bookingId} not found`);
    }

    return booking;
  }

  async confirmBooking(bookingId: string): Promise<TrainerBookingDocument> {
    const booking = await this.trainerBookingModel
      .findByIdAndUpdate(
        bookingId,
        {
          status: BookingStatus.CONFIRMED,
        },
        { new: true },
      )
      .exec();

    if (!booking) {
      throw new NotFoundException(`Booking with ID ${bookingId} not found`);
    }

    return booking;
  }
}
