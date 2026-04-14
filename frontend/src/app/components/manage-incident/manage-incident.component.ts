import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { NavbarComponent } from '../navbar/navbar.component';
import { IncidentService } from '../../services/incident.service';
import { Incident } from '../../models/incident.model';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-manage-incident',
  standalone: true,
  imports: [CommonModule, NavbarComponent],
  templateUrl: './manage-incident.component.html',
  styleUrl: './manage-incident.component.css'
})
export class ManageIncidentComponent implements OnInit {
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly incidentService = inject(IncidentService);
  private readonly authService = inject(AuthService);

  incident?: Incident;
  loading = true;
  updating = false;
  errorMessage = '';
  successMessage = '';

  get isAdmin(): boolean {
    return this.authService.isAdmin();
  }

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));

    if (!id) {
      this.errorMessage = 'Invalid incident ID.';
      this.loading = false;
      return;
    }

    this.loadIncident(id);
  }

  loadIncident(id: number): void {
    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.incidentService.getById(id).subscribe({
      next: (data: any) => {
        this.incident = data;
        this.loading = false;
        console.log('Loaded incident:', data);
      },
      error: (error: any) => {
        this.errorMessage = error?.error?.message ?? error?.error ?? 'Failed to load incident.';
        this.loading = false;
        console.error('Load incident error:', error);
      }
    });
  }

  updateStatus(status: 'FIXING' | 'SOLVED'): void {
    if (!this.incident?.id) {
      this.errorMessage = 'Incident ID is missing.';
      return;
    }

    this.updating = true;
    this.errorMessage = '';
    this.successMessage = '';

    console.log('Updating incident status:', {
      id: this.incident.id,
      currentStatus: this.incident.status,
      nextStatus: status
    });

    this.incidentService.updateStatus(this.incident.id, status).subscribe({
      next: (updated: any) => {
        this.incident = updated;
        this.updating = false;
        this.successMessage = `Status changed to ${updated.status}.`;
        console.log('Status updated successfully:', updated);
      },
      error: (error: any) => {
        this.updating = false;
        this.errorMessage =
          error?.error?.message ??
          error?.error ??
          'Failed to update status.';
        console.error('Update status error:', error);
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/incidents'], { replaceUrl: true });
  }
}