import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SplashComponent } from './features/splash/splash.component';
import { WelcomeModalComponent, WelcomeFormResult } from './features/onboarding/welcome-modal.component';
import { PreferencesService } from './core/services/preferences.service';
import { ActivityService } from './core/services/activity.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [CommonModule, SplashComponent, WelcomeModalComponent],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {
  private readonly preferencesService = inject(PreferencesService);
  private readonly activityService = inject(ActivityService);

  // Signaux d'affichage des écrans conformes à la maquette
  readonly showSplash = signal<boolean>(true);
  readonly showWelcomeModal = signal<boolean>(false);
  readonly isInitialized = signal<boolean>(false);

  // Coordonnées cibles actuelles (par défaut Paris, actualisées par le formulaire)
  currentLat = 48.8566;
  currentLon = 2.3522;
  currentLocationName = 'Paris, France';

  ngOnInit(): void {
    // Vérification de l'état d'onboarding au démarrage
  }

  onSplashFinished(): void {
    this.showSplash.set(false);
    this.isInitialized.set(true);

    if (!this.preferencesService.hasCompletedOnboarding()) {
      this.showWelcomeModal.set(true);
    } else {
      // Si déjà complété, on lance directement la recherche initiale
      this.loadInitialActivities();
    }
  }

  onWelcomeFormCompleted(result: WelcomeFormResult): void {
    this.currentLat = result.latitude;
    this.currentLon = result.longitude;
    this.currentLocationName = result.locationName;
    this.showWelcomeModal.set(false);

    this.activityService.searchActivities({
      latitude: result.latitude,
      longitude: result.longitude,
      radius: result.radius,
      profile: result.profile,
      category: result.category
    }).subscribe();
  }

  onWelcomeModalClosed(): void {
    this.showWelcomeModal.set(false);
    if (this.activityService.currentActivities().length === 0) {
      this.loadInitialActivities();
    }
  }

  openSettingsModal(): void {
    this.showWelcomeModal.set(true);
  }

  private loadInitialActivities(): void {
    this.activityService.searchActivities({
      latitude: this.currentLat,
      longitude: this.currentLon,
      radius: 3000,
      profile: this.preferencesService.selectedProfile(),
      category: this.preferencesService.selectedCategory()
    }).subscribe();
  }
}
