import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AccountUserSiderBarComponent } from './account-user-sider-bar.component';

describe('AccountUserSiderBarComponent', () => {
  let component: AccountUserSiderBarComponent;
  let fixture: ComponentFixture<AccountUserSiderBarComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AccountUserSiderBarComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AccountUserSiderBarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
