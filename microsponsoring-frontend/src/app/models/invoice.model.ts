import { Sponsor } from './sponsor.model';

export interface Invoice {
  invoiceId?: string;
  amount: number;
  status: string;
  createdAt?: Date;
  updatedAt?: Date;
  company?: any; // companyNonProfits
  sponsor?: any; // Sponsor
  invoiceDate?: Date;
  paymentStatus?: string;
  acceptedTerms?: boolean;
  generatedPdfUrl?: string;
  benefits?: any[]; // RecognitionBenefits[]
}

export enum PaymentStatus {
  PENDING = 'PENDING',
  PAID = 'PAID',
  FAILED = 'FAILED',
  CANCELLED = 'CANCELLED'
} 