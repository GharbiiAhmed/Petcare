// src/modules/salons/salons.controller.ts

import {
  Controller,
  Get,
  Post,
  Body,
  Param,
  Delete,
  Put,
  HttpCode,
  HttpStatus,
  UseGuards,
} from '@nestjs/common';
import { SalonsService } from './salons.service';
import { CreateSalonDto } from './dto/create-salon.dto';
import { UpdateSalonDto } from './dto/update-salon.dto';
import { User } from '../users/schemas/user.schema';
import { JwtAuthGuard } from '../auth/guards/jwt-auth.guard';
import { CurrentUser } from '../../common/decorators/current-user.decorator';

@Controller('salons')
export class SalonsController {
  constructor(private readonly salonsService: SalonsService) {}

  @Post()
  @HttpCode(HttpStatus.CREATED)
  async create(@Body() createSalonDto: CreateSalonDto): Promise<User> {
    return this.salonsService.create(createSalonDto);
  }

  @Get()
  async findAll(): Promise<User[]> {
    return this.salonsService.findAll();
  }

  @Get(':id')
  async findOne(@Param('id') id: string): Promise<any> {
    return this.salonsService.findOne(id);
  }

  @Put(':id')
  @UseGuards(JwtAuthGuard)
  async update(
    @Param('id') id: string,
    @Body() updateSalonDto: UpdateSalonDto,
  ): Promise<User> {
    return this.salonsService.update(id, updateSalonDto);
  }

  @Delete(':id')
  @HttpCode(HttpStatus.NO_CONTENT)
  @UseGuards(JwtAuthGuard)
  async remove(@Param('id') id: string): Promise<void> {
    return this.salonsService.remove(id);
  }

  // Convert existing user to salon (called from join form)
  @Post('convert/:userId')
  @UseGuards(JwtAuthGuard)
  @HttpCode(HttpStatus.OK)
  async convertUserToSalon(
    @Param('userId') userId: string,
    @CurrentUser() currentUser: User,
    @Body() salonData: Omit<CreateSalonDto, 'email' | 'name' | 'password'>,
  ): Promise<User> {
    // Ensure user can only convert themselves
    if (currentUser._id.toString() !== userId) {
      throw new Error('You can only convert your own account');
    }
    return this.salonsService.convertUserToSalon(userId, salonData);
  }
}


