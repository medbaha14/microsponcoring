import { Component, Input, HostListener } from '@angular/core';
import { User } from '../../../models/user.model';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { TokenHandler } from '../../../services/token-handler';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-organisation-card',
  standalone: true,
  templateUrl: './organisation-card.component.html',
  styleUrl: './organisation-card.component.css',
  imports: [CommonModule]
})
export class OrganisationCardComponent {
  @Input() user!: User;
  currentUserType: string | null = null;

  constructor(private router: Router) {
    const currentUser = TokenHandler.getUser();
    this.currentUserType = currentUser?.userType || null;
  }

  resolveImageUrl(url: string | undefined): string {
    if (!url) return '';
    if (url.startsWith('http') || url.startsWith('data:image')) {
      return url;
    }
    return `${environment.baseUrl}${url}`;
  }

  @HostListener('click')
  goToProfile() {
    console.log('Card clicked', this.user?.userId);
    if (this.user && this.user.userId) {
      if (this.currentUserType === 'SPONSOR') {
        window.open(`/profile/${this.user.userId}`, '_blank');
      } else {
        this.router.navigate(['/profile', this.user.userId]);
      }
    }
  }
} 