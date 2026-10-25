import { Component, EventEmitter, Output, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PreferencesService } from '../../core/services/preferences.service';
import { Activity } from '../../core/models/activity.model';

@Component({
  selector: 'app-favorites-modal',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './favorites-modal.component.html',
  styleUrls: ['./favorites-modal.component.scss']
})
export class FavoritesModalComponent {
  readonly preferences = inject(PreferencesService);

  @Output() close = new EventEmitter<void>();
  @Output() selectFavorite = new EventEmitter<Activity>();

  get favorites(): Activity[] {
    return this.preferences.favorites();
  }

  onSelect(activity: Activity): void {
    this.selectFavorite.emit(activity);
    this.close.emit();
  }

  removeFavorite(activity: Activity, event: Event): void {
    event.stopPropagation();
    this.preferences.toggleFavorite(activity);
  }

  callEstablishment(phone: string | undefined, event: Event): void {
    event.stopPropagation();
    if (phone) {
      window.location.href = `tel:${phone.replace(/\s+/g, '')}`;
    }
  }

  openWebsite(url: string | undefined, osmUrl: string, event: Event): void {
    event.stopPropagation();
    const target = url || osmUrl;
    window.open(target, '_blank', 'noopener,noreferrer');
  }
}
