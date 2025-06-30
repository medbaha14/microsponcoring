import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';

@Component({
  selector: 'app-payment-success',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './payment-success.component.html',
  styleUrls: ['./payment-success.component.css']
})
export class PaymentSuccessComponent implements OnInit {
  paymentId: string = '';
  amount: string = '';
  companyName: string = '';

  constructor(
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    // Get payment details from URL parameters
    this.route.queryParams.subscribe(params => {
      this.paymentId = params['cko-payment-id'] || '';
      this.amount = params['amount'] || '';
      this.companyName = params['company'] || '';
    });
  }

  goToDashboard(): void {
    this.router.navigate(['/dashboard/sponsor']);
  }

  goToOrganisations(): void {
    this.router.navigate(['/dashboard/sponsor/organisations']);
  }
} 