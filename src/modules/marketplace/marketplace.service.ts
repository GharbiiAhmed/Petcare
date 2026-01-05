// src/modules/marketplace/marketplace.service.ts

import {
  Injectable,
  NotFoundException,
  BadRequestException,
} from '@nestjs/common';
import { InjectModel } from '@nestjs/mongoose';
import { Model, Types } from 'mongoose';
import {
  MarketplaceListing,
  MarketplaceListingDocument,
  ListingStatus,
} from './schemas/marketplace-listing.schema';
import {
  MarketplaceInquiry,
  MarketplaceInquiryDocument,
  InquiryStatus,
} from './schemas/marketplace-inquiry.schema';
import { User, UserDocument } from '../users/schemas/user.schema';
import { CreateMarketplaceListingDto } from './dto/create-marketplace-listing.dto';
import { UpdateMarketplaceListingDto } from './dto/update-marketplace-listing.dto';
import { CreateMarketplaceInquiryDto } from './dto/create-marketplace-inquiry.dto';
import { UpdateMarketplaceInquiryDto } from './dto/update-marketplace-inquiry.dto';

@Injectable()
export class MarketplaceService {
  constructor(
    @InjectModel(MarketplaceListing.name)
    private readonly listingModel: Model<MarketplaceListingDocument>,
    @InjectModel(MarketplaceInquiry.name)
    private readonly inquiryModel: Model<MarketplaceInquiryDocument>,
    @InjectModel(User.name)
    private readonly userModel: Model<UserDocument>,
  ) {}

  // Listing Management
  async createListing(
    sellerId: string,
    createListingDto: CreateMarketplaceListingDto,
  ): Promise<MarketplaceListingDocument> {
    const listing = new this.listingModel({
      seller: new Types.ObjectId(sellerId),
      petName: createListingDto.petName,
      species: createListingDto.species,
      breed: createListingDto.breed,
      age: createListingDto.age,
      color: createListingDto.color,
      weight: createListingDto.weight,
      gender: createListingDto.gender,
      description: createListingDto.description,
      price: createListingDto.price,
      images: createListingDto.images || [],
      location: createListingDto.location,
      latitude: createListingDto.latitude,
      longitude: createListingDto.longitude,
      vaccinated: createListingDto.vaccinated,
      neutered: createListingDto.neutered,
      medicalHistory: createListingDto.medicalHistory,
      traits: createListingDto.traits || [],
      status: ListingStatus.ACTIVE,
    });

    return await listing.save();
  }

  async findAllListings(status?: ListingStatus): Promise<any[]> {
    const query: any = {};
    if (status) {
      query.status = status;
    }

    const listings = await this.listingModel
      .find(query)
      .populate('seller', 'name email phoneNumber profileImage location')
      .populate('buyer', 'name email phoneNumber profileImage')
      .sort({ createdAt: -1 })
      .exec();

    return listings;
  }

  async findListingById(listingId: string): Promise<MarketplaceListingDocument> {
    const listing = await this.listingModel
      .findByIdAndUpdate(listingId, { $inc: { views: 1 } }, { new: true })
      .populate('seller', 'name email phoneNumber profileImage location')
      .populate('buyer', 'name email phoneNumber profileImage')
      .exec();

    if (!listing) {
      throw new NotFoundException(`Listing with ID ${listingId} not found`);
    }

    return listing;
  }

  async findListingsByUser(
    userId: string,
    status?: ListingStatus,
  ): Promise<any[]> {
    const query: any = {
      seller: new Types.ObjectId(userId),
    };

    if (status) {
      query.status = status;
    }

    return await this.listingModel
      .find(query)
      .populate('buyer', 'name email phoneNumber profileImage')
      .sort({ createdAt: -1 })
      .exec();
  }

