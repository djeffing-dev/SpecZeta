import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ListUserAnnonceComponent } from './list-user-annonce.component';

describe('ListUserAnnonceComponent', () => {
  let component: ListUserAnnonceComponent;
  let fixture: ComponentFixture<ListUserAnnonceComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ListUserAnnonceComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ListUserAnnonceComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
