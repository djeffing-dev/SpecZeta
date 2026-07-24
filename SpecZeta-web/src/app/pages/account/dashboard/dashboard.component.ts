import { Component, inject, OnInit, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { UserService } from '../../../services/user/user.service';
import { AuthService } from '../../../services/auth/auth.service';
import { User, UserProfileResponse, DashboardResponse, UpdateProfileRequest } from '../../../models/user.model';
import { AccountUserSiderBarComponent } from '../layout/account-user-sider-bar/account-user-sider-bar.component';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, AccountUserSiderBarComponent],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly userService = inject(UserService);
  private readonly authService = inject(AuthService);
  private readonly platformId = inject(PLATFORM_ID);

  showNavigation = false;
  loading = false;
  saving = false;
  uploadingPhoto = false;
  errorMessage: string | null = null;
  successMessage: string | null = null;

  /** Types d'images acceptés côté backend (jpeg/png/webp). */
  private readonly allowedPhotoTypes = ['image/jpeg', 'image/png', 'image/webp'];
  /** Taille maximale de la photo de profil : 5 Mo. */
  private readonly maxPhotoSize = 5 * 1024 * 1024;

  /** Profil chargé depuis le backend (source de l'email en lecture seule et de la photo). */
  profile: UserProfileResponse | null = null;

  /** Avatar de repli affiché lorsque l'utilisateur n'a pas de photo de profil. */
  readonly avatarPlaceholder = 'assets/img/team-1.jpg';

  readonly form = this.fb.group({
    pseudo: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(80)]],
    ville: ['', [Validators.maxLength(120)]],
    telephone: ['', [Validators.maxLength(30)]],
    dateNaissance: [''],
    siteWeb: ['', [Validators.maxLength(255)]],
    adresse: ['', [Validators.maxLength(200)]],
    codePostal: ['', [Validators.maxLength(10)]],
    pays: ['', [Validators.maxLength(100)]],
    biographie: ['', [Validators.maxLength(2000)]],
  });

  ngOnInit(): void {
    if (!isPlatformBrowser(this.platformId)) {
      return; // jamais exécuté côté serveur
    }
    this.loadProfile();
  }

  get f() {
    return this.form.controls;
  }

  loadProfile(): void {
    this.loading = true;
    this.errorMessage = null;

    this.userService.getMyprofile().subscribe({
      next: (res) => {
        this.profile = res.data;
        this.patchForm(res.data);
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage =
          err?.error?.message ?? 'Impossible de charger votre profil pour le moment.';
      },
    });
  }

  private patchForm(p: UserProfileResponse): void {
    this.form.patchValue({
      pseudo: p.pseudo ?? '',
      ville: p.ville ?? '',
      telephone: p.telephone != null ? String(p.telephone) : '',
      dateNaissance: this.toDateInputValue(p.dateNaissance),
      siteWeb: p.siteWeb ?? '',
      adresse: p.adresse ?? '',
      codePostal: p.codePostal ?? '',
      pays: p.pays ?? '',
      biographie: p.biographie ?? '',
    });
  }

  submit(): void {
    this.errorMessage = null;
    this.successMessage = null;

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const raw = this.form.getRawValue();

    const payload: UpdateProfileRequest = {
      pseudo: raw.pseudo!.trim(),
      ville: this.emptyToNull(raw.ville),
      telephone: this.emptyToNull(raw.telephone),
      // La photo est gérée par un upload dédié ; on conserve ici la valeur courante.
      photoUrl: this.profile?.photoUrl ?? null,
      biographie: this.emptyToNull(raw.biographie),
      dateNaissance: this.emptyToNull(raw.dateNaissance),
      adresse: this.emptyToNull(raw.adresse),
      codePostal: this.emptyToNull(raw.codePostal),
      pays: this.emptyToNull(raw.pays),
      siteWeb: this.emptyToNull(raw.siteWeb),
    };

    this.saving = true;

    this.userService.updateProfile(payload).subscribe({
      next: (res) => {
        this.saving = false;
        this.profile = res.data;
        this.patchForm(res.data);
        this.successMessage = 'Votre profil a été mis à jour avec succès.';
        this.syncCurrentUser(res.data);
      },
      error: (err) => {
        this.saving = false;
        this.errorMessage =
          err?.error?.message ?? 'La mise à jour de votre profil a échoué. Veuillez réessayer.';
      },
    });
  }

  /** Répercute pseudo/ville/photo mis à jour dans la session (navbar, sidebar). */
  private syncCurrentUser(p: UserProfileResponse): void {
    const current = this.authService.getUserInfo();
    if (!current) {
      return;
    }
    this.authService.setCurrentUser({
      ...current,
      pseudo: p.pseudo,
      ville: p.ville,
      photoUrl: p.photoUrl,
    });
  }

  /**
   * Déclenché lors de la sélection d'un fichier image. Valide le type et la taille,
   * envoie la photo au backend puis répercute la nouvelle URL dans le profil et la session.
   */
  onPhotoSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) {
      return;
    }

    this.errorMessage = null;
    this.successMessage = null;

    if (!this.allowedPhotoTypes.includes(file.type)) {
      this.errorMessage = 'Format non supporté. Utilisez une image JPEG, PNG ou WebP.';
      input.value = '';
      return;
    }
    if (file.size > this.maxPhotoSize) {
      this.errorMessage = 'La photo est trop volumineuse (5 Mo maximum).';
      input.value = '';
      return;
    }

    this.uploadingPhoto = true;

    this.userService.uploadProfilUrl(file).subscribe({
      next: (res) => {
        this.uploadingPhoto = false;
        const newUrl = res.data;
        if (this.profile) {
          this.profile = { ...this.profile, photoUrl: newUrl };
          this.syncCurrentUser(this.profile);
        }
        this.successMessage = 'Votre photo de profil a été mise à jour avec succès.';
        input.value = '';
      },
      error: (err) => {
        this.uploadingPhoto = false;
        this.errorMessage =
          err?.error?.message ?? "La mise à jour de votre photo de profil a échoué. Veuillez réessayer.";
        input.value = '';
      },
    });
  }

  /** URL de la photo de profil, ou avatar de repli si aucune photo n'est définie. */
  get avatarUrl(): string {
    return this.profile?.photoUrl || this.avatarPlaceholder;
  }

  onAvatarError(event: Event): void {
    (event.target as HTMLImageElement).src = this.avatarPlaceholder;
  }

  toggleNavigation(): void {
    this.showNavigation = !this.showNavigation;
  }

  closeNavigation(): void {
    this.showNavigation = false;
  }

  /** Convertit une chaîne vide en `null` (le backend ignore les champs `null`). */
  private emptyToNull(value: string | null | undefined): string | null {
    const trimmed = (value ?? '').trim();
    return trimmed.length ? trimmed : null;
  }

  /**
   * Normalise une date backend (chaîne ISO ou tableau [année, mois, jour])
   * vers le format `yyyy-MM-dd` attendu par un <input type="date">.
   */
  private toDateInputValue(value: any): string {
    if (!value) return '';
    if (Array.isArray(value)) {
      const [y, m, d] = value;
      return `${y}-${String(m).padStart(2, '0')}-${String(d).padStart(2, '0')}`;
    }
    if (typeof value === 'string') {
      return value.length >= 10 ? value.substring(0, 10) : value;
    }
    return '';
  }
}
