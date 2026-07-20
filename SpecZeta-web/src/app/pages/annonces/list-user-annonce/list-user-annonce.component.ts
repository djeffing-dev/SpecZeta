import { Component, inject, OnInit, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { AnnonceService } from '../../../services/annonce/annonce.service';
import { Router, RouterModule } from '@angular/router';
import {
  AnnonceListResponse,
  CategorieAnnonce,
  EtatEsthetique,
  StatutAnnonce,
} from '../../../models/annoce';
import { AuthService } from '../../../services/auth/auth.service';
import { User } from '../../../models/user.model';
import { AccountUserSiderBarComponent } from "../../account/layout/account-user-sider-bar/account-user-sider-bar.component";

@Component({
  selector: 'app-list-user-annonce',
  standalone: true,
  imports: [CommonModule, RouterModule, AccountUserSiderBarComponent],
  templateUrl: './list-user-annonce.component.html',
  styleUrl: './list-user-annonce.component.css'
})
export class ListUserAnnonceComponent implements OnInit {
  private readonly platformId = inject(PLATFORM_ID);
  private readonly annonceService = inject(AnnonceService);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  annonces: AnnonceListResponse[] = [];
  loading = false;
  errorMessage: string | null = null;
  showNavigation = false;

  /** Utilisateur connecté, affiché dans la barre latérale du tableau de bord. */
  currentUser: User | null = null;

  readonly placeholderImg = 'assets/img/p-1.jpg';
  /** Avatar de repli si le vendeur n'a pas de photo de profil. */
  readonly avatarPlaceholder = 'assets/img/team-1.jpg';

  private readonly categorieLabels: Record<string, string> = {
    [CategorieAnnonce.ORDINATEUR_PORTABLE]: 'Ordinateur portable',
    [CategorieAnnonce.ORDINATEUR_FIXE]: 'Ordinateur fixe',
    [CategorieAnnonce.COMPOSANT_PC]: 'Composant PC',
    [CategorieAnnonce.PERIPHERIQUE]: 'Périphérique',
    [CategorieAnnonce.ECRAN]: 'Écran',
    [CategorieAnnonce.SMARTPHONE]: 'Smartphone',
    [CategorieAnnonce.TABLETTE]: 'Tablette',
    [CategorieAnnonce.CONSOLE]: 'Console',
    [CategorieAnnonce.ACCESSOIRE_GAMING]: 'Accessoire gaming',
    [CategorieAnnonce.RESEAU]: 'Réseau',
    [CategorieAnnonce.AUTRE]: 'Autre',
  };

  private readonly etatLabels: Record<string, string> = {
    [EtatEsthetique.NEUF]: 'Neuf',
    [EtatEsthetique.TRES_BON]: 'Très bon état',
    [EtatEsthetique.BON]: 'Bon état',
    [EtatEsthetique.POUR_PIECES]: 'Pour pièces',
  };

  ngOnInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      this.currentUser = this.authService.getUserInfo();
      this.loadMyAnnonces();
    }
  }

  /** Nombre d'annonces actuellement publiées (statut ACTIVE). */
  get activeCount(): number {
    return this.annonces.filter(a => a.status === StatutAnnonce.ACTIVE).length;
  }

  loadMyAnnonces(): void {
    this.loading = true;
    this.errorMessage = null;

    this.annonceService.getMyAnnonces().subscribe({
      next: (res) => {
        const content = res.data?.content ?? [];
        this.annonces = content.map((ann: any) => ({
          ...ann,
          createdAt: this.parseBackendDate(ann.createdAt)
        }));
        // console.log("Annonce : ", this.annonces);
        this.loading = false;
      },
      error: (err) => {
        this.errorMessage =
          err?.error?.message ?? 'Impossible de charger les annonces pour le moment.';
        this.loading = false;
      },
    });
  }
  categorieLabel(categorie: string): string {
    return this.categorieLabels[categorie] ?? categorie;
  }

  etatLabel(etat: string): string {
    return this.etatLabels[etat] ?? etat;
  }

  statusLabel(status: string): string {
    switch (status) {
      case StatutAnnonce.ACTIVE: return 'Publiée';
      case StatutAnnonce.EN_ATTENTE: return 'En attente';
      case StatutAnnonce.VENDUE: return 'Vendue';
      case StatutAnnonce.SUSPENDUE: return 'Suspendue';
      default: return status || 'Inconnu';
    }
  }

  statusClass(status: string): string {
    switch (status) {
      case StatutAnnonce.ACTIVE: return 'bg-light-success text-success';
      case StatutAnnonce.EN_ATTENTE: return 'bg-light-warning text-warning';
      case StatutAnnonce.VENDUE: return 'bg-light-info text-info';
      case StatutAnnonce.SUSPENDUE: return 'bg-light-danger text-danger';
      default: return 'bg-light-secondary text-secondary';
    }
  }

  deleteAnnonce(id: number): void {
    if (confirm('Voulez-vous vraiment supprimer définitivement cette annonce ?')) {
      this.annonceService.delete(id).subscribe({
        next: () => {
          this.annonces = this.annonces.filter(a => a.id !== id);
        },
        error: (err) => {
          alert(err?.error?.message ?? 'La suppression a échoué.');
        }
      });
    }
  }

  editAnnonce(id: number): void {
    this.router.navigate(['/update-annonce', id]);
  }

  /** Une annonce est « nouvelle » si elle a été publiée il y a moins d'une semaine. */
  isNew(createdAt: any): boolean {
    const parsed = this.parseBackendDate(createdAt);
    if (!parsed) {
      return false;
    }
    const oneWeekMs = 7 * 24 * 60 * 60 * 1000;
    return Date.now() - parsed.getTime() < oneWeekMs;
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

  onImgError(event: Event): void {
    (event.target as HTMLImageElement).src = this.placeholderImg;
  }

  onAvatarError(event: Event): void {
    (event.target as HTMLImageElement).src = this.avatarPlaceholder;
  }


  parseBackendDate(dateVal: any): Date | null {
    if (!dateVal) return null;
    if (dateVal instanceof Date) return dateVal;
    if (Array.isArray(dateVal)) {
      const [year, month, day, hours = 0, minutes = 0, seconds = 0, ms = 0] = dateVal;
      return new Date(year, month - 1, day, hours, minutes, seconds, ms / 1000000);
    }
    if (typeof dateVal === 'string') {
      if (dateVal.includes(',')) {
        const parts = dateVal.split(',').map(Number);
        const [year, month, day, hours = 0, minutes = 0, seconds = 0, ms = 0] = parts;
        return new Date(year, month - 1, day, hours, minutes, seconds, ms / 1000000);
      }
      const parsed = new Date(dateVal);
      if (!isNaN(parsed.getTime())) {
        return parsed;
      }
    }
    return null;
  }



}
