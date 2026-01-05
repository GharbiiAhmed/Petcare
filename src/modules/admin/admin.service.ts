// src/modules/admin/admin.service.ts
import { Injectable, NotFoundException, BadRequestException, ForbiddenException } from '@nestjs/common';
import { InjectModel } from '@nestjs/mongoose';
import { Model, Types } from 'mongoose';
import { User, UserDocument } from '../users/schemas/user.schema';
import { SubscriptionsService } from '../subscriptions/subscriptions.service';

@Injectable()
export class AdminService {
  constructor(
    @InjectModel(User.name)
    private readonly userModel: Model<UserDocument>,
    private readonly subscriptionsService: SubscriptionsService,
  ) {}

  /**
   * Get all users (admin only)
   */
  async getAllUsers(adminId: string): Promise<UserDocument[]> {
    await this.ensureAdmin(adminId);
    return this.userModel.find().exec();
  }

  /**
   * Get pending role approval requests (vet and trainer)
   */
  async getPendingRoleApprovals(adminId: string): Promise<UserDocument[]> {
    await this.ensureAdmin(adminId);
    return this.userModel.find({
      role: { $in: ['vet', 'trainer'] },
      roleApprovalStatus: 'pending',
    }).exec();
  }

  /**
   * Approve a user's role request (vet or trainer)
   */
  async approveRole(adminId: string, userId: string, notes?: string): Promise<UserDocument> {
    await this.ensureAdmin(adminId);
    
    const user = await this.userModel.findById(userId);
    if (!user) {
      throw new NotFoundException('User not found');
    }

    if (user.roleApprovalStatus !== 'pending') {
      throw new BadRequestException(`User role approval status is not pending. Current status: ${user.roleApprovalStatus}`);
    }

    if (user.role !== 'vet' && user.role !== 'trainer') {
      throw new BadRequestException('Only vet and trainer roles require approval');
    }

    // Approve the role
    user.roleApprovalStatus = 'approved';
    if (notes) {
      user.roleApprovalNotes = notes;
    }
    await user.save();

    // Ensure user has active subscription (required for professional roles)
    const subscription = await this.subscriptionsService.findByUserId(userId);
    if (!subscription || subscription.status !== 'active' && subscription.status !== 'expires_soon') {
      console.warn(`⚠️ User ${userId} approved for ${user.role} role but has no active subscription`);
    }

    console.log(`✅ Admin ${adminId} approved ${user.role} role for user ${userId}`);
    return user;
  }

  /**
   * Reject a user's role request (vet or trainer)
   */
  async rejectRole(adminId: string, userId: string, notes: string): Promise<UserDocument> {
    await this.ensureAdmin(adminId);
    
    const user = await this.userModel.findById(userId);
    if (!user) {
      throw new NotFoundException('User not found');
    }

    if (user.roleApprovalStatus !== 'pending') {
      throw new BadRequestException(`User role approval status is not pending. Current status: ${user.roleApprovalStatus}`);
    }

    if (user.role !== 'vet' && user.role !== 'trainer') {
      throw new BadRequestException('Only vet and trainer roles require approval');
    }

    // Reject the role and downgrade to owner
    user.roleApprovalStatus = 'rejected';
    user.roleApprovalNotes = notes;
    const previousRole = user.role;
    user.role = 'owner';
    await user.save();

    console.log(`❌ Admin ${adminId} rejected ${previousRole} role for user ${userId}. Downgraded to owner.`);
    return user;
  }

  /**
   * Get user by ID (admin only)
   */
  async getUserById(adminId: string, userId: string): Promise<UserDocument> {
    await this.ensureAdmin(adminId);
    const user = await this.userModel.findById(userId);
    if (!user) {
      throw new NotFoundException('User not found');
    }
    return user;
  }

  /**
   * Delete a user (admin only)
   */
  async deleteUser(adminId: string, userId: string): Promise<void> {
    await this.ensureAdmin(adminId);
    const result = await this.userModel.findByIdAndDelete(userId);
    if (!result) {
      throw new NotFoundException('User not found');
    }
    console.log(`🗑️ Admin ${adminId} deleted user ${userId}`);
  }

  /**
   * Ensure user is an admin
   */
  private async ensureAdmin(userId: string): Promise<void> {
    const user = await this.userModel.findById(userId);
    if (!user) {
      throw new NotFoundException('User not found');
    }
    if (user.role !== 'admin') {
      throw new ForbiddenException('Only admins can perform this action');
    }
  }
}

