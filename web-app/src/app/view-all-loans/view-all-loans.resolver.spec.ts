import { TestBed } from '@angular/core/testing';

import { ViewAllLoansResolver } from './view-all-loans.resolver';

describe('ViewAllLoansResolver', () => {
  let resolver: ViewAllLoansResolver;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    resolver = TestBed.inject(ViewAllLoansResolver);
  });

  it('should be created', () => {
    expect(resolver).toBeTruthy();
  });
});
