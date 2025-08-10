import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { RouterModule } from '@angular/router';
import { HttpClientModule, HTTP_INTERCEPTORS } from '@angular/common/http';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';

import { AppComponent } from './app.component';
import { routes } from './app.routes';
import { LoginComponent } from './shared/components/login/login.component';
import { RegisterComponent } from './shared/components/register/register.component';
import { ForgotPasswordComponent } from './shared/components/forgot-password/forgot-password.component';
import { ResetPasswordComponent } from './shared/components/reset-password/reset-password.component';
import { AuthInterceptor } from './services/auth.interceptor';
import { SponsorModalComponent } from './dashboard/admin/sponsor-modal/sponsor-modal.component';
import { OrganisationModalComponent } from './dashboard/admin/organisation-modal/organisation-modal.component';
import { UserListComponent } from './dashboard/admin/user-list/user-list.component';
import { AddUserModalComponent } from './dashboard/admin/add-user-modal/add-user-modal.component';

@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    RegisterComponent,
    ForgotPasswordComponent,
    ResetPasswordComponent,
    SponsorModalComponent,
    OrganisationModalComponent,
    UserListComponent,
    AddUserModalComponent
    // Add any non-standalone components here
  ],
  imports: [
    BrowserModule,
    HttpClientModule,
    RouterModule.forRoot(routes),
    ReactiveFormsModule,
    FormsModule
    // Add any other modules here
  ],
  providers: [
    {
      provide: HTTP_INTERCEPTORS,
      useClass: AuthInterceptor,
      multi: true
    }
  ],
  bootstrap: [AppComponent]
})
export class AppModule { } 