import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { MonthlyAccrualTabComponent } from './monthly-accrual-tab.component';

describe('MonthlyAccrualTabComponent', () => {
  let component: MonthlyAccrualTabComponent;
  let fixture: ComponentFixture<MonthlyAccrualTabComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ MonthlyAccrualTabComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(MonthlyAccrualTabComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
