import { CommonModule, isPlatformBrowser } from '@angular/common';
import { Component, DestroyRef, inject, OnInit, PLATFORM_ID } from '@angular/core';
import { User } from '../../models/user.model';
import { AuthService } from '../../services/auth/auth.service';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

@Component({
  selector: 'app-topbar',
  imports: [CommonModule],
  templateUrl: './topbar.component.html',
  styleUrl: './topbar.component.css'
})
export class TopbarComponent implements OnInit {
  protected readonly auth = inject(AuthService);
  // Injecter le DestroyRef pour gérer la désinscription automatique
  private destroyRef = inject(DestroyRef);
  private platformId = inject(PLATFORM_ID);
  user: User | null = null;
  staticProfilUrl: string = "assets/img/avatar-app.jpg";

  ngOnInit(): void {
    if (!isPlatformBrowser(this.platformId)) {
      return
    }
    this.auth.currentUser$.pipe(
      // Sécurité : se désabonne automatiquement quand le composant meurt
      takeUntilDestroyed(this.destroyRef)
    ).subscribe(user =>
      (user == null) ? this.user = this.auth.getUserInfo()
        : this.user = user
    )


  }

}
