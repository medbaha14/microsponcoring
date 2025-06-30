import { User } from './user.model';
import { PageCustomizations } from './page-customizations.model';

export interface CompanyNonProfits {
  companyId: string;
  activityType: string;
  details: string;
  totalSponsorships: number;
  totalAmountReceived: number;
  createdAt: Date;
  updatedAt: Date;
  user: User;
  pageCustomizations?: PageCustomizations;
} 