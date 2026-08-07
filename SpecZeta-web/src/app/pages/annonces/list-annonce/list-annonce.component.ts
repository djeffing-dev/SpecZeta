import { Component, inject, OnDestroy, OnInit, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { AnnonceService } from '../../../services/annonce/annonce.service';
import {
  AnnonceListResponse,
  CategorieAnnonce,
  EtatEsthetique,
} from '../../../models/annoce';
import { RouterLink } from '@angular/router';
import { FavorisService } from '../../../services/favoris/favoris.service';
import { AuthService } from '../../../services/auth/auth.service';
import { Observable } from 'rxjs';
import { ApiResponse } from '../../../models/api-response.model';
import { FavoriResponse } from '../../../models/favoris';

/** Notification éphémère affichée en bas à droite après une action favoris. */
interface Feedback {
  type: 'success' | 'danger' | 'info';
  text: string;
}

@Component({
  selector: 'app-list-annonce',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './list-annonce.component.html',
  styleUrl: './list-annonce.component.css',
})
export class ListAnnonceComponent implements OnInit, OnDestroy {
  private readonly annonceService = inject(AnnonceService);
  private readonly favorisService = inject(FavorisService)
  private readonly authService = inject(AuthService);
  private readonly platformId = inject(PLATFORM_ID);

  annonces: AnnonceListResponse[] = [];
  loading = false;
  errorMessage: string | null = null;

  /** Ids des annonces déjà en favoris — alimente l'état du cœur de chaque carte. */
  private favorisIds = new Set<number>();

  /** Ids dont la requête favoris est en cours — évite le double-clic. */
  private favorisPending = new Set<number>();

  /** Notification éphémère de résultat d'une action favoris. */
  feedback: Feedback | null = null;
  private feedbackTimer: any;

  /** Nombre de favoris chargés en une fois pour hydrater les cœurs de la liste. */
  private readonly FAVORIS_PAGE_SIZE = 200;

  /** Contrôle l'affichage du panneau de filtres en mode responsive. */
  showFilter = false;

  /** Image de repli si l'annonce n'a pas de photo principale. */
  readonly placeholderImg = 'assets/img/p-1.jpg';

  /** Libellés FR des catégories pour l'affichage. */
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

  /** Libellés FR des états esthétiques pour l'affichage. */
  private readonly etatLabels: Record<string, string> = {
    [EtatEsthetique.NEUF]: 'Neuf',
    [EtatEsthetique.TRES_BON]: 'Très bon état',
    [EtatEsthetique.BON]: 'Bon état',
    [EtatEsthetique.POUR_PIECES]: 'Pour pièces',
  };

  /** Listes exposées au template pour personnaliser le formulaire de recherche. */
  readonly categories = Object.entries(this.categorieLabels).map(([value, label]) => ({ value, label }));
  readonly etats = Object.entries(this.etatLabels).map(([value, label]) => ({ value, label }));

  ngOnInit(): void {
    if (!isPlatformBrowser(this.platformId)) {
      return; // jamais exécuté côté serveur
    }
    this.loadAnnonces();
    this.loadFavoris();
  }

  ngOnDestroy(): void {
    clearTimeout(this.feedbackTimer);
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

  loadAnnonces(): void {
    this.loading = true;
    this.errorMessage = null;

    this.annonceService.getPublic().subscribe({
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

  /** Une annonce est « nouvelle » si elle a été publiée il y a moins d'une semaine. */
  isNew(createdAt: any): boolean {
    const parsed = this.parseBackendDate(createdAt);
    if (!parsed) {
      return false;
    }
    const created = parsed.getTime();
    const oneWeekMs = 7 * 24 * 60 * 60 * 1000;
    return Date.now() - created < oneWeekMs;
  }

  categorieLabel(categorie: string): string {
    return this.categorieLabels[categorie] ?? categorie;
  }

  etatLabel(etat: string): string {
    return this.etatLabels[etat] ?? etat;
  }

  toggleFilter(): void {
    this.showFilter = !this.showFilter;
  }

  closeFilter(): void {
    this.showFilter = false;
  }

  // --------------------------------------------------------------- Favoris

  /**
   * Récupère les favoris de l'utilisateur pour pré-remplir l'état des cœurs.
   * Silencieux en cas d'échec : un problème sur les favoris ne doit jamais
   * empêcher la consultation du catalogue.
   */
  private loadFavoris(): void {
    if (!this.authService.isAuthenticated()) {
      this.favorisIds.clear();
      return;
    }

    this.favorisService.getAll(0, this.FAVORIS_PAGE_SIZE).subscribe({
      next: (res) => {
        const content = res.data?.content ?? [];
        this.favorisIds = new Set(
          content.filter((f) => f?.annonce).map((f) => f.annonce.id)
        );
      },
      error: () => this.favorisIds.clear(),
    });
  }

  isFavori(annonceId: number): boolean {
    return this.favorisIds.has(annonceId);
  }

  isFavorisPending(annonceId: number): boolean {
    return this.favorisPending.has(annonceId);
  }

  favorisLabel(annonceId: number): string {
    return this.isFavori(annonceId) ? 'Retirer des favoris' : 'Ajouter aux favoris';
  }

  /**
   * Ajoute ou retire l'annonce des favoris.
   * Mise à jour optimiste : le cœur bascule immédiatement, et est restauré
   * si le backend refuse l'opération.
   */
  toggleFavoris(annonce: AnnonceListResponse, event: Event): void {
    event.preventDefault();
    event.stopPropagation();

    if (!this.authService.isAuthenticated()) {
      this.showFeedback('info', 'Connectez-vous pour ajouter cette annonce à vos favoris.');
      this.openLoginModal();
      return;
    }

    const id = annonce.id;
    if (this.favorisPending.has(id)) {
      return;
    }

    const wasFavori = this.favorisIds.has(id);
    this.favorisPending.add(id);

    if (wasFavori) {
      this.favorisIds.delete(id);
    } else {
      this.favorisIds.add(id);
    }

    const request$: Observable<ApiResponse<FavoriResponse>> = wasFavori
      ? (this.favorisService.delete(id) as unknown as Observable<ApiResponse<FavoriResponse>>)
      : this.favorisService.create({ annonceId: id }) as Observable<ApiResponse<FavoriResponse>>;

    request$.subscribe({
      next: () => {
        this.favorisPending.delete(id);
        this.showFeedback(
          'success',
          wasFavori
            ? `« ${annonce.titre} » a été retirée de vos favoris.`
            : `« ${annonce.titre} » a été ajoutée à vos favoris.`
        );
      },
      error: (err) => {
        this.favorisPending.delete(id);
        // Rollback : on restaure l'état précédent du cœur
        if (wasFavori) {
          this.favorisIds.add(id);
        } else {
          this.favorisIds.delete(id);
        }
        this.showFeedback(
          'danger',
          err?.error?.message ?? "L'opération sur les favoris a échoué. Veuillez réessayer."
        );
      },
    });
  }

  dismissFeedback(): void {
    clearTimeout(this.feedbackTimer);
    this.feedback = null;
  }

  private showFeedback(type: Feedback['type'], text: string): void {
    clearTimeout(this.feedbackTimer);
    this.feedback = { type, text };
    this.feedbackTimer = setTimeout(() => (this.feedback = null), 4000);
  }

  /** Ouvre la modale de connexion (déclarée dans le footer) via Bootstrap. */
  private openLoginModal(): void {
    if (!isPlatformBrowser(this.platformId)) {
      return;
    }
    const modalEl = document.getElementById('login');
    const bootstrap = (window as any).bootstrap;
    if (modalEl && bootstrap?.Modal) {
      bootstrap.Modal.getOrCreateInstance(modalEl).show();
    }
  }

  /** Remplace l'image cassée par le visuel de repli. */
  onImgError(event: Event): void {
    (event.target as HTMLImageElement).src = this.placeholderImg;
  }
}
