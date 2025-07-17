import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ClientBeneficiaryTabComponent } from './client-beneficiary-tab.component';

describe('ClientBeneficiaryTabComponent', () => {
  let component: ClientBeneficiaryTabComponent;
  let fixture: ComponentFixture<ClientBeneficiaryTabComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ClientBeneficiaryTabComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ClientBeneficiaryTabComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
