import { TestBed } from '@angular/core/testing';

import { ViewAllLoansService } from './view-all-loans.service';

describe('ViewAllLoansService', () => {
  let service: ViewAllLoansService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ViewAllLoansService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
