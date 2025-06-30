import { User } from './user.model';

export interface RecognitionBenefits {
  id?: string;
  benefitId?: string;
  name?: string;
  rewardType?: string;
  currency?: number;
  currencyType?: string;
  sponsorshipType?: string;
  showName?: boolean;
  showLogo?: boolean;
  logoSize?: string;
  placement?: string;
  createdAt?: string;
  updatedAt?: string;
  user?: User;
} 