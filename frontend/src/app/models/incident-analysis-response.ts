export interface IncidentAnalysisResponse {
  predictedSeverity: string;
  predictedCategory: string;
  recommendedRole: string;
  recommendation: string;
  aiSuccess: boolean;
  errorMessage: string | null;
}