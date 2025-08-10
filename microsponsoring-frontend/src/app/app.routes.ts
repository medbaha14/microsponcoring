import { Routes } from '@angular/router';
import { MainComponent as AdminMainComponent } from './dashboard/admin/main/main.component';
import { MainComponent as OrganisationMainComponent } from './dashboard/organisation/main/main.component';
import { MainComponent as SponsorMainComponent } from './dashboard/sponsor/main/main.component';
import { LoginComponent } from './shared/components/login/login.component';
import { RegisterComponent } from './shared/components/register/register.component';
import { ForgotPasswordComponent } from './shared/components/forgot-password/forgot-password.component';
import { ResetPasswordComponent } from './shared/components/reset-password/reset-password.component';
import { AuthGuard } from './services/auth.guard';
import { UserListComponent } from './dashboard/admin/user-list/user-list.component';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'forgot-password', component: ForgotPasswordComponent },
  { path: 'reset-password', component: ResetPasswordComponent },
  { path: 'profile/:userId', loadComponent: () => import('./dashboard/organisation/profile/profile.component').then(m => m.OrganisationProfileComponent) },
  
  // Payment result pages
  { path: 'payment/success', loadComponent: () => import('./shared/components/payment-success/payment-success.component').then(m => m.PaymentSuccessComponent) },
  { path: 'payment/failure', loadComponent: () => import('./shared/components/payment-failure/payment-failure.component').then(m => m.PaymentFailureComponent) },
        
  {
    path: 'dashboard',
    children: [
      {
        path: 'admin',
        canActivate: [AuthGuard],
        data: { allowedTypes: ['ADMIN'] },
        component: AdminMainComponent,
        children: [
          { path: 'user-list', component: UserListComponent },
          { path: 'stats-page', loadComponent: () => import('./dashboard/admin/stats-page/stats-page.component').then(m => m.StatsPageComponent) },
          { path: 'invoices', loadComponent: () => import('./dashboard/admin/invoices/invoices.component').then(m => m.InvoicesComponent) },
        ]
      },
      {
        path: 'sponsor',
        canActivate: [AuthGuard],
        data: { allowedTypes: ['SPONSOR'] },
        component: SponsorMainComponent,
        children: [
           { path: 'sponsored', loadComponent: () => import('./dashboard/sponsor/sponsored/sponsored.component').then(m => m.SponsoredComponent) },
          { path: 'organisation-list', loadComponent: () => import('./dashboard/sponsor/organisation-list/organisation-list.component').then(m => m.OrganisationListComponent) },
          { path: 'profile', loadComponent: () => import('./dashboard/sponsor/profile/profile.component').then(m => m.ProfileComponent) },
        ]
      },
      {
        path: 'organisation',
        canActivate: [AuthGuard],
        data: { allowedTypes: ['ORGANISATION_NONPROFIT'] },
        component: OrganisationMainComponent,
        children: [
          { path: 'sidebar', loadComponent: () => import('./dashboard/organisation/sidebar/sidebar.component').then(m => m.SidebarComponent) },
          { path: 'profile', loadComponent: () => import('./dashboard/organisation/profile/profile.component').then(m => m.OrganisationProfileComponent) },
          { path: 'customize-profile', loadComponent: () => import('./dashboard/organisation/customize-profile/customize-profile.component').then(m => m.CustomizeProfileComponent) },
          { path: 'edit-recognition-benefits', loadComponent: () => import('./dashboard/organisation/edit-recognition-benefits/edit-recognition-benefits.component').then(m => m.EditRecognitionBenefitsComponent) },
          { path: 'sponsoring-list', loadComponent: () => import('./dashboard/organisation/sponsoring-list/sponsoring-list.component').then(m => m.SponsoringListComponent) },
          { path: 'recognition-benefits', loadComponent: () => import('./dashboard/organisation/recognition-benefits/recognition-benefits.component').then(m => m.RecognitionBenefitsComponent) },
          { path: 'bank-accounts', loadComponent: () => import('./dashboard/organisation/bank-accounts/bank-accounts.component').then(m => m.BankAccountsComponent) },
        ]
      }
    ]
  },
  { path: '**', redirectTo: 'login' }
];
