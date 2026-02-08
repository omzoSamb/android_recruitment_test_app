# Méthodes Implémentées et Use Cases

## Introduction

Ce document détaille toutes les méthodes implémentées dans le projet, les Use Cases qui encapsulent
la logique métier, et explique comment chaque méthode contribue aux fonctionnalités de
l'application.
Les Use Cases suivent le pattern Clean Architecture et respectent le principe de responsabilité
unique

## Use Cases du Domain Layer

### GetAllAlbumsUseCase

**Description :**

Le Use Case `GetAllAlbumsUseCase` encapsule l'action métier de récupérer tous les albums disponibles
Il délègue cette responsabilité au Repository et retourne un Flow pour permettre une réactivité
automatique

**Implémentation :**

```kotlin
class GetAllAlbumsUseCase @Inject constructor(
    private val repository: AlbumRepository
) {
    operator fun invoke(): Flow<List<Album>> = repository.getAllAlbums()
}
```

**Fonctionnement :**

Ce Use Case est extrêmement simple car il ne fait que déléguer au Repository.
Cependant, cette simplicité est intentionnelle et suit le principe de responsabilité unique. Le Use
Case représente une action métier claire : "récupérer tous les albums"
La logique de récupération (depuis la base de données locale ou l'API) est gérée par le Repository

**Utilisation :**

Le ViewModel utilise ce Use Case pour charger les albums :

```kotlin
private fun loadAlbums() {
    viewModelScope.launch {
        getAllAlbumsUseCase()
            .catch { e ->
                _uiState.value = UiState.Error(e.message ?: "Error occurred")
            }
            .collect { albums ->
                _uiState.value = UiState.Success(albums)
            }
    }
}
```

Le ViewModel collecte le Flow retourné par le Use Case et met à jour l'état UI en conséquence
Si une erreur se produit, elle est capturée et transformée en état d'erreur

**Avantages :**

- Simplicité : Use Case simple
- Testabilité : facile à tester en mockant le Repository
- Réutilisabilité: peut être utilisé dans différents contextes (différents ViewModels, différents
  écrans)

### RefreshAlbumsUseCase

**Description :**

Le Use Case `RefreshAlbumsUseCase` encapsule l'action métier de rafraîchir les albums depuis l'API
Contrairement à `GetAllAlbumsUseCase`, celui-ci est une opération suspend car il fait un appel
réseau

**Implémentation :**

```kotlin
class RefreshAlbumsUseCase @Inject constructor(
    private val repository: AlbumRepository
) {
    suspend operator fun invoke() = repository.refreshAlbums()
}
```

**Fonctionnement :**

Ce Use Case déclenche un rafraîchissement des données depuis l'API. Le Repository gère la logique
de :

1. Faire l'appel API pour récupérer les nouveaux albums
2. Convertir les DTOs en Entities
3. Sauvegarder les Entities dans la base de données locale
4. Room émet automatiquement les nouvelles données via Flow

**Utilisation :**

Le ViewModel utilise ce Use Case pour rafraîchir les albums :

```kotlin
fun refreshAlbums() {
    viewModelScope.launch {
        _isRefreshing.value = true
        try {
            refreshAlbumsUseCase()
        } catch (e: Exception) {
            _uiState.value = UiState.Error(e.message ?: "Error during refresh")
        } finally {
            _isRefreshing.value = false
        }
    }
}
```

Le ViewModel gère l'état de rafraîchissement (`isRefreshing`) pour afficher un indicateur visuel à
l'utilisateur pendant le rafraîchissement

**Avantages :**

- Séparation des responsabilités : le ViewModel ne connaît pas les détails de l'appel API
- Gestion d'erreurs : les erreurs réseau sont gérées de manière centralisée
- Offline-first : les données sont toujours disponibles depuis la base de données locale, même si le
  rafraîchissement échoue

### GetAlbumByIdUseCase

**Description :**

Le Use Case `GetAlbumByIdUseCase` encapsule l'action métier de récupérer un album spécifique par son
ID
Il est utilisé dans l'écran de détail pour charger les informations complètes d'un album

**Implémentation :**

```kotlin
class GetAlbumByIdUseCase @Inject constructor(
    private val repository: AlbumRepository
) {
    suspend operator fun invoke(albumId: Int): Album? = repository.getAlbumById(albumId)
}
```

**Fonctionnement :**

Ce Use Case récupère un album depuis la base de données locale en utilisant son ID
Il retourne `null` si l'album n'existe pas, ce qui permet au ViewModel de gérer ce cas d'erreur

**Utilisation :**

Le `DetailViewModel` utilise ce Use Case pour charger un album :

```kotlin
fun loadAlbum(albumId: Int) {
    savedStateHandle["albumId"] = albumId
    viewModelScope.launch {
        _uiState.value = UiState.Loading
        try {
            val album = getAlbumByIdUseCase(albumId)
            if (album != null) {
                _uiState.value = UiState.Success(album)
            } else {
                _uiState.value = UiState.Error("Album not found")
            }
        } catch (e: Exception) {
            _uiState.value = UiState.Error(e.message ?: "Error while loading")
        }
    }
}
```

Le ViewModel vérifie si l'album existe et affiche un message d'erreur approprié si ce n'est pas le
cas

**Avantages :**

- Gestion des cas null : le retour nullable permet de gérer explicitement le cas où l'album n'existe
  pas
- Offline-first : l'album est récupéré depuis la base de données locale, garantissant un
  fonctionnement offline

### ToggleFavoriteUseCase

**Description :**

Le Use Case `ToggleFavoriteUseCase` encapsule l'action métier de basculer le statut favori d'un
album
Il retourne le nouveau statut (true si maintenant favori, false sinon)

**Implémentation :**

```kotlin
class ToggleFavoriteUseCase @Inject constructor(
    private val repository: AlbumRepository
) {
    suspend operator fun invoke(albumId: Int): Boolean = repository.toggleFavorite(albumId)
}
```

**Fonctionnement :**

Ce Use Case bascule le statut favori d'un album dans la base de données locale. Le Repository :

1. Récupère le statut actuel depuis la base de données
2. Inverse le statut
3. Met à jour la base de données
4. Retourne le nouveau statut

Room émet automatiquement les données mises à jour via Flow, ce qui met à jour l'UI automatiquement

**Utilisation :**

Le ViewModel utilise ce Use Case pour basculer le statut favori :

```kotlin
fun toggleFavorite(albumId: Int) {
    viewModelScope.launch {
        try {
            toggleFavoriteUseCase(albumId)
        } catch (e: Exception) {
            // Gestion d'erreur (peut afficher un snackbar)
        }
    }
}
```

Dans `DetailViewModel`, le Use Case est utilisé pour mettre à jour l'état local :

```kotlin
fun toggleFavorite() {
    viewModelScope.launch {
        val currentState = _uiState.value
        if (currentState is UiState.Success) {
            try {
                val newFavoriteStatus = toggleFavoriteUseCase(currentState.data.id)
                _uiState.value = UiState.Success(
                    currentState.data.copy(isFavorite = newFavoriteStatus)
                )
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Error during update")
            }
        }
    }
}
```

**Avantages :**

- Persistance locale : le statut favori est persistant et survit aux redémarrages de l'application
- Réactivité : les changements sont automatiquement propagés via Flow
- Retour de valeur : le retour du nouveau statut permet de mettre à jour l'UI immédiatement

### GetFavoriteAlbumsUseCase

**Description :**

Le Use Case `GetFavoriteAlbumsUseCase` encapsule l'action métier de récupérer tous les albums
marqués comme favoris
Il retourne un Flow pour permettre une réactivité automatique

**Implémentation :**

```kotlin
class GetFavoriteAlbumsUseCase @Inject constructor(
    private val repository: AlbumRepository
) {
    operator fun invoke(): Flow<List<Album>> = repository.getFavoriteAlbums()
}
```

**Fonctionnement :**

Ce Use Case récupère tous les albums favoris depuis la base de données locale. Comme
`GetAllAlbumsUseCase`, il retourne un Flow pour la réactivité

**Utilisation :**

Le `AlbumsViewModel` utilise ce Use Case en combinaison avec `GetAllAlbumsUseCase` pour filtrer les
albums :

```kotlin
private fun loadAlbums() {
    viewModelScope.launch {
        combine(
            getAllAlbumsUseCase(),
            getFavoriteAlbumsUseCase(),
            _showFavoritesOnly
        ) { allAlbums, favoriteAlbums, showFavorites ->
            if (showFavorites) favoriteAlbums else allAlbums
        }
            .catch { e ->
                _uiState.value = UiState.Error(e.message ?: "Error occurred")
            }
            .collect { albums ->
                _uiState.value = UiState.Success(albums)
            }
    }
}
```

Le ViewModel combine les deux Flow avec l'état `showFavoritesOnly` pour déterminer quels albums
afficher
Si `showFavoritesOnly` est true, il affiche uniquement les favoris, sinon il affiche tous les albums

**Avantages :**

- Filtrage réactif : le filtrage est réactif et se met à jour automatiquement quand les favoris
  changent
- Performance : les deux Flow sont combinés efficacement
- Simplicité : la logique de filtrage est centralisée dans le ViewModel

## Méthodes du Repository

### getAllAlbums()

**Description :**

Cette méthode retourne un Flow de tous les albums disponibles. Elle lit depuis la base de données
locale, garantissant un fonctionnement offline

**Implémentation :**

```kotlin
override fun getAllAlbums(): Flow<List<Album>> =
    albumDao.getAllAlbums().map { entities ->
        entities.map { it.toDomain() }
    }
```

**Fonctionnement :**

La méthode :

1. Récupère un Flow d'Entities depuis le DAO
2. Transforme chaque Entity en modèle de domaine via `toDomain()`
3. Retourne un Flow de modèles de domaine

**Avantages :**

- Offline-first : les données sont toujours disponibles depuis la base de données locale
- Réactivité : Room émet automatiquement les nouvelles valeurs quand les données changent
- Transformation : les Entities sont transformées en modèles de domaine, respectant la séparation
  des couches

### refreshAlbums()

**Description :**

Cette méthode rafraîchit les albums depuis l'API et les sauvegarde dans la base de données locale

**Implémentation :**

```kotlin
override suspend fun refreshAlbums() {
    try {
        val albumsFromApi = albumApiService.getAlbums()
        val albumEntities = albumsFromApi.map { it.toEntity() }
        albumDao.insertAll(albumEntities)
    } catch (e: Exception) {
        throw e
    }
}
```

**Fonctionnement :**

La méthode :

1. Fait un appel API pour récupérer les albums
2. Transforme les DTOs en Entities via `toEntity()`
3. Sauvegarde les Entities dans la base de données avec `insertAll()`
4. Room émet automatiquement les nouvelles données via Flow

**Gestion d'erreurs :**

Les erreurs réseau sont propagées au ViewModel qui les transforme en état d'erreur UI. Si l'appel
API échoue, les données locales restent disponibles.

**Avantages :**

- Synchronisation : les données sont synchronisées avec l'API
- Persistance : les nouvelles données sont sauvegardées localement
- Réactivité : les changements sont automatiquement propagés via Flow

### toggleFavorite()

**Description :**

Cette méthode bascule le statut favori d'un album dans la base de données locale

**Implémentation :**

```kotlin
override suspend fun toggleFavorite(albumId: Int): Boolean {
    val currentStatus = albumDao.isFavorite(albumId)
    val newStatus = !currentStatus
    albumDao.updateFavoriteStatus(albumId, newStatus)
    return newStatus
}
```

**Fonctionnement :**

La méthode :

1. Récupère le statut actuel depuis la base de données
2. Inverse le statut
3. Met à jour la base de données
4. Retourne le nouveau statut

**Avantages :**

- Atomicité : l'opération est atomique (lecture puis écriture)
- Persistance : le changement est immédiatement persistant
- Réactivité : Room émet automatiquement les données mises à jour

### getAlbumById()

**Description :**

Cette méthode récupère un album spécifique par son ID depuis la base de données locale

**Implémentation :**

```kotlin
override suspend fun getAlbumById(id: Int): Album? {
    return albumDao.getAlbumById(id)?.toDomain()
}
```

**Fonctionnement :**

La méthode :

1. Récupère l'Entity depuis le DAO
2. Transforme l'Entity en modèle de domaine si elle existe
3. Retourne null si l'album n'existe pas

**Avantages :**

- Offline-first : l'album est récupéré depuis la base de données locale
- Gestion des cas null : le retour nullable permet de gérer explicitement le cas où l'album n'existe
  pas

### getFavoriteAlbums()

**Description :**

Cette méthode retourne un Flow de tous les albums marqués comme favoris

**Implémentation :**

```kotlin
override fun getFavoriteAlbums(): Flow<List<Album>> {
    return albumDao.getFavoriteAlbums().map { entities ->
        entities.map { it.toDomain() }
    }
}
```

**Fonctionnement :**

Similaire à `getAllAlbums()`, mais filtre uniquement les albums avec `isFavorite = true`

**Avantages :**

- Réactivité : les changements de favoris sont automatiquement propagés
- Performance : le filtrage est fait au niveau de la base de données (efficace)

## Méthodes des ViewModels

### AlbumsViewModel.loadAlbums()

**Description :**

Cette méthode privée charge les albums au démarrage du ViewModel. Elle combine les Flow de tous les
albums et des favoris pour gérer le filtrage

**Implémentation :**

```kotlin
private fun loadAlbums() {
    viewModelScope.launch {
        combine(
            getAllAlbumsUseCase(),
            getFavoriteAlbumsUseCase(),
            _showFavoritesOnly
        ) { allAlbums, favoriteAlbums, showFavorites ->
            if (showFavorites) favoriteAlbums else allAlbums
        }
            .catch { e ->
                _uiState.value = UiState.Error(e.message ?: "Error occurred")
            }
            .collect { albums ->
                _uiState.value = UiState.Success(albums)
            }
    }
    
    // Load data from the API in the background
    viewModelScope.launch {
        try {
            refreshAlbumsUseCase()
        } catch (e: Exception) {
            // The error will be handled by the above Flow
        }
    }
}
```

**Fonctionnement :**

La méthode :

1. Combine les Flow de tous les albums, des favoris, et l'état `showFavoritesOnly`
2. Filtre les albums selon `showFavoritesOnly`
3. Met à jour l'état UI avec les albums filtrés
4. Lance un rafraîchissement depuis l'API en arrière-plan

**Avantages :**

- Réactivité : le filtrage est réactif et se met à jour automatiquement
- Offline-first : les données sont d'abord chargées depuis la base de données locale
- Synchronisation : les données sont rafraîchies depuis l'API en arrière-plan

### AlbumsViewModel.refreshAlbums()

**Description :**

Cette méthode publique permet de rafraîchir manuellement les albums depuis l'API

**Implémentation :**

```kotlin
fun refreshAlbums() {
    viewModelScope.launch {
        _isRefreshing.value = true
        try {
            refreshAlbumsUseCase()
        } catch (e: Exception) {
            _uiState.value = UiState.Error(e.message ?: "Error during refresh")
        } finally {
            _isRefreshing.value = false
        }
    }
}
```

**Fonctionnement :**

La méthode :

1. Met à jour l'état `isRefreshing` à true
2. Appelle le Use Case de rafraîchissement
3. Gère les erreurs et met à jour l'état UI
4. Met à jour l'état `isRefreshing` à false dans le bloc finally

**Avantages :**

- Feedback utilisateur : l'état `isRefreshing` permet d'afficher un indicateur visuel
- Gestion d'erreurs : les erreurs sont gérées et affichées à l'utilisateur
- Garantie : le bloc finally garantit que `isRefreshing` est toujours remis à false

### AlbumsViewModel.toggleFavorite()

**Description :**

Cette méthode bascule le statut favori d'un album

**Implémentation :**

```kotlin
fun toggleFavorite(albumId: Int) {
    viewModelScope.launch {
        try {
            toggleFavoriteUseCase(albumId)
        } catch (e: Exception) {
            // Error handling - could show a snackbar
        }
    }
}
```

**Fonctionnement :**

La méthode délègue simplement au Use Case. Les changements sont automatiquement propagés via Flow,
donc l'UI se met à jour automatiquement

**Avantages :**

- Simplicité : la méthode est simple car la réactivité est gérée par Flow
- Pas de mise à jour manuelle : pas besoin de mettre à jour manuellement l'état UI

### AlbumsViewModel.toggleShowFavoritesOnly()

**Description :**

Cette méthode bascule l'état de filtrage des favoris

**Implémentation :**

```kotlin
fun toggleShowFavoritesOnly() {
    _showFavoritesOnly.value = !_showFavoritesOnly.value
}
```

**Fonctionnement :**

La méthode inverse simplement l'état `showFavoritesOnly`.
Comme ce StateFlow est utilisé dans `combine()`, le changement déclenche automatiquement une
nouvelle émission avec les albums filtrés

**Avantages :**

- Réactivité : le filtrage est automatique grâce à `combine()`
- Simplicité : pas besoin de logique complexe

### DetailViewModel.loadAlbum()

**Description :**

Cette méthode charge un album spécifique par son ID

**Implémentation :**

```kotlin
fun loadAlbum(albumId: Int) {
    savedStateHandle["albumId"] = albumId
    viewModelScope.launch {
        _uiState.value = UiState.Loading
        try {
            val album = getAlbumByIdUseCase(albumId)
            if (album != null) {
                _uiState.value = UiState.Success(album)
            } else {
                _uiState.value = UiState.Error("Album not found")
            }
        } catch (e: Exception) {
            _uiState.value = UiState.Error(e.message ?: "Error while loading")
        }
    }
}
```

**Fonctionnement :**

La méthode :

1. Sauvegarde l'ID dans `savedStateHandle` pour la survie aux changements de configuration
2. Met à jour l'état à Loading
3. Appelle le Use Case pour récupérer l'album
4. Gère le cas où l'album n'existe pas
5. Met à jour l'état UI en conséquence

**Avantages :**

- Gestion des changements de configuration : `savedStateHandle` préserve l'ID lors des rotations
- Gestion des cas null : le cas où l'album n'existe pas est géré explicitement
