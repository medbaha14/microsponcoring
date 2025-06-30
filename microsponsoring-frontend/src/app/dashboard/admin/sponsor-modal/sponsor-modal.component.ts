import { Component, Input, Output, EventEmitter, OnChanges, SimpleChanges } from '@angular/core';
import { Sponsor } from '../../../models/sponsor.model';
import { SponsorService } from '../../../services/sponsor.service';
import { User } from '../../../models/user.model';

@Component({
  selector: 'app-sponsor-modal',
  templateUrl: './sponsor-modal.component.html',
  styleUrls: ['./sponsor-modal.component.css'],
  // styleUrls removed because the file does not exist
})
export class SponsorModalComponent implements OnChanges {
  @Input() mode: 'view' | 'edit' | 'add' = 'view';
  @Input() sponsor: Sponsor | null = null;
  @Input() selectedEditUser?: User | null;
  @Output() close = new EventEmitter<void>();
  @Output() operationSuccess = new EventEmitter<void>();

  formSponsor: Sponsor = {
    sponsorId: '',
    sponcerCat: '',
    paymentMethod: '',
    totalSponsorships: 0,
    totalAmountSpent: 0,
    createdAt: new Date(),
    updatedAt: new Date(),
    user: {} as any
  };

  constructor(private sponsorService: SponsorService) {}

  ngOnChanges(changes: SimpleChanges) {
    if (changes['sponsor'] && this.sponsor) {
      this.formSponsor = { ...this.sponsor };
    } else if (this.mode === 'add') {
      this.formSponsor = {
        sponsorId: '',
        sponcerCat: '',
        paymentMethod: '',
        totalSponsorships: 0,
        totalAmountSpent: 0,
        createdAt: new Date(),
        updatedAt: new Date(),
        user: {} as any
      };
    }
  }

  onSubmit() {
    console.log(this.selectedEditUser);
    
    if (this.selectedEditUser) {
      this.formSponsor = { ...this.formSponsor, user: this.selectedEditUser };
    }
    this.save();
  }

  save() {
    if (this.mode === 'add') {
      this.sponsorService.create(this.formSponsor).subscribe({
        next: () => {
          this.operationSuccess.emit();
          this.close.emit();
        },
        error: (error) => {
          console.error('Error creating sponsor:', error);
        }
      });
    } else if (this.mode === 'edit' && this.sponsor?.sponsorId) {
      this.sponsorService.update(parseInt(this.sponsor.sponsorId), this.formSponsor).subscribe({
        next: () => {
          this.operationSuccess.emit();
          this.close.emit();
        },
        error: (error) => {
          console.error('Error updating sponsor:', error);
        }
      });
    }
  }
} 