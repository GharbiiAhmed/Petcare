// src/modules/salons/salons.service.ts

import {
  Injectable,
  NotFoundException,
  ConflictException,
} from '@nestjs/common';
import { InjectModel } from '@nestjs/mongoose';
import { Model } from 'mongoose';
import { CreateSalonDto } from './dto/create-salon.dto';
import { UpdateSalonDto } from './dto/update-salon.dto';
import { UsersService } from '../users/users.service';
import { User, UserDocument } from '../users/schemas/user.schema';
import { Salon, SalonDocument } from './schemas/salon.schema';
import { MailService } from '../mail/mail.service';
import * as bcrypt from 'bcrypt';

@Injectable()
export class SalonsService {
  constructor(
    @InjectModel(User.name)
    private readonly userModel: Model<UserDocument>,
    @InjectModel(Salon.name)
    private readonly salonModel: Model<SalonDocument>,
    private readonly usersService: UsersService,
    private readonly mailService: MailService,
  ) {}

  async create(createSalonDto: CreateSalonDto): Promise<UserDocument> {
    // Check if user with email already exists
    const existingUser = await this.usersService.findByEmail(
      createSalonDto.email,
    );
    if (existingUser) {
      throw new ConflictException('User with this email already exists');
    }

    // Hash password
    const hashedPassword = await bcrypt.hash(createSalonDto.password, 10);

    // Create salon user with role 'salon'
    const salonData = {
      ...createSalonDto,
      password: hashedPassword,
      role: 'salon',
      isVerified: false,
      balance: 0,
    };

    const createdUser = await new this.userModel(salonData).save();

    // Create salon record in salons collection
    const salon = new this.salonModel({
      user: createdUser._id,
      salonName: createSalonDto.salonName,
      salonAddress: createSalonDto.salonAddress,
      services: createSalonDto.services || [],
      yearsOfExperience: createSalonDto.yearsOfExperience,
      latitude: createSalonDto.latitude,
      longitude: createSalonDto.longitude,
      bio: createSalonDto.bio,
      pricing: createSalonDto.pricing
        ? new Map(Object.entries(createSalonDto.pricing))
        : undefined,
    });

    await salon.save();

    return createdUser;
  }

  async findAll(): Promise<UserDocument[]> {
    const salons = await this.salonModel
      .find()
      .populate('user')
      .exec();
    return salons.map((salon) => {
      const user = salon.user as unknown as UserDocument;
      if (!user || !('_id' in user)) {
        throw new NotFoundException('User not populated correctly');
      }
      // Merge latitude and longitude from Salon into User document
      if (salon.latitude !== undefined) {
        (user as any).latitude = salon.latitude;
      }
      if (salon.longitude !== undefined) {
        (user as any).longitude = salon.longitude;
      }
      return user;
    });
  }

  async findOne(id: string): Promise<any> {
    const salon = await this.salonModel
      .findOne({ user: id })
      .populate('user')
      .exec();
    if (!salon) {
      throw new NotFoundException(`Salon with ID ${id} not found`);
    }
    const user = salon.user as unknown as UserDocument;
    if (!user || !('_id' in user)) {
      throw new NotFoundException('User not populated correctly');
    }
    // Convert Mongoose document to plain object to ensure all fields are serialized
    const userObj = (user as any).toObject
      ? (user as any).toObject({ virtuals: true })
      : JSON.parse(JSON.stringify(user));
    // Merge all salon fields into User object for client consumption
    userObj.salonName = salon.salonName;
    userObj.salonAddress = salon.salonAddress;
    userObj.salonServices = salon.services;
    userObj.salonYearsOfExperience = salon.yearsOfExperience;
    userObj.salonBio = salon.bio;
    userObj.salonPricing = salon.pricing
      ? Object.fromEntries(salon.pricing)
      : undefined;
    // Use salon's latitude/longitude if available, otherwise keep user's
    if (salon.latitude !== undefined) userObj.latitude = salon.latitude;
    if (salon.longitude !== undefined) userObj.longitude = salon.longitude;
    return userObj;
  }

  async findByEmail(email: string): Promise<UserDocument | null> {
    const user = await this.usersService.findByEmail(email);
    if (!user || user.role !== 'salon') {
      return null;
    }
    const salon = await this.salonModel
      .findOne({ user: user._id })
      .populate('user')
      .exec();
    if (!salon) {
      return null;
    }
    const populatedUser = salon.user as unknown as UserDocument;
    return populatedUser && '_id' in populatedUser ? populatedUser : null;
  }

