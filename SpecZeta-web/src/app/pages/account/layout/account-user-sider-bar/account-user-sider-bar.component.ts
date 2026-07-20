import { Component, inject, Input, OnInit, PLATFORM_ID } from '@angular/core';
import { AuthService } from '../../../../services/auth/auth.service';
import { User } from '../../../../models/user.model';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { RouterModule } from '@angular/router';

@Component({
  selector: 'app-account-user-sider-bar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './account-user-sider-bar.component.html',
  styleUrl: './account-user-sider-bar.component.css'
})
export class AccountUserSiderBarComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly platformId = inject(PLATFORM_ID);
  @Input() showNavigation: boolean = false;

  readonly placeholderImg = 'assets/img/p-1.jpg';
  /** Avatar de repli si le vendeur n'a pas de photo de profil. */
  readonly avatarPlaceholder = 'assets/img/team-1.jpg';
  
  /** Utilisateur connecté, affiché dans la barre latérale du tableau de bord. */
  currentUser: User | null = null;

  ngOnInit() {
    if (isPlatformBrowser(this.platformId)) {
      this.currentUser = this.authService.getUserInfo();
    }
  }

  logout(): void {
    this.authService.logout();
  }

  toggleNavigation(): void {
    this.showNavigation = !this.showNavigation;
  }

  closeNavigation(): void {
    this.showNavigation = false;
  }

  
  onAvatarError(event: Event): void {
    (event.target as HTMLImageElement).src = this.avatarPlaceholder;
  }


}
