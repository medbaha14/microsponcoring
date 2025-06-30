export interface RegisterRequest {
  email: string;
  username: string;
  password: string;
  fullName?: string;
  userType?: 'SPONSOR' | 'ORGANISATION_NONPROFIT' | 'ADMIN';
} 