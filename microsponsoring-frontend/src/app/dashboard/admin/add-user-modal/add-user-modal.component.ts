import { Component, EventEmitter, Output, Input, OnChanges, SimpleChanges } from '@angular/core';
import { UserService } from '../../../services/user.service';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { User } from '../../../models/user.model';
import { environment } from '../../../../environments/environment';
import { Sponsor } from '../../../models/sponsor.model';
import { CompanyNonProfits } from '../../../models/companies-non-profits.model';
import { PaymentAccountType } from '../../../models/payment-account-type.enum';

@Component({
  selector: 'app-add-user-modal',
  templateUrl: './add-user-modal.component.html',
  styleUrls: ['./add-user-modal.component.css']
})
export class AddUserModalComponent implements OnChanges {
  @Input() user: User | null = null;
  @Output() close = new EventEmitter<void>();
  @Output() userAdded = new EventEmitter<void>();

  newUser: any = {};
  uploadingImage = false;
  public paymentMethods = Object.values(PaymentAccountType);

  get profilePictureUrl(): string {
    if (!this.newUser.profilePicture) {
      return '';
    }
    if (this.newUser.profilePicture.startsWith('http')) {
      return this.newUser.profilePicture;
    }
    return environment.baseUrl + this.newUser.profilePicture;
  }

  constructor(private userService: UserService) {}

  ngOnChanges(changes: SimpleChanges) {
    if (changes['user'] && this.user) {
      this.newUser = { ...this.user };
      // Don't prefill password for edit
      this.newUser.password = '';
      this.setDefaultSponsorFields();
      this.setDefaultCompanyFields();
    } else if (!this.user) {
      this.newUser = {};
      this.setDefaultSponsorFields();
      this.setDefaultCompanyFields();
    }
  }

  private setDefaultSponsorFields() {
    if (this.newUser.userType === 'SPONSOR') {
      this.newUser.paymentMethod = this.newUser.paymentMethod || PaymentAccountType.CREDIT_CARD;
      this.newUser.sponcerCat = this.newUser.sponcerCat || '';
    }
  }

  private setDefaultCompanyFields() {
    if (this.newUser.userType === 'ORGANISATION_NONPROFIT') {
      this.newUser.companyNonProfits.activityType = this.newUser.companyNonProfits.activityType || '';
    }
  }

  onUserTypeChange() {
    if (this.newUser.userType === 'SPONSOR') {
      this.setDefaultSponsorFields();
    } else if (this.newUser.userType === 'ORGANISATION_NONPROFIT') {
      this.setDefaultCompanyFields();
    }
  }

  onFileSelected(event: any) {
    const file: File = event.target.files[0];
    if (file) {
      if (!this.user || !this.user.userId) {
        alert('User must be created or selected before uploading a profile picture.');
        return;
      }
      this.uploadingImage = true;
      const formData = new FormData();
      formData.append('file', file);
      formData.append('userId', this.user.userId);
      fetch(`${environment.uploadUrl}/profile-picture`, {
        method: 'POST',
        body: formData
      })
      .then(res => res.text())
      .then(data => {
        this.newUser.profilePicture = environment.baseUrl + data;
        this.uploadingImage = false;
      })
      .catch(() => {
        alert('Image upload failed');
        this.uploadingImage = false;
      });
    }
  }

  submitAddUser() {
    if (this.user && this.user.userId) {
      // Edit mode
      const updatedUser = { ...this.newUser };

      if (this.newUser.userType === 'SPONSOR') {
        const sponsor: Sponsor = {
          sponcerCat: this.newUser.sponsor?.sponcerCat || this.newUser.sponcerCat || 'GENERAL',
          paymentMethod: this.newUser.sponsor?.paymentMethod || this.newUser.paymentMethod || 'CREDIT_CARD'
        } as Sponsor;
        updatedUser.sponsor = sponsor;
        
        // Clean up any flat properties that might conflict
        delete updatedUser.sponcerCat;
        delete updatedUser.paymentMethod;
        
      } else if (this.newUser.userType === 'ORGANISATION_NONPROFIT') {
        const companyNonProfits: CompanyNonProfits = {
          // Read from the nested structure, not the flat property
          activityType: this.newUser.companyNonProfits?.activityType || 'GENERAL'
        } as CompanyNonProfits;
        updatedUser.companyNonProfits = companyNonProfits;
        
        // Clean up any flat properties that might conflict
        delete updatedUser.activityType;
      }
      
      if (!updatedUser.password) delete updatedUser.password; // Don't send empty password
      this.userService.update(this.user.userId, updatedUser).subscribe({
        next: () => {
          this.userAdded.emit();
        },
        error: err => {
          alert('Failed to update user: ' + (err.error || err.message));
        }
      });
    } else {
      // Add mode - also need to fix here
      const userToCreate = { ...this.newUser };
      
      if (userToCreate.userType === 'SPONSOR') {
        userToCreate.sponsor = {
          sponcerCat: userToCreate.sponsor?.sponcerCat || userToCreate.sponcerCat || 'GENERAL',
          paymentMethod: userToCreate.sponsor?.paymentMethod || userToCreate.paymentMethod || 'CREDIT_CARD'
        };
        delete userToCreate.sponcerCat;
        delete userToCreate.paymentMethod;
        
      } else if (userToCreate.userType === 'ORGANISATION_NONPROFIT') {
        userToCreate.companyNonProfits = {
          activityType: userToCreate.companyNonProfits?.activityType || 'GENERAL'
        };
        delete userToCreate.activityType;
      }
      
      this.userService.create(userToCreate).subscribe({
        next: () => {
          this.userAdded.emit();
        },
        error: err => {
          alert('Failed to add user: ' + (err.error || err.message));
        }
      });
    }
  }
} 