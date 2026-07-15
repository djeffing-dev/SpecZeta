import { Component, inject, OnInit, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { AnnonceService } from '../../../services/annonce/annonce.service';
import {
  AnnonceListResponse,
  CategorieAnnonce,
  EtatEsthetique,
} from '../../../models/annoce';

@Component({
  selector: 'app-list-annonce',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './list-annonce.component.html',
  styleUrl: './list-annonce.component.css',
})
export class ListAnnonceComponent implements OnInit {
  private readonly annonceService = inject(AnnonceService);
  private readonly platformId = inject(PLATFORM_ID);

  annonces: AnnonceListResponse[] = [];
  loading = false;
  errorMessage: string | null = null;

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

  /** Remplace l'image cassée par le visuel de repli. */
  onImgError(event: Event): void {
    (event.target as HTMLImageElement).src = this.placeholderImg;
  }
}
