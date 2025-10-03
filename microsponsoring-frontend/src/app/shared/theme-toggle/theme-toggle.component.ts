import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ThemeService, Theme } from '../../services/theme.service';

@Component({
  selector: 'app-theme-toggle',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="theme-toggle" (click)="toggleTheme()">
      <div class="toggle-container" [class.active]="isDarkMode">
        <div class="toggle-slider">
          <span class="toggle-icon">{{ isDarkMode ? '🌙' : '☀️' }}</span>
        </div>
      </div>
      <span class="toggle-label">{{ isDarkMode ? 'Dark' : 'Light' }}</span>
    </div>
  `,
  styles: [`
    .theme-toggle {
      display: flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;
      padding: 8px 12px;
      border-radius: 8px;
      transition: all 0.3s ease;
      user-select: none;
    }

    .theme-toggle:hover {
      background: rgba(255, 255, 255, 0.1);
    }

    .toggle-container {
      position: relative;
      width: 50px;
      height: 24px;
      background: #ccc;
      border-radius: 12px;
      transition: background 0.3s ease;
    }

    .toggle-container.active {
      background: var(--color-medium-teal);
    }

    .toggle-slider {
      position: absolute;
      top: 2px;
      left: 2px;
      width: 20px;
      height: 20px;
      background: white;
      border-radius: 50%;
      transition: transform 0.3s ease;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    .toggle-container.active .toggle-slider {
      transform: translateX(26px);
    }

    .toggle-icon {
      font-size: 12px;
    }

    .toggle-label {
      font-size: 0.9rem;
      font-weight: 500;
      color: var(--color-cream);
    }

    body.light-mode .toggle-label {
      color: var(--color-dark-teal);
    }

    body.light-mode .theme-toggle:hover {
      background: rgba(0, 0, 0, 0.05);
    }
  `]
})
export class ThemeToggleComponent implements OnInit {
  isDarkMode = true;

  constructor(private themeService: ThemeService) {}

  ngOnInit(): void {
    this.themeService.theme$.subscribe(theme => {
      this.isDarkMode = theme === 'dark';
    });
  }

  toggleTheme(): void {
    this.themeService.toggleTheme();
  }
}
