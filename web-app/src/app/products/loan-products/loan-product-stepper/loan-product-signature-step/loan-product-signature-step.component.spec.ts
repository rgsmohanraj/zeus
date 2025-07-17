import { ComponentFixture, TestBed } from '@angular/core/testing';

import { LoanProductSignatureStepComponent } from './loan-product-signature-step.component';

describe('LoanProductSignatureStepComponent', () => {
  let component: LoanProductSignatureStepComponent;
  let fixture: ComponentFixture<LoanProductSignatureStepComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ LoanProductSignatureStepComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(LoanProductSignatureStepComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
