import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ViewBulkChargesComponent } from './view-bulk-charges.component';

describe('ViewBulkChargesComponent', () => {
  let component: ViewBulkChargesComponent;
  let fixture: ComponentFixture<ViewBulkChargesComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ViewBulkChargesComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewBulkChargesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
