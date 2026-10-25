export type ActivityCategory = 'ALL' | 'CULTURE' | 'FOOD' | 'LEISURE' | 'ACCOMMODATION' | 'SHOPPING';

export interface CategoryOption {
  id: ActivityCategory;
  label: string;
  icon: string;
}

export const CATEGORY_OPTIONS: CategoryOption[] = [
  { id: 'ALL', label: 'Toutes activités', icon: '✨' },
  { id: 'CULTURE', label: 'Culture & Musées', icon: '🏛️' },
  { id: 'FOOD', label: 'Restaurants & Cafés', icon: '🍽️' },
  { id: 'LEISURE', label: 'Loisirs & Espaces verts', icon: '🌳' },
  { id: 'ACCOMMODATION', label: 'Hébergements adaptés', icon: '🏨' },
  { id: 'SHOPPING', label: 'Commerces accessibles', icon: '🛍️' }
];
