import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PartnerBasicDetailsComponent } from './partner-basic-details.component';

describe('PartnerBasicDetailsComponent', () => {
  let component: PartnerBasicDetailsComponent;
  let fixture: ComponentFixture<PartnerBasicDetailsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PartnerBasicDetailsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(PartnerBasicDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
