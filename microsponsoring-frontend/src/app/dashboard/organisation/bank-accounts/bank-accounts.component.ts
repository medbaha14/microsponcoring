import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BankAccountFormComponent, BankAccount } from '../../shared/bank-account-form/bank-account-form.component';
import { HttpClient } from '@angular/common/http';
import { TokenHandler } from '../../../services/token-handler';
import { PaymentAccountType } from '../../shared/bank-account-form/bank-account-form.component';

@Component({
  selector: 'app-bank-accounts',
  standalone: true,
  imports: [CommonModule, BankAccountFormComponent],
  templateUrl: './bank-accounts.component.html',
  styleUrl: './bank-accounts.component.css'
})
export class BankAccountsComponent implements OnInit {
  bankAccounts: BankAccount[] = [];
  selectedAccount: BankAccount = {
    id: '',
    userId: '',
    accountType: PaymentAccountType.BANK_ACCOUNT,
    accountHolderName: '',
    iban: '',
    bic: '',
    bankName: '',
    country: ''
  };
  showForm = false;
  isEdit = false;
  loading = false;
  private apiUrl = 'http://localhost:8080/api/bank-accounts';

  constructor(private http: HttpClient) {}

  ngOnInit() {
    this.loadAccounts();
  }

  loadAccounts() {
    this.loading = true;
    const user = TokenHandler.getUser();
    if (user && user.userId) {
      this.http.get<BankAccount[]>(`${this.apiUrl}/user/${user.userId}`).subscribe(accounts => {
        this.bankAccounts = accounts;
        this.loading = false;
      }, () => this.loading = false);
    }
  }

  addAccount() {
    this.selectedAccount = {
      id: '',
      userId: '',
      accountType: PaymentAccountType.BANK_ACCOUNT,
      accountHolderName: '',
      iban: '',
      bic: '',
      bankName: '',
      country: ''
    };
    this.isEdit = false;
    this.showForm = true;
  }

  editAccount(account: BankAccount) {
    this.selectedAccount = { ...account };
    this.isEdit = true;
    this.showForm = true;
  }

  saveAccount(account: BankAccount) {
    const user = TokenHandler.getUser();
    if (user && user.userId) {
      account.userId = user.userId;
      if (this.isEdit && account.id) {
        this.http.put<BankAccount>(`${this.apiUrl}/${account.id}`, account).subscribe(() => {
          this.loadAccounts();
          this.showForm = false;
        });
      } else {
        this.http.post<BankAccount>(`${this.apiUrl}`, account).subscribe(() => {
          this.loadAccounts();
          this.showForm = false;
        });
      }
    }
  }

  deleteAccount() {
    if (this.selectedAccount && this.selectedAccount.id) {
      this.http.delete(`${this.apiUrl}/${this.selectedAccount.id}`).subscribe(() => {
        this.loadAccounts();
        this.showForm = false;
      });
    }
  }

  cancelForm() {
    this.showForm = false;
  }
} 