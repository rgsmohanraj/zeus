import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PartnerPreviewComponent } from './partner-preview.component';

describe('PartnerPreviewComponent', () => {
  let component: PartnerPreviewComponent;
  let fixture: ComponentFixture<PartnerPreviewComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PartnerPreviewComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(PartnerPreviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
