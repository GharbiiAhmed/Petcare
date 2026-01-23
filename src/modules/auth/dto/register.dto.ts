// src/modules/auth/dto/register.dto.ts
import {
  IsEmail,
  IsNotEmpty,
  IsString,
  IsEnum,
  MinLength,
} from 'class-validator';
import { UserRole } from '../../users/dto/create-user.dto';

export class RegisterDto {
  @IsEmail()
  email: string;

  @IsNotEmpty()
  @IsString()
  name: string;

  @IsString()
  @MinLength(6)
  password: string;

  @IsEnum(UserRole, {
    message:
      'role must be one of the following values: owner, vet, admin, sitter, trainer, salon',
  })
  role: UserRole;
}
