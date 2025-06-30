import { Component, EventEmitter, Output, Input } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { RecognitionBenefitsService } from '../../../services/recognition-benefits.service';
import { RecognitionBenefits } from '../../../models/recognition-benefits.model';
import { CompanyNonProfits } from '../../../models/companies-non-profits.model';
import Swal from 'sweetalert2';

@Component({
  selector: 'app-edit-recognition-benefits',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './edit-recognition-benefits.component.html',
  styleUrl: './edit-recognition-benefits.component.css'
})
export class EditRecognitionBenefitsComponent {
  @Input() company: CompanyNonProfits | null = null;
  @Output() benefitSaved = new EventEmitter<void>();
  @Output() close = new EventEmitter<void>();
  benefitForm: FormGroup;

  constructor(
    private fb: FormBuilder,
    private recognitionBenefitsService: RecognitionBenefitsService
  ) {
    this.benefitForm = this.fb.group({
      rewardType: ['', Validators.required],
      currency: [null],
      sponsorshipType: ['', Validators.required],
      showName: [false],
      showLogo: [false],
      logoSize: [''],
      placement: ['']
    });
  }

  saveBenefit() {
    if (this.benefitForm.valid && this.company) {
      const benefitData: RecognitionBenefits = {
        ...this.benefitForm.value,
        currency: Number(this.benefitForm.value.currency),
        companyNonProfits: this.company
      };

      this.recognitionBenefitsService.create(benefitData).subscribe({
        next: () => {
          Swal.fire('Success', 'Benefit saved successfully!', 'success');
          this.benefitForm.reset();
          this.benefitSaved.emit();
        },
        error: (err) => {
          console.error('Error saving benefit:', err);
          Swal.fire('Error', 'Failed to save benefit.', 'error');
        }
      });
    } else {
      Swal.fire('Error', 'Please fill out all required fields or ensure company is present.', 'error');
    }
  }
}
