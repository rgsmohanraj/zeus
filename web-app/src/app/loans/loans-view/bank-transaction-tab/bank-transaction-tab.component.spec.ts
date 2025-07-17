import { ComponentFixture, TestBed } from '@angular/core/testing';

import { BankTransactionTabComponent } from './bank-transaction-tab.component';

describe('BankTransactionTabComponent', () => {
  let component: BankTransactionTabComponent;
  let fixture: ComponentFixture<BankTransactionTabComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ BankTransactionTabComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(BankTransactionTabComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
