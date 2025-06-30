import { Component, Input, OnChanges, SimpleChanges, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { DomSanitizer, SafeStyle } from '@angular/platform-browser';
import { OrganisationProfile } from '../../../models/organisation-profile.model';
import { EditRecognitionBenefitsComponent } from '../edit-recognition-benefits/edit-recognition-benefits.component';
import { RecognitionBenefitsComponent } from '../recognition-benefits/recognition-benefits.component';
import { ProfileUpdateService } from '../../../services/profile-update.service';
import { Subscription } from 'rxjs';
import { UserService } from '../../../services/user.service';
import { TokenHandler } from '../../../services/token-handler';
import { companyNonProfitsService } from '../../../services/companies-non-profits.service';
import { CompanyNonProfits } from '../../../models/companies-non-profits.model';
import { RecognitionBenefitsService } from '../../../services/recognition-benefits.service';
import { RecognitionBenefits } from '../../../models/recognition-benefits.model';
import { PaymentService, CompanyStats } from '../../../services/payment.service';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-organisation-profile',
  standalone: true,
  imports: [CommonModule, EditRecognitionBenefitsComponent, RecognitionBenefitsComponent],
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css']
})
export class OrganisationProfileComponent implements OnInit, OnChanges, OnDestroy {
  @Input() profile: OrganisationProfile | null = null;
  company: CompanyNonProfits | null = null;
  benefits: RecognitionBenefits[] = [];
  stats: CompanyStats | null = null;

  isEditBenefitsOverlayVisible = false;
  backgroundStyle: SafeStyle = '';
  private profileUpdateSubscription!: Subscription;
  userType: string | null = null;
  isOwner: boolean = false;
  isSponsor: boolean = false;

  constructor(
    private sanitizer: DomSanitizer,
    private profileUpdateService: ProfileUpdateService,
    private userService: UserService,
    private companyService: companyNonProfitsService,
    private recognitionBenefitsService: RecognitionBenefitsService,
    private paymentService: PaymentService,
    private route: ActivatedRoute
  ) {}

  ngOnInit() {
    this.profileUpdateSubscription = this.profileUpdateService.profileUpdate$.subscribe(profile => {
      this.profile = profile;
      this.updateBackgroundStyle();
    });

    const user = TokenHandler.getUser();
    this.userType = user?.userType || null;
    this.isSponsor = this.userType === 'SPONSOR';
    // isOwner will be set after profile is loaded
    this.route.paramMap.subscribe(params => {
      const userId = params.get('userId');
      if (userId) {
        this.userService.getOrganisationProfile(userId).subscribe(profile => {
          this.profile = profile;
          this.isOwner = user && profile && user.userId === profile.userId;
          this.updateBackgroundStyle();
        });
        this.companyService.getCompanyByUserId(userId).subscribe(company => {
          this.company = company;
          if (company && company.companyId) {
            this.recognitionBenefitsService.getAllByCompanyId(String(company.companyId)).subscribe(benefits => {
              this.benefits = benefits;
            });
            // Load company statistics
            this.loadCompanyStats(company.companyId);
          }
        });
      } else if (!this.profile) {
        this.loadProfile();
      }
    });
  }

  loadProfile() {
    const user = TokenHandler.getUser();
    if (user && user.userId) {
      this.userService.getOrganisationProfile(user.userId).subscribe(profile => {
        this.profile = profile;
        this.isOwner = user && profile && user.userId === profile.userId;
        this.updateBackgroundStyle();
      });

      this.companyService.getCompanyByUserId(user.userId).subscribe(company => {
        this.company = company;
        if (company && company.companyId) {
          this.recognitionBenefitsService.getAllByCompanyId(String(company.companyId)).subscribe(benefits => {
            this.benefits = benefits;
          });
          // Load company statistics
          this.loadCompanyStats(company.companyId);
        }
      });
    }
  }

  loadCompanyStats(companyId: string): void {
    this.paymentService.getCompanyStats(companyId).subscribe({
      next: (stats: CompanyStats) => {
        this.stats = stats;
      },
      error: (err: any) => {
        console.error('Error loading company stats:', err);
      }
    });
  }

  ngOnDestroy() {
    if (this.profileUpdateSubscription) {
      this.profileUpdateSubscription.unsubscribe();
    }
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes['profile'] && this.profile) {
      this.updateBackgroundStyle();
      
      // If the profile input changes, re-fetch the company and benefits
      if (this.profile.userId) {
        this.companyService.getCompanyByUserId(this.profile.userId).subscribe(company => {
          this.company = company;
          if (company && company.companyId) {
            this.recognitionBenefitsService.getAllByCompanyId(String(company.companyId)).subscribe(benefits => {
              this.benefits = benefits;
            });
            // Load company statistics
            this.loadCompanyStats(company.companyId);
          }
        });
      }
    }
  }

  updateBackgroundStyle() {
    if (!this.profile) return;
    
    let style = '';
    if (this.profile.backgroundImageUrl) {
      const overlayColor = this.hexToRgba(this.profile.backgroundColor || '#ffffff', 0.6);
      style = `background: linear-gradient(${overlayColor}, ${overlayColor}), url('${this.resolveImageUrl(this.profile.backgroundImageUrl)}') center/cover no-repeat;`;
    } else {
      style = `background-color: ${this.profile.backgroundColor || '#ffffff'};`;
    }
    this.backgroundStyle = this.sanitizer.bypassSecurityTrustStyle(style);
  }

  openEditBenefitsOverlay() {
    this.isEditBenefitsOverlayVisible = true;
  }

  closeEditBenefitsOverlay() {
    this.isEditBenefitsOverlayVisible = false;
  }

  resolveImageUrl(url: string | undefined): string {
    if (!url) return '';
    if (url.startsWith('http') || url.startsWith('data:image')) {
      return url;
    }
    return `http://localhost:8080${url}`;
  }

  private hexToRgba(hex: string, alpha: number): string {
    hex = hex.replace('#', '');
    const r = parseInt(hex.substring(0, 2), 16) || 0;
    const g = parseInt(hex.substring(2, 4), 16) || 0;
    const b = parseInt(hex.substring(4, 6), 16) || 0;
    return `rgba(${r}, ${g}, ${b}, ${alpha})`;
  }

  pay() {
    alert('Payment flow coming soon!');
  }
}
