import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login.component';
import { DashboardComponent } from './components/dashboard/dashboard.component';
import { IncidentsComponent } from './components/incidents/incidents.component';
import { ManageIncidentComponent } from './components/manage-incident/manage-incident.component';
import { authGuard } from './guards/auth.guard';
import { adminGuard } from './guards/admin.guard';
import { loginGuard } from './guards/login.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'login', component: LoginComponent, canActivate: [loginGuard] },
  { path: 'dashboard', component: DashboardComponent, canActivate: [authGuard, adminGuard] },
  { path: 'incidents', component: IncidentsComponent, canActivate: [authGuard] },
  { path: 'incidents/:id/manage', component: ManageIncidentComponent, canActivate: [authGuard] },
  { path: '**', redirectTo: 'login' }
];