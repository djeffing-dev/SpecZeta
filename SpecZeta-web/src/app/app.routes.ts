import { Routes } from '@angular/router';
import { SiteComponent } from './pages/site/site.component';
import { DashboardComponent } from './pages/account/dashboard/dashboard.component';
import { LoginComponent } from './pages/auth/login/login.component';
import { SignupComponent } from './pages/auth/signup/signup.component';
import { authGuard } from './core/guards/auth.guard';
import { CreateAnnonceComponent } from './pages/annonces/create-annonce/create-annonce.component';
import { ListAnnonceComponent } from './pages/annonces/list-annonce/list-annonce.component';
import { ListUserAnnonceComponent } from './pages/annonces/list-user-annonce/list-user-annonce.component';
import { DetailAnnoceComponent } from './pages/annonces/detail-annoce/detail-annoce.component';

export const routes: Routes = [
    {
        path: '',
        component: SiteComponent,
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
    
    {
        path: "dashboard",
        canActivate: [authGuard],
        component: DashboardComponent
    },

    {
        path: "create-annonce",
        canActivate: [authGuard],
        component: CreateAnnonceComponent
    },

    {
        path:"annonce-user-list",
        canActivate: [authGuard],
        component: ListUserAnnonceComponent
    },

    {
        path: "update-annonce/:id",
        canActivate: [authGuard],
        loadComponent: () => import('./pages/annonces/update-annonce/update-annonce.component')
            .then(m => m.UpdateAnnonceComponent)
    },

    {
        path: "annonce-detail/:id",
        canActivate: [authGuard],
        component: DetailAnnoceComponent
    },

    {
        path:"annonce-list",
        component: ListAnnonceComponent
    },


];
