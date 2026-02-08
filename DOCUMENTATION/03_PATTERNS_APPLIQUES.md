# Patterns Appliqués dans le Projet

## Introduction

Ce document détaille tous les patterns de conception appliqués dans le projet, explique pourquoi je
les ai choisis, et comment je les ai implémenté.
Les patterns utilisés respectent les principes SOLID et les bonnes pratiques Android modernes

## Pattern MVVM (Model-View-ViewModel)

### Description

Le pattern MVVM (Model-View-ViewModel) est un pattern architectural qui sépare l'interface
utilisateur (View) de la logique métier (ViewModel) et des données (Model).
Dans le contexte Android avec Jetpack Compose, la View est représentée par les Composables, le
ViewModel contient la logique de présentation, et le Model est représenté par les entités du domain
layer

### Implémentation dans le Projet

**View (Composables) :**

Les Composables représentent la couche View. Ils sont responsables uniquement de l'affichage et de
la collecte des interactions utilisateur. Ils ne contiennent aucune logique métier.

Exemple avec `AlbumsScreen` :

```kotlin
@Composable
fun AlbumsScreen(
    onAlbumClick: (Int) -> Unit,
    viewModel: AlbumsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is UiState.Loading -> LoadingIndicator()
        is UiState.Error -> ErrorMessage(message = state.message)
        is UiState.Success -> AlbumsList(
            albums = state.data,
            onAlbumClick = onAlbumClick,
            onToggleFavorite = { viewModel.toggleFavorite(it) }
        )
    }
}
```

Le Composable observe simplement le `uiState` du ViewModel et réagit aux changements. Il ne fait
aucune logique métier, il se contente d'afficher l'état actuel.

**ViewModel :**

Le ViewModel contient la logique de présentation. Il transforme les données de la couche domain en
un état UI compréhensible par la View.
Il utilise des StateFlow pour exposer l'état de manière réactive.

Exemple avec `AlbumsViewModel` :

```kotlin
@HiltViewModel
class AlbumsViewModel @Inject constructor(
    private val getAllAlbumsUseCase: GetAllAlbumsUseCase,
    private val refreshAlbumsUseCase: RefreshAlbumsUseCase,
    // ...
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<Album>>>(UiState.Initial)
    val uiState: StateFlow<UiState<List<Album>>> = _uiState.asStateFlow()

    init {
        loadAlbums()
    }

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

Le ViewModel :

- Utilise les Use Cases pour récupérer les données
- Transforme les données en `UiState` (Loading, Success, Error)
- Expose l'état via StateFlow pour la réactivité
- Gère les erreurs et les transforme en état d'erreur UI

**Model :**

Le Model est représenté par les entités de la couche domain (`Album`). Ces entités sont pures et ne
contiennent aucune logique de présentation.

### Avantages du Pattern MVVM

1. Séparation des responsabilités : la View ne contient que de la logique d'affichage, le ViewModel
   contient la logique de présentation, et le Model contient les données
2. Testabilité : le ViewModel peut être testé indépendamment de la View. On peut tester la logique
   de présentation sans avoir besoin de l'UI
3. Survie aux changements de configuration : le ViewModel survit aux rotations d'écran et autres
   changements de configuration, ce qui préserve l'état de l'application
4. Réactivité : l'utilisation de StateFlow permet une mise à jour automatique de l'UI quand l'état
   change

### Comparaison avec le Projet Initial

Dans le projet initial, le pattern MVVM était partiellement implémenté mais avec des problèmes :

- Le ViewModel utilisait `GlobalScope` (anti pattern)
- Pas de gestion d'erreurs
- Le ViewModel dépendait directement des DTOs du module data
- Pas de séparation claire entre les états (Loading, Success, Error)

J'ai corrigé actuellement tous ces problèmes en utilisant :

- `viewModelScope` au lieu de `GlobalScope`
- `UiState` sealed class pour gérer tous les états
- Use Cases pour découpler le ViewModel du Repository
- StateFlow pour la réactivité

## Pattern Repository

### Description

Le pattern Repository fournit une abstraction pour l'accès aux données.
Il cache les détails d'implémentation des sources de données (API, base de données) et fournit une
interface unifiée pour accéder aux données

### Implémentation dans le Projet

**Interface dans le Domain Layer :**

L'interface `AlbumRepository` est définie dans le module `domain`, ce qui garantit que la couche
domain ne dépend pas des détails d'implémentation.

```kotlin
interface AlbumRepository {
    fun getAllAlbums(): Flow<List<Album>>
    suspend fun refreshAlbums()
    suspend fun getAlbumById(id: Int): Album?
    suspend fun toggleFavorite(albumId: Int): Boolean
    fun getFavoriteAlbums(): Flow<List<Album>>
}
```

Cette interface définit le contrat que toute implémentation doit respecter. Elle utilise les modèles
de domaine (`Album`) et non les DTOs ou Entities

**Implémentation dans le Data Layer :**

L'implémentation dans le module `data` combine les données de l'API et de la base de données
locale :

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
        try {
            val albumsFromApi = albumApiService.getAlbums()
            val albumEntities = albumsFromApi.map { it.toEntity() }
            albumDao.insertAll(albumEntities)
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun toggleFavorite(albumId: Int): Boolean {
        val currentStatus = albumDao.isFavorite(albumId)
        val newStatus = !currentStatus
        albumDao.updateFavoriteStatus(albumId, newStatus)
        return newStatus
    }
}
```

