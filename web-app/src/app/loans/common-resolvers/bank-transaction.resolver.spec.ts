import { TestBed } from '@angular/core/testing';

import { BankTransactionResolver } from './bank-transaction.resolver';

describe('BankTransactionResolver', () => {
  let resolver: BankTransactionResolver;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    resolver = TestBed.inject(BankTransactionResolver);
  });

  it('should be created', () => {
    expect(resolver).toBeTruthy();
  });
});
