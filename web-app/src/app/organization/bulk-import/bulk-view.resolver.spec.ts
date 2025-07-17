import { TestBed } from '@angular/core/testing';

import { BulkViewResolver } from './bulk-view.resolver';

describe('BulkViewResolver', () => {
  let resolver: BulkViewResolver;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    resolver = TestBed.inject(BulkViewResolver);
  });

  it('should be created', () => {
    expect(resolver).toBeTruthy();
  });
});
