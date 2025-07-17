import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PartnerLegalDocumentsComponent } from './partner-legal-documents.component';

describe('PartnerLegalDocumentsComponent', () => {
  let component: PartnerLegalDocumentsComponent;
  let fixture: ComponentFixture<PartnerLegalDocumentsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ PartnerLegalDocumentsComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(PartnerLegalDocumentsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
