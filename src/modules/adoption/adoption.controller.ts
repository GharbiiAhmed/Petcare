// src/modules/adoption/adoption.controller.ts

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
import { AdoptionService } from './adoption.service';
import { CreateAdoptionListingDto } from './dto/create-adoption-listing.dto';
import { UpdateAdoptionListingDto } from './dto/update-adoption-listing.dto';
import { CreateAdoptionApplicationDto } from './dto/create-adoption-application.dto';
import { JwtAuthGuard } from '../auth/guards/jwt-auth.guard';
import { AdoptionListingStatus } from './schemas/adoption-listing.schema';

@Controller('adoption')
export class AdoptionController {
  constructor(private readonly adoptionService: AdoptionService) {}

  // Listing Management
  @Post('listings')
  @UseGuards(JwtAuthGuard)
  @HttpCode(HttpStatus.CREATED)
  async createListing(
    @Body() createListingDto: CreateAdoptionListingDto,
    @Request() req: any,
  ) {
    return this.adoptionService.createListing(req.user.sub, createListingDto);
  }

  @Get('listings')
  async getAllListings(@Query('status') status?: AdoptionListingStatus) {
    return this.adoptionService.findAllListings(status);
  }

  @Get('listings/search')
  async searchListings(
    @Query('q') query?: string,
    @Query('species') species?: string,
  ) {
    return this.adoptionService.searchListings(query, species);
  }

  @Get('listings/:listingId')
  async getListing(@Param('listingId') listingId: string) {
    return this.adoptionService.findListingById(listingId);
  }

  @Get('rescuer/:rescuerId/listings')
  async getRescuerListings(
    @Param('rescuerId') rescuerId: string,
    @Query('status') status?: AdoptionListingStatus,
  ) {
    return this.adoptionService.findListingsByRescuer(rescuerId, status);
  }

  @Put('listings/:listingId')
  @UseGuards(JwtAuthGuard)
  async updateListing(
    @Param('listingId') listingId: string,
    @Body() updateListingDto: UpdateAdoptionListingDto,
  ) {
    return this.adoptionService.updateListing(listingId, updateListingDto);
  }

  @Delete('listings/:listingId')
  @UseGuards(JwtAuthGuard)
  @HttpCode(HttpStatus.NO_CONTENT)
  async deleteListing(@Param('listingId') listingId: string) {
    return this.adoptionService.deleteListing(listingId);
  }

  @Put('listings/:listingId/adopted')
  @UseGuards(JwtAuthGuard)
  async markListingAsAdopted(
    @Param('listingId') listingId: string,
    @Body() body: { adopterId: string },
  ) {
    return this.adoptionService.markAsAdopted(listingId, body.adopterId);
  }

  @Post('listings/:listingId/like')
  @UseGuards(JwtAuthGuard)
  async toggleLike(
    @Param('listingId') listingId: string,
    @Request() req: any,
  ) {
    return this.adoptionService.toggleLike(listingId, req.user.sub);
  }

  // Application Management
  @Post('listings/:listingId/applications')
  @UseGuards(JwtAuthGuard)
  @HttpCode(HttpStatus.CREATED)
  async createApplication(
    @Param('listingId') listingId: string,
    @Body() createApplicationDto: CreateAdoptionApplicationDto,
    @Request() req: any,
  ) {
    return this.adoptionService.createApplication(
      req.user.sub,
      listingId,
      createApplicationDto,
    );
  }

  @Get('rescuer/applications')
  @UseGuards(JwtAuthGuard)
  async getRescuerApplications(@Request() req: any) {
    return this.adoptionService.findApplicationsByRescuer(req.user.sub);
  }

  @Get('applicant/applications')
  @UseGuards(JwtAuthGuard)
  async getApplicantApplications(@Request() req: any) {
    return this.adoptionService.findApplicationsByApplicant(req.user.sub);
  }

  @Get('applications/:applicationId')
  @UseGuards(JwtAuthGuard)
  async getApplication(@Param('applicationId') applicationId: string) {
    return this.adoptionService.findApplication(applicationId);
  }

  @Put('applications/:applicationId/approve')
  @UseGuards(JwtAuthGuard)
  async approveApplication(@Param('applicationId') applicationId: string) {
    return this.adoptionService.approveApplication(applicationId);
  }

  @Put('applications/:applicationId/reject')
  @UseGuards(JwtAuthGuard)
  async rejectApplication(
    @Param('applicationId') applicationId: string,
    @Body() body: { rejectionReason: string },
  ) {
    return this.adoptionService.rejectApplication(
      applicationId,
      body.rejectionReason,
    );
  }

  @Put('applications/:applicationId/complete')
  @UseGuards(JwtAuthGuard)
  async completeApplication(@Param('applicationId') applicationId: string) {
    return this.adoptionService.completeApplication(applicationId);
  }
}
