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
  Query,
} from '@nestjs/common';
import { BookingsService } from './bookings.service';
import { CreateBookingDto } from './dto/create-booking.dto';
import { UpdateBookingDto } from './dto/update-booking.dto';
import { User } from '../users/schemas/user.schema';
import { JwtAuthGuard } from '../auth/guards/jwt-auth.guard';
import { CurrentUser } from '../../common/decorators/current-user.decorator';

@Controller('bookings')
@UseGuards(JwtAuthGuard)
export class BookingsController {
  constructor(private readonly bookingsService: BookingsService) {}

  @Post()
  @HttpCode(HttpStatus.CREATED)
  async create(
    @CurrentUser() user: User,
    @Body() createBookingDto: CreateBookingDto,
  ) {
    const userId = String(user._id ?? user.id);
    return this.bookingsService.create(userId, createBookingDto);
  }

  @Get()
  async findAll(
    @CurrentUser() user: User,
    @Query('role') role?: 'owner' | 'provider',
  ) {
    const userId = String(user._id ?? user.id);
    return this.bookingsService.findAll(userId, role);
  }

  @Get(':id')
  async findOne(@Param('id') id: string, @CurrentUser() user: User) {
    const userId = String(user._id ?? user.id);
    return this.bookingsService.findOne(id, userId);
  }

  @Put(':id')
  async update(
    @Param('id') id: string,
    @CurrentUser() user: User,
    @Body() updateBookingDto: UpdateBookingDto,
  ) {
    const userId = String(user._id ?? user.id);
    return this.bookingsService.update(id, userId, updateBookingDto);
  }

  @Delete(':id')
  @HttpCode(HttpStatus.NO_CONTENT)
  async remove(@Param('id') id: string, @CurrentUser() user: User) {
    const userId = String(user._id ?? user.id);
    return this.bookingsService.remove(id, userId);
  }
}

