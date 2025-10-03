import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { SponsorService } from '../../../services/sponsor.service';
import { PaymentService, SponsorStats } from '../../../services/payment.service';
import { Sponsor } from '../../../models/sponsor.model';
import { TokenHandler } from '../../../services/token-handler';
import { UserService } from '../../../services/user.service';

type Draft = {
  fullName?: string;
  email?: string;
  location?: string;
  websiteUrl?: string;
  bio?: string;
};

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './profile.component.html',
  styleUrls: ['./profile.component.css'],
})
export class ProfileComponent implements OnInit {
  sponsor: Sponsor | null = null;
  stats: SponsorStats | null = null;
  user: any = null;

  loading = true;
  error = '';

  // --- ÉDITION DE BLOC ---
  editMode = false;
  saving = false;
  formDraft: Draft = {};

  defaultAvatar =
    'https://ui-avatars.com/api/?name=Sponsor&background=2c3e50&color=ecf0f1';

  constructor(
    private sponsorService: SponsorService,
    private paymentService: PaymentService,
    private userService: UserService
  ) {}

  ngOnInit(): void {
    this.load();
  }

  private load(): void {
    const authUser = TokenHandler.getUser();
    if (!authUser?.userId) {
      this.error = 'User not authenticated';
      this.loading = false;
      return;
    }

    this.userService.getById(authUser.userId).subscribe({
      next: (u) => {
        this.user = u;
        this.loading = false;
      },
      error: () => {
        this.error = 'Failed to load user';
        this.loading = false;
      },
    });

    this.sponsorService.getByUserId(authUser.userId).subscribe({
      next: (sp: Sponsor) => {
        this.sponsor = sp;
        if (sp?.sponsorId) {
          this.paymentService.getSponsorStats(String(sp.sponsorId)).subscribe({
            next: (s) => (this.stats = s),
          });
        }
      },
    });
  }

  // ===== Bloc édition =====
  beginAll() {
    if (!this.user) return;
    this.editMode = true;
    this.formDraft = {
      fullName: this.user.fullName ?? this.user.username ?? '',
      email: this.user.email ?? '',
      location: this.user.location ?? '',
      websiteUrl: this.user.websiteUrl ?? '',
      bio: this.user.bio ?? '',
    };
  }

  cancelAll() {
    this.editMode = false;
    this.formDraft = {};
  }

  private diffPayload(current: any, draft: Draft): Draft {
    const out: Draft = {};
    (Object.keys(draft) as (keyof Draft)[]).forEach((k) => {
      if (draft[k] !== current[k]) out[k] = draft[k];
    });
    return out;
  }

  saveAll() {
    if (!this.user?.userId) return;
    this.saving = true;

    // N’envoie que ce qui change (sinon envoie formDraft tel quel,
    // le @PutMapping ignore les nulls)
    const payload = this.diffPayload(this.user, this.formDraft);

    // rien à changer ?
    if (Object.keys(payload).length === 0) {
      this.saving = false;
      this.editMode = false;
      return;
    }

    // La méthode patch() de ton service fait maintenant un PUT
    this.userService.patch(this.user.userId, payload).subscribe({
      next: () => {
        this.user = { ...this.user, ...payload };
        this.saving = false;
        this.editMode = false;
        this.formDraft = {};
      },
      error: (e) => {
        console.error(e);
        this.saving = false;
      },
    });
  }
}
