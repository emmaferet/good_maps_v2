import { Component, EventEmitter, Output } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './header.component.html',
  styleUrls: ['./header.component.scss']
})
export class HeaderComponent {
  @Output() openFilters = new EventEmitter<void>();
  @Output() openInfo = new EventEmitter<void>();

  onFiltersClick(): void {
    this.openFilters.emit();
  }

  onInfoClick(): void {
    this.openInfo.emit();
  }
}
