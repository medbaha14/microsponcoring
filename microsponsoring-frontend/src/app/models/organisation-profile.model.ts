import { RecognitionBenefits } from "./recognition-benefits.model";

export interface OrganisationProfile {
  // User fields
  userId?: string;
  username?: string;
  email?: string;
  fullName?: string;
  profilePicture?: string;
  websiteUrl?: string;

  // companyNonProfits fields
  activityType?: string;
  details?: string;
  totalSponsorships?: number;
  companyId?: string;

  // PageCustomizations fields
  backgroundColor?: string;
  primaryColor?: string;
  secondaryColor?: string;
  fontStyle?: string;
  logoUrl?: string;
  bannerImageUrl?: string;
  backgroundImageUrl?: string;

  // Local fields
  recognitionBenefits?: RecognitionBenefits[];
} 