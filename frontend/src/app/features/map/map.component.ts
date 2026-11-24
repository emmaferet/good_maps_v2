import { Component, ElementRef, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output, SimpleChanges, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import * as L from 'leaflet';
import { Activity } from '../../core/models/activity.model';

@Component({
  selector: 'app-map',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './map.component.html',
  styleUrls: ['./map.component.scss']
})
export class MapComponent implements OnInit, OnChanges, OnDestroy {
  @ViewChild('mapContainer', { static: true }) mapContainer!: ElementRef<HTMLDivElement>;

  @Input() centerLat = 48.8566;
  @Input() centerLon = 2.3522;
  @Input() activities: Activity[] = [];
  @Input() selectedActivity: Activity | null = null;

  @Output() activitySelected = new EventEmitter<Activity>();
  @Output() mapBoundsChanged = new EventEmitter<{ lat: number; lon: number }>();

  private map: L.Map | null = null;
  private markersLayer: L.LayerGroup | null = null;

  ngOnInit(): void {
    this.initMap();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (!this.map) return;

    if (changes['centerLat'] || changes['centerLon']) {
      this.map.setView([this.centerLat, this.centerLon], 14, { animate: true });
    }

    if (changes['activities'] || changes['selectedActivity']) {
      this.updateMarkers();
    }
  }

  ngOnDestroy(): void {
    if (this.map) {
      this.map.remove();
      this.map = null;
    }
  }

  private initMap(): void {
    if (this.map) return;

    this.map = L.map(this.mapContainer.nativeElement, {
      center: [this.centerLat, this.centerLon],
      zoom: 14,
      zoomControl: false // Déplacé pour un rendu mobile épuré
    });

    // Tuiles cartographiques libres et gratuites sans clé API requise (OpenStreetMap France)
    L.tileLayer('https://{s}.tile.openstreetmap.fr/osmfr/{z}/{x}/{y}.png', {
      maxZoom: 19,
      attribution: '&copy; <a href="https://www.openstreetmap.org/copyright" target="_blank" rel="noopener noreferrer">OpenStreetMap</a> contributors &copy; <a href="https://openstreetmap.fr/" target="_blank" rel="noopener noreferrer">OSM France</a>'
    }).addTo(this.map);

    // Contrôles de zoom positionnés en haut à droite
    L.control.zoom({ position: 'topright' }).addTo(this.map);

    this.markersLayer = L.layerGroup().addTo(this.map);

    this.map.on('moveend', () => {
      if (!this.map) return;
      const center = this.map.getCenter();
      this.mapBoundsChanged.emit({ lat: center.lat, lon: center.lng });
    });

    this.updateMarkers();

    // Garantit le bon calcul des dimensions du conteneur Leaflet après le rendu initial
    setTimeout(() => {
      this.map?.invalidateSize();
    }, 100);
  }

  private updateMarkers(): void {
    if (!this.markersLayer) return;
    this.markersLayer.clearLayers();

    this.activities.forEach(activity => {
      const isSelected = this.selectedActivity?.id === activity.id;
      const markerIcon = this.createCustomPinIcon(isSelected, activity.name);

      const marker = L.marker([activity.latitude, activity.longitude], {
        icon: markerIcon,
        title: activity.name,
        zIndexOffset: isSelected ? 1000 : 100
      });

      marker.on('click', () => {
        this.activitySelected.emit(activity);
        if (this.map) {
          this.map.panTo([activity.latitude, activity.longitude], { animate: true });
        }
      });

      this.markersLayer?.addLayer(marker);
    });
  }

  private createCustomPinIcon(isSelected: boolean, title: string): L.DivIcon {
    const size = isSelected ? 44 : 32;
    const height = isSelected ? 54 : 40;
    const pinClass = isSelected ? 'goodmaps-pin selected' : 'goodmaps-pin';

    // Marqueur SVG rouge corail conforme au visuel 3 de la maquette
    const html = `
      <div class="${pinClass}" style="width: ${size}px; height: ${height}px;">
        <svg viewBox="0 0 36 46" fill="none" xmlns="http://www.w3.org/2000/svg">
          <filter id="shadow-${isSelected}" x="-20%" y="-10%" width="140%" height="130%">
            <feDropShadow dx="0" dy="4" stdDeviation="3" flood-color="rgba(0,0,0,0.25)"/>
          </filter>
          <!-- Épingle extérieure rouge corail -->
          <path d="M18 0C8.05887 0 0 8.05887 0 18C0 29.5 18 46 18 46C18 46 36 29.5 36 18C36 8.05887 27.9411 0 18 0Z" 
                fill="${isSelected ? '#D4363B' : '#FF5A5F'}" 
                filter="url(#shadow-${isSelected})"/>
          <!-- Point central foncé -->
          <circle cx="18" cy="18" r="${isSelected ? 7 : 5}" fill="#111827"/>
        </svg>
      </div>
    `;

    return L.divIcon({
      className: 'custom-pin-wrapper',
      html: html,
      iconSize: [size, height],
      iconAnchor: [size / 2, height],
      popupAnchor: [0, -height]
    });
  }
}
