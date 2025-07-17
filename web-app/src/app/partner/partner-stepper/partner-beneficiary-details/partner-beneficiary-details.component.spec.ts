import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PartnerBeneficiaryDetailsComponent } from './partner-beneficiary-details.component';

describe('PartnerBeneficiaryDetailsComponent', () => {
  let component: PartnerBeneficiaryDetailsComponent;
  let fixture: ComponentFixture<PartnerBeneficiaryDetailsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PartnerBeneficiaryDetailsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(PartnerBeneficiaryDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
