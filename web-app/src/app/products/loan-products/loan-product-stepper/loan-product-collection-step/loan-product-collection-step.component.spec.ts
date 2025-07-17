import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LoanProductCollectionStepComponent } from './loan-product-collection-step.component';

describe('LoanProductCollectionStepComponent', () => {
  let component: LoanProductCollectionStepComponent;
  let fixture: ComponentFixture<LoanProductCollectionStepComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ LoanProductCollectionStepComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(LoanProductCollectionStepComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
