import { Routes } from '@angular/router';
import { SiteComponent } from './pages/site/site.component';
import { DashboardComponent } from './pages/account/dashboard/dashboard.component';
import { LoginComponent } from './pages/auth/login/login.component';
import { SignupComponent } from './pages/auth/signup/signup.component';

export const routes: Routes = [
    {
        path: '',
        component: SiteComponent,
    },

    {
        path: "dashboard",
        component: DashboardComponent
    },
    
    // {
    //     path: 'login',
    //     component: LoginComponent
    // },

    {
    path: 'oauth/redirect',
    loadComponent: () => import('./pages/auth/oauth-redirect/oauth-redirect.component')
      .then(m => m.OauthRedirectComponent)
    },

    {
        path: 'singup',
        component: SignupComponent
    },

    {
        path: 'verify-otp',
        loadComponent: () =>
          import('./pages/auth/otp/otp.component').then(m => m.OtpComponent),
    },

];
