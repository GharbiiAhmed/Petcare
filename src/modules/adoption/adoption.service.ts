// src/modules/adoption/adoption.service.ts

import {
  Injectable,
  NotFoundException,
  BadRequestException,
} from '@nestjs/common';
import { InjectModel } from '@nestjs/mongoose';
import { Model, Types } from 'mongoose';
import {
  AdoptionListing,
  AdoptionListingDocument,
  AdoptionListingStatus,
} from './schemas/adoption-listing.schema';
import {
  AdoptionApplication,
  AdoptionApplicationDocument,
  ApplicationStatus,
} from './schemas/adoption-application.schema';
import { User, UserDocument } from '../users/schemas/user.schema';
import { CreateAdoptionListingDto } from './dto/create-adoption-listing.dto';
import { UpdateAdoptionListingDto } from './dto/update-adoption-listing.dto';
import { CreateAdoptionApplicationDto } from './dto/create-adoption-application.dto';
import { UpdateAdoptionApplicationDto } from './dto/update-adoption-application.dto';

@Injectable()
export class AdoptionService {
  constructor(
    @InjectModel(AdoptionListing.name)
    private readonly listingModel: Model<AdoptionListingDocument>,
    @InjectModel(AdoptionApplication.name)
    private readonly applicationModel: Model<AdoptionApplicationDocument>,
    @InjectModel(User.name)
    private readonly userModel: Model<UserDocument>,
  ) {}

  // Listing Management
  async createListing(
    rescuerId: string,
    createListingDto: CreateAdoptionListingDto,
  ): Promise<AdoptionListingDocument> {
    const listing = new this.listingModel({
      rescuer: new Types.ObjectId(rescuerId),
      petName: createListingDto.petName,
      species: createListingDto.species,
      breed: createListingDto.breed,
      age: createListingDto.age,
      color: createListingDto.color,
      weight: createListingDto.weight,
      gender: createListingDto.gender,
      description: createListingDto.description,
      images: createListingDto.images || [],
      location: createListingDto.location,
      latitude: createListingDto.latitude,
      longitude: createListingDto.longitude,
      vaccinated: createListingDto.vaccinated,
      neutered: createListingDto.neutered,
      medicalHistory: createListingDto.medicalHistory,
      traits: createListingDto.traits || [],
      adoptionRequirements: createListingDto.adoptionRequirements,
      status: AdoptionListingStatus.AVAILABLE,
    });

    return await listing.save();
  }

  async findAllListings(
    status?: AdoptionListingStatus,
  ): Promise<AdoptionListingDocument[]> {
    const query: any = {};
    if (status) {
      query.status = status;
    }

    return await this.listingModel
      .find(query)
      .populate('rescuer', 'name email phoneNumber profileImage location')
      .populate('adoptedBy', 'name email phoneNumber profileImage')
      .sort({ createdAt: -1 })
      .exec();
  }

  async findListingById(
    listingId: string,
  ): Promise<AdoptionListingDocument> {
    const listing = await this.listingModel
      .findByIdAndUpdate(listingId, { $inc: { views: 1 } }, { new: true })
      .populate('rescuer', 'name email phoneNumber profileImage location')
      .populate('adoptedBy', 'name email phoneNumber profileImage')
      .exec();

    if (!listing) {
      throw new NotFoundException(`Listing with ID ${listingId} not found`);
    }

    return listing;
  }

  async findListingsByRescuer(
    rescuerId: string,
    status?: AdoptionListingStatus,
  ): Promise<AdoptionListingDocument[]> {
    const query: any = {
      rescuer: new Types.ObjectId(rescuerId),
    };

    if (status) {
      query.status = status;
    }

    return await this.listingModel
      .find(query)
      .populate('adoptedBy', 'name email phoneNumber profileImage')
      .sort({ createdAt: -1 })
      .exec();
  }

