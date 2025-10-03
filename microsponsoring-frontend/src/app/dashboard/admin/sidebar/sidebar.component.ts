import { Component, OnInit } from '@angular/core';
import { RouterModule } from '@angular/router';
import { ThemeService } from '../../../services/theme.service';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [RouterModule],
  templateUrl: './sidebar.component.html',
  styleUrl: './sidebar.component.css'
})
export class SidebarComponent implements OnInit {
  isDarkMode = false;
  isExpanded = true;

  constructor(private themeService: ThemeService) {}

  ngOnInit() {
    this.themeService.theme$.subscribe(theme => {
      this.isDarkMode = theme === 'dark';
    });
    
    // Debug: Log environment information
    console.log('Current environment:', environment);
    console.log('API URL:', environment.apiUrl);
    console.log('Production mode:', environment.production);
  }

  toggleDarkMode() {
    this.themeService.toggleTheme();
  }

  toggleSidebar() {
    this.isExpanded = !this.isExpanded;
  }
}
