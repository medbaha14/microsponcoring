import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OrganisationProfileComponent } from './profile.component';

describe('ProfileComponent', () => {
  let component: OrganisationProfileComponent;
  let fixture: ComponentFixture<OrganisationProfileComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [OrganisationProfileComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(OrganisationProfileComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
