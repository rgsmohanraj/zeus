import { TestBed } from '@angular/core/testing';

import { PartnerUpdateResolver } from './partner-update.resolver';

describe('PartnerUpdateResolver', () => {
  let resolver: PartnerUpdateResolver;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    resolver = TestBed.inject(PartnerUpdateResolver);
  });

  it('should be created', () => {
    expect(resolver).toBeTruthy();
  });
});
