import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EditRecognitionBenefitsComponent } from './edit-recognition-benefits.component';

describe('EditRecognitionBenefitsComponent', () => {
  let component: EditRecognitionBenefitsComponent;
  let fixture: ComponentFixture<EditRecognitionBenefitsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EditRecognitionBenefitsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EditRecognitionBenefitsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
