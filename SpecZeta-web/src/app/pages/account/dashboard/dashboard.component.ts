import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AccountUserSiderBarComponent } from "../layout/account-user-sider-bar/account-user-sider-bar.component";

@Component({
  selector: 'app-dashboard',
  imports: [CommonModule, AccountUserSiderBarComponent],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent {
  showNavigation = false;

  toggleNavigation(): void {
    this.showNavigation = !this.showNavigation;
  }

  closeNavigation(): void {
    this.showNavigation = false;
  }
}
