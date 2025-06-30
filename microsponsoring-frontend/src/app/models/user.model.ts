import { CompanyNonProfits } from './companies-non-profits.model';
import { PageCustomizations } from './page-customizations.model';
import { RecognitionBenefits } from './recognition-benefits.model';
import { Sponsor } from './sponsor.model';

export interface User {
  userId?: string;
  email: string;
  username: string;
  password: string;
  fullName?: string;
  userType?: 'SPONSOR' | 'ORGANISATION_NONPROFIT' | 'ADMIN';
  status?: 'ACTIVE' | 'PENDING' | 'SUSPENDED';
  acceptedConditions?: boolean;
  lastLogin?: string;
  profilePicture?: string;
  bio?: string;
  location?: string;
  websiteUrl?: string;
  isVerified?: boolean;
  createdAt?: string;
  updatedAt?: string;
  companyNonProfits?: CompanyNonProfits;
  pageCustomizations?: PageCustomizations;
  recognitionBenefits?: RecognitionBenefits[];
  sponsor?: Sponsor;
  active?: string;
} 