import { Component, inject, OnInit, OnDestroy, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { AnnonceService } from '../../../services/annonce/annonce.service';
import {
  AnnonceResponse,
  CategorieAnnonce,
  EtatEsthetique,
  StatutAnnonce,
} from '../../../models/annoce';

@Component({
  selector: 'app-detail-annoce',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './detail-annoce.component.html',
  styleUrl: './detail-annoce.component.css'
})
export class DetailAnnoceComponent implements OnInit, OnDestroy {
  private readonly platformId = inject(PLATFORM_ID);
  private readonly route = inject(ActivatedRoute);
  private readonly annonceService = inject(AnnonceService);

  annonce: AnnonceResponse | null = null;
  loading = false;
  errorMessage: string | null = null;

  // Slider State
  images: string[] = [];
  currentIndex = 0;
  autoSlideInterval: any;

  readonly placeholderImg = 'assets/img/p-1.jpg';

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

  private readonly modeRemiseLabels: Record<string, string> = {
    'MAIN_PROPRE': 'Remise en main propre',
    'ENVOI': 'Envoi postal',
    'LES_DEUX': 'Remise en main propre / Envoi postal',
  };

  ngOnInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      this.loadAnnonce();
    }
  }

  ngOnDestroy(): void {
    this.stopAutoSlide();
  }

  loadAnnonce(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (!id) {
      this.errorMessage = 'Identifiant de l\'annonce manquant.';
      return;
    }

    this.loading = true;
    this.errorMessage = null;

    this.annonceService.getById(id).subscribe({
      next: (res) => {
        this.annonce = res.data;
        if (this.annonce) {
          // Remplir la liste des images pour le carrousel
          if (this.annonce.medias && this.annonce.medias.length > 0) {
            this.images = this.annonce.medias.map(m => m.dropboxUrl);
          } else {
            // this.images = [this.annonce.photoPrincipaleUrl || this.placeholderImg];
          }
          this.startAutoSlide();
        }
        this.loading = false;
      },
      error: (err) => {
        this.errorMessage = err?.error?.message ?? 'Impossible de charger les détails de l\'annonce.';
        this.loading = false;
      }
    });
  }

  // Carrousel Logic
  startAutoSlide(): void {
    this.stopAutoSlide();
    if (this.images.length > 1) {
      this.autoSlideInterval = setInterval(() => {
        this.nextSlide();
      }, 4000);
    }
  }

  stopAutoSlide(): void {
    if (this.autoSlideInterval) {
      clearInterval(this.autoSlideInterval);
    }
  }

  nextSlide(): void {
    if (this.images.length === 0) return;
    this.currentIndex = (this.currentIndex + 1) % this.images.length;
  }

  prevSlide(): void {
    if (this.images.length === 0) return;
    this.currentIndex = (this.currentIndex - 1 + this.images.length) % this.images.length;
  }

  goToSlide(index: number): void {
    this.currentIndex = index;
    this.startAutoSlide(); // Réinitialise le timer d'auto-slide
  }

  onImgError(event: Event, index: number): void {
    this.images[index] = this.placeholderImg;
    (event.target as HTMLImageElement).src = this.placeholderImg;
  }

  // Label Helpers
  categorieLabel(categorie: string): string {
    return this.categorieLabels[categorie] ?? categorie;
  }

  etatLabel(etat: string): string {
    return this.etatLabels[etat] ?? etat;
  }

  modeRemiseLabel(mode: string): string {
    return this.modeRemiseLabels[mode] ?? mode;
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

