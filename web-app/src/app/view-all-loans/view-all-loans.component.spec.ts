import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ViewAllLoansComponent } from './view-all-loans.component';

describe('ViewAllLoansComponent', () => {
  let component: ViewAllLoansComponent;
  let fixture: ComponentFixture<ViewAllLoansComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ViewAllLoansComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewAllLoansComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
