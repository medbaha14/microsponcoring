import { NotificationType } from './notification-type.model';

export interface Notification {
  id: string;
  message: string;
  createdAt: string;
  isRead: boolean;
  type?: NotificationType;
}
