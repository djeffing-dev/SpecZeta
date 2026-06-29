import { TestBed } from '@angular/core/testing';

import { FacebookOuth2Service } from './facebook-outh2.service';

describe('FacebookOuth2Service', () => {
  let service: FacebookOuth2Service;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(FacebookOuth2Service);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
