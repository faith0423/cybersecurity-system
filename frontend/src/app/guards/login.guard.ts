import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const loginGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isLoggedIn()) {
    const role = authService.getRole();

    if (role === 'ADMIN') {
      router.navigate(['/dashboard'], { replaceUrl: true });
    } else {
      router.navigate(['/incidents'], { replaceUrl: true });
    }

    return false;
  }

  return true;
};