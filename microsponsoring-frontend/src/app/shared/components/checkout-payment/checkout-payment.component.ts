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

  loading = false;          // état "en cours"
  paymentLocked = false;    // verrou anti double-clic
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
    // récup companyId si non passé en @Input
    if (!this.companyId) {
      this.route.paramMap.subscribe(params => {
        const urlCompanyId = params.get('companyId');
        if (urlCompanyId) {
          this.companyId = urlCompanyId;
        } else {
          const url = window.location.href;
          const match = url.match(/profile\/([a-f0-9-]{36})/);
          if (match) this.companyId = match[1];
        }
      });
    }

    const user = TokenHandler.getUser();
    if (user) this.paymentData.cardholderName = user.fullName || '';
  }

  get totalAmount(): number {
    return this.benefits.reduce((sum, b) => sum + (b.currency || 0), 0);
  }

  get totalAmountInPence(): number {
    return Math.round(this.totalAmount * 100);
  }

  async processPayment(): Promise<void> {
    // Validation simple
    if (!this.validateForm()) return;

    // Si déjà soumis/verrouillé, on ignore
    if (this.paymentLocked) return;

    // Verrouille immédiatement pour empêcher tout second clic
    this.paymentLocked = true;
    this.loading = true;
    this.error = '';

    try {
      const request: CheckoutPaymentSessionRequest = {
        amount: this.totalAmountInPence,
        currency: 'GBP',
        reference: `Sponsorship-${Date.now()}`,
        shipping: {
          address: { ...this.paymentData.billingAddress }
        },
        billing: {
          address: { ...this.paymentData.billingAddress }
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
          sponsor_id: TokenHandler.getUser()?.sponsor?.sponsorId || '',
          company_id: this.companyId,
          benefit_ids: this.benefits.map(b => b.id || b.benefitId).join(',')
        }
      };

      this.paymentService.createCheckoutSession(request).subscribe({
        next: (response) => {
          this.loading = false;
          // on laisse paymentLocked = true pour empêcher toute resoumission
          this.paymentSuccess.emit(response);
          // Décommente pour rediriger
          // this.redirectToCheckout(response.id);
        },
        error: (err) => {
          // En cas d'échec réseau/API, on redonne la main à l'utilisateur
          this.loading = false;
          this.paymentLocked = false;
          this.error = 'Payment session creation failed. Please try again.';
          this.paymentError.emit(this.error);
          console.error('Payment error:', err);
        }
      });
    } catch (err) {
      this.loading = false;
      this.paymentLocked = false;
      this.error = 'An unexpected error occurred. Please try again.';
      this.paymentError.emit(this.error as any);
      console.error('Payment error:', err);
    }
  }

  private redirectToCheckout(sessionId: string): void {
    if (sessionId.startsWith('mock_session_')) {
      alert('Check the console for debug output before redirect!');
      // window.location.href = `${window.location.origin}/payment/success?session_id=${sessionId}`;
    } else {
      const checkoutUrl = `https://pay.sandbox.checkout.com/pay/${sessionId}`;
      window.location.href = checkoutUrl;
    }
  }

  private validateForm(): boolean {
    if (!this.paymentData.cardNumber || this.paymentData.cardNumber.replace(/\s/g, '').length < 13) {
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
    const a = this.paymentData.billingAddress;
    if (!a.address_line1 || !a.city || !a.zip || !a.country) {
      this.error = 'Please complete billing address';
      return false;
    }
    return true;
  }

  formatCardNumber(event: any): void {
    let value = (event.target.value as string).replace(/\s/g, '');
    value = value.replace(/\D/g, '');
    value = value.replace(/(\d{4})/g, '$1 ').trim();
    this.paymentData.cardNumber = value;
  }

  formatExpiry(event: any, field: 'expiryMonth' | 'expiryYear'): void {
    let value = (event.target.value as string).replace(/\D/g, '');
    if (field === 'expiryMonth') {
      if (parseInt(value || '0', 10) > 12) value = '12';
      if (value.length > 2) value = value.substring(0, 2);
    } else {
      if (value.length > 2) value = value.substring(0, 2);
    }
    this.paymentData[field] = value;
  }
}
