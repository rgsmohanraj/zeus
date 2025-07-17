import { TestBed } from '@angular/core/testing';

import { ViewAllLoansSearchTemplateResolver } from './view-all-loans-search-template.resolver';

describe('ViewAllLoansSearchTemplateResolver', () => {
  let resolver: ViewAllLoansSearchTemplateResolver;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    resolver = TestBed.inject(ViewAllLoansSearchTemplateResolver);
  });

  it('should be created', () => {
    expect(resolver).toBeTruthy();
  });
});
