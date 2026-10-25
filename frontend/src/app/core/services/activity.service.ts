import { Injectable, inject, signal } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, catchError, finalize, map, of, tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { Activity, GeocodeResult, SearchQuery } from '../models/activity.model';
import { PreferencesService } from './preferences.service';

@Injectable({
  providedIn: 'root'
})
export class ActivityService {
  private readonly http = inject(HttpClient);
  private readonly preferencesService = inject(PreferencesService);
  private readonly baseUrl = environment.apiBaseUrl;

  readonly isLoading = signal<boolean>(false);
  readonly errorMessage = signal<string | null>(null);
  readonly currentActivities = signal<Activity[]>([]);
  readonly selectedActivity = signal<Activity | null>(null);

  /**
   * Recherche de POIs accessibles autour de coordonnées GPS.
   */
  searchActivities(query: SearchQuery): Observable<Activity[]> {
    this.isLoading.set(true);
    this.errorMessage.set(null);

    let params = new HttpParams()
      .set('latitude', query.latitude.toString())
      .set('longitude', query.longitude.toString())
      .set('radius', (query.radius || 3000).toString())
      .set('profile', query.profile || this.preferencesService.selectedProfile())
      .set('category', query.category || this.preferencesService.selectedCategory())
      .set('limit', (query.limit || 40).toString());

    return this.http.get<Activity[]>(`${this.baseUrl}/activities/search`, { params }).pipe(
      map(activities => this.enrichWithFavorites(activities)),
      tap(activities => {
        this.currentActivities.set(activities);
        if (activities.length > 0 && !this.selectedActivity()) {
          this.selectedActivity.set(activities[0]);
        }
      }),
      catchError(err => {
        console.error('Erreur lors de la recherche d’activités:', err);
        const msg = err.error?.detail || 'Impossible de joindre le service cartographique.';
        this.errorMessage.set(msg);
        return of([]);
      }),
      finalize(() => this.isLoading.set(false))
    );
  }

  /**
   * Récupération des meilleures suggestions (Bouton 'Obtenir des suggestions').
   */
  getSuggestions(query: SearchQuery): Observable<Activity[]> {
    this.isLoading.set(true);
    this.errorMessage.set(null);

    let params = new HttpParams()
      .set('latitude', query.latitude.toString())
      .set('longitude', query.longitude.toString())
      .set('radius', (query.radius || 3000).toString())
      .set('profile', query.profile || this.preferencesService.selectedProfile())
      .set('category', query.category || this.preferencesService.selectedCategory());

    return this.http.get<Activity[]>(`${this.baseUrl}/activities/suggestions`, { params }).pipe(
      map(activities => this.enrichWithFavorites(activities)),
      tap(suggestions => {
        if (suggestions.length > 0) {
          this.currentActivities.set(suggestions);
          this.selectedActivity.set(suggestions[0]);
        }
      }),
      catchError(err => {
        console.error('Erreur lors du calcul des suggestions:', err);
        this.errorMessage.set('Erreur lors de la récupération des suggestions d’accessibilité.');
        return of([]);
      }),
      finalize(() => this.isLoading.set(false))
    );
  }

  /**
   * Géocodage textuel d'une adresse ou commune via l'API proxy Nominatim.
   */
  geocode(term: string): Observable<GeocodeResult[]> {
    if (!term || term.trim().length < 2) {
      return of([]);
    }

    const params = new HttpParams().set('query', term.trim());
    return this.http.get<GeocodeResult[]>(`${this.baseUrl}/activities/geocode`, { params }).pipe(
      catchError(err => {
        console.warn('Erreur lors du géocodage textuel:', err);
        return of([]);
      })
    );
  }

  selectActivity(activity: Activity | null): void {
    this.selectedActivity.set(activity);
  }

  private enrichWithFavorites(activities: Activity[]): Activity[] {
    return activities.map(act => ({
      ...act,
      isFavorite: this.preferencesService.isFavorite(act.id)
    }));
  }
}
