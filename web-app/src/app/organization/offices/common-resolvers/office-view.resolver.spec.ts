import { TestBed } from '@angular/core/testing';

import { OfficeViewResolver } from './office-view.resolver';

describe('OfficeViewResolver', () => {
  let resolver: OfficeViewResolver;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    resolver = TestBed.inject(OfficeViewResolver);
  });

  it('should be created', () => {
    expect(resolver).toBeTruthy();
  });
});
