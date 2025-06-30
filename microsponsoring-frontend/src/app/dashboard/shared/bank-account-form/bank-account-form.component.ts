import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

export enum PaymentAccountType {
  BANK_ACCOUNT = 'BANK_ACCOUNT',
  PAYPAL = 'PAYPAL',
  STRIPE = 'STRIPE',
  CREDIT_CARD = 'CREDIT_CARD'
}

export interface BankAccount {
  id?: string;
  userId?: string;
  accountType: PaymentAccountType;
  accountHolderName: string;
  accountName?: string; // For PayPal/Stripe account names
  email?: string; // For PayPal accounts
  iban?: string; // For bank accounts
  bic?: string; // For bank accounts
  bankName?: string; // For bank accounts
  country: string;
  currency?: string; // USD, EUR, etc.
  isDefault?: boolean;
  isActive?: boolean;
  metadata?: any; // For additional provider-specific data
}

@Component({
  selector: 'app-bank-account-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './bank-account-form.component.html',
  styleUrl: './bank-account-form.component.css'
})
export class BankAccountFormComponent {
  @Input() bankAccount: BankAccount = {
    accountType: PaymentAccountType.BANK_ACCOUNT,
    accountHolderName: '',
    country: '',
    currency: 'USD',
    isDefault: false,
    isActive: true
  };
  @Input() isEdit = false;
  @Output() save = new EventEmitter<BankAccount>();
  @Output() delete = new EventEmitter<void>();
  @Output() cancel = new EventEmitter<void>();

  PaymentAccountType = PaymentAccountType;
  accountTypes = Object.values(PaymentAccountType);

  get isBankAccount(): boolean {
    return this.bankAccount.accountType === PaymentAccountType.BANK_ACCOUNT;
  }

  get isPayPal(): boolean {
    return this.bankAccount.accountType === PaymentAccountType.PAYPAL;
  }

  get isStripe(): boolean {
    return this.bankAccount.accountType === PaymentAccountType.STRIPE;
  }

  get isCreditCard(): boolean {
    return this.bankAccount.accountType === PaymentAccountType.CREDIT_CARD;
  }

  onAccountTypeChange() {
    // Reset fields when account type changes
    this.bankAccount.iban = '';
    this.bankAccount.bic = '';
    this.bankAccount.bankName = '';
    this.bankAccount.email = '';
    this.bankAccount.accountName = '';
  }

  onSave() {
    // Validate based on account type
    if (this.isBankAccount && (!this.bankAccount.iban || !this.bankAccount.bic || !this.bankAccount.bankName)) {
      alert('Please fill in all required fields for bank account (IBAN, BIC, Bank Name)');
      return;
    }
    
    if (this.isPayPal && !this.bankAccount.email) {
      alert('Please enter a valid PayPal email address');
      return;
    }

    if (this.isStripe && !this.bankAccount.accountName) {
      alert('Please enter a Stripe account name');
      return;
    }

    this.save.emit(this.bankAccount);
  }

  onDelete() {
    this.delete.emit();
  }

  onCancel() {
    this.cancel.emit();
  }
} 