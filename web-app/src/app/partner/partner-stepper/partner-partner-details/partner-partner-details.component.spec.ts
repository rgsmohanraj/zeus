import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PartnerPartnerDetailsComponent } from './partner-partner-details.component';

describe('PartnerPartnerDetailsComponent', () => {
  let component: PartnerPartnerDetailsComponent;
  let fixture: ComponentFixture<PartnerPartnerDetailsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PartnerPartnerDetailsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(PartnerPartnerDetailsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
