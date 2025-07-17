import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DisbursementSummaryComponent } from './disbursement-summary.component';

describe('DisbursementSummaryComponent', () => {
  let component: DisbursementSummaryComponent;
  let fixture: ComponentFixture<DisbursementSummaryComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DisbursementSummaryComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DisbursementSummaryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