  async updateListing(
    listingId: string,
    updateListingDto: UpdateMarketplaceListingDto,
  ): Promise<MarketplaceListingDocument> {
    const listing = await this.listingModel
      .findByIdAndUpdate(listingId, updateListingDto, { new: true })
      .populate('seller', 'name email phoneNumber profileImage')
      .populate('buyer', 'name email phoneNumber profileImage')
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

  async markAsSold(
    listingId: string,
    buyerId: string,
  ): Promise<MarketplaceListingDocument> {
    const listing = await this.listingModel
      .findByIdAndUpdate(
        listingId,
        {
          status: ListingStatus.SOLD,
          buyer: new Types.ObjectId(buyerId),
          soldDate: new Date(),
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
    maxPrice?: number,
    minPrice?: number,
  ): Promise<any[]> {
    const searchQuery: any = {
      status: ListingStatus.ACTIVE,
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

    if (minPrice !== undefined && maxPrice !== undefined) {
      searchQuery.price = { $gte: minPrice, $lte: maxPrice };
    } else if (maxPrice !== undefined) {
      searchQuery.price = { $lte: maxPrice };
    } else if (minPrice !== undefined) {
      searchQuery.price = { $gte: minPrice };
    }

    return await this.listingModel
      .find(searchQuery)
      .populate('seller', 'name email phoneNumber profileImage location')
      .sort({ createdAt: -1 })
      .exec();
  }

  async toggleLike(
    listingId: string,
    userId: string,
  ): Promise<MarketplaceListingDocument> {
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

  // Inquiry Management
  async createInquiry(
    buyerId: string,
    listingId: string,
    createInquiryDto: CreateMarketplaceInquiryDto,
  ): Promise<MarketplaceInquiryDocument> {
    const listing = await this.listingModel.findById(listingId).exec();
    if (!listing) {
      throw new NotFoundException(`Listing with ID ${listingId} not found`);
    }

    // Check if listing is already sold
    if (listing.status === ListingStatus.SOLD) {
      throw new BadRequestException('This listing has already been sold');
    }

    const inquiry = new this.inquiryModel({
      buyer: new Types.ObjectId(buyerId),
      listing: new Types.ObjectId(listingId),
      message: createInquiryDto.message,
      phoneNumber: createInquiryDto.phoneNumber,
      email: createInquiryDto.email,
      status: InquiryStatus.PENDING,
    });

    return await inquiry.save();
  }

  async findInquiriesBySeller(
    sellerId: string,
  ): Promise<MarketplaceInquiryDocument[]> {
    // Get all listings by seller
    const sellerListings = await this.listingModel
      .find({ seller: new Types.ObjectId(sellerId) })
      .select('_id')
      .exec();

    const listingIds = sellerListings.map((l) => l._id);

    return await this.inquiryModel
      .find({ listing: { $in: listingIds } })
      .populate('buyer', 'name email phoneNumber profileImage')
      .populate('listing', 'petName species price')
      .sort({ createdAt: -1 })
      .exec();
  }

  async findInquiriesByBuyer(
    buyerId: string,
  ): Promise<MarketplaceInquiryDocument[]> {
    return await this.inquiryModel
      .find({ buyer: new Types.ObjectId(buyerId) })
      .populate('listing', 'petName species price seller')
      .sort({ createdAt: -1 })
      .exec();
  }

  async findInquiry(inquiryId: string): Promise<MarketplaceInquiryDocument> {
    const inquiry = await this.inquiryModel
      .findById(inquiryId)
      .populate('buyer', 'name email phoneNumber profileImage')
      .populate('listing', 'petName species price images')
      .exec();

    if (!inquiry) {
      throw new NotFoundException(`Inquiry with ID ${inquiryId} not found`);
    }

    return inquiry;
  }

  async updateInquiry(
    inquiryId: string,
    updateInquiryDto: UpdateMarketplaceInquiryDto,
  ): Promise<MarketplaceInquiryDocument> {
    const inquiry = await this.inquiryModel
      .findByIdAndUpdate(inquiryId, updateInquiryDto, { new: true })
      .populate('buyer', 'name email phoneNumber profileImage')
      .populate('listing', 'petName species price')
      .exec();

    if (!inquiry) {
      throw new NotFoundException(`Inquiry with ID ${inquiryId} not found`);
    }

    return inquiry;
  }

  async acceptInquiry(inquiryId: string): Promise<MarketplaceInquiryDocument> {
    return await this.updateInquiry(inquiryId, {
      status: InquiryStatus.ACCEPTED,
    });
  }

  async rejectInquiry(
    inquiryId: string,
    rejectionReason: string,
  ): Promise<MarketplaceInquiryDocument> {
    return await this.updateInquiry(inquiryId, {
      status: InquiryStatus.REJECTED,
      rejectionReason,
    });
  }

  async completeInquiry(inquiryId: string): Promise<MarketplaceInquiryDocument> {
    const inquiry = await this.inquiryModel.findById(inquiryId).exec();
    if (!inquiry) {
      throw new NotFoundException(`Inquiry with ID ${inquiryId} not found`);
    }

    // Mark listing as sold
    await this.markAsSold(inquiry.listing.toString(), inquiry.buyer.toString());

    return await this.updateInquiry(inquiryId, {
      status: InquiryStatus.COMPLETED,
    });
  }
}
