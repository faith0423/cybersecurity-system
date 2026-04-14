export interface Incident {
  id?: number;
  title: string;
  description: string;
  severity: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  status?: 'SUBMITTED' | 'ASSIGNED' | 'FIXING' | 'SOLVED';
  createdAt?: string;
  createdBy?: string;
  category?: string;
  recommendation?: string;
  assignedRole?: string;
  userId?: number;
}