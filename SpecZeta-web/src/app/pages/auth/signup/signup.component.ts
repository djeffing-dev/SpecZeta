import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  AbstractControl,
  FormBuilder,
  ReactiveFormsModule,
  ValidationErrors,
  Validators,
} from '@angular/forms';
import { Router } from '@angular/router';

import { AuthService } from '../../../services/auth/auth.service';
import { environment } from '../../../../environments/environment';

/** Vérifie que les champs `password` et `confirmPassword` sont identiques. */
function passwordsMatch(group: AbstractControl): ValidationErrors | null {
  const password = group.get('password')?.value;
  const confirm = group.get('confirmPassword')?.value;
  return password === confirm ? null : { passwordMismatch: true };
}

@Component({
  selector: 'app-signup',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './signup.component.html',
  styleUrl: './signup.component.css'
})
export class SignupComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  loading = false;
  errorMessage: string | null = null;
  successMessage: string | null = null;

  readonly form = this.fb.nonNullable.group(
    {
      email: ['', [Validators.required, Validators.email]],
      pseudo: ['', [Validators.required, Validators.minLength(3)]],
      password: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', [Validators.required]],
    },
    { validators: passwordsMatch }
  );

  get f() {
    return this.form.controls;
  }

  submit(): void {
    this.errorMessage = null;
    this.successMessage = null;

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const { email, pseudo, password } = this.form.getRawValue();
    this.loading = true;

    this.authService.signup({ email, pseudo, password }).subscribe({
      next: (res) => {
        this.loading = false;
        this.successMessage ='Compte créé. Vérifiez votre boîte mail pour le code de confirmation.';
        this.router.navigate(['/verify-otp'], { state: { email: email } });
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage =
          err?.error?.message ?? 'La création du compte a échoué.';
        
          
      },
    });
  }

  /** Redirige vers le flux OAuth2 Google géré par Spring Security côté backend. */
  signupWithGoogle(): void {
    window.location.href = `${environment.mediaUrl}/oauth2/authorization/google`;
  }
}
