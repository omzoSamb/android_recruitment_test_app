# Documentation du Projet

## Vue d'ensemble

Cette documentation détaille tous les aspects de l'implémentation du test technique Android,
organisée en plusieurs fichiers thématiques pour faciliter la navigation et la compréhension.

## Structure de la Documentation

### 1. Architecture MVVM Multi-Modulaire

**Fichier :** `01_ARCHITECTURE_MVVM_MULTI_MODULAIRE.md`

Ce document explique en détail l'architecture choisie pour le projet :

- Comparaison entre l'état initial et l'état final
- Description détaillée de chaque module (app, domain, data)
- Flux de données dans l'application
- Avantages de cette architecture

### 2. Choix de Librairies

**Fichier :** `02_CHOIX_LIBRAIRIES.md`

Ce document justifie chaque librairie utilisée dans le projet :

- Hilt pour l'injection de dépendances
- Room pour la persistance locale
- Retrofit et OkHttp pour le réseau
- Kotlinx Serialization pour la sérialisation
- Coil pour le chargement d'images
- Jetpack Compose pour l'UI
- Spark Design System
- Kotlin Coroutines et Flow
- Mockito pour les tests

### 3. Patterns Appliqués

**Fichier :** `03_PATTERNS_APPLIQUES.md`

Ce document détaille tous les patterns de conception utilisés :

- Pattern MVVM (Model-View-ViewModel)
- Pattern Repository
- Pattern Use Case
- Pattern Dependency Injection (Hilt)
- Pattern Observer (StateFlow)
- Pattern Sealed Class pour les états UI

### 4. Implémentation de l'Interface Utilisateur

**Fichier :** `04_IMPLEMENTATION_UI.md`

Ce document explique l'implémentation de l'UI :

- Pourquoi Jetpack Compose a été choisi
- Structure de l'écran principal (AlbumsScreen)
- Structure de l'écran de détail (DetailScreen)
- Composants UI réutilisables
- Thème personnalisé (MusicTheme)
- Configuration du thème XML pour support DayNight
- Détection automatique du thème système
- Choix de design et expérience utilisateur

### 5. Méthodes et Use Cases

**Fichier :** `05_METHODES_ET_USE_CASES.md`

Ce document détaille toutes les méthodes implémentées :

- Use Cases du domain layer
- Méthodes du Repository
- Méthodes des ViewModels
- Fonctionnement de chaque méthode
- Avantages de chaque implémentation

### 6. Bugs Corrigés et Anomalies Détectées

**Fichier :** `06_BUGS_CORRIGES.md`

Ce document liste tous les bugs trouvés et corrigés :

- Bug critique : double Launcher dans AndroidManifest
- Anti-pattern : utilisation de GlobalScope
- Absence de gestion d'erreurs
- Couplage fort entre les couches
- Absence de persistance offline
- Absence de fonctionnalité favoris
- Injection de dépendances manuelle
- Bug : thème XML incompatible avec Spark (correction du thème XML pour support DayNight)

### 7. Tests Unitaires et d'Intégration

**Fichier :** `07_TESTS_UNITAIRES_INTEGRATION.md`

Ce document détaille la stratégie de tests :

- Tests unitaires des ViewModels
- Tests des Use Cases
- Tests d'intégration du Repository
- Outils de test utilisés (Mockito, Kotlinx Coroutines Test)
- Couverture de tests

### 8. Utils et Composants Réutilisables

**Fichier :** `08_UTILS_ET_COMPOSANTS.md`

Ce document détaille les utilitaires et composants :

- UiState (sealed class pour les états)
- FlowExtensions (extensions pour Flow)
- NetworkUtils (vérification de connexion)
- AnalyticsHelper (préparation analytics)
- Composants UI réutilisables (LoadingIndicator, ErrorMessage, FavoriteButton, AlbumGridCard)

## Comment Utiliser cette Documentation

### Pour Comprendre l'Architecture

Commencez par lire `01_ARCHITECTURE_MVVM_MULTI_MODULAIRE.md` pour comprendre la structure globale du
projet, puis `03_PATTERNS_APPLIQUES.md` pour comprendre les patterns utilisés.

### Pour Comprendre les Choix Techniques

Lisez `02_CHOIX_LIBRAIRIES.md` pour comprendre pourquoi chaque librairie a été choisie, et
`05_METHODES_ET_USE_CASES.md` pour comprendre comment les fonctionnalités sont implémentées.

### Pour Comprendre l'UI

Lisez `04_IMPLEMENTATION_UI.md` pour comprendre l'implémentation de l'interface utilisateur et
`08_UTILS_ET_COMPOSANTS.md` pour les composants réutilisables.

### Pour Comprendre les Tests

Lisez `07_TESTS_UNITAIRES_INTEGRATION.md` pour comprendre la stratégie de tests et comment les tests
sont organisés.

### Pour Comprendre les Corrections

Lisez `06_BUGS_CORRIGES.md` pour comprendre quels bugs ont été détectés et comment ils ont été
corrigés.

## Conformité avec le README.md

Tous les documents de cette documentation répondent aux exigences du README.md :

- ✅ **Architecture** : Documentée dans `01_ARCHITECTURE_MVVM_MULTI_MODULAIRE.md`
- ✅ **Choix de librairies** : Justifiés dans `02_CHOIX_LIBRAIRIES.md`
- ✅ **Patterns appliqués** : Détailés dans `03_PATTERNS_APPLIQUES.md`
- ✅ **Justification des choix** : Présente dans tous les documents
- ✅ **Tests** : Documentés dans `07_TESTS_UNITAIRES_INTEGRATION.md`
- ✅ **Bugs corrigés** : Listés dans `06_BUGS_CORRIGES.md`

## Navigation Rapide

- **Architecture** → `01_ARCHITECTURE_MVVM_MULTI_MODULAIRE.md`
- **Librairies** → `02_CHOIX_LIBRAIRIES.md`
- **Patterns** → `03_PATTERNS_APPLIQUES.md`
- **UI** → `04_IMPLEMENTATION_UI.md`
- **Méthodes** → `05_METHODES_ET_USE_CASES.md`
- **Bugs** → `06_BUGS_CORRIGES.md`
- **Tests** → `07_TESTS_UNITAIRES_INTEGRATION.md`
- **Utils** → `08_UTILS_ET_COMPOSANTS.md`

## Conclusion

Cette documentation fournit une vue complète et détaillée de l'implémentation du projet. Chaque
document peut être lu indépendamment, mais ils sont conçus pour se compléter et offrir une
compréhension complète de l'architecture, des choix techniques, et de l'implémentation.
