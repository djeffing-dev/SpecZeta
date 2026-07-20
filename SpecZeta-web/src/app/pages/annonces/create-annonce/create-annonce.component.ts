import { Component, inject, OnInit, PLATFORM_ID } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { switchMap } from 'rxjs';

import { AnnonceService } from '../../../services/annonce/annonce.service';
import {
  AnnonceRequest,
  CategorieAnnonce,
  EtatEsthetique,
  ModeRemise,
  StatutAnnonce,
} from '../../../models/annoce';
import { GeolocationService } from '../../../services/geolocalisation/geolocalisation.service';
import { AccountUserSiderBarComponent } from "../../account/layout/account-user-sider-bar/account-user-sider-bar.component";

/** Option affichable dans un <select> : valeur envoyée au backend + libellé FR. */
interface SelectOption<T> {
  value: T;
  label: string;
}

@Component({
  selector: 'app-create-annonce',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, AccountUserSiderBarComponent],
  templateUrl: './create-annonce.component.html',
  styleUrl: './create-annonce.component.css',
})
export class CreateAnnonceComponent implements OnInit{
  private readonly platformId = inject(PLATFORM_ID);
  private readonly annonceService = inject(AnnonceService);
  private readonly geolocationService= inject(GeolocationService)
  private readonly fb = inject(FormBuilder);
  private readonly router = inject(Router);

  loading = false;
  errorMessage: string | null = null;
  successMessage: string | null = null;

  /** Photos sélectionnées (3 à 5 requises pour publier l'annonce). */
  selectedFiles: File[] = [];
  previews: string[] = [];
  photosError: string | null = null;

  readonly MIN_PHOTOS = 3;
  readonly MAX_PHOTOS = 5;

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
    ville:['' as string | null],
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
      this.detectCity();
  }

  get f() {
    return this.form.controls;
  }

  get ft() {
    return this.form.controls.ficheTechnique.controls;
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

  private validatePhotos(): boolean {
    const count = this.selectedFiles.length;
    if (count < this.MIN_PHOTOS || count > this.MAX_PHOTOS) {
      this.photosError = `Veuillez ajouter entre ${this.MIN_PHOTOS} et ${this.MAX_PHOTOS} photos (actuellement : ${count}).`;
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

  private detectCity(): void {
    // this.loading = true;

    this.geolocationService.getCityFromBrowser().subscribe({
      next: (res) => {
        if (res) {
          console.log("La ville de l'utilisateur : ", res)
          this.form.controls.latitude.setValue(res.position.coords.latitude);
          this.form.controls.longitude.setValue(res.position.coords.longitude);
          this.form.controls.ville.setValue(res.ville);

          console.log("latitude : ", this.form.controls.latitude.value)
          console.log("longitude : ", this.form.controls.longitude.value)
        }
        // si null → le champ reste vide, l'utilisateur peut le remplir manuellement
        // this.loading = false;
      },
      error: (err) => {
        console.error('❌ Erreur subscribe :', err);
        this.loading = false;
      }
    });
  }

  submit(): void {
    this.errorMessage = null;
    this.successMessage = null;
    // this.detectCity();

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

    const payload: AnnonceRequest = {
      titre: raw.titre!,
      description: raw.description!,
      prix: raw.prix!,
      categorie: raw.categorie!,
      etat: raw.etat!,
      modeRemise: raw.modeRemise!,
      latitude: raw.latitude as number,
      longitude: raw.longitude as number,
      ville: raw.ville as string,
      ficheTechnique: ficheRenseignee ? (ficheSaisie as AnnonceRequest['ficheTechnique']) : undefined!,
    };

    this.loading = true;

    // Flux complet : création → upload des photos → publication (statut ACTIVE).
    this.annonceService
      .create(payload)
      .pipe(
        switchMap((res) => {
          const id = res.data.id;
          return this.annonceService.uploadMedias(id, this.selectedFiles).pipe(
            switchMap(() => this.annonceService.updateStatut(id, StatutAnnonce.ACTIVE))
          );
        })
      )
      .subscribe({
        next: (res) => {
          this.loading = false;
          this.successMessage = 'Votre annonce a été créée et publiée avec succès.';
          this.revokePreviews();
          this.router.navigate(['/dashboard'], {
            state: { annonceId: res.data?.id },
          });
        },
        error: (err) => {
          this.loading = false;
          this.errorMessage =
            err?.error?.message ?? "La création de l'annonce a échoué. Veuillez réessayer.";
        },
      });
  }
}
