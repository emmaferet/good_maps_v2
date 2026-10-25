import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SplashComponent } from './features/splash/splash.component';
import { WelcomeModalComponent, WelcomeFormResult } from './features/onboarding/welcome-modal.component';
import { HeaderComponent } from './features/header/header.component';
import { MapComponent } from './features/map/map.component';
import { ActivityCardComponent } from './features/activity-card/activity-card.component';
import { InfoModalComponent } from './features/info-modal/info-modal.component';
import { FavoritesModalComponent } from './features/favorites-modal/favorites-modal.component';
import { PreferencesService } from './core/services/preferences.service';
import { ActivityService } from './core/services/activity.service';
import { Activity } from './core/models/activity.model';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    SplashComponent,
    WelcomeModalComponent,
    HeaderComponent,
    MapComponent,
    ActivityCardComponent,
    InfoModalComponent,
    FavoritesModalComponent
  ],
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {
  readonly preferencesService = inject(PreferencesService);
  readonly activityService = inject(ActivityService);

  // Signaux d'affichage des écrans
  readonly showSplash = signal<boolean>(true);
  readonly showWelcomeModal = signal<boolean>(false);
  readonly showInfoModal = signal<boolean>(false);
  readonly showFavoritesModal = signal<boolean>(false);
  readonly toastMessage = signal<string | null>(null);

  // Coordonnées de navigation cartographique (Paris par défaut)
  centerLat = 48.8719; // Centré sur l'Opéra Garnier pour la démonstration
  centerLon = 2.3316;
  currentLocationName = 'Paris, France';
  currentRadius = 3000;

  ngOnInit(): void {
    // Si l'utilisateur est déjà venu, on prépare les activités
    if (this.preferencesService.hasCompletedOnboarding()) {
      const history = this.preferencesService.searchHistory();
      if (history.length > 0) {
        this.centerLat = history[0].lat;
        this.centerLon = history[0].lon;
        this.currentLocationName = history[0].query;
      }
    }
  }

  onSplashFinished(): void {
    this.showSplash.set(false);

    if (!this.preferencesService.hasCompletedOnboarding()) {
      this.showWelcomeModal.set(true);
    } else {
      this.fetchActivities();
    }
  }

  onWelcomeCompleted(result: WelcomeFormResult): void {
    this.centerLat = result.latitude;
    this.centerLon = result.longitude;
    this.currentLocationName = result.locationName;
    this.currentRadius = result.radius;
    this.showWelcomeModal.set(false);

    this.fetchActivities();
  }

  onActivitySelected(activity: Activity): void {
    this.activityService.selectActivity(activity);
  }

  onCloseActivityCard(): void {
    this.activityService.selectActivity(null);
  }

  onGetSuggestions(): void {
    this.activityService.getSuggestions({
      latitude: this.centerLat,
      longitude: this.centerLon,
      radius: this.currentRadius,
      profile: this.preferencesService.selectedProfile(),
      category: this.preferencesService.selectedCategory()
    }).subscribe({
      next: (activities) => {
        const count = activities.length;
        const profile = this.preferencesService.selectedProfile();
        this.showToast(`🎯 ${count} suggestions optimisées pour le profil ${profile} trouvées !`);
      },
      error: () => {
        this.showToast(`⚠️ Impossible de récupérer les suggestions actuellement.`);
      }
    });
  }

  openFiltersModal(): void {
    this.showWelcomeModal.set(true);
  }

  openInfoModal(): void {
    this.showInfoModal.set(true);
  }

  closeInfoModal(): void {
    this.showInfoModal.set(false);
  }

  openFavoritesModal(): void {
    this.showFavoritesModal.set(true);
  }

  closeFavoritesModal(): void {
    this.showFavoritesModal.set(false);
  }

  onSelectFavorite(activity: Activity): void {
    this.centerLat = activity.latitude;
    this.centerLon = activity.longitude;
    this.activityService.selectActivity(activity);
    this.showFavoritesModal.set(false);
  }

  private showToast(msg: string): void {
    this.toastMessage.set(msg);
    setTimeout(() => {
      this.toastMessage.set(null);
    }, 4000);
  }

  private fetchActivities(): void {
    this.activityService.searchActivities({
      latitude: this.centerLat,
      longitude: this.centerLon,
      radius: this.currentRadius,
      profile: this.preferencesService.selectedProfile(),
      category: this.preferencesService.selectedCategory()
    }).subscribe();
  }
}
