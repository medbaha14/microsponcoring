import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-payment-failure',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './payment-failure.component.html',
  styleUrls: ['./payment-failure.component.css']
})
export class PaymentFailureComponent implements OnInit {
  errorCode: string = '';
  errorMessage: string = '';
  companyName: string = '';

  constructor(
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    // Get error details from URL parameters
    this.route.queryParams.subscribe(params => {
      this.errorCode = params['cko-error-code'] || '';
      this.errorMessage = params['cko-error-message'] || 'Payment was declined or failed to process.';
      this.companyName = params['company'] || '';
    });
  }

  tryAgain(): void {
    // Go back to the organisation page to try payment again
    this.router.navigate(['/dashboard/sponsor/organisations']);
  }

  goToDashboard(): void {
    this.router.navigate(['/dashboard/sponsor']);
  }

  contactSupport(): void {
    // You can implement contact support functionality
    window.open('mailto:support@microsponsoring.com?subject=Payment Issue', '_blank');
  }
} 