import { TestBed } from '@angular/core/testing';

import { CreateResolver } from './create.resolver';

describe('CreateResolver', () => {
  let resolver: CreateResolver;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    resolver = TestBed.inject(CreateResolver);
  });

  it('should be created', () => {
    expect(resolver).toBeTruthy();
  });
});
