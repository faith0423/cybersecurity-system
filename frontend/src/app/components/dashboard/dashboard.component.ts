import { Component, OnDestroy, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { NavbarComponent } from '../navbar/navbar.component';
import { IncidentService } from '../../services/incident.service';
import { Incident } from '../../models/incident.model';
import Chart from 'chart.js/auto';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink, NavbarComponent],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit, OnDestroy {
  private readonly incidentService = inject(IncidentService);

  incidents: Incident[] = [];
  loading = true;
  private refreshTimer?: number;
  private categoryChart?: Chart;
  private severityChart?: Chart;

  ngOnInit(): void {
    this.loadDashboard();
    // Refresh every 15 seconds so admin sees live updates from specialists
    this.refreshTimer = window.setInterval(() => this.loadDashboard(), 15000);
  }

  ngOnDestroy(): void {
    if (this.refreshTimer) window.clearInterval(this.refreshTimer);
    this.categoryChart?.destroy();
    this.severityChart?.destroy();
  }

  loadDashboard(): void {
    this.incidentService.getAll().subscribe({
      next: (data) => {
        this.incidents = data;
        this.loading = false;
        setTimeout(() => this.renderCharts(), 0);
      },
      error: () => { this.loading = false; }
    });
  }

  // ── Stats from real DB status ──
  get total(): number     { return this.incidents.length; }

  get submitted(): number {
    return this.incidents.filter(i => i.status === 'SUBMITTED').length;
  }

  get inProgress(): number {
    return this.incidents.filter(i => i.status === 'ASSIGNED' || i.status === 'FIXING').length;
  }

  get solved(): number {
    return this.incidents.filter(i => i.status === 'SOLVED').length;
  }

  get critical(): number {
    return this.incidents.filter(i => i.severity === 'CRITICAL' || i.severity === 'HIGH').length;
  }

  get recentIncidents(): Incident[] {
    return [...this.incidents].reverse().slice(0, 6);
  }

  get topCategory(): string {
    if (!this.incidents.length) return 'No incidents yet';
    const counts: Record<string, number> = {};
    for (const i of this.incidents) {
      const c = i.category || 'Uncategorized';
      counts[c] = (counts[c] || 0) + 1;
    }
    return Object.entries(counts).sort((a, b) => b[1] - a[1])[0][0];
  }

  get topRecommendation(): string {
    if (!this.incidents.length) return 'No recommendation available yet.';
    const relevant = this.incidents
      .filter(i => (i.category || 'Uncategorized') === this.topCategory && i.recommendation)
      .map(i => i.recommendation!);
    if (!relevant.length) return 'Review incident details and apply standard response.';
    const counts: Record<string, number> = {};
    for (const r of relevant) counts[r] = (counts[r] || 0) + 1;
    return Object.entries(counts).sort((a, b) => b[1] - a[1])[0][0];
  }

  get summarySentence(): string {
    if (!this.incidents.length) return 'No incidents have been logged yet.';
    return `Most incidents are ${this.topCategory.toLowerCase()}-related. ${this.solved} resolved, ${this.inProgress} in progress.`;
  }

  severityClass(severity?: string): string {
    switch (severity?.toUpperCase()) {
      case 'CRITICAL': return 'pill pill-critical';
      case 'HIGH':     return 'pill pill-high';
      case 'MEDIUM':   return 'pill pill-medium';
      default:         return 'pill pill-low';
    }
  }

  statusClass(status?: string): string {
    switch (status?.toUpperCase()) {
      case 'SOLVED':   return 'status status-solved';
      case 'FIXING':   return 'status status-fixing';
      case 'ASSIGNED': return 'status status-assigned';
      default:         return 'status status-submitted';
    }
  }

  private renderCharts(): void {
    const categoryCanvas = document.getElementById('categoryChart') as HTMLCanvasElement | null;
    const severityCanvas  = document.getElementById('severityChart')  as HTMLCanvasElement | null;
    if (!categoryCanvas || !severityCanvas) return;

    const categoryCounts: Record<string, number> = {};
    const severityCounts: Record<string, number> = {};

    for (const i of this.incidents) {
      const cat = i.category || 'Uncategorized';
      categoryCounts[cat] = (categoryCounts[cat] || 0) + 1;
      const sev = i.severity || 'UNKNOWN';
      severityCounts[sev] = (severityCounts[sev] || 0) + 1;
    }

    this.categoryChart?.destroy();
    this.severityChart?.destroy();

    this.categoryChart = new Chart(categoryCanvas, {
      type: 'pie',
      data: {
        labels: Object.keys(categoryCounts),
        datasets: [{
          data: Object.values(categoryCounts),
          backgroundColor: ['#8b5cf6','#2563eb','#7c3aed','#38bdf8','#a78bfa','#60a5fa'],
          borderColor: '#0f172a',
          borderWidth: 2,
          hoverOffset: 10
        }]
      },
      options: {
        animation: { animateRotate: true, animateScale: true, duration: 1000 },
        plugins: { legend: { labels: { color: '#f8fafc', padding: 16 } } }
      }
    });

    this.severityChart = new Chart(severityCanvas, {
      type: 'bar',
      data: {
        labels: Object.keys(severityCounts),
        datasets: [{
          label: 'Incidents by Severity',
          data: Object.values(severityCounts),
          backgroundColor: ['#38bdf8','#818cf8','#8b5cf6','#e24b4a'],
          borderRadius: 10,
          borderSkipped: false
        }]
      },
      options: {
        animation: { duration: 1000 },
        scales: {
          x: { ticks: { color: '#f8fafc' }, grid: { color: 'rgba(148,163,184,0.1)' } },
          y: { ticks: { color: '#f8fafc', stepSize: 1 }, grid: { color: 'rgba(148,163,184,0.1)' } }
        },
        plugins: { legend: { labels: { color: '#f8fafc' } } }
      }
    });
  }
}