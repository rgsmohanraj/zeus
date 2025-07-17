import { TestBed } from '@angular/core/testing';

import { PartnerCreateResolver } from './partner-create.resolver';

describe('PartnerCreateResolver', () => {
  let resolver: PartnerCreateResolver;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    resolver = TestBed.inject(PartnerCreateResolver);
  });

  it('should be created', () => {
    expect(resolver).toBeTruthy();
  });
});
