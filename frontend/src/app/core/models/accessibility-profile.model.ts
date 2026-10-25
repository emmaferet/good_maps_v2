export type AccessibilityProfile = 'ALL' | 'PMR' | 'VISUAL' | 'AUDITORY' | 'COGNITIVE';

export interface AccessibilityProfileOption {
  id: AccessibilityProfile;
  label: string;
  sublabel: string;
  icon: string;
}

export const ACCESSIBILITY_PROFILE_OPTIONS: AccessibilityProfileOption[] = [
  {
    id: 'PMR',
    label: 'Mobilité Réduite (PMR)',
    sublabel: 'Fauteuil roulant, rampe d’accès, ascenseur',
    icon: '♿'
  },
  {
    id: 'VISUAL',
    label: 'Déficience Visuelle',
    sublabel: 'Bandes podotactiles, guidage audio, braille',
    icon: '👁️'
  },
  {
    id: 'AUDITORY',
    label: 'Déficience Auditive',
    sublabel: 'Boucle magnétique (T-coil), sous-titrage',
    icon: '🦻'
  },
  {
    id: 'COGNITIVE',
    label: 'Déficience Cognitive',
    sublabel: 'Espace calme, signalétique simplifiée',
    icon: '🧠'
  },
  {
    id: 'ALL',
    label: 'Tous Profils Confondus',
    sublabel: 'Afficher tous les lieux avec informations d’accessibilité',
    icon: '🌐'
  }
];
