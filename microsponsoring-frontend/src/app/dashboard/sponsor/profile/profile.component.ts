import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SponsorService } from '../../../services/sponsor.service';
import { PaymentService, SponsorStats } from '../../../services/payment.service';
import { Sponsor } from '../../../models/sponsor.model';
import { TokenHandler } from '../../../services/token-handler';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.css'
})
export class ProfileComponent implements OnInit {
  sponsor: Sponsor | null = null;
  stats: SponsorStats | null = null;
  loading = true;
  error = '';

  constructor(
    private sponsorService: SponsorService,
    private paymentService: PaymentService
  ) { }

  ngOnInit(): void {
    this.loadSponsorData();
  }

  loadSponsorData(): void {
    const user = TokenHandler.getUser();
    if (!user || !user.userId) {
      this.error = 'User not authenticated';
      this.loading = false;
      return;
    }

    this.sponsorService.getByUserId(user.userId).subscribe({
      next: (sponsor: Sponsor) => {
        this.sponsor = sponsor;
        if (sponsor.sponsorId) {
          this.loadSponsorStats(sponsor.sponsorId);
        }
      },
      error: (err: any) => {
        this.error = 'Failed to load sponsor data';
        this.loading = false;
        console.error('Error loading sponsor:', err);
      }
    });
  }

  loadSponsorStats(sponsorId: string): void {
    this.paymentService.getSponsorStats(sponsorId).subscribe({
      next: (stats: SponsorStats) => {
        this.stats = stats;
        this.loading = false;
      },
      error: (err: any) => {
        this.error = 'Failed to load statistics';
        this.loading = false;
        console.error('Error loading stats:', err);
      }
    });
  }
}
