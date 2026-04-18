import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Incident } from '../models/incident.model';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class IncidentService {
  private readonly apiUrl = '/api/incidents';

  constructor(private http: HttpClient) {}

  private getHeaders(): HttpHeaders {
    const token = localStorage.getItem('token') || '';
    return new HttpHeaders({
      'Authorization': 'Bearer ' + token,
      'Content-Type': 'application/json'
    });
  }

  getAll(): Observable<Incident[]> {
    return this.http.get<Incident[]>(this.apiUrl, { headers: this.getHeaders() });
  }

  getAllIncidents(): Observable<Incident[]> {
    return this.getAll();
  }

  getById(id: number): Observable<Incident> {
    return this.http.get<Incident>(`${this.apiUrl}/${id}`, { headers: this.getHeaders() });
  }

  createIncident(incident: Partial<Incident>): Observable<Incident> {
    return this.http.post<Incident>(this.apiUrl, incident, { headers: this.getHeaders() });
  }

  updateStatus(id: number, status: string): Observable<Incident> {
    return this.http.patch<Incident>(`${this.apiUrl}/${id}/status`, { status }, { headers: this.getHeaders() });
  }

  deleteIncident(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`, { headers: this.getHeaders() });
  }
  downloadIncidentReport(): Observable<Blob> {
  const token = localStorage.getItem('token');

  return this.http.get(`${environment.apiBaseUrl}/api/reports/incidents/pdf`, {
    responseType: 'blob',
    headers: {
      Authorization: `Bearer ${token}`
    }
  });
}
   emailIncidentReport(): Observable<string> {
  const token = localStorage.getItem('token');

  return this.http.post(`${environment.apiBaseUrl}/api/reports/incidents/pdf/email`, {}, {
    responseType: 'text',
    headers: {
      Authorization: `Bearer ${token}`
    }
  });
}
}