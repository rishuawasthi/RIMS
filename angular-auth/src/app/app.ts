import { Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Sidebar } from './layout/sidebar/sidebar';
import { Navbar } from './layout/navbar/navbar';
import { AuthService } from './core/services/auth';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    RouterOutlet,
    Sidebar,
    Navbar
  ],
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {
  readonly authService = inject(AuthService);
}