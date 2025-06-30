import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SponsoringListComponent } from './sponsoring-list.component';

describe('SponsoringListComponent', () => {
  let component: SponsoringListComponent;
  let fixture: ComponentFixture<SponsoringListComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SponsoringListComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SponsoringListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
