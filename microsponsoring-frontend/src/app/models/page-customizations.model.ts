import { User } from './user.model';

export interface PageCustomizations {
  id?: number;
  backgroundColor?: string;
  backgroundImage?: string;
  fontStyle?: string;
  user?: User;
} 