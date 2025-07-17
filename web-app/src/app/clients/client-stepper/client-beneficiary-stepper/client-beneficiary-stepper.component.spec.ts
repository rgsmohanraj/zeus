import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ClientBeneficiaryStepperComponent } from './client-beneficiary-stepper.component';

describe('ClientBeneficiaryStepperComponent', () => {
  let component: ClientBeneficiaryStepperComponent;
  let fixture: ComponentFixture<ClientBeneficiaryStepperComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ClientBeneficiaryStepperComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ClientBeneficiaryStepperComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
