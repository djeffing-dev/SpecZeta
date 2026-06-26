import {
  Component,
  ElementRef,
  OnDestroy,
  OnInit,
  QueryList,
  ViewChildren,
} from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { Subscription, interval } from 'rxjs';

import { AuthService } from '../../../services/auth/auth.service';
import { OtpService } from '../../../services/otp/otp.service';

@Component({
  selector: 'app-otp',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './otp.component.html',
  styleUrl: './otp.component.css',
})
export class OtpComponent implements OnInit, OnDestroy {
  @ViewChildren('digitInput') digitInputs!: QueryList<ElementRef<HTMLInputElement>>;

  digits: string[] = Array(6).fill('');
  email = '';
  isLoading = false;
  errorMessage = '';
  successMessage = '';
  countdown = 120; // secondes
  canResend = false;

  private timerSub?: Subscription;

  constructor(
    private readonly otpService: OtpService,
    private readonly auth: AuthService,
    private readonly router: Router,
  ) {
    // L'email est transmis via le state du router (ne persiste pas au refresh).
    const nav = this.router.getCurrentNavigation();
    this.email = (nav?.extras?.state?.['email'] as string) ?? '';
  }

  ngOnInit(): void {
    if (!this.email) {
      // Aucun email en state → l'utilisateur a rechargé la page ou y accède directement.
      // this.router.navigate(['/signup']);
      return;
    }
    this.startCountdown();
  }

  ngOnDestroy(): void {
    this.timerSub?.unsubscribe();
  }

  // ----------------------------------------------------------------- Timer
  private startCountdown(): void {
    this.countdown = 120;
    this.canResend = false;
    this.timerSub?.unsubscribe();

    this.timerSub = interval(1000).subscribe(() => {
      this.countdown--;
      if (this.countdown <= 0) {
        this.canResend = true;
        this.timerSub?.unsubscribe();
      }
    });
  }

  get countdownDisplay(): string {
    const m = Math.floor(this.countdown / 60).toString().padStart(2, '0');
    const s = (this.countdown % 60).toString().padStart(2, '0');
    return `${m}:${s}`;
  }

  // ----------------------------------------------------------------- Inputs
  get code(): string {
    return this.digits.join('');
  }

  onDigitInput(index: number, event: Event): void {
    const input = event.target as HTMLInputElement;
    // Ne garder que le dernier chiffre tapé
    const value = input.value.replace(/\D/g, '').slice(-1);
    this.digits[index] = value;
    input.value = value; // sync DOM

    if (value && index < 5) {
      this.digitInputs.toArray()[index + 1].nativeElement.focus();
    }

    if (this.code.length === 6) {
      this.submit();
    }
  }

  onKeyDown(index: number, event: KeyboardEvent): void {
    if (event.key === 'Backspace' && !this.digits[index] && index > 0) {
      this.digits[index - 1] = '';
      this.digitInputs.toArray()[index - 1].nativeElement.focus();
    }
  }

  onPaste(event: ClipboardEvent): void {
    event.preventDefault();
    const text   = event.clipboardData?.getData('text') ?? '';
    const pasted = text.replace(/\D/g, '').slice(0, 6).split('');

    pasted.forEach((d, i) => {
      this.digits[i] = d;
      const input = this.digitInputs.toArray()[i];
      if (input) input.nativeElement.value = d;
    });

    if (pasted.length === 6) {
      this.submit();
    } else if (pasted.length > 0) {
      const focusIndex = Math.min(pasted.length, 5);
      this.digitInputs.toArray()[focusIndex].nativeElement.focus();
    }
  }

  // ----------------------------------------------------------------- Submit
  submit(): void {
    if (this.code.length !== 6 || this.isLoading) return;

    this.isLoading      = true;
    this.errorMessage   = '';
    this.successMessage = '';

    this.otpService.verifyOtp({ email: this.email, plainCode: this.code }).subscribe({
      next: res => {
        console.log(res);
        this.auth.storeSession(res);
        this.router.navigate(['/dashboard']);
      },
      error: err => {
        this.isLoading    = false;
        this.errorMessage = err?.error?.detail ?? 'Code invalide. Veuillez réessayer.';
        this.clearDigits();
      },
    });
  }

  // ----------------------------------------------------------------- Resend
  resend(): void {
    if (!this.canResend || this.isLoading) return;

    this.errorMessage   = '';
    this.successMessage = '';

    this.otpService.resendOtp(this.email).subscribe({
      next: () => {
        this.successMessage = 'Un nouveau code a été envoyé à votre adresse email.';
        this.clearDigits();
        this.startCountdown();
      },
      error: err => {
        console.error('Resend OTP error:', err);
        this.errorMessage = err?.error?.detail ?? "Impossible d'envoyer le code.";
      },
    });
  }

  // --------------------------------------------------------------- Helpers
  private clearDigits(): void {
    this.digits = Array(6).fill('');
    this.digitInputs?.toArray().forEach(el => (el.nativeElement.value = ''));
    setTimeout(() => this.digitInputs?.first?.nativeElement.focus(), 0);
  }
}
