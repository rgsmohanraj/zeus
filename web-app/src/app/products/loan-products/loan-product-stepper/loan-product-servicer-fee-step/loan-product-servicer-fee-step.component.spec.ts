import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LoanProductServicerFeeStepComponent } from './loan-product-servicer-fee-step.component';

describe('LoanProductServicerFeeStepComponent', () => {
  let component: LoanProductServicerFeeStepComponent;
  let fixture: ComponentFixture<LoanProductServicerFeeStepComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ LoanProductServicerFeeStepComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(LoanProductServicerFeeStepComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
