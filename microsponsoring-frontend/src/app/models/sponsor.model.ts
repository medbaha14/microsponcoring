import { Invoice } from './invoice.model';
import { User } from './user.model';

export interface Sponsor {
  sponsorId: string;
  sponcerCat: string;
  paymentMethod: string;
  totalSponsorships: number;
  totalAmountSpent: number;
  createdAt: Date;
  updatedAt: Date;
  user: User;
  invoices?: Invoice[];
} 