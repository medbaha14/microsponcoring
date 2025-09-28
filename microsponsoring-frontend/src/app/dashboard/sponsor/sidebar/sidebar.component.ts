import { Component } from '@angular/core';
import { RouterModule } from '@angular/router';
import { ThemeService } from '../../../services/theme.service';
import { NotificationService } from '../../../services/notification.service';
import { Notification } from '../../../models/notification.model';
import { NotificationDropdownComponent } from '../../shared/notification-dropdown/notification-dropdown.component';

@Component({
  selector: 'app-sponsor-sidebar',
  standalone: true,
  imports: [RouterModule, NotificationDropdownComponent],
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.css']
})
export class SidebarComponent {
  isDarkMode = false;
  isExpanded = true;
  notifications: Notification[] = [];
  showNotifications = false;
  unreadCount = 0;

  constructor(private themeService: ThemeService,
    private notificationService: NotificationService) {}

  ngOnInit() {
    this.themeService.darkMode$.subscribe(isDark => {
      this.isDarkMode = isDark;
    });
  }

  toggleDarkMode() {
    this.themeService.toggleDarkMode();
  }

  toggleSidebar() {
    this.isExpanded = !this.isExpanded;
  }
}
