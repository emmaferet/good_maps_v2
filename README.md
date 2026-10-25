# 🗺️ Good Maps — Plateforme d'Activités Accessibles (OpenStreetMap)

[![CI/CD Pipeline](https://img.shields.io/badge/CI%2FCD-GitHub%20Actions-blue?logo=github-actions)](https://github.com)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.x-brightgreen?logo=springboot)](https://spring.io/projects/spring-boot)
[![Angular](https://img.shields.io/badge/Angular-19-red?logo=angular)](https://angular.dev)
[![Java](https://img.shields.io/badge/Java-17%20%7C%2021-orange?logo=openjdk)](https://openjdk.org)
[![OpenStreetMap](https://img.shields.io/badge/Data-OpenStreetMap%20%2F%20Overpass-green?logo=openstreetmap)](https://www.openstreetmap.org)
[![A11y](https://img.shields.io/badge/Accessibility-WCAG%20AA%20Compliant-purple)](https://www.w3.org/WAI/)

> **Good Maps** est une plateforme SaaS cartographique éco-conçue dédiée au recensement, à la qualification et à la recommandation d'activités accessibles pour les personnes à mobilité réduite (PMR) ainsi que les personnes en situation de handicap sensoriel (visuel, auditif) et cognitif.

---

## 🏛️ Architecture du Projet (Monorepo)

```text
good_maps_v2/
├── backend/                  # API REST Spring Boot 3 (Java 17/21)
│   ├── src/                  # Code source (Clean Architecture)
│   │   ├── main/             # Controllers, Services, Clients OSM, DTOs, Cache
│   │   └── test/             # Tests unitaires & intégration (JUnit 5, Mockito)
│   ├── pom.xml               # Configuration Maven et dépendances
│   └── Dockerfile            # Image multi-stage backend
├── frontend/                 # Application Web Angular (Dernière version stable)
│   ├── src/                  # Composants Standalone, SCSS Design System, Leaflet
│   ├── package.json          # Dépendances Node & scripts
│   └── Dockerfile            # Image multi-stage frontend (Nginx Alpine)
├── .github/workflows/        # Pipelines CI/CD automatisés
├── docker-compose.yml        # Déploiement conteneurisé en 1 commande
├── .gitignore                # Exclusion unifiée (Java, Node, IDEs, OS)
└── .env.example              # Variables d'environnement documentées
```

---

## 🎨 Parcours Utilisateur & Écrans Clés

1. **Écran 1 (Splash & Chargement)** : Écran d'accueil épuré présentant le logo officiel Good Maps et sa baseline *"Suggestions d'activités adaptées"*.
2. **Écran 2 (Formulaire Bienvenue & Filtres)** : Modale intuitive permettant de configurer son profil d'accessibilité (PMR, visuel, auditif, cognitif) et ses préférences avant d'accéder à la carte.
3. **Écran 3 (Carte Interactive & Fiche Descriptive)** : Carte Leaflet fluide avec épingles thématiques, bouton d'action *"Obtenir des suggestions"* et fiche contextuelle complète avec actions rapides (*Réserver en ligne*, *Appeler*).

---

## 🚀 Démarrage Rapide

### Prérequis
- **Git**
- **Docker & Docker Compose** (recommandé pour une exécution immédiate)
- Ou pour le développement local :
  - **Java 17+** et Maven
  - **Node.js 20+** et npm

### Option 1 : Lancement en une commande avec Docker Compose
```bash
# Copier le template d'environnement
cp .env.example .env

# Démarrer la stack complète (Frontend + Backend)
docker compose up --build
```
L'application est accessible sur :
- **Frontend** : [http://localhost:4200](http://localhost:4200) (ou port 80 en production)
- **Backend API REST** : [http://localhost:8080](http://localhost:8080)
- **Healthcheck** : [http://localhost:8080/actuator/health](http://localhost:8080/actuator/health)

---

## 🛡️ Sécurité & Bonnes Pratiques
- **Contrôle strict des quotas OSM** : Service proxy avec cache Caffeine en mémoire pour respecter la Fair Use Policy des serveurs OpenStreetMap.
- **Assainissement des données** : Validation des coordonnées GPS et des rayons de recherche avec Spring Validation.
- **Sécurisation des liens sortants** : Validation stricte des URLs `https://` et attributs obligatoires `rel="noopener noreferrer"` pour éliminer les risques de *reverse tabnabbing*.
- **CORS restreint** : Filtrage paramétrable des origines autorisées.
