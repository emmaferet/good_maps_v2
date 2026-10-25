import { Component, EventEmitter, Input, Output, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Activity } from '../../core/models/activity.model';
import { PreferencesService } from '../../core/services/preferences.service';

@Component({
  selector: 'app-activity-card',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './activity-card.component.html',
  styleUrls: ['./activity-card.component.scss']
})
export class ActivityCardComponent {
  private readonly preferences = inject(PreferencesService);

  @Input({ required: true }) activity!: Activity;
  @Output() close = new EventEmitter<void>();

  get isFavorite(): boolean {
    return this.preferences.isFavorite(this.activity.id);
  }

  toggleFavorite(): void {
    this.preferences.toggleFavorite(this.activity);
  }

  callEstablishment(): void {
    if (this.activity.contact.phone) {
      window.location.href = `tel:${this.activity.contact.phone.replace(/\s+/g, '')}`;
    } else {
      alert(`Numéro de téléphone non renseigné sur OpenStreetMap pour ${this.activity.name}.`);
    }
  }

  bookOnline(): void {
    if (this.activity.contact.website) {
      window.open(this.activity.contact.website, '_blank', 'noopener,noreferrer');
    } else {
      // Fallback vers la fiche officielle OpenStreetMap
      window.open(this.activity.osmUrl, '_blank', 'noopener,noreferrer');
    }
  }
}