  async updateListing(
    listingId: string,
    updateListingDto: UpdateAdoptionListingDto,
  ): Promise<AdoptionListingDocument> {
    const listing = await this.listingModel
      .findByIdAndUpdate(listingId, updateListingDto, { new: true })
      .populate('rescuer', 'name email phoneNumber profileImage')
      .populate('adoptedBy', 'name email phoneNumber profileImage')
      .exec();

    if (!listing) {
      throw new NotFoundException(`Listing with ID ${listingId} not found`);
    }

    return listing;
  }

  async deleteListing(listingId: string): Promise<void> {
    const listing = await this.listingModel.findByIdAndDelete(listingId).exec();

    if (!listing) {
      throw new NotFoundException(`Listing with ID ${listingId} not found`);
    }
  }

  async markAsAdopted(
    listingId: string,
    adopterId: string,
  ): Promise<AdoptionListingDocument> {
    const listing = await this.listingModel
      .findByIdAndUpdate(
        listingId,
        {
          status: AdoptionListingStatus.ADOPTED,
          adoptedBy: new Types.ObjectId(adopterId),
          adoptedDate: new Date(),
        },
        { new: true },
      )
      .exec();

    if (!listing) {
      throw new NotFoundException(`Listing with ID ${listingId} not found`);
    }

    return listing;
  }

  async searchListings(
    query: string,
    species?: string,
  ): Promise<AdoptionListingDocument[]> {
    const searchQuery: any = {
      status: AdoptionListingStatus.AVAILABLE,
    };

    if (query) {
      searchQuery.$or = [
        { petName: { $regex: query, $options: 'i' } },
        { species: { $regex: query, $options: 'i' } },
        { breed: { $regex: query, $options: 'i' } },
        { description: { $regex: query, $options: 'i' } },
      ];
    }

    if (species) {
      searchQuery.species = { $regex: species, $options: 'i' };
    }

    return await this.listingModel
      .find(searchQuery)
      .populate('rescuer', 'name email phoneNumber profileImage location')
      .sort({ createdAt: -1 })
      .exec();
  }

  async toggleLike(
    listingId: string,
    userId: string,
  ): Promise<AdoptionListingDocument> {
    const listing = await this.listingModel.findById(listingId).exec();

    if (!listing) {
      throw new NotFoundException(`Listing with ID ${listingId} not found`);
    }

    const userObjectId = new Types.ObjectId(userId);
    const isLiked = listing.likedBy?.some((id) => id.equals(userObjectId));

    if (isLiked) {
      // Unlike
      await this.listingModel.findByIdAndUpdate(
        listingId,
        {
          $pull: { likedBy: userObjectId },
          $inc: { likes: -1 },
        },
        { new: true },
      );
    } else {
      // Like
      await this.listingModel.findByIdAndUpdate(
        listingId,
        {
          $push: { likedBy: userObjectId },
          $inc: { likes: 1 },
        },
        { new: true },
      );
    }

    return await this.listingModel.findById(listingId).exec();
  }

  // Application Management
  async createApplication(
    applicantId: string,
    listingId: string,
    createApplicationDto: CreateAdoptionApplicationDto,
  ): Promise<AdoptionApplicationDocument> {
    const listing = await this.listingModel.findById(listingId).exec();
    if (!listing) {
      throw new NotFoundException(`Listing with ID ${listingId} not found`);
    }

    // Check if listing is already adopted
    if (listing.status === AdoptionListingStatus.ADOPTED) {
      throw new BadRequestException(
        'This pet has already been adopted',
      );
    }

    const application = new this.applicationModel({
      applicant: new Types.ObjectId(applicantId),
      listing: new Types.ObjectId(listingId),
      phoneNumber: createApplicationDto.phoneNumber,
      address: createApplicationDto.address,
      housingType: createApplicationDto.housingType,
      ownRent: createApplicationDto.ownRent,
      otherPets: createApplicationDto.otherPets,
      familyDescription: createApplicationDto.familyDescription,
      workSchedule: createApplicationDto.workSchedule,
      motivation: createApplicationDto.motivation,
      experience: createApplicationDto.experience,
      vetReference: createApplicationDto.vetReference,
      additionalInfo: createApplicationDto.additionalInfo,
      status: ApplicationStatus.PENDING,
    });

    return await application.save();
  }