  async update(id: string, updateSalonDto: UpdateSalonDto): Promise<UserDocument> {
    // Separate user fields from salon fields
    const userFields: any = {};
    const salonFields: any = {};

    // User fields that go to the User model
    if (updateSalonDto.phoneNumber !== undefined) {
      userFields.phoneNumber = updateSalonDto.phoneNumber;
    }
    if (updateSalonDto.country !== undefined) {
      userFields.country = updateSalonDto.country;
    }
    if (updateSalonDto.city !== undefined) {
      userFields.city = updateSalonDto.city;
    }
    if (updateSalonDto.name !== undefined) {
      userFields.name = updateSalonDto.name;
    }
    if (updateSalonDto.email !== undefined) {
      userFields.email = updateSalonDto.email;
    }

    // Salon fields that go to the Salon model
    if (updateSalonDto.salonName !== undefined) {
      salonFields.salonName = updateSalonDto.salonName;
    }
    if (updateSalonDto.salonAddress !== undefined) {
      salonFields.salonAddress = updateSalonDto.salonAddress;
    }
    if (updateSalonDto.services !== undefined) {
      salonFields.services = updateSalonDto.services;
    }
    if (updateSalonDto.yearsOfExperience !== undefined) {
      salonFields.yearsOfExperience = updateSalonDto.yearsOfExperience;
    }
    if (updateSalonDto.latitude !== undefined) {
      salonFields.latitude = updateSalonDto.latitude;
    }
    if (updateSalonDto.longitude !== undefined) {
      salonFields.longitude = updateSalonDto.longitude;
    }
    if (updateSalonDto.bio !== undefined) {
      salonFields.bio = updateSalonDto.bio;
    }
    if (updateSalonDto.pricing !== undefined) {
      salonFields.pricing = new Map(Object.entries(updateSalonDto.pricing));
    }

    // Update user fields if any
    if (Object.keys(userFields).length > 0) {
      await this.userModel.findByIdAndUpdate(id, { $set: userFields }).exec();
    }

    // Update salon fields if any
    const salon = await this.salonModel
      .findOneAndUpdate({ user: id }, { $set: salonFields }, { new: true })
      .populate('user')
      .exec();

    if (!salon) {
      throw new NotFoundException(`Salon with ID ${id} not found`);
    }
    const user = salon.user as unknown as UserDocument;
    if (!user || !('_id' in user)) {
      throw new NotFoundException('User not populated correctly');
    }
    // Convert Mongoose document to plain object to ensure all fields are serialized
    const userObj = (user as any).toObject
      ? (user as any).toObject({ virtuals: true })
      : JSON.parse(JSON.stringify(user));
    // Merge all salon fields into User object for client consumption
    userObj.salonName = salon.salonName;
    userObj.salonAddress = salon.salonAddress;
    userObj.salonServices = salon.services;
    userObj.salonYearsOfExperience = salon.yearsOfExperience;
    userObj.salonBio = salon.bio;
    userObj.salonPricing = salon.pricing
      ? Object.fromEntries(salon.pricing)
      : undefined;
    // Use salon's latitude/longitude if available, otherwise keep user's
    if (salon.latitude !== undefined) userObj.latitude = salon.latitude;
    if (salon.longitude !== undefined) userObj.longitude = salon.longitude;
    return userObj;
  }

  async remove(id: string): Promise<void> {
    const result = await this.salonModel
      .findOneAndDelete({ user: id })
      .exec();

    if (!result) {
      throw new NotFoundException(`Salon with ID ${id} not found`);
    }

    // Also update user role back to 'owner'
    await this.userModel
      .findByIdAndUpdate(id, { $set: { role: 'owner' } })
      .exec();
  }

  // Convert existing user to salon (when they complete the join form)
  async convertUserToSalon(
    userId: string,
    salonData: Omit<CreateSalonDto, 'email' | 'name' | 'password'>,
  ): Promise<UserDocument> {
    const user = await this.usersService.findOne(userId);
    if (!user) {
      throw new NotFoundException(`User with ID ${userId} not found`);
    }

    // Check if salon record already exists
    let salon = await this.salonModel.findOne({ user: userId }).exec();

    if (salon) {
      // Already a salon, just update the fields
      salon = await this.salonModel
        .findOneAndUpdate(
          { user: userId },
          {
            $set: {
              salonName: salonData.salonName,
              salonAddress: salonData.salonAddress,
              services: salonData.services || [],
              yearsOfExperience: salonData.yearsOfExperience,
              latitude: salonData.latitude,
              longitude: salonData.longitude,
              bio: salonData.bio,
              pricing: salonData.pricing
                ? new Map(Object.entries(salonData.pricing))
                : undefined,
            },
          },
          { new: true },
        )
        .populate('user')
        .exec();
    } else {
      // Create new salon record in salons collection
      const salonRecordData: any = {
        user: userId,
        salonName: salonData.salonName,
        salonAddress: salonData.salonAddress,
        services: salonData.services || [],
        yearsOfExperience: salonData.yearsOfExperience,
        latitude: salonData.latitude,
        longitude: salonData.longitude,
        bio: salonData.bio,
        pricing: salonData.pricing
          ? new Map(Object.entries(salonData.pricing))
          : undefined,
      };

      // Ensure email is not included
      delete salonRecordData.email;

      const salonRecord = new this.salonModel(salonRecordData);

      salon = await salonRecord.save();
      await salon.populate('user');

      // Update user role to 'salon' - keep existing verification status
      await this.userModel
        .findByIdAndUpdate(userId, {
          $set: {
            role: 'salon',
          },
        })
        .exec();

      console.log(`[Salon Conversion] User ${userId} converted to salon role`);
    }

    // Return the user with updated role
    const updatedUser = await this.usersService.findOne(userId);
    return updatedUser;
  }
}


