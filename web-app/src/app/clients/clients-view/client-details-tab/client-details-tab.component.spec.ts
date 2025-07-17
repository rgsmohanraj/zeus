import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ClientDetailsTabComponent } from './client-details-tab.component';

describe('ClientDetailsTabComponent', () => {
  let component: ClientDetailsTabComponent;
  let fixture: ComponentFixture<ClientDetailsTabComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ ClientDetailsTabComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ClientDetailsTabComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
