import { Component, Input, Output, EventEmitter, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PaymentService, CheckoutPaymentSessionRequest } from '../../../services/payment.service';
import { RecognitionBenefits } from '../../../models/recognition-benefits.model';
import { TokenHandler } from '../../../services/token-handler';
import { ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-checkout-payment',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './checkout-payment.component.html',
  styleUrls: ['./checkout-payment.component.css']
})
export class CheckoutPaymentComponent implements OnInit {
  @Input() benefits: RecognitionBenefits[] = [];
  @Input() companyId: string = '';
  @Input() companyName: string = '';
  @Output() paymentSuccess = new EventEmitter<any>();
  @Output() paymentError = new EventEmitter<string>();

  loading = false;
  error = '';
  
  // Payment form data
  paymentData = {
    cardNumber: '',
    expiryMonth: '',
    expiryYear: '',
    cvv: '',
    cardholderName: '',
    billingAddress: {
      address_line1: '',
      address_line2: '',
      city: '',
      state: '',
      zip: '',
      country: 'GB'
    }
  };

  constructor(
    private paymentService: PaymentService,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    // If companyId is empty, try to get it from the URL
    if (!this.companyId) {
      this.route.paramMap.subscribe(params => {
        const urlCompanyId = params.get('companyId');
        if (urlCompanyId) {
          this.companyId = urlCompanyId;
        } else {
          // Fallback: extract from URL using regex
          const url = window.location.href;
          const match = url.match(/profile\/([a-f0-9-]{36})/);
          if (match) {
            this.companyId = match[1];
          }
        }
      });
    }
    // Initialize with current user data if available
    const user = TokenHandler.getUser();
    if (user) {
      this.paymentData.cardholderName = user.fullName || '';
    }
  }

  get totalAmount(): number {
    return this.benefits.reduce((sum, benefit) => sum + (benefit.currency || 0), 0);
  }

  get totalAmountInPence(): number {
    return Math.round(this.totalAmount * 100); // Convert to pence for Checkout.com
  }

  async processPayment(): Promise<void> {
    if (!this.validateForm()) {
      return;
    }

    this.loading = true;
    this.error = '';

    // Debug prints
    console.log('companyId:', this.companyId);
    console.log('benefits:', this.benefits)

    try {
      const request: CheckoutPaymentSessionRequest = {
        amount: this.totalAmountInPence,
        currency: 'GBP',
        reference: `Sponsorship-${Date.now()}`,
        shipping: {
          address: {
            address_line1: this.paymentData.billingAddress.address_line1,
            address_line2: this.paymentData.billingAddress.address_line2,
            city: this.paymentData.billingAddress.city,
            state: this.paymentData.billingAddress.state,
            zip: this.paymentData.billingAddress.zip,
            country: this.paymentData.billingAddress.country
          }
        },
        billing: {
          address: {
            address_line1: this.paymentData.billingAddress.address_line1,
            address_line2: this.paymentData.billingAddress.address_line2,
            city: this.paymentData.billingAddress.city,
            state: this.paymentData.billingAddress.state,
            zip: this.paymentData.billingAddress.zip,
            country: this.paymentData.billingAddress.country
          }
        },
        threeDs: {
          enabled: false,
          attempt_n3d: false,
          challenge_indicator: 'no_preference',
          exemption: 'low_value',
          allow_upgrade: true
        },
        enabled_payment_methods: ['card'],
        success_url: `${window.location.origin}/payment/success`,
        failure_url: `${window.location.origin}/payment/failure`,
        metadata: {
          sponsor_id: TokenHandler.getUser()?.sponsor.sponsorId || '',
          company_id: this.companyId,
          benefit_ids: this.benefits.map(b => b.id || b.benefitId).join(',')
        }
      };

      this.paymentService.createCheckoutSession(request).subscribe({
        next: (response) => {
          this.loading = false;
          // Redirect to Checkout.com hosted payment page
          // this.redirectToCheckout(response.id);
        },
        error: (err) => {
          this.loading = false;
          this.error = 'Payment session creation failed. Please try again.';
          this.paymentError.emit(this.error);
          console.error('Payment error:', err);
        }
      });

    } catch (err) {
      this.loading = false;
      this.error = 'An unexpected error occurred. Please try again.';
      this.paymentError.emit(this.error);
      console.error('Payment error:', err);
    }
  }

  private redirectToCheckout(sessionId: string): void {
    // Check if this is a mock session (for testing purposes)
    if (sessionId.startsWith('mock_session_')) {
      alert('Check the console for debug output before redirect!');
      // window.location.href = `${window.location.origin}/payment/success?session_id=${sessionId}`;
    } else {
      const checkoutUrl = `https://pay.sandbox.checkout.com/pay/${sessionId}`;
      window.location.href = checkoutUrl;
    }
  }

  private validateForm(): boolean {
    if (!this.paymentData.cardNumber || this.paymentData.cardNumber.length < 13) {
      this.error = 'Please enter a valid card number';
      return false;
    }
    if (!this.paymentData.expiryMonth || !this.paymentData.expiryYear) {
      this.error = 'Please enter card expiry date';
      return false;
    }
    if (!this.paymentData.cvv || this.paymentData.cvv.length < 3) {
      this.error = 'Please enter a valid CVV';
      return false;
    }
    if (!this.paymentData.cardholderName) {
      this.error = 'Please enter cardholder name';
      return false;
    }
    if (!this.paymentData.billingAddress.address_line1 || !this.paymentData.billingAddress.city || 
        !this.paymentData.billingAddress.zip || !this.paymentData.billingAddress.country) {
      this.error = 'Please complete billing address';
      return false;
    }
    return true;
  }

  formatCardNumber(event: any): void {
    let value = event.target.value.replace(/\s/g, '');
    value = value.replace(/\D/g, '');
    value = value.replace(/(\d{4})/g, '$1 ').trim();
    this.paymentData.cardNumber = value;
  }

  formatExpiry(event: any, field: 'expiryMonth' | 'expiryYear'): void {
    let value = event.target.value.replace(/\D/g, '');
    if (field === 'expiryMonth') {
      if (parseInt(value) > 12) value = '12';
      if (value.length > 2) value = value.substring(0, 2);
    } else {
      if (value.length > 2) value = value.substring(0, 2);
    }
    this.paymentData[field] = value;
  }
} 