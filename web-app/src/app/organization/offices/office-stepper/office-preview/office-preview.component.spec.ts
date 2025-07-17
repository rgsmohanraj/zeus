import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OfficePreviewComponent } from './office-preview.component';

describe('OfficePreviewComponent', () => {
  let component: OfficePreviewComponent;
  let fixture: ComponentFixture<OfficePreviewComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ OfficePreviewComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(OfficePreviewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
