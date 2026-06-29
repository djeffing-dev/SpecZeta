import { Component, OnInit, inject, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { GoogleOauth2Service } from '../../../services/auth/oauh2/googleOauth2/google-oauth2.service';

type OauthStatus = 'loading' | 'success' | 'error';

@Component({
  selector: 'app-oauth-redirect',
  standalone: true,
  imports: [CommonModule, RouterLink],
  template: `
    <div class="section-authentication-signin d-flex align-items-center justify-content-center min-vh-100">
      <div class="container">
        <div class="row row-cols-1 row-cols-lg-2 row-cols-xl-3">
          <div class="col mx-auto">
            <div class="card oauth-card mb-0">
              <div class="card-body">
                <div class="p-4 text-center">

                  <!-- Logo -->
                  <div class="d-flex align-items-center justify-content-center gap-2 mb-4">
                    <svg width="36" height="36" viewBox="0 0 28 28" fill="none">
                      <rect width="28" height="28" rx="8" fill="var(--bs-primary)" />
                      <path d="M7 14h4l3-6 3 12 3-6h4" stroke="white" stroke-width="2.2"
                            stroke-linecap="round" stroke-linejoin="round" />
                    </svg>
                    <span class="fw-bold text-dark" style="font-size:1.5rem;letter-spacing:-0.02em;">
                      Spec<span class="text-primary">Zeta</span>
                    </span>
                  </div>

                  <!-- État : connexion en cours -->
                  <ng-container *ngIf="status === 'loading'">
                    <div class="oauth-spinner mx-auto mb-4">
                      <div class="spinner-border text-primary" style="width:3.5rem;height:3.5rem;" role="status">
                        <span class="visually-hidden">Chargement…</span>
                      </div>
                      <i class="fab fa-google oauth-spinner-icon text-primary"></i>
                    </div>
                    <h5 class="mb-1">Connexion en cours…</h5>
                    <p class="text-muted mb-0">Authentification via votre compte Google.</p>
                  </ng-container>

                  <!-- État : succès -->
                  <ng-container *ngIf="status === 'success'">
                    <div class="oauth-icon-circle oauth-icon-circle--success text-success mx-auto mb-3">
                      <i class="fas fa-check"></i>
                    </div>
                    <h5 class="mb-1">Connexion réussie&nbsp;!</h5>
                    <p class="text-muted mb-0">Redirection vers votre tableau de bord…</p>
                  </ng-container>

                  <!-- État : erreur -->
                  <ng-container *ngIf="status === 'error'">
                    <div class="oauth-icon-circle oauth-icon-circle--danger text-danger mx-auto mb-3">
                      <i class="fas fa-times"></i>
                    </div>
                    <h5 class="mb-1">Échec de la connexion</h5>
                    <div class="alert alert-danger py-2 text-center mt-3 mb-3">{{ errorMessage }}</div>
                    <a routerLink="/login" class="btn btn-primary fw-medium">
                      Retour à la connexion
                    </a>
                  </ng-container>

                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .oauth-card {
      border: none;
      border-radius: 1rem;
      box-shadow: 0 10px 40px rgba(15, 23, 42, 0.08);
    }

    /* Spinner avec le logo Google centré */
    .oauth-spinner {
      position: relative;
      width: 3.5rem;
      height: 3.5rem;
    }
    .oauth-spinner-icon {
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      font-size: 1.25rem;
    }

    /* Pastille d'icône succès / erreur */
    .oauth-icon-circle {
      width: 4rem;
      height: 4rem;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 1.6rem;
      animation: oauth-pop 0.35s ease;
    }
    .oauth-icon-circle--success {
      background-color: rgba(var(--bs-success-rgb, 25, 135, 84), 0.12);
    }
    .oauth-icon-circle--danger {
      background-color: rgba(var(--bs-danger-rgb, 220, 53, 69), 0.12);
    }

    @keyframes oauth-pop {
      0%   { transform: scale(0.4); opacity: 0; }
      60%  { transform: scale(1.1); opacity: 1; }
      100% { transform: scale(1); }
    }
  `]
})
export class OauthRedirectComponent implements OnInit {
  private readonly platformId = inject(PLATFORM_ID);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly googleOauth2Service = inject(GoogleOauth2Service);

  status: OauthStatus = 'loading';
  errorMessage = '';
  private alreadyHandled = false; // protège contre un double appel (StrictMode, re-render...)

  ngOnInit(): void {
    if (!isPlatformBrowser(this.platformId)) {
      return; // jamais exécuté côté serveur
    }

    if (this.alreadyHandled) {
      return;
    }

    const code = this.route.snapshot.queryParamMap.get('code');
    const error = this.route.snapshot.queryParamMap.get('error');

    if (error) {
      this.fail("L'authentification Google a été refusée ou a échoué.");
      return;
    }

    if (!code) {
      this.fail("Aucun code d'autorisation reçu.");
      return;
    }

    this.alreadyHandled = true;

    this.googleOauth2Service.getToken(code).subscribe({
      next: () => {
        this.status = 'success';
        // Laisse le message de succès s'afficher avant de rediriger.
        setTimeout(() => this.router.navigate(['/dashboard']), 1200);
      },
      error: (err) => {
        console.error('Erreur lors de l\'échange du code:', err);
        this.fail('Impossible de finaliser la connexion. Veuillez réessayer.');
      }
    });
  }

  private fail(message: string): void {
    this.status = 'error';
    this.errorMessage = message;
  }
}
