import { AccessibilityProfile } from './accessibility-profile.model';
import { ActivityCategory } from './activity-category.model';

export interface AccessibilityDetails {
  wheelchair: string;
  wheelchairDescription?: string;
  toiletsWheelchair?: boolean;
  tactilePaving?: boolean;
  hearingLoop?: boolean;
  braille?: boolean;
  blindDescription?: string;
  deafDescription?: string;
  levelAccess?: string;
  rawTags?: Record<string, string>;
}

export interface ContactInfo {
  address?: string;
  phone?: string;
  website?: string;
  email?: string;
}

export interface Activity {
  id: string;
  name: string;
  category: ActivityCategory;
  latitude: number;
  longitude: number;
  distanceMeters: number;
  openingHours?: string;
  openNow?: boolean;
  description: string;
  accessibility: AccessibilityDetails;
  contact: ContactInfo;
  score: number;
  recommended: boolean;
  osmUrl: string;
  isFavorite?: boolean;
}

export interface GeocodeResult {
  displayName: string;
  latitude: number;
  longitude: number;
  type: string;
  boundingBox?: number[];
}

export interface SearchQuery {
  latitude: number;
  longitude: number;
  radius?: number;
  profile?: AccessibilityProfile;
  category?: ActivityCategory;
  limit?: number;
}
