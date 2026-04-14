import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { IncidentAnalysisResponse } from '../models/incident-analysis-response';

@Injectable({
  providedIn: 'root'
})
export class AiAnalysisService {
  private apiUrl = 'http://localhost:8080/api/ai';

  constructor(private http: HttpClient) {}

  analyzeIncident(description: string): Observable<IncidentAnalysisResponse> {
    return this.http.post<IncidentAnalysisResponse>(`${this.apiUrl}/analyze`, {
      description
    });
  }
}