import { TestBed } from '@angular/core/testing';

import { LoanProductServierFeeResolver } from './loan-product-servier-fee.resolver';

describe('LoanProductServierFeeResolver', () => {
  let resolver: LoanProductServierFeeResolver;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    resolver = TestBed.inject(LoanProductServierFeeResolver);
  });

  it('should be created', () => {
    expect(resolver).toBeTruthy();
  });
});
