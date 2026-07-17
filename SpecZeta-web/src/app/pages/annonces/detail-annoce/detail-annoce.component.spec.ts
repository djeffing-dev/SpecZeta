import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DetailAnnoceComponent } from './detail-annoce.component';

describe('DetailAnnoceComponent', () => {
  let component: DetailAnnoceComponent;
  let fixture: ComponentFixture<DetailAnnoceComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DetailAnnoceComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DetailAnnoceComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
