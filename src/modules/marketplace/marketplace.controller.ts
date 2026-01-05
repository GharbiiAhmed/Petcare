// src/modules/marketplace/marketplace.controller.ts

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
  Query,
} from '@nestjs/common';
import { MarketplaceService } from './marketplace.service';
import { CreateMarketplaceListingDto } from './dto/create-marketplace-listing.dto';
import { UpdateMarketplaceListingDto } from './dto/update-marketplace-listing.dto';
import { CreateMarketplaceInquiryDto } from './dto/create-marketplace-inquiry.dto';
import { JwtAuthGuard } from '../auth/guards/jwt-auth.guard';
import { ListingStatus } from './schemas/marketplace-listing.schema';

@Controller('marketplace')
export class MarketplaceController {
  constructor(private readonly marketplaceService: MarketplaceService) {}

  // Listing Management
  @Post('listings')
  @UseGuards(JwtAuthGuard)
  @HttpCode(HttpStatus.CREATED)
  async createListing(
    @Body() createListingDto: CreateMarketplaceListingDto,
    @Request() req: any,
  ) {
    return this.marketplaceService.createListing(
      req.user.sub,
      createListingDto,
    );
  }

  @Get('listings')
  async getAllListings(@Query('status') status?: ListingStatus) {
    return this.marketplaceService.findAllListings(status);
  }

  @Get('listings/search')
  async searchListings(
    @Query('q') query?: string,
    @Query('species') species?: string,
    @Query('minPrice') minPrice?: string,
    @Query('maxPrice') maxPrice?: string,
  ) {
    return this.marketplaceService.searchListings(
      query,
      species,
      maxPrice ? parseInt(maxPrice) : undefined,
      minPrice ? parseInt(minPrice) : undefined,
    );
  }

  @Get('listings/:listingId')
  async getListing(@Param('listingId') listingId: string) {
    return this.marketplaceService.findListingById(listingId);
  }

  @Get('user/:userId/listings')
  async getUserListings(
    @Param('userId') userId: string,
    @Query('status') status?: ListingStatus,
  ) {
    return this.marketplaceService.findListingsByUser(userId, status);
  }

  @Put('listings/:listingId')
  @UseGuards(JwtAuthGuard)
  async updateListing(
    @Param('listingId') listingId: string,
    @Body() updateListingDto: UpdateMarketplaceListingDto,
  ) {
    return this.marketplaceService.updateListing(listingId, updateListingDto);
  }

  @Delete('listings/:listingId')
  @UseGuards(JwtAuthGuard)
  @HttpCode(HttpStatus.NO_CONTENT)
  async deleteListing(@Param('listingId') listingId: string) {
    return this.marketplaceService.deleteListing(listingId);
  }

  @Put('listings/:listingId/sold')
  @UseGuards(JwtAuthGuard)
  async markListingAsSold(
    @Param('listingId') listingId: string,
    @Body() body: { buyerId: string },
  ) {
    return this.marketplaceService.markAsSold(listingId, body.buyerId);
  }

  @Post('listings/:listingId/like')
  @UseGuards(JwtAuthGuard)
  async toggleLike(
    @Param('listingId') listingId: string,
    @Request() req: any,
  ) {
    return this.marketplaceService.toggleLike(listingId, req.user.sub);
  }

  // Inquiry Management
  @Post('listings/:listingId/inquiries')
  @UseGuards(JwtAuthGuard)
  @HttpCode(HttpStatus.CREATED)
  async createInquiry(
    @Param('listingId') listingId: string,
    @Body() createInquiryDto: CreateMarketplaceInquiryDto,
    @Request() req: any,
  ) {
    return this.marketplaceService.createInquiry(
      req.user.sub,
      listingId,
      createInquiryDto,
    );
  }

  @Get('seller/inquiries')
  @UseGuards(JwtAuthGuard)
  async getSellerInquiries(@Request() req: any) {
    return this.marketplaceService.findInquiriesBySeller(req.user.sub);
  }

  @Get('buyer/inquiries')
  @UseGuards(JwtAuthGuard)
  async getBuyerInquiries(@Request() req: any) {
    return this.marketplaceService.findInquiriesByBuyer(req.user.sub);
  }

  @Get('inquiries/:inquiryId')
  @UseGuards(JwtAuthGuard)
  async getInquiry(@Param('inquiryId') inquiryId: string) {
    return this.marketplaceService.findInquiry(inquiryId);
  }

  @Put('inquiries/:inquiryId/accept')
  @UseGuards(JwtAuthGuard)
  async acceptInquiry(@Param('inquiryId') inquiryId: string) {
    return this.marketplaceService.acceptInquiry(inquiryId);
  }

  @Put('inquiries/:inquiryId/reject')
  @UseGuards(JwtAuthGuard)
  async rejectInquiry(
    @Param('inquiryId') inquiryId: string,
    @Body() body: { rejectionReason: string },
  ) {
    return this.marketplaceService.rejectInquiry(
      inquiryId,
      body.rejectionReason,
    );
  }

  @Put('inquiries/:inquiryId/complete')
  @UseGuards(JwtAuthGuard)
  async completeInquiry(@Param('inquiryId') inquiryId: string) {
    return this.marketplaceService.completeInquiry(inquiryId);
  }
}
