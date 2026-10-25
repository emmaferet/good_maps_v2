import { Component, EventEmitter, Output, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PreferencesService } from '../../core/services/preferences.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent {
  readonly preferences = inject(PreferencesService);

  @Output() openFilters = new EventEmitter<void>();
  @Output() openInfo = new EventEmitter<void>();
  @Output() openFavorites = new EventEmitter<void>();

  onFiltersClick(): void {
    this.openFilters.emit();
  }

  onInfoClick(): void {
    this.openInfo.emit();
  }

  onFavoritesClick(): void {
    this.openFavorites.emit();
  }
}

