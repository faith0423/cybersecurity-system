import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router } from '@angular/router';
import { IncidentService } from '../../services/incident.service';
import { AuthService } from '../../services/auth.service';
import { NavbarComponent } from '../navbar/navbar.component';
import { Incident } from '../../models/incident.model';

@Component({
  selector: 'app-incidents',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, NavbarComponent],
  templateUrl: './incidents.component.html',
  styleUrls: ['./incidents.component.css']
})
export class IncidentsComponent implements OnInit {
  private readonly incidentService = inject(IncidentService);
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly fb = inject(FormBuilder);

  incidentForm: FormGroup;
  incidents: Incident[] = [];
  loading = false;
  submitting = false;
  errorMessage = '';
  searchTerm = '';

  currentPage = 1;
  pageSize = 5;

  get isAdmin(): boolean {
    return this.authService.isAdmin();
  }

  constructor() {
    this.incidentForm = this.fb.group({
      title: [
        '',
        [
           Validators.required, Validators.minLength(10), Validators.maxLength(100),this.mustContainLettersValidator()
      ]
      ],
      description: [
        '',
         [
          Validators.required, Validators.minLength(10), Validators.maxLength(1000),this.mustContainLettersValidator()
        ]]
    });
  }

  ngOnInit(): void {
    this.loadIncidents();
  }

  loadIncidents(): void {
    this.loading = true;
    this.incidentService.getAllIncidents().subscribe({
      next: (data) => {
        this.incidents = data;
        this.loading = false;
      },
      error: () => {
        this.errorMessage = 'Could not load incidents.';
        this.loading = false;
      }
    });
  }

  submitIncident(): void {
    if (this.incidentForm.invalid) {
      this.incidentForm.markAllAsTouched();
      return;
    }

    this.submitting = true;
    this.errorMessage = '';

    const payload = {
      title: this.incidentForm.value.title,
      description: this.incidentForm.value.description
    };

    this.incidentService.createIncident(payload).subscribe({
      next: (saved) => {
        this.incidents = [saved, ...this.incidents];
        this.currentPage = 1;
        this.searchTerm = '';
        this.submitting = false;
        this.incidentForm.reset();
      },
      error: (err) => {
        this.submitting = false;
        this.errorMessage = err?.error?.message ?? 'Failed to submit incident.';
      }
    });
  }

  manageIncident(id: number): void {
    this.router.navigate(['/incidents', id, 'manage']);
  }

  deleteIncident(id?: number): void {
    if (!id || !confirm('Delete this incident?')) return;

    this.incidentService.deleteIncident(id).subscribe({
      next: () => {
        this.incidents = this.incidents.filter(i => i.id !== id);
        if (this.currentPage > this.totalPages) this.currentPage = this.totalPages;
      },
      error: () => {
        this.errorMessage = 'Could not delete incident.';
      }
    });
  }

  onSearchChange(event: Event) {
    const searchValue = (event.target as HTMLInputElement).value;
    this.searchTerm = (searchValue || '').trim().toLowerCase();
    this.currentPage = 1;
  }

  get filteredIncidents(): Incident[] {
    if (!this.searchTerm) {
      return this.incidents;
    }

    return this.incidents.filter((incident) => {
      const searchable = [
        incident.title,
        incident.description,
        incident.category,
        incident.severity,
        incident.assignedRole,
        incident.status,
        incident.createdBy
      ]
        .filter(Boolean)
        .join(' ')
        .toLowerCase();

      return searchable.includes(this.searchTerm);
    });
  }

  get totalPages(): number {
    return Math.ceil(this.filteredIncidents.length / this.pageSize) || 1;
  }

  get paginatedIncidents(): Incident[] {
    const start = (this.currentPage - 1) * this.pageSize;
    return this.filteredIncidents.slice(start, start + this.pageSize);
  }

  get pageNumbers(): number[] {
    return Array.from({ length: this.totalPages }, (_, i) => i + 1);
  }

  goToPage(page: number): void {
    if (page >= 1 && page <= this.totalPages) this.currentPage = page;
  }

  previousPage(): void {
    if (this.currentPage > 1) this.currentPage--;
  }

  nextPage(): void {
    if (this.currentPage < this.totalPages) this.currentPage++;
  }

  severityClass(severity?: string): string {
    switch (severity?.toUpperCase()) {
      case 'CRITICAL': return 'pill pill-critical';
      case 'HIGH': return 'pill pill-high';
      case 'MEDIUM': return 'pill pill-medium';
      default: return 'pill pill-low';
    }
  }

  statusClass(status?: string): string {
    switch (status?.toUpperCase()) {
      case 'SOLVED': return 'status status-solved';
      case 'FIXING': return 'status status-fixing';
      case 'ASSIGNED': return 'status status-assigned';
      default: return 'status status-submitted';
    }
  }
  downloadPdfReport(): void {
  this.incidentService.downloadIncidentReport().subscribe({
    next: (blob) => {
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = 'incident-report.pdf';
      a.click();
      window.URL.revokeObjectURL(url);
    },
    error: () => {
      this.errorMessage = 'Could not download PDF report.';
    }
  });
}
emailPdfReport(): void {
  this.incidentService.emailIncidentReport().subscribe({
    next: (message) => {
      alert(message);
    },
    error: () => {
      this.errorMessage = 'Could not email PDF report.';
    }
  });
}
  private mustContainLettersValidator(): Validators {
    return (control: AbstractControl): ValidationErrors | null => {
      const value = (control.value ?? '').toString().trim();

      if (!value) {
        return null;
      }

      const hasLetter = /[a-zA-Z]/.test(value);

      if (!hasLetter) {
        return { mustContainLetters: true };
      }

      return null;
    };
  }
}
