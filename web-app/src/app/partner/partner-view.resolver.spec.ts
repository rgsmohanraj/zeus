import { TestBed } from '@angular/core/testing';

import { PartnerViewResolver } from './partner-view.resolver';

describe('PartnerViewResolver', () => {
  let resolver: PartnerViewResolver;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    resolver = TestBed.inject(PartnerViewResolver);
  });

  it('should be created', () => {
    expect(resolver).toBeTruthy();
  });
});