Cette implémentation :

- Retourne toujours les données depuis la base de données locale (offline-first)
- Rafraîchit les données depuis l'API en arrière plan
- Convertit les Entities en modèles de domaine
- Gère les opérations de favoris directement dans la base de données

### Avantages du Pattern Repository

1. Abstraction : la couche domain ne connaît pas les détails d'implémentation (API, Room, etc.)
2. Flexibilité : on peut changer l'implémentation (par exemple, utiliser une autre API) sans
   affecter le domain layer
3. Testabilité : on peut facilement mocker le Repository dans les tests
4. Offline-first : Le Repository peut combiner plusieurs sources de données (API + DB locale)

## Pattern Use Case

### Description

Le pattern Use Case encapsule une action métier spécifique. Chaque Use Case représente une
fonctionnalité unique de l'application.
Ce pattern respecte le principe de responsabilité unique (SRP) et facilite la testabilité

### Implémentation dans le Projet

Chaque fonctionnalité de l'application est encapsulée dans un Use Case :

**GetAllAlbumsUseCase :**

```kotlin
class GetAllAlbumsUseCase @Inject constructor(
    private val repository: AlbumRepository
) {
    operator fun invoke(): Flow<List<Album>> = repository.getAllAlbums()
}
```

Ce Use Case encapsule l'action "récupérer tous les albums". Il délègue simplement au Repository et
retourne un Flow pour la réactivité.

**RefreshAlbumsUseCase :**

```kotlin
class RefreshAlbumsUseCase @Inject constructor(
    private val repository: AlbumRepository
) {
    suspend operator fun invoke() = repository.refreshAlbums()
}
```

Ce Use Case encapsule l'action "rafraîchir les albums depuis l'API". C'est une opération suspend car
elle fait un appel réseau.

**ToggleFavoriteUseCase :**

```kotlin
class ToggleFavoriteUseCase @Inject constructor(
    private val repository: AlbumRepository
) {
    suspend operator fun invoke(albumId: Int): Boolean = repository.toggleFavorite(albumId)
}
```

Ce Use Case encapsule l'action "basculer le statut favori d'un album". Il retourne le nouveau
statut (true si maintenant favori, false sinon).

**GetAlbumByIdUseCase :**

```kotlin
class GetAlbumByIdUseCase @Inject constructor(
    private val repository: AlbumRepository
) {
    suspend operator fun invoke(albumId: Int): Album? = repository.getAlbumById(albumId)
}
```

Ce Use Case encapsule l'action "récupérer un album par son ID". Il retourne `null` si l'album n'
existe pas.

**GetFavoriteAlbumsUseCase :**

