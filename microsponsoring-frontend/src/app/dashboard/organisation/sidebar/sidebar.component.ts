import { Component, OnInit } from '@angular/core';
import { RouterModule } from '@angular/router';
import { ThemeService, Theme } from '../../../services/theme.service';
import { TokenHandler } from '../../../services/token-handler';
import { NotificationDropdownComponent } from '../../shared/notification-dropdown/notification-dropdown.component';

@Component({
  selector: 'app-organisation-sidebar',
  standalone: true,
  imports: [RouterModule, NotificationDropdownComponent],
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.css']
})
export class SidebarComponent implements OnInit {
  isDarkMode = false;
  isExpanded = true;

  constructor(private themeService: ThemeService) {}

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

  shareProfile() {
    const user = TokenHandler.getUser();
    if (user && user.userId) {
      const url = `${window.location.origin}/profile/${user.userId}`;
      if (navigator.share) {
        navigator.share({
          title: 'Check out my organisation profile!',
          url
        });
      } else {
        navigator.clipboard.writeText(url);
        alert('Profile link copied to clipboard!');
      }
    }
  }
}
