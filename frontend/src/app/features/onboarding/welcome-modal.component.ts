import { Component, EventEmitter, Output, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ACCESSIBILITY_PROFILE_OPTIONS, AccessibilityProfile } from '../../core/models/accessibility-profile.model';
import { CATEGORY_OPTIONS, ActivityCategory } from '../../core/models/activity-category.model';
import { PreferencesService } from '../../core/services/preferences.service';
import { ActivityService } from '../../core/services/activity.service';
import { GeocodeResult } from '../../core/models/activity.model';

export interface WelcomeFormResult {
  latitude: number;
  longitude: number;
  locationName: string;
  radius: number;
  profile: AccessibilityProfile;
  category: ActivityCategory;
}

@Component({
  selector: 'app-welcome-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './welcome-modal.component.html',
  styleUrls: ['./welcome-modal.component.scss']
})
export class WelcomeModalComponent {
  private readonly preferences = inject(PreferencesService);
  private readonly activityService = inject(ActivityService);

  @Output() completed = new EventEmitter<WelcomeFormResult>();
  @Output() closed = new EventEmitter<void>();

  readonly profileOptions = ACCESSIBILITY_PROFILE_OPTIONS;
  readonly categoryOptions = CATEGORY_OPTIONS;

  // État local du formulaire
  selectedProfile = this.preferences.selectedProfile();
  selectedCategory = this.preferences.selectedCategory();
  selectedRadius = 3000;
  locationInput = 'Paris, France';
  selectedLat = 48.8566;
  selectedLon = 2.3522;

  // Autocomplétion Nominatim
  geocodeSuggestions = signal<GeocodeResult[]>([]);
  isGeocoding = signal<boolean>(false);
  isLocating = signal<boolean>(false);

  onProfileSelect(profile: AccessibilityProfile): void {
    this.selectedProfile = profile;
  }

  onCategorySelect(category: ActivityCategory): void {
    this.selectedCategory = category;
  }

  onLocationInputChange(val: string): void {
    this.locationInput = val;
    if (val.trim().length >= 3) {
      this.isGeocoding.set(true);
      this.activityService.geocode(val).subscribe({
        next: (results) => {
          this.geocodeSuggestions.set(results);
          this.isGeocoding.set(false);
        },
        error: () => this.isGeocoding.set(false)
      });
    } else {
      this.geocodeSuggestions.set([]);
    }
  }

  selectSuggestion(place: GeocodeResult): void {
    this.locationInput = place.displayName;
    this.selectedLat = place.latitude;
    this.selectedLon = place.longitude;
    this.geocodeSuggestions.set([]);
  }

  useCurrentLocation(): void {
    if (!navigator.geolocation) {
      alert('La géolocalisation n’est pas supportée par votre navigateur.');
      return;
    }

    this.isLocating.set(true);
    navigator.geolocation.getCurrentPosition(
      (pos) => {
        this.selectedLat = pos.coords.latitude;
        this.selectedLon = pos.coords.longitude;
        this.locationInput = `Position GPS (${pos.coords.latitude.toFixed(4)}, ${pos.coords.longitude.toFixed(4)})`;
        this.isLocating.set(false);
      },
      (err) => {
        console.warn('Erreur de géolocalisation:', err);
        this.isLocating.set(false);
        alert('Impossible de récupérer votre position actuelle. Utilisation de la ville par défaut.');
      },
      { timeout: 8000 }
    );
  }

  submit(): void {
    this.preferences.setProfile(this.selectedProfile);
    this.preferences.setCategory(this.selectedCategory);
    this.preferences.completeOnboarding();

    this.preferences.addToHistory({
      query: this.locationInput,
      lat: this.selectedLat,
      lon: this.selectedLon,
      profile: this.selectedProfile,
      category: this.selectedCategory
    });

    this.completed.emit({
      latitude: this.selectedLat,
      longitude: this.selectedLon,
      locationName: this.locationInput,
      radius: this.selectedRadius,
      profile: this.selectedProfile,
      category: this.selectedCategory
    });
  }

  close(): void {
    this.closed.emit();
  }
}
