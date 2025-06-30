import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RecognitionBenefitsComponent } from './recognition-benefits.component';

describe('RecognitionBenefitsComponent', () => {
  let component: RecognitionBenefitsComponent;
  let fixture: ComponentFixture<RecognitionBenefitsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RecognitionBenefitsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RecognitionBenefitsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
