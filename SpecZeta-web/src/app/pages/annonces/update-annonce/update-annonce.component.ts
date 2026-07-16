import { Component, inject, OnInit, PLATFORM_ID } from '@angular/core';
import { AnnonceService } from '../../../services/annonce/annonce.service';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import {
  AnnonceMediaResponse,
  AnnonceResponse,
  CategorieAnnonce,
  EtatEsthetique,
  ModeRemise,
  UpdateAnnonceRequest,
} from '../../../models/annoce';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { of, switchMap } from 'rxjs';

interface SelectOption<T> {
  value: T;
  label: string;
}

@Component({
  selector: 'app-update-annonce',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './update-annonce.component.html',
  styleUrl: './update-annonce.component.css'
})
export class UpdateAnnonceComponent implements OnInit {

  private readonly platformId = inject(PLATFORM_ID);
  private readonly annonceService = inject(AnnonceService);
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);

  id!: number;
  annonce!: AnnonceResponse;

  loading = false;      // chargement initial de l'annonce
  saving = false;       // enregistrement des modifications
  errorMessage: string | null = null;
  successMessage: string | null = null;

  /** Photos déjà enregistrées sur l'annonce (affichées telles quelles). */
  existingMedias: AnnonceMediaResponse[] = [];

  /** Nouvelles photos sélectionnées pour remplacer les actuelles (optionnel). */
  selectedFiles: File[] = [];
  previews: string[] = [];
  photosError: string | null = null;

  readonly MIN_PHOTOS = 3;
  readonly MAX_PHOTOS = 5;

  readonly placeholderImg = 'assets/img/p-1.jpg';

  /** Listes de choix alimentant les <select> du formulaire. */
  readonly categories: SelectOption<CategorieAnnonce>[] = [
    { value: CategorieAnnonce.ORDINATEUR_PORTABLE, label: 'Ordinateur portable' },
    { value: CategorieAnnonce.ORDINATEUR_FIXE, label: 'Ordinateur fixe' },
    { value: CategorieAnnonce.COMPOSANT_PC, label: 'Composant PC' },
    { value: CategorieAnnonce.PERIPHERIQUE, label: 'Périphérique' },
    { value: CategorieAnnonce.ECRAN, label: 'Écran' },
    { value: CategorieAnnonce.SMARTPHONE, label: 'Smartphone' },
    { value: CategorieAnnonce.TABLETTE, label: 'Tablette' },
    { value: CategorieAnnonce.CONSOLE, label: 'Console' },
    { value: CategorieAnnonce.ACCESSOIRE_GAMING, label: 'Accessoire gaming' },
    { value: CategorieAnnonce.RESEAU, label: 'Réseau' },
    { value: CategorieAnnonce.AUTRE, label: 'Autre' },
  ];

  readonly etats: SelectOption<EtatEsthetique>[] = [
    { value: EtatEsthetique.NEUF, label: 'Neuf' },
    { value: EtatEsthetique.TRES_BON, label: 'Très bon état' },
    { value: EtatEsthetique.BON, label: 'Bon état' },
    { value: EtatEsthetique.POUR_PIECES, label: 'Pour pièces' },
  ];

  readonly modesRemise: SelectOption<ModeRemise>[] = [
    { value: 'MAIN_PROPRE', label: 'Remise en main propre' },
    { value: 'ENVOI', label: 'Envoi postal' },
    { value: 'LES_DEUX', label: 'Les deux' },
  ];

  readonly typesStockage = ['HDD', 'SSD', 'NVMe', 'eMMC'];

  readonly form = this.fb.group({
    titre: ['', [Validators.required, Validators.minLength(5), Validators.maxLength(200)]],
    description: ['', [Validators.required, Validators.minLength(20), Validators.maxLength(10000)]],
    prix: [null as number | null, [Validators.required, Validators.min(0.01)]],
    categorie: ['', [Validators.required]],
    etat: ['', [Validators.required]],
    modeRemise: ['', [Validators.required]],
    latitude: [null as number | null],
    longitude: [null as number | null],
    ficheTechnique: this.fb.group({
      modele: ['', [Validators.maxLength(150)]],
      marque: ['', [Validators.maxLength(100)]],
      processeur: ['', [Validators.maxLength(200)]],
      gpu: ['', [Validators.maxLength(200)]],
      ramGo: [null as number | null, [Validators.min(0)]],
      stockageGo: [null as number | null, [Validators.min(0)]],
      typeStockage: [''],
      ecranTaille: ['', [Validators.maxLength(30)]],
      ecranResolution: ['', [Validators.maxLength(30)]],
      socket: ['', [Validators.maxLength(50)]],
    }),
  });

  ngOnInit(): void {
    const id = this.getId();
    if (id === null) {
      this.errorMessage = 'Annonce introuvable.';
      return;
    }
    this.id = id;

    if (isPlatformBrowser(this.platformId)) {
      this.findAnnonceById(this.id);
    }
    console.log('annonce trouvere :', this.annonce);
  }

  get f() {
    return this.form.controls;
  }

  get ft() {
    return this.form.controls.ficheTechnique.controls;
  }

  getId(): number | null {
    const annonceId = this.route.snapshot.paramMap.get('id');
    if (annonceId && !isNaN(Number(annonceId))) {
      return Number(annonceId);
    }
    console.error("Aucun ID valide trouvé dans l'URL");
    return null;
  }

  /** Charge l'annonce existante et pré-remplit le formulaire. */
  findAnnonceById(id: number): void {
    this.loading = true;
    this.errorMessage = null;

    this.annonceService.getById(id).subscribe({
      next: (res) => {
        this.annonce = res.data;
        // console.log('Annonce chargée pour modification :', this.annonce);
        this.existingMedias = (this.annonce.medias ?? [])
          .slice()
          .sort((m1, m2) => m1.ordreAffichage - m2.ordreAffichage);
          // console.log('Medias existants :', this.existingMedias);
        this.patchForm(this.annonce, this.existingMedias);
        this.loading = false;
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage =
          err?.error?.message ?? "Impossible de charger l'annonce à modifier.";
      },
    });
  }

  private patchForm(a: AnnonceResponse, m:any): void {
    this.form.patchValue({
      titre: a.titre,
      description: a.description,
      prix: a.prix,
      categorie: a.categorie,
      etat: a.etat,
      modeRemise: a.modeRemise,
      latitude: a.latitude,
      longitude: a.longitude,
      ficheTechnique: {
        modele: a.ficheTechnique?.modele ?? '',
        marque: a.ficheTechnique?.marque ?? '',
        processeur: a.ficheTechnique?.processeur ?? '',
        gpu: a.ficheTechnique?.gpu ?? '',
        ramGo: a.ficheTechnique?.ramGo ?? null,
        stockageGo: a.ficheTechnique?.stockageGo ?? null,
        typeStockage: a.ficheTechnique?.typeStockage ?? '',
        ecranTaille: a.ficheTechnique?.ecranTaille ?? '',
        ecranResolution: a.ficheTechnique?.ecranResolution ?? '',
        socket: '',
      },
    });
    this.existingMedias = m;
    console.log('les medias existants :', this.existingMedias);

  }

  /** Récupère les fichiers sélectionnés et génère les aperçus. */
  onFilesSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const files = input.files ? Array.from(input.files) : [];

    this.revokePreviews();
    this.selectedFiles = files;
    this.previews = isPlatformBrowser(this.platformId)
      ? files.map((file) => URL.createObjectURL(file))
      : [];

    this.validatePhotos();
  }

  /**
   * En modification, les photos sont optionnelles : on ne les valide que si
   * l'utilisateur en a sélectionné de nouvelles (pour remplacer les actuelles).
   */
  private validatePhotos(): boolean {
    const count = this.selectedFiles.length;
    if (count === 0) {
      this.photosError = null;
      return true;
    }
    if (count < this.MIN_PHOTOS || count > this.MAX_PHOTOS) {
      this.photosError = `Pour remplacer les photos, ajoutez entre ${this.MIN_PHOTOS} et ${this.MAX_PHOTOS} images (actuellement : ${count}).`;
      return false;
    }
    this.photosError = null;
    return true;
  }

  private revokePreviews(): void {
    if (isPlatformBrowser(this.platformId)) {
      this.previews.forEach((url) => URL.revokeObjectURL(url));
    }
    this.previews = [];
  }

  submit(): void {
    this.errorMessage = null;
    this.successMessage = null;

    const photosOk = this.validatePhotos();

    if (this.form.invalid) {
      this.form.markAllAsTouched();
    }

    if (this.form.invalid || !photosOk) {
      return;
    }

    const raw = this.form.getRawValue();
    const ficheSaisie = raw.ficheTechnique;

    // On n'envoie la fiche technique que si au moins un champ est renseigné.
    const ficheRenseignee = Object.values(ficheSaisie).some(
      (v) => v !== null && v !== '' && v !== undefined
    );

    const payload: UpdateAnnonceRequest = {
      titre: raw.titre!,
      description: raw.description!,
      prix: raw.prix!,
      categorie: raw.categorie!,
      etat: raw.etat!,
      modeRemise: raw.modeRemise!,
      latitude: raw.latitude as number,
      longitude: raw.longitude as number,
      ficheTechnique: ficheRenseignee
        ? (ficheSaisie as UpdateAnnonceRequest['ficheTechnique'])
        : undefined,
    };

    this.saving = true;

    const hasNewPhotos = this.selectedFiles.length > 0;

    // Mise à jour de l'annonce, puis remplacement des photos si de nouvelles ont été fournies.
    this.annonceService
      .update(this.id, payload)
      .pipe(
        switchMap(() =>
          hasNewPhotos
            ? this.annonceService.uploadMedias(this.id, this.selectedFiles)
            : of(null)
        )
      )
      .subscribe({
        next: () => {
          this.saving = false;
          this.successMessage = 'Votre annonce a été mise à jour avec succès.';
          this.revokePreviews();
          this.router.navigate(['/annonce-user-list']);
        },
        error: (err) => {
          this.saving = false;
          this.errorMessage =
            err?.error?.message ?? "La mise à jour de l'annonce a échoué. Veuillez réessayer.";
        },
      });
  }

  cancel(): void {
    this.router.navigate(['/annonce-user-list']);
  }

  onImgError(event: Event): void {
    (event.target as HTMLImageElement).src = this.placeholderImg;
  }
}
