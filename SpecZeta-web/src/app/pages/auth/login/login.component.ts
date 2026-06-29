import { Component, inject, OnInit, PLATFORM_ID  } from '@angular/core';
import { CommonModule, isPlatformBrowser } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';

import { AuthService } from '../../../services/auth/auth.service';
import { environment } from '../../../../environments/environment';
import { OtpService } from '../../../services/otp/otp.service';
import { GoogleOauth2Service } from '../../../services/auth/oauh2/googleOauth2/google-oauth2.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent implements OnInit{

  private readonly fb = inject(FormBuilder);
  private readonly platformId = inject(PLATFORM_ID);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly otpService= inject(OtpService)
  private readonly gooleOauth2Service= inject(GoogleOauth2Service)

  loading = false;
  url:string = "";
  errorMessage: string | null = null;

  readonly form = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required]],
    rememberMe: [false],
  });

  get f() {
    return this.form.controls;
  }

  ngOnInit(): void {
    if (isPlatformBrowser(this.platformId)) {
      this.loginWithGoogle();
    }
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
    this.gooleOauth2Service.getUrl().subscribe({
      next: (res) => {
        this.url =res;
      }
    })
  }


}
