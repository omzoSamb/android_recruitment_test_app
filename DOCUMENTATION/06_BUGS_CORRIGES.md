# Bugs Corrigés et Anomalies Détectées

## Introduction

Ce document détaille tous les bugs et anomalies détectés dans le projet initial, explique pourquoi
ils étaient problématiques, et comment ils ont été corrigés

## Bug Critique : Double Launcher dans AndroidManifest

### Description du Bug

Dans le projet initial, le fichier `AndroidManifest.xml` contenait deux activités avec des intent
filters de type LAUNCHER :

```xml
<activity
    android:name=".MainActivity"
    android:exported="true"
    android:label="@string/app_name"
    android:theme="@style/Theme.AndroidRecruitmentTestApp">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>

<activity
    android:name=".DetailsActivity"
    android:exported="true"
    android:theme="@style/Theme.AndroidRecruitmentTestApp">
    <intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.LAUNCHER" />
    </intent-filter>
</activity>
```

### Pourquoi c'était un Problème

Un intent filter avec `MAIN` et `LAUNCHER` indique à Android que cette activité est un point
d'entrée de l'application et doit apparaître dans le launcher (écran d'accueil)
Avoir deux activités avec ce filtre signifie que :

1. Deux icônes dans le launcher : Android installait l'application deux fois, créant deux icônes
   distinctes dans le launcher
2. Confusion utilisateur : l'utilisateur pourrait ne pas savoir quelle icône utiliser
3. Comportement inattendu : cliquer sur une icône pouvait ouvrir `DetailsActivity` directement, ce
   qui n'a pas de sens car cette activité nécessite un `albumId` en paramètre
4. Problèmes de build : certains outils de build peuvent générer des avertissements ou des erreurs
   avec cette configuration

### Correction Appliquée

L'intent filter a été supprimé de `DetailsActivity` car cette activité n'est pas un point d'entrée
de l'application. Elle est uniquement accessible via une navigation depuis `MainActivity` :

```xml
<activity
    android:name=".DetailsActivity"
    android:exported="true"
    android:theme="@style/Theme.AndroidRecruitmentTestApp">
    <!-- Pas d'intent-filter : cette activité n'est pas un point d'entrée -->
</activity>
```

### Résultat

Après cette correction :

- Une seule icône apparaît dans le launcher
- L'application se comporte de manière cohérente
- `DetailsActivity` est uniquement accessible via navigation depuis `MainActivity`
- Le build fonctionne sans avertissements

## Anti Pattern : Utilisation de GlobalScope

### Description du Problème

Dans le projet initial, le `AlbumsViewModel` utilisait `GlobalScope` pour lancer des coroutines :

```kotlin
@OptIn(DelicateCoroutinesApi::class)
class AlbumsViewModel(
    private val repository: AlbumRepository,
) : ViewModel() {
    
    fun loadAlbums() {
        GlobalScope.launch {
            try {
                _albums.emit(repository.getAllAlbums())
            } catch (_: Exception) { /* TODO: Handle errors */ }
        }
    }
}
```

### Pourquoi c'était un Problème

`GlobalScope` est un anti pattern dans Android pour plusieurs raisons :

1. Fuites mémoire : les coroutines lancées dans `GlobalScope` ne sont pas annulées quand le
   ViewModel est détruit. Elles continuent à s'exécuter même après que l'utilisateur ait quitté
   l'écran, ce qui peut causer des fuites mémoire

2. Cycle de vie non respecté : `GlobalScope` ne respecte pas le cycle de vie Android. Les coroutines
   peuvent continuer à s'exécuter même après la destruction du composant qui les a lancées

3. Tests difficiles : les tests deviennent difficiles car les coroutines ne sont pas contrôlables et
   peuvent continuer à s'exécuter après la fin du test

4. Annotation `@DelicateCoroutinesApi` : cette annotation indique que l'API est délicate et doit
   être utilisée avec précaution. Son utilisation nécessite une justification solide, ce qui n'est
   pas le cas ici

### Correction Appliquée

`GlobalScope` a été remplacé par `viewModelScope`, qui est automatiquement annulé quand le ViewModel
est détruit :

```kotlin
@HiltViewModel
class AlbumsViewModel @Inject constructor(
    private val getAllAlbumsUseCase: GetAllAlbumsUseCase,
    // ...
) : ViewModel() {
    
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
}
```

### Résultat

Après cette correction :

- Les coroutines sont automatiquement annulées quand le ViewModel est détruit
- Pas de fuites mémoire
- Respect du cycle de vie Android
- Tests plus faciles à écrire et à contrôler

## Absence de Gestion d'Erreurs

### Description du Problème

Dans le projet initial, les erreurs étaient silencieusement ignorées :

```kotlin
fun loadAlbums() {
    GlobalScope.launch {
        try {
            _albums.emit(repository.getAllAlbums())
        } catch (_: Exception) { 
            /* TODO: Handle errors */ 
        }
    }
}
```

Le commentaire `/* TODO: Handle errors */` indiquait que la gestion d'erreurs n'était pas
implémentée.

### Pourquoi c'était un Problème

1. Expérience utilisateur médiocre : si une erreur se produisait (réseau indisponible, erreur
   serveur, etc.), l'utilisateur ne voyait rien. L'écran restait vide ou bloqué, sans explication

2. Debugging difficile : sans gestion d'erreurs, il était difficile de comprendre pourquoi
   l'application ne fonctionnait pas

3. Pas de feedback : l'utilisateur ne savait pas si l'application était en train de charger, si une
   erreur s'était produite, ou si les données étaient simplement vides

### Correction Appliquée

Une gestion d'erreurs complète a été implémentée avec la sealed class `UiState` :

```kotlin
sealed class UiState<out T> {
    data object Initial : UiState<Nothing>()
    data object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}
```

Les erreurs sont maintenant capturées et transformées en état d'erreur :

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

L'UI affiche un message d'erreur approprié :

```kotlin
when (val state = uiState) {
    is UiState.Error -> {
        ErrorMessage(message = state.message)
    }
    // ...
}
```

### Résultat

Après cette correction :

- Les erreurs sont affichées à l'utilisateur avec un message clair
- L'utilisateur comprend ce qui s'est passé
- Le debugging est facilité
- L'expérience utilisateur est améliorée

## Couplage Fort entre les Couches

### Description du Problème

Dans le projet initial, le ViewModel dépendait directement des DTOs du module data :

```kotlin
class AlbumsViewModel(
    private val repository: AlbumRepository,
) : ViewModel() {
    
    private val _albums = MutableSharedFlow<List<AlbumDto>>()
    val albums: SharedFlow<List<AlbumDto>> = _albums
}
```

Le ViewModel utilisait `AlbumDto` (un DTO du module data) au lieu d'utiliser le modèle de domaine

### Pourquoi c'était un Problème

1. Violation de Clean Architecture : le module `app` (presentation layer) dépendait directement des
   détails d'implémentation du module `data`. Cela violait le principe de dépendance inversée

2. Couplage fort : si la structure de `AlbumDto` changeait (par exemple, si l'API changeait), le
   ViewModel et l'UI devaient être modifiés

3. Testabilité réduite : il était difficile de tester le ViewModel sans dépendre du module data

4. Pas de séparation des responsabilités : le ViewModel connaissait les détails d'implémentation (
   DTOs) au lieu de travailler avec des abstractions (modèles de domaine)

### Correction Appliquée

Un module `domain` a été créé avec un modèle de domaine `Album` :

```kotlin
// domain/src/main/java/fr/leboncoin/domain/model/Album.kt
data class Album(
    val id: Int,
    val albumId: Int,
    val title: String,
    val url: String,
    val thumbnailUrl: String,
    val isFavorite: Boolean = false
)
```

Le ViewModel utilise maintenant le modèle de domaine :

```kotlin
@HiltViewModel
class AlbumsViewModel @Inject constructor(
    private val getAllAlbumsUseCase: GetAllAlbumsUseCase,
    // ...
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<UiState<List<Album>>>(UiState.Initial)
    val uiState: StateFlow<UiState<List<Album>>> = _uiState.asStateFlow()
}
```

Le Repository dans le module data convertit les DTOs en modèles de domaine :

```kotlin
fun AlbumDto.toEntity() = AlbumEntity(/* ... */)
fun AlbumEntity.toDomain() = Album(/* ... */)
```

### Résultat

Après cette correction :

- Respect de Clean Architecture
- Découplage entre les couches
- Testabilité améliorée
- Maintenabilité accrue

## Absence de Persistance Offline

### Description du Problème

Dans le projet initial, il n'y avait pas de système de persistance. Les données étaient récupérées
depuis l'API à chaque fois, et perdues quand l'application était fermée

### Pourquoi c'était un Problème

1. Non conformité aux exigences : le README.md exige explicitement "un système de persistance des
   données afin que les données puissent être disponibles offline, même après redémarrage de
   l'application"

2. Expérience utilisateur médiocre : sans connexion réseau, l'application ne pouvait pas afficher de
   données, même si elles avaient été chargées précédemment

3. Performance : les données étaient rechargées depuis l'API à chaque ouverture de l'application,
   même si elles n'avaient pas changé

4. Consommation de données : chaque ouverture de l'application consommait des données réseau

### Correction Appliquée

Room a été intégré pour persister les données localement :

1. Entity Room : `AlbumEntity` pour représenter les données en base
2. DAO : `AlbumDao` pour les opérations de base de données
3. Database : `AppDatabase` pour la configuration Room
4. Repository : Le Repository lit depuis la base de données locale et rafraîchit depuis l'API en
   arrière plan

```kotlin
override fun getAllAlbums(): Flow<List<Album>> =
    albumDao.getAllAlbums().map { entities ->
        entities.map { it.toDomain() }
    }

override suspend fun refreshAlbums() {
    val albumsFromApi = albumApiService.getAlbums()
    val albumEntities = albumsFromApi.map { it.toEntity() }
    albumDao.insertAll(albumEntities)
}
```

### Résultat

Après cette correction :

- Les données sont disponibles offline
- Les données persistent après redémarrage
- Performance améliorée (pas de rechargement inutile)
- Conformité aux exigences du README

## Absence de Fonctionnalité Favoris

### Description du Problème

Le projet initial n'avait pas de fonctionnalité de favoris, alors que le README.md l'exige
explicitement : "Vous devez intégrer une fonctionnalité de mise en favoris qui persiste en local"

### Pourquoi c'était un Problème

1. Non conformité aux exigences : la fonctionnalité était explicitement demandée dans le README

2. Expérience utilisateur incomplète : les utilisateurs ne pouvaient pas marquer leurs albums
   préférés

3. Pas de persistance : si une fonctionnalité de favoris avait existé, je pense qu'elle n'aurait pas
   persisté localement

### Correction Appliquée

Un système complet de favoris a été implémenté :

1. Champ `isFavorite` : ajouté dans `AlbumEntity` et `Album`
2. Migration de base de données : migration pour ajouter le champ `isFavorite`
3. Méthodes DAO : `updateFavoriteStatus()`, `isFavorite()`, `getFavoriteAlbums()`
4. Use Case : `ToggleFavoriteUseCase` et `GetFavoriteAlbumsUseCase`
5. UI : boutons favoris dans la grille et l'écran de détail
6. Filtre : possibilité de filtrer pour afficher uniquement les favoris

### Résultat

Après cette correction :

- Fonctionnalité de favoris complète
- Persistance locale des favoris
- UI pour gérer les favoris
- Filtre pour afficher uniquement les favoris
- Conformité aux exigences du README

## Injection de Dépendances Manuelle

### Description du Problème

Le projet initial utilisait une injection de dépendances manuelle :

```kotlin
class AlbumsViewModel(
    private val repository: AlbumRepository,
) : ViewModel() {
    
    class Factory(
        private val repository: AlbumRepository,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AlbumsViewModel(repository) as T
        }
    }
}
```

### Pourquoi c'était un Problème

1. Boilerplate : beaucoup de code répétitif pour chaque ViewModel
2. Erreurs possibles : le cast `as T` peut échouer à l'exécution
3. Maintenance : chaque ajout de dépendance nécessite de modifier le Factory
4. Pas de validation à la compilation : les erreurs ne sont détectées qu'à l'exécution

### Correction Appliquée

Hilt a été intégré pour l'injection de dépendances :

```kotlin
@HiltViewModel
class AlbumsViewModel @Inject constructor(
    private val getAllAlbumsUseCase: GetAllAlbumsUseCase,
    // ...
) : ViewModel() {
    // Hilt injecte automatiquement les dépendances
}
```

Les dépendances sont fournies via des modules Hilt :

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Provides
    @Singleton
    fun provideAlbumRepository(/* ... */): IAlbumRepository {
        return AlbumRepository(/* ... */)
    }
}
```

### Résultat

Après cette correction :

- Moins de code boilerplate
- Validation à la compilation
- Gestion automatique du cycle de vie
- Conformité aux bonnes pratiques Android

## Bug : Thème XML Incompatible avec Spark

### Description du Problème

Initialement, le thème XML utilisait des thèmes Material qui n'étaient pas disponibles ou
incompatibles :

```xml
<!-- Tentative 1 : Material DayNight (n'existe pas) -->
<style name="Theme.AndroidRecruitmentTestApp"
    parent="android:Theme.Material.DayNight.NoActionBar" />

    <!-- Tentative 2 : Material Light (n'existe pas) -->
<style name="Theme.AndroidRecruitmentTestApp" parent="android:Theme.Material.Light.NoActionBar" />

    <!-- Tentative 3 : Material NoActionBar (n'existe pas) -->
<style name="Theme.AndroidRecruitmentTestApp" parent="android:Theme.Material.NoActionBar" />
```

Ces tentatives généraient des erreurs de compilation :

```
ERROR: AAPT: error: resource android:style/Theme.Material.DayNight.NoActionBar not found.
ERROR: AAPT: error: resource android:style/Theme.Light.NoActionBar not found.
ERROR: AAPT: error: resource android:style/Theme.NoActionBar not found.
```

### Pourquoi c'était un Problème

1. Erreurs de compilation : le projet ne pouvait pas compiler à cause de références à des thèmes
   inexistants
2. Incompatibilité avec Spark : le projet utilise Spark Design System, pas Material Design. Utiliser
   des thèmes Material dans le XML était incohérent
3. Confusion des responsabilités : le thème XML ne doit gérer que la configuration système (barre de
   statut, barre de navigation), pas le contenu UI qui est géré par Compose
4. Thème sombre non fonctionnel : même si le thème Compose détectait le mode sombre, le thème XML ne
   s'adaptait pas correctement au système

### Correction Appliquée

J'ai utilisé un thème XML minimal `android:Theme` comme parent, qui est le thème de base le plus
simple disponible dans tous les SDK Android :

**Thème pour mode clair** (`values/themes.xml`) :

```xml
<style name="Theme.AndroidRecruitmentTestApp" parent="android:Theme">
    <item name="android:windowNoTitle">true</item>
    <item name="android:windowActionBar">false</item>
    <item name="android:windowFullscreen">false</item>
    <item name="android:windowContentOverlay">@null</item>
</style>
```

**Thème pour mode sombre** (`values-night/themes.xml`) :

```xml
<style name="Theme.AndroidRecruitmentTestApp" parent="android:Theme">
    <item name="android:windowNoTitle">true</item>
    <item name="android:windowActionBar">false</item>
    <item name="android:windowFullscreen">false</item>
    <item name="android:windowContentOverlay">@null</item>
</style>
```

**Amélioration du thème Compose** :

Le thème Compose a également été amélioré pour mieux détecter les changements de thème système :

```kotlin
@Composable
fun MusicTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isDarkMode = isSystemInDarkTheme()
    
    val finalDarkTheme = darkTheme || isDarkMode
    val colors = if (finalDarkTheme) MusicColorsDark else MusicColorsLight

    // Utiliser key avec uiMode pour forcer la recomposition quand le thème change
    key(configuration.uiMode) {
        SparkTheme(colors = colors) {
            content()
        }
    }
}
```

### Résultat

Après cette correction :

- Le projet compile sans erreurs
- Le thème XML est compatible avec tous les SDK Android
- Le thème sombre s'applique correctement quand le système change de thème
- Séparation claire des responsabilités : XML pour la configuration système, Compose pour l'UI
- Cohérence avec Spark Design System (pas de Material dans le XML)
- Détection automatique et réactive des changements de thème système
