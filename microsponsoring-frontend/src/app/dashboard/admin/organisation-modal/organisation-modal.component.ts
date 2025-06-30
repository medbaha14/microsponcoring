import { Component, Input, Output, EventEmitter, OnChanges, SimpleChanges } from '@angular/core';
import { CompanyNonProfits } from '../../../models/companies-non-profits.model';
import { companyNonProfitsService } from '../../../services/companies-non-profits.service';
import { User } from '../../../models/user.model';

@Component({
  selector: 'app-organisation-modal',
  templateUrl: './organisation-modal.component.html',
  styleUrls: ['./organisation-modal.component.css'],
  // styleUrls removed because the file does not exist
})
export class OrganisationModalComponent implements OnChanges {
  @Input() mode: 'view' | 'edit' | 'add' = 'view';
  @Input() organisation: CompanyNonProfits | null = null;
  @Input() selectedEditUser?: User | null;
  @Output() close = new EventEmitter<void>();
  @Output() operationSuccess = new EventEmitter<void>();

  formOrganisation: CompanyNonProfits = {
    companyId: '',
    activityType: '',
    details: '',
    totalSponsorships: 0,
    totalAmountReceived: 0,
    createdAt: new Date(),
    updatedAt: new Date(),
    user: {} as any
  };

  constructor(private companiesService: companyNonProfitsService) {}

  ngOnChanges(changes: SimpleChanges) {
    if (changes['organisation'] && this.organisation) {
      this.formOrganisation = { ...this.organisation };
    } else if (this.mode === 'add') {
      this.formOrganisation = {
        companyId: '',
        activityType: '',
        details: '',
        totalSponsorships: 0,
        totalAmountReceived: 0,
        createdAt: new Date(),
        updatedAt: new Date(),
        user: {} as any
      };
    }
  }

  save() {
    if (this.mode === 'add') {
      this.companiesService.create(this.formOrganisation).subscribe({
        next: () => {
          this.operationSuccess.emit();
          this.close.emit();
        },
        error: (error) => {
          console.error('Error creating organisation:', error);
        }
      });
    } else if (this.mode === 'edit' && this.organisation?.companyId) {
      this.companiesService.update(parseInt(this.organisation.companyId), this.formOrganisation).subscribe({
        next: () => {
          this.operationSuccess.emit();
          this.close.emit();
        },
        error: (error) => {
          console.error('Error updating organisation:', error);
        }
      });
    }
  }
} 