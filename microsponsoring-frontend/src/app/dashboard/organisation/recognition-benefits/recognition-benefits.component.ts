import { Component, Input, OnInit, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RecognitionBenefits } from '../../../models/recognition-benefits.model';
import { CheckoutPaymentComponent } from '../../../shared/components/checkout-payment/checkout-payment.component';

@Component({
  selector: 'app-recognition-benefits',
  standalone: true,
  imports: [CommonModule, CheckoutPaymentComponent],
  templateUrl: './recognition-benefits.component.html',
  styleUrls: ['./recognition-benefits.component.css']
})
export class RecognitionBenefitsComponent implements OnInit {
  @Input() secondaryColor: string = 'var(--color-cream)';
  @Input() bannerImageUrl?: string;
  @Input() recognitionBenefits?: RecognitionBenefits;
  @Input() fontStyle?: string;
  @Input() isSponsor: boolean = false;
  @Input() isOwner: boolean = false;
  @Input() companyId: string = '';
  @Input() companyName: string = '';
  @Output() payClicked = new EventEmitter<void>();
  @Output() editBenefit = new EventEmitter<void>();

  showPaymentForm = false;

  ngOnInit() {
    console.log("companyId", this.companyId);
    
    if (!this.recognitionBenefits) {
      this.recognitionBenefits = {
        rewardType: '',
        currency: 0,
        sponsorshipType: '',
        showName: false,
        showLogo: false,
        logoSize: '',
        placement: ''
      };
    }
  }

  pay() {
    this.showPaymentForm = true;
    this.payClicked.emit();
  }

  onPaymentSuccess(event: any) {
    this.showPaymentForm = false;
    // Handle successful payment
    console.log('Payment successful:', event);
  }

  onPaymentError(error: string) {
    this.showPaymentForm = false;
    // Handle payment error
    console.error('Payment error:', error);
  }

  closePaymentForm() {
    this.showPaymentForm = false;
  }

  edit() {
    this.editBenefit.emit();
  }

  get benefits(): RecognitionBenefits[] {
    return this.recognitionBenefits ? [this.recognitionBenefits] : [];
  }
}
