import { TestBed } from '@angular/core/testing';

import { PartnerResolver } from './partner.resolver';

describe('PartnerResolver', () => {
  let resolver: PartnerResolver;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    resolver = TestBed.inject(PartnerResolver);
  });

  it('should be created', () => {
    expect(resolver).toBeTruthy();
  });
});
