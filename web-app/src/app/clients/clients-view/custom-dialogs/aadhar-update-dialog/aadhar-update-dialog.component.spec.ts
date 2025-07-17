import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AadharUpdateDialogComponent } from './aadhar-update-dialog.component';

describe('AadharUpdateDialogComponent', () => {
  let component: AadharUpdateDialogComponent;
  let fixture: ComponentFixture<AadharUpdateDialogComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ AadharUpdateDialogComponent ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AadharUpdateDialogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