```kotlin
class GetFavoriteAlbumsUseCase @Inject constructor(
    private val repository: AlbumRepository
) {
    operator fun invoke(): Flow<List<Album>> = repository.getFavoriteAlbums()
}
```

Ce Use Case encapsule l'action "récupérer tous les albums favoris". Il retourne un Flow pour la
réactivité.

### Avantages du Pattern Use Case

1. Responsabilité unique : chaque Use Case a une seule responsabilité, ce qui facilite la
   compréhension et la maintenance
2. Réutilisabilité : les Use Cases peuvent être réutilisés dans différents contextes (différents
   ViewModels, différents écrans)
3. Testabilité : chaque Use Case peut être testé indépendamment en mockant le Repository
4. Documentation : les Use Cases servent de documentation vivante des fonctionnalités de
   l'application

### Utilisation dans les ViewModels

Les ViewModels utilisent les Use Cases au lieu d'appeler directement le Repository :

```kotlin
@HiltViewModel
class AlbumsViewModel @Inject constructor(
    private val getAllAlbumsUseCase: GetAllAlbumsUseCase,
    private val refreshAlbumsUseCase: RefreshAlbumsUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel() {

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

    fun toggleFavorite(albumId: Int) {
        viewModelScope.launch {
            toggleFavoriteUseCase(albumId)
        }
    }
}
```

Cette approche :

- Découple le ViewModel du Repository
- Rend le code plus lisible (les noms des Use Cases sont explicites)
- Facilite les tests (on peut mocker les Use Cases)

## Pattern Dependency Injection (Hilt)

### Description

Le pattern Dependency Injection (DI) consiste à fournir les dépendances d'un objet depuis l'
extérieur plutôt que de les créer à l'intérieur.
Hilt est l'implémentation de DI recommandée par Google pour Android

### Implémentation dans le Projet

**Configuration de Hilt :**

`@HiltAndroidApp` est utilisé pour initialiser Hilt :

```kotlin
@HiltAndroidApp
class PhotoApp : Application() {
    override fun onCreate() {
        super.onCreate()
    }
}
```

**Modules Hilt :**

Les dépendances sont fournies via des modules Hilt :

**DataModule :**

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient { /* ... */
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit { /* ... */
    }

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase { /* ... */
    }

    @Provides
    @Singleton
    fun provideAlbumRepository(
        apiService: AlbumApiService,
        albumDao: AlbumDao
    ): IAlbumRepository {
        return AlbumRepository(apiService, albumDao)
    }
}
```

**AppModule :**

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideAnalyticsHelper(@ApplicationContext context: Context): AnalyticsHelper {
        return AnalyticsHelper().apply {
            initialize(context)
        }
    }
}
```

**Injection dans les Composants :**

Les dépendances sont injectées automatiquement :

```kotlin
@HiltViewModel
class AlbumsViewModel @Inject constructor(
    private val getAllAlbumsUseCase: GetAllAlbumsUseCase,
    private val refreshAlbumsUseCase: RefreshAlbumsUseCase,
    // ...
) : ViewModel() { /* ... */ }

@AndroidEntryPoint
class MainActivity : ComponentActivity() { /* ... */ }
```

### Avantages du Pattern Dependency Injection

1. Découplage : les composants ne créent pas leurs propres dépendances, ce qui réduit le couplage
2. Testabilité : on peut facilement injecter des mocks dans les tests
3. Maintenabilité : les changements dans la création des dépendances sont centralisés dans les
   modules
4. Gestion du cycle de vie : Hilt gère automatiquement le cycle de vie des dépendances

### Comparaison avec le Projet Initial

Le projet initial utilisait une injection manuelle avec un pattern Factory :

```kotlin
// Ancien code
class AlbumsViewModel(
    private val repository: AlbumRepository,
) : ViewModel() {
    class Factory(
        private val repository: AlbumRepository,
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AlbumsViewModel(repository) as T
        }
    }
}
```

Cette approche nécessitait beaucoup de code boilerplate et était sujette aux erreurs. Hilt élimine
tout ce boilerplate et garantit une injection correcte à la compilation.

