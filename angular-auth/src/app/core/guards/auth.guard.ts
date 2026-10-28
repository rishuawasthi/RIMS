import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth';

/**
 * Prevents unauthenticated users from accessing protected RIMS operational routes
 * and enforces role-based access control (RBAC) based on route metadata.
 */
export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.isAuthenticated()) {
    return router.createUrlTree(['/login'], {
      queryParams: { returnUrl: state.url },
    });
  }

  const expectedRoles = route.data?.['roles'] as string[] | undefined;
  if (expectedRoles && expectedRoles.length > 0) {
    const userRole = authService.currentUser()?.role;
    if (!userRole || !expectedRoles.includes(userRole)) {
      return router.createUrlTree(['/dashboard']);
    }
  }

  return true;
};

/**
 * Prevents already-logged-in users from revisiting the /login screen.
 */
export const guestGuard: CanActivateFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (!authService.isAuthenticated()) {
    return true;
  }

  return router.createUrlTree(['/dashboard']);
};
