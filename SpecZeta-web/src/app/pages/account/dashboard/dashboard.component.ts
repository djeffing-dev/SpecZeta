import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-dashboard',
  imports: [CommonModule],
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
