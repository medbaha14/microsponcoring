import { ComponentFixture, TestBed } from '@angular/core/testing';
import { BankAccountsComponent } from './bank-accounts.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';

describe('BankAccountsComponent', () => {
  let component: BankAccountsComponent;
  let fixture: ComponentFixture<BankAccountsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [BankAccountsComponent, HttpClientTestingModule]
    }).compileComponents();
    fixture = TestBed.createComponent(BankAccountsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
}); 