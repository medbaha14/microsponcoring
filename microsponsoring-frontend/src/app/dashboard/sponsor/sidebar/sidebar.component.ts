import { Component, OnInit } from '@angular/core';
import { RouterModule } from '@angular/router';
import { ThemeService, Theme } from '../../../services/theme.service';
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
export class SidebarComponent implements OnInit {
  isDarkMode = false;
  isExpanded = true;
  notifications: Notification[] = [];
  showNotifications = false;
  unreadCount = 0;

  constructor(private themeService: ThemeService,
    private notificationService: NotificationService) {}

  ngOnInit() {
    this.themeService.theme$.subscribe(theme => {
      this.isDarkMode = theme === 'dark';
    });
  }

  toggleDarkMode() {
    this.themeService.toggleTheme();
  }

  toggleSidebar() {
    this.isExpanded = !this.isExpanded;
  }
}