## Pattern Observer (StateFlow)

### Description

Le pattern Observer permet à un objet (observable) de notifier automatiquement ses observateurs
quand son état change.
J'ai utilisé StateFlow comme mécanisme d'observation.

### Implémentation dans le Projet

**Exposition de l'état dans les ViewModels :**

Les ViewModels exposent leur état via StateFlow :

```kotlin
private val _uiState = MutableStateFlow<UiState<List<Album>>>(UiState.Initial)
val uiState: StateFlow<UiState<List<Album>>> = _uiState.asStateFlow()
```

Le pattern utilise une variable privée mutable (`_uiState`) et une propriété publique immuable (
`uiState`).
Cela garantit que seul le ViewModel peut modifier l'état, mais n'importe qui peut l'observer.

**Observation dans les Composables :**

Les Composables observent le StateFlow avec `collectAsStateWithLifecycle()` :

```kotlin
val uiState by viewModel.uiState.collectAsStateWithLifecycle()

when (val state = uiState) {
    is UiState.Loading -> LoadingIndicator()
    is UiState.Success -> AlbumsList(albums = state.data)
    is UiState.Error -> ErrorMessage(message = state.message)
}
```

Quand le `_uiState` change dans le ViewModel, le Composable se recompose automatiquement pour
afficher le nouvel état.

**Réactivité avec Room :**

Room peut retourner des Flow, ce qui permet une réactivité automatique :

```kotlin
@Query("SELECT * FROM albums")
fun getAllAlbums(): Flow<List<AlbumEntity>>
```

Quand les données changent dans la base de données (par exemple, après un toggle favorite), Room
émet automatiquement les nouvelles valeurs via Flow, ce qui met à jour l'UI automatiquement.

### Avantages du Pattern Observer avec StateFlow

1. Réactivité automatique : l'UI se met à jour automatiquement quand l'état change
2. Thread-safe** : StateFlow est thread-safe et peut être utilisé depuis n'importe quel thread
3. Respect du cycle de vie : `collectAsStateWithLifecycle()` respecte automatiquement le cycle de
   vie
4. Performance : StateFlow est optimisé et ne recrée que les composants nécessaires

## Pattern Sealed Class pour les États UI

### Description

Sealed classes utilisé pour représenter les différents états possibles de l'UI de manière type-safe.
Cela permet au compilateur de vérifier que tous les cas sont gérés

### Implémentation dans le Projet

La classe `UiState` est une sealed class qui représente tous les états possibles :

```kotlin
sealed class UiState<out T> {
    data object Initial : UiState<Nothing>()
    data object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}
```

**Utilisation dans les ViewModels :**

Les ViewModels utilisent `UiState` pour représenter l'état :

```kotlin
private val _uiState = MutableStateFlow<UiState<List<Album>>>(UiState.Initial)

fun loadAlbums() {
    viewModelScope.launch {
        _uiState.value = UiState.Loading
        try {
            val albums = getAllAlbumsUseCase()
            _uiState.value = UiState.Success(albums)
        } catch (e: Exception) {
            _uiState.value = UiState.Error(e.message ?: "Error occurred")
        }
    }
}
```

**Utilisation dans les Composables :**

Les Composables utilisent `when` pour gérer tous les cas :

```kotlin
when (val state = uiState) {
    is UiState.Initial -> { /* État initial */
    }
    is UiState.Loading -> LoadingIndicator()
    is UiState.Success -> AlbumsList(albums = state.data)
    is UiState.Error -> ErrorMessage(message = state.message)
}
```

Le compilateur garantit que tous les cas sont gérés. Si on ajoute un nouveau cas à `UiState`, le
compilateur forcera la mise à jour de tous les `when` qui l'utilisent.

### Avantages du Pattern Sealed Class

1. Type-safety : le compilateur garantit que tous les cas sont gérés
2. Exhaustivité : les `when` expressions doivent gérer tous les cas
3. Clarté : le code est plus clair et expressif
4. Maintenabilité : les changements dans les états sont détectés à la compilation
