import { TestBed } from '@angular/core/testing';

import { PartnerPartnerDataResolver } from './partner-partner-data.resolver';

describe('PartnerPartnerDataResolver', () => {
  let resolver: PartnerPartnerDataResolver;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    resolver = TestBed.inject(PartnerPartnerDataResolver);
  });

  it('should be created', () => {
    expect(resolver).toBeTruthy();
  });
});
