import { Injectable, signal } from '@angular/core';
import { AccessibilityProfile } from '../models/accessibility-profile.model';
import { ActivityCategory } from '../models/activity-category.model';
import { Activity } from '../models/activity.model';

export interface SearchHistoryItem {
  query: string;
  lat: number;
  lon: number;
  profile: AccessibilityProfile;
  category: ActivityCategory;
  timestamp: number;
}

@Injectable({
  providedIn: 'root'
})
export class PreferencesService {
  private readonly STORAGE_PROFILE_KEY = 'goodmaps_profile';
  private readonly STORAGE_CATEGORY_KEY = 'goodmaps_category';
  private readonly STORAGE_HISTORY_KEY = 'goodmaps_search_history';
  private readonly STORAGE_FAVORITES_KEY = 'goodmaps_favorites';
  private readonly STORAGE_ONBOARDED_KEY = 'goodmaps_onboarded';

  // Signals réactifs pour Angular 19
  readonly selectedProfile = signal<AccessibilityProfile>(this.loadProfile());
  readonly selectedCategory = signal<ActivityCategory>(this.loadCategory());
  readonly favorites = signal<Activity[]>(this.loadFavorites());
  readonly searchHistory = signal<SearchHistoryItem[]>(this.loadHistory());
  readonly hasCompletedOnboarding = signal<boolean>(this.loadOnboardedState());

  setProfile(profile: AccessibilityProfile): void {
    this.selectedProfile.set(profile);
    this.safeSetItem(this.STORAGE_PROFILE_KEY, profile);
  }

  setCategory(category: ActivityCategory): void {
    this.selectedCategory.set(category);
    this.safeSetItem(this.STORAGE_CATEGORY_KEY, category);
  }

  completeOnboarding(): void {
    this.hasCompletedOnboarding.set(true);
    this.safeSetItem(this.STORAGE_ONBOARDED_KEY, 'true');
  }

  resetOnboarding(): void {
    this.hasCompletedOnboarding.set(false);
    this.safeSetItem(this.STORAGE_ONBOARDED_KEY, 'false');
  }

  // --- Gestion des Favoris (Bonus SaaS) ---
  toggleFavorite(activity: Activity): boolean {
    const current = this.favorites();
    const exists = current.some(fav => fav.id === activity.id);
    let updated: Activity[];

    if (exists) {
      updated = current.filter(fav => fav.id !== activity.id);
    } else {
      updated = [{ ...activity, isFavorite: true }, ...current];
    }

    this.favorites.set(updated);
    this.safeSetItem(this.STORAGE_FAVORITES_KEY, JSON.stringify(updated));
    return !exists;
  }

  isFavorite(activityId: string): boolean {
    return this.favorites().some(fav => fav.id === activityId);
  }

  // --- Mémorisation de l'Historique de Recherche (Bonus SaaS) ---
  addToHistory(item: Omit<SearchHistoryItem, 'timestamp'>): void {
    const current = this.searchHistory().filter(h => h.query.toLowerCase() !== item.query.toLowerCase());
    const newItem: SearchHistoryItem = { ...item, timestamp: Date.now() };
    const updated = [newItem, ...current].slice(0, 8); // Conserve les 8 dernières recherches
    this.searchHistory.set(updated);
    this.safeSetItem(this.STORAGE_HISTORY_KEY, JSON.stringify(updated));
  }

  clearHistory(): void {
    this.searchHistory.set([]);
    if (typeof window !== 'undefined' && window.localStorage) {
      window.localStorage.removeItem(this.STORAGE_HISTORY_KEY);
    }
  }

  // --- Helpers de stockage sécurisé ---
  private loadProfile(): AccessibilityProfile {
    const val = this.safeGetItem(this.STORAGE_PROFILE_KEY);
    return (val as AccessibilityProfile) || 'PMR';
  }

  private loadCategory(): ActivityCategory {
    const val = this.safeGetItem(this.STORAGE_CATEGORY_KEY);
    return (val as ActivityCategory) || 'ALL';
  }

  private loadOnboardedState(): boolean {
    return this.safeGetItem(this.STORAGE_ONBOARDED_KEY) === 'true';
  }

  private loadFavorites(): Activity[] {
    try {
      const data = this.safeGetItem(this.STORAGE_FAVORITES_KEY);
      return data ? JSON.parse(data) : [];
    } catch {
      return [];
    }
  }

  private loadHistory(): SearchHistoryItem[] {
    try {
      const data = this.safeGetItem(this.STORAGE_HISTORY_KEY);
      return data ? JSON.parse(data) : [];
    } catch {
      return [];
    }
  }

  private safeGetItem(key: string): string | null {
    if (typeof window !== 'undefined' && window.localStorage) {
      return window.localStorage.getItem(key);
    }
    return null;
  }

  private safeSetItem(key: string, value: string): void {
    if (typeof window !== 'undefined' && window.localStorage) {
      try {
        window.localStorage.setItem(key, value);
      } catch (err) {
        console.warn('Impossible d’écrire dans le localStorage:', err);
      }
    }
  }
}
