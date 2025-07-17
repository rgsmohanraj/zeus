import { ComponentFixture, TestBed } from '@angular/core/testing';

import { InitiatePennydropComponent } from './initiate-pennydrop.component';

describe('InitiatePennydropComponent', () => {
  let component: InitiatePennydropComponent;
  let fixture: ComponentFixture<InitiatePennydropComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ InitiatePennydropComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(InitiatePennydropComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
