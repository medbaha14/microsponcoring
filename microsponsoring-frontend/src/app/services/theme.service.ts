import { Injectable, PLATFORM_ID, Inject } from '@angular/core';
import { BehaviorSubject } from 'rxjs';
import { isPlatformBrowser } from '@angular/common';

@Injectable({
  providedIn: 'root'
})
export class ThemeService {
  private darkMode = new BehaviorSubject<boolean>(false);
  darkMode$ = this.darkMode.asObservable();

  constructor(@Inject(PLATFORM_ID) private platformId: Object) {
    if (isPlatformBrowser(this.platformId)) {
      // Only access localStorage in the browser
      const isDarkMode = localStorage.getItem('darkMode') === 'true';
      if (isDarkMode) {
        this.setDarkMode(true);
      }
    }
  }

  setDarkMode(isDark: boolean) {
    this.darkMode.next(isDark);
    if (isPlatformBrowser(this.platformId)) {
      localStorage.setItem('darkMode', isDark.toString());
      if (isDark) {
        document.body.classList.add('dark-mode');
      } else {
        document.body.classList.remove('dark-mode');
      }
    }
  }

  toggleDarkMode() {
    this.setDarkMode(!this.darkMode.value);
  }
} 