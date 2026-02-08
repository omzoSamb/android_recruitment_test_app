# Architecture MVVM Multi-Modulaire

## Vue d'ensemble

L'application a été entièrement refactorisée pour adopter une architecture **MVVM (
Model-View-ViewModel)** combinée aux principes de **Clean Architecture**, organisée en trois modules
distincts : `app`, `domain`, et `data`. Cette architecture garantit une séparation claire des
responsabilités, une meilleure testabilité, et une maintenabilité du code.

## État Initial vs État Final

### État Initial du Projet

Le projet initial présentait une architecture simple avec seulement deux modules (`app` et `data`)
et plusieurs problèmes architecturaux :

**Structure initiale :**

- Module `app` : Contenait toute la logique de présentation, les ViewModels, et l'injection de
  dépendances manuelle
- Module `data` : Contenait les repositories, les appels réseau, et la base de données
- Pas de module `domain` : La logique métier était mélangée avec la présentation et les données
- Injection de dépendances manuelle : Utilisation d'un pattern Factory et de
  `AppDependenciesProvider`
- Utilisation de `GlobalScope` : Anti-pattern qui peut causer des fuites mémoire
- Pas de gestion d'erreurs : Les exceptions étaient silencieusement ignorées
- Pas de persistance offline : Les données n'étaient pas sauvegardées localement

**Problèmes identifiés :**

1. Couplage fort : les ViewModels dépendaient directement des DTOs du module data
2. Pas de séparation des responsabilités : la logique métier était dans les ViewModels
3. Difficile à tester : pas de couche d'abstraction pour mocker les dépendances
4. Pas de persistance : les données étaient perdues à chaque redémarrage
5. Gestion d'erreurs absente : aucune gestion des cas d'erreur réseau ou autres

### État Final du Projet

L'architecture finale respecte les principes SOLID et les bonnes pratiques Android modernes :

```
┌─────────────────────────────────────────────────────────┐
│              PRESENTATION LAYER (app)                   │
│  ┌───────────────────────────────────────────────────┐   │
│  │  Activities (MainActivity, DetailsActivity)      │   │
│  │  Composables (AlbumsScreen, DetailScreen)        │   │
│  │  ViewModels (AlbumsViewModel, DetailViewModel)    │   │
│  │  UI Components (LoadingIndicator, ErrorMessage)  │   │
│  │  Theme (MusicTheme)                               │   │
│  └───────────────────────────────────────────────────┘   │
└──────────────────────┬──────────────────────────────────┘
                       │
                       │ Utilise les Use Cases
                       │
┌──────────────────────▼──────────────────────────────────┐
│              DOMAIN LAYER (domain)                      │
│  ┌───────────────────────────────────────────────────┐   │
│  │  Models (Album)                                   │   │
│  │  Repository Interfaces (AlbumRepository)          │   │
│  │  Use Cases                                        │   │
│  │    - GetAllAlbumsUseCase                         │   │
│  │    - RefreshAlbumsUseCase                        │   │
│  │    - GetAlbumByIdUseCase                         │   │
│  │    - ToggleFavoriteUseCase                         │   │
│  │    - GetFavoriteAlbumsUseCase                      │   │
│  └───────────────────────────────────────────────────┘   │
└──────────────────────┬──────────────────────────────────┘
                       │
                       │ Implémente les interfaces
                       │
┌──────────────────────▼──────────────────────────────────┐
│              DATA LAYER (data)                          │
│  ┌───────────────────────────────────────────────────┐   │
│  │  Repository Implementation (AlbumRepository)     │   │
│  │  Data Sources                                     │   │
│  │    - API (AlbumApiService via Retrofit)          │   │
│  │    - Database (AlbumDao via Room)                │   │
│  │  DTOs / Entities                                  │   │
│  │    - AlbumDto (réseau)                           │   │
│  │    - AlbumEntity (base de données)               │   │
│  │  Mappers (toDomain, toEntity)                    │   │
│  └───────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────┘
```

## Détail des Modules

### Module `app` (Presentation Layer)

Le module `app` est responsable de tout ce qui concerne l'interface utilisateur et la présentation
des données à l'utilisateur.

**Structure du module :**

```
app/
├── src/main/java/fr/leboncoin/androidrecruitmenttestapp/
│   ├── MainActivity.kt                    # Point d'entrée de l'application
│   ├── DetailsActivity.kt                 # Écran de détail
│   ├── PhotoApp.kt                        # Application class avec @HiltAndroidApp
│   ├── di/
│   │   └── AppModule.kt                   # Module Hilt pour les dépendances app
│   ├── ui/
│   │   ├── albumsScreen/
│   │   │   ├── AlbumsScreen.kt            # Composable principal de la liste
│   │   │   └── AlbumsViewModel.kt          # ViewModel pour la liste
│   │   ├── detailScreen/
│   │   │   ├── DetailScreen.kt            # Composable de détail
│   │   │   └── DetailViewModel.kt        # ViewModel pour le détail
│   │   └── favorite/
│   │       └── FavoritesViewModel.kt      # ViewModel pour les favoris
│   ├── coreui/
│   │   ├── components/                    # Composants UI réutilisables
│   │   │   ├── LoadingIndicator.kt
│   │   │   ├── ErrorMessage.kt
│   │   │   └── AlbumGridCard.kt
│   │   └── theme/
│   │       └── MusicTheme.kt              # Thème personnalisé
│   └── utils/
│       ├── UiState.kt                     # Sealed class pour les états UI
│       ├── FlowExtensions.kt              # Extensions pour Flow
│       ├── NetworkUtils.kt                # Utilitaires réseau
│       └── AnalyticsHelper.kt             # Helper pour analytics
```

**Responsabilités :**

- Affichage de l'interface utilisateur avec Jetpack Compose
- Gestion de la navigation entre les écrans
- Observation des StateFlow depuis les ViewModels
- Gestion des interactions utilisateur (clics, pull-to-refresh, etc.)
- Application du thème et du design system Spark

**Dépendances :**

- Dépend du module `domain` pour accéder aux Use Cases et aux modèles
- N'a pas de dépendance directe avec le module `data` (respect de Clean Architecture)

### Module `domain` (Business Logic Layer)

Le module `domain` contient la logique métier pure, indépendante de toute infrastructure Android ou
externe.

**Structure du module :**

```
domain/
├── src/main/java/fr/leboncoin/domain/
│   ├── model/
│   │   └── Album.kt                       # Modèle de domaine
│   ├── repository/
│   │   └── AlbumRepository.kt             # Interface du repository
│   └── usecase/
│       ├── GetAllAlbumsUseCase.kt         # Récupérer tous les albums
│       ├── RefreshAlbumsUseCase.kt        # Rafraîchir depuis l'API
│       ├── GetAlbumByIdUseCase.kt         # Récupérer un album par ID
│       ├── ToggleFavoriteUseCase.kt       # Basculer le statut favori
│       └── GetFavoriteAlbumsUseCase.kt    # Récupérer les favoris
```

**Responsabilités :**

- Définition des modèles de domaine (entités métier)
- Définition des interfaces de repository (contrats)
- Implémentation des Use Cases (actions métier)
- Logique métier pure, sans dépendances Android

**Avantages :**

- Indépendance : peut être testé sans Android (tests JUnit simples)
- Réutilisabilité : la logique métier peut être réutilisée dans d'autres contextes
- Testabilité : facile à tester car pas de dépendances externes
- Maintenabilité : les changements dans les couches data ou app n'affectent pas le domain

**Exemple de Use Case :**

```kotlin
class GetAllAlbumsUseCase @Inject constructor(
    private val repository: AlbumRepository
) {
    operator fun invoke(): Flow<List<Album>> = repository.getAllAlbums()
}
```

Ce Use Case encapsule une action métier simple : récupérer tous les albums. Il délègue au repository
et retourne un Flow pour la réactivité

### Module `data` (Data Layer)

Le module `data` est responsable de l'accès aux données, qu'elles proviennent d'une API distante ou
d'une base de données locale

**Structure du module :**

```
data/
├── src/main/java/fr/leboncoin/data/
│   ├── di/
│   │   └── DataModule.kt                  # Module Hilt pour les dépendances data
│   ├── database/
│   │   ├── AppDatabase.kt                 # Base de données Room
│   │   ├── AlbumDao.kt                    # DAO(Data Access Object) pour les opérations DB
│   │   └── AlbumEntity.kt                  # Entity Room
│   ├── network/
│   │   ├── api/
│   │   │   └── AlbumApiService.kt        # Interface Retrofit
│   │   └── model/
│   │       └── AlbumDto.kt                # DTO(Data Transfer Object)  réseau
│   └── repository/
│       └── AlbumRepository.kt             # Implémentation du repository
```

**Responsabilités :**

- Implémentation des interfaces définies dans le domain
- Gestion des appels réseau via Retrofit
- Gestion de la persistance locale via Room
- Mapping entre les DTOs/Entities et les modèles de domaine
- Gestion des migrations de base de données

**Implémentation du Repository :**
Le repository dans le module data implémente l'interface définie dans le domain. Il combine les
données de l'API et de la base de données locale pour offrir une expérience offline-first.

```kotlin
class AlbumRepository @Inject constructor(
    private val albumApiService: AlbumApiService,
    private val albumDao: AlbumDao
) : IAlbumRepository {

    override fun getAllAlbums(): Flow<List<Album>> =
        albumDao.getAllAlbums().map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun refreshAlbums() {
        val albumsFromApi = albumApiService.getAlbums()
        val albumEntities = albumsFromApi.map { it.toEntity() }
        albumDao.insertAll(albumEntities)
    }
}
```

Cette implémentation garantit que :

- Les données sont toujours disponibles depuis la base de données locale (offline-first)
- Les données peuvent être rafraîchies depuis l'API
- Les changements dans la base de données sont automatiquement propagés via Flow

## Flux de Données

### Chargement Initial des Albums

1. UI (AlbumsScreen) : le composable observe le `uiState` du ViewModel
2. ViewModel (AlbumsViewModel) : appelle `GetAllAlbumsUseCase()` dans son `init`
3. Use Case (GetAllAlbumsUseCase) : appelle `repository.getAllAlbums()`
4. Repository (AlbumRepository) : retourne un Flow depuis `albumDao.getAllAlbums()`
5. DAO (AlbumDao) : retourne un Flow de `AlbumEntity` depuis Room
6. Mapper : `AlbumEntity.toDomain()` convertit l'entity en modèle de domaine
7. Flow : les données remontent jusqu'à l'UI via le ViewModel

En parallèle, le ViewModel lance un rafraîchissement depuis l'API :

1. ViewModel : appelle `RefreshAlbumsUseCase()` en arrière-plan
2. Use Case : appelle `repository.refreshAlbums()`
3. Repository : fait un appel API via `albumApiService.getAlbums()`
4. Repository : sauvegarde les données dans Room via `albumDao.insertAll()`
5. Room : émet automatiquement les nouvelles données via Flow
6. UI : se met à jour automatiquement grâce à la réactivité de Flow

### Toggle Favorite

1. UI : l'utilisateur clique sur le bouton favoris
2. ViewModel : appelle `toggleFavoriteUseCase(albumId)`
3. Use Case : appelle `repository.toggleFavorite(albumId)`
4. Repository : met à jour la base de données via `albumDao.updateFavoriteStatus()`
5. Room : émet automatiquement les données mises à jour via Flow
6. UI : se met à jour automatiquement

## Avantages de cette Architecture

### Séparation des Responsabilités

Chaque module a une responsabilité claire et bien définie. Le module `app` ne connaît pas les
détails d'implémentation du module `data`, et le module `domain` est complètement indépendant.

### Testabilité

Chaque couche peut être testée indépendamment :

- Les ViewModels peuvent être testés en mockant les Use Cases
- Les Use Cases peuvent être testés en mockant le Repository
- Le Repository peut être testé en mockant l'API et le DAO

### Maintenabilité

Les changements dans une couche n'affectent pas les autres couches. Par exemple, si on change l'API
ou la structure de la base de données, seul le module `data` est affecté.

### Évolutivité

Il est facile d'ajouter de nouvelles fonctionnalités :

- Nouveau Use Case ? Ajoutez-le dans le module `domain`
- Nouvelle source de données ? Implémentez-la dans le module `data`
- Nouvel écran ? Créez-le dans le module `app`

### Offline-First

L'architecture garantit que les données sont toujours disponibles depuis la base de données locale,
même sans connexion réseau. Les données sont rafraîchies en arrière-plan quand la connexion est
disponible.

