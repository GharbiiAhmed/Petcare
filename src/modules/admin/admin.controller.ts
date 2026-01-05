// src/modules/admin/admin.controller.ts
import {
  Controller,
  Get,
  Post,
  Body,
  Param,
  Delete,
  UseGuards,
  HttpCode,
  HttpStatus,
} from '@nestjs/common';
import {
  ApiTags,
  ApiOperation,
  ApiBearerAuth,
  ApiResponse,
} from '@nestjs/swagger';
import { AdminService } from './admin.service';
import { JwtAuthGuard } from '../auth/guards/jwt-auth.guard';
import { CurrentUser } from '../../common/decorators/current-user.decorator';
import { UserDocument } from '../users/schemas/user.schema';

@ApiTags('admin')
@Controller('admin')
@UseGuards(JwtAuthGuard)
@ApiBearerAuth()
export class AdminController {
  constructor(private readonly adminService: AdminService) {}

  @Get('users')
  @ApiOperation({ summary: 'Get all users (admin only)' })
  @ApiResponse({ status: 200, description: 'List of all users' })
  @ApiResponse({ status: 403, description: 'Forbidden - not an admin' })
  async getAllUsers(@CurrentUser() user: UserDocument) {
    return this.adminService.getAllUsers(user._id.toString());
  }

  @Get('users/:id')
  @ApiOperation({ summary: 'Get user by ID (admin only)' })
  @ApiResponse({ status: 200, description: 'User details' })
  @ApiResponse({ status: 404, description: 'User not found' })
  @ApiResponse({ status: 403, description: 'Forbidden - not an admin' })
  async getUserById(
    @CurrentUser() user: UserDocument,
    @Param('id') userId: string,
  ) {
    return this.adminService.getUserById(user._id.toString(), userId);
  }

  @Get('role-approvals/pending')
  @ApiOperation({ summary: 'Get pending role approval requests (admin only)' })
  @ApiResponse({ status: 200, description: 'List of users with pending role approvals' })
  @ApiResponse({ status: 403, description: 'Forbidden - not an admin' })
  async getPendingRoleApprovals(@CurrentUser() user: UserDocument) {
    return this.adminService.getPendingRoleApprovals(user._id.toString());
  }

  @Post('role-approvals/:userId/approve')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: 'Approve a user\'s role request (admin only)' })
  @ApiResponse({ status: 200, description: 'Role approved successfully' })
  @ApiResponse({ status: 404, description: 'User not found' })
  @ApiResponse({ status: 400, description: 'Bad request - role not pending' })
  @ApiResponse({ status: 403, description: 'Forbidden - not an admin' })
  async approveRole(
    @CurrentUser() user: UserDocument,
    @Param('userId') userId: string,
    @Body() body: { notes?: string },
  ) {
    return this.adminService.approveRole(user._id.toString(), userId, body.notes);
  }

  @Post('role-approvals/:userId/reject')
  @HttpCode(HttpStatus.OK)
  @ApiOperation({ summary: 'Reject a user\'s role request (admin only)' })
  @ApiResponse({ status: 200, description: 'Role rejected successfully' })
  @ApiResponse({ status: 404, description: 'User not found' })
  @ApiResponse({ status: 400, description: 'Bad request - role not pending' })
  @ApiResponse({ status: 403, description: 'Forbidden - not an admin' })
  async rejectRole(
    @CurrentUser() user: UserDocument,
    @Param('userId') userId: string,
    @Body() body: { notes: string },
  ) {
    return this.adminService.rejectRole(user._id.toString(), userId, body.notes);
  }

  @Delete('users/:id')
  @HttpCode(HttpStatus.NO_CONTENT)
  @ApiOperation({ summary: 'Delete a user (admin only)' })
  @ApiResponse({ status: 204, description: 'User deleted successfully' })
  @ApiResponse({ status: 404, description: 'User not found' })
  @ApiResponse({ status: 403, description: 'Forbidden - not an admin' })
  async deleteUser(
    @CurrentUser() user: UserDocument,
    @Param('id') userId: string,
  ) {
    await this.adminService.deleteUser(user._id.toString(), userId);
  }
}