  async findApplicationsByRescuer(
    rescuerId: string,
  ): Promise<AdoptionApplicationDocument[]> {
    // Get all listings by rescuer
    const rescuerListings = await this.listingModel
      .find({ rescuer: new Types.ObjectId(rescuerId) })
      .select('_id')
      .exec();

    const listingIds = rescuerListings.map((l) => l._id);

    return await this.applicationModel
      .find({ listing: { $in: listingIds } })
      .populate('applicant', 'name email phoneNumber profileImage')
      .populate('listing', 'petName species images')
      .sort({ createdAt: -1 })
      .exec();
  }

  async findApplicationsByApplicant(
    applicantId: string,
  ): Promise<AdoptionApplicationDocument[]> {
    return await this.applicationModel
      .find({ applicant: new Types.ObjectId(applicantId) })
      .populate('listing', 'petName species images rescuer')
      .sort({ createdAt: -1 })
      .exec();
  }

  async findApplication(
    applicationId: string,
  ): Promise<AdoptionApplicationDocument> {
    const application = await this.applicationModel
      .findById(applicationId)
      .populate('applicant', 'name email phoneNumber profileImage')
      .populate('listing', 'petName species images adoptionRequirements')
      .exec();

    if (!application) {
      throw new NotFoundException(
        `Application with ID ${applicationId} not found`,
      );
    }

    return application;
  }

  async updateApplication(
    applicationId: string,
    updateApplicationDto: UpdateAdoptionApplicationDto,
  ): Promise<AdoptionApplicationDocument> {
    const application = await this.applicationModel
      .findByIdAndUpdate(applicationId, updateApplicationDto, { new: true })
      .populate('applicant', 'name email phoneNumber profileImage')
      .populate('listing', 'petName species images')
      .exec();

    if (!application) {
      throw new NotFoundException(
        `Application with ID ${applicationId} not found`,
      );
    }

    return application;
  }

  async approveApplication(
    applicationId: string,
  ): Promise<AdoptionApplicationDocument> {
    const application = await this.applicationModel
      .findByIdAndUpdate(
        applicationId,
        {
          status: ApplicationStatus.APPROVED,
          approvedAt: new Date(),
        },
        { new: true },
      )
      .populate('applicant', 'name email phoneNumber profileImage')
      .populate('listing', 'petName species images')
      .exec();

    if (!application) {
      throw new NotFoundException(
        `Application with ID ${applicationId} not found`,
      );
    }

    return application;
  }

  async rejectApplication(
    applicationId: string,
    rejectionReason: string,
  ): Promise<AdoptionApplicationDocument> {
    const application = await this.applicationModel
      .findByIdAndUpdate(
        applicationId,
        {
          status: ApplicationStatus.REJECTED,
          rejectionReason,
          rejectedAt: new Date(),
        },
        { new: true },
      )
      .populate('applicant', 'name email phoneNumber profileImage')
      .populate('listing', 'petName species images')
      .exec();

    if (!application) {
      throw new NotFoundException(
        `Application with ID ${applicationId} not found`,
      );
    }

    return application;
  }

  async completeApplication(
    applicationId: string,
  ): Promise<AdoptionApplicationDocument> {
    const application = await this.applicationModel.findById(applicationId).exec();
    if (!application) {
      throw new NotFoundException(
        `Application with ID ${applicationId} not found`,
      );
    }

    // Mark listing as adopted
    await this.markAsAdopted(
      application.listing.toString(),
      application.applicant.toString(),
    );

    return await this.applicationModel
      .findByIdAndUpdate(
        applicationId,
        {
          status: ApplicationStatus.COMPLETED,
          completedAt: new Date(),
        },
        { new: true },
      )
      .populate('applicant', 'name email phoneNumber profileImage')
      .populate('listing', 'petName species images')
      .exec();
  }
}
