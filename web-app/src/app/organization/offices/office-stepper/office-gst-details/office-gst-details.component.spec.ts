import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OfficeGstDetailsComponent } from './office-gst-details.component';

describe('OfficeGstDetailsComponent', () => {
  let component: OfficeGstDetailsComponent;
  let fixture: ComponentFixture<OfficeGstDetailsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ OfficeGstDetailsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(OfficeGstDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
