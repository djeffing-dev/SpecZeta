import { Component, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { AuthService } from '../../../services/auth/auth.service';
import { environment } from '../../../../environments/environment';
import { OtpService } from '../../../services/otp/otp.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly otpService= inject(OtpService)

  loading = false;
  errorMessage: string | null = null;

  readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]],
    rememberMe: [false],
  });

  get f() {
    return this.form.controls;
  }

  submit(): void {
    this.errorMessage = null;

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const { email, password, rememberMe } = this.form.getRawValue();
    this.loading = true;

    this.authService.login(email, password, rememberMe).subscribe({
      next: () => {
        this.loading = false;
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.loading = false;
        this.errorMessage =
          err?.error?.message ?? 'Email ou mot de passe incorrect.';
        
        // HTTP 403 = compte non vérifié → redirection vers la page OTP
        if (err?.status === 403 ) {
          this.router.navigate(['/verify-otp'], { state: { email } });
          this.otpService.resendOtp(email).subscribe({
            next:response=>{
              console.log('OTP resent:', response);
            },
            error:err =>{
              console.error('Error resending OTP:', err);
            }
          })
          return;
        }
      },
    });
  }

  /** Redirige vers le flux OAuth2 Google géré par Spring Security côté backend. */
  loginWithGoogle(): void {
    window.location.href = `${environment.mediaUrl}/oauth2/authorization/google`;
  }
}
