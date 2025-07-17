import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OfficeBasicDetailsComponent } from './office-basic-details.component';

describe('OfficeBasicDetailsComponent', () => {
  let component: OfficeBasicDetailsComponent;
  let fixture: ComponentFixture<OfficeBasicDetailsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ OfficeBasicDetailsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(OfficeBasicDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
