import { Injectable } from '@nestjs/common';

@Injectable()
export class AppService {
  getStatus(): { message: string } {
    return { message: 'Petcare backend is running 🚀' };
  }
}
