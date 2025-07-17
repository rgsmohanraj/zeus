import { TestBed } from '@angular/core/testing';

import { ClientDetailsViewResolver } from './client-details-view.resolver';

describe('ClientDetailsViewResolver', () => {
  let resolver: ClientDetailsViewResolver;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    resolver = TestBed.inject(ClientDetailsViewResolver);
  });

  it('should be created', () => {
    expect(resolver).toBeTruthy();
  });
});
