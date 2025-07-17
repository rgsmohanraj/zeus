import { TestBed } from '@angular/core/testing';

import { ClientDetailsResolver } from './client-details.resolver';

describe('ClientDetailsResolver', () => {
  let resolver: ClientDetailsResolver;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    resolver = TestBed.inject(ClientDetailsResolver);
  });

  it('should be created', () => {
    expect(resolver).toBeTruthy();
  });
});
