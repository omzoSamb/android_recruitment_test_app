# Justification des Choix de Librairies

## Introduction

Ce document détaille chaque librairie utilisée dans le projet, explique pourquoi elle a été choisie,
et comment elle s'intègre dans l'architecture globale de l'application
Toutes les librairies ont été sélectionnées en fonction de leur adéquation aux besoins du projet, de
leur maturité, de leur performance, et de leur alignement avec les bonnes pratiques Android
modernes

## Framework d'Injection de Dépendances

### Hilt

**Pourquoi Hilt ?**

Hilt est le framework d'injection de dépendances recommandé par Google pour les applications
Android.
Il est basé sur Dagger mais simplifié spécifiquement pour Android, ce qui le rend plus facile à
utiliser tout en conservant la puissance de Dagger

**Avantages :**

- Génération de code à la compilation : contrairement à l'injection manuelle utilisée dans le projet
  initial, Hilt génère le code d'injection à la compilation, ce qui garantit des performances
  optimales et une détection précoce des erreurs
- Intégration native Android : Hilt s'intègre parfaitement avec les composants Android (Activities,
  ViewModels, Services, etc.) grâce à des annotations dédiées comme `@AndroidEntryPoint` et
  `@HiltViewModel`
- Gestion automatique du cycle de vie : Hilt gère automatiquement le cycle de vie des dépendances,
  ce qui évite les fuites mémoire et simplifie le code
- Réduction du boilerplate : l'injection manuelle avec Factory pattern (comme dans le projet
  initial) nécessitait beaucoup de code répétitif. Hilt élimine ce boilerplate

**Utilisation dans le projet :**

Dans le projet initial, l'injection de dépendances était faite manuellement :

```kotlin
// Ancien code (projet initial)
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

Avec Hilt, cette complexité est éliminée :

```kotlin
// Nouveau code (projet actuel)
@HiltViewModel
class AlbumsViewModel @Inject constructor(
    private val getAllAlbumsUseCase: GetAllAlbumsUseCase,
    private val refreshAlbumsUseCase: RefreshAlbumsUseCase,
    // ...
) : ViewModel() {
    // Hilt injecte automatiquement les dépendances
}
```

**Modules Hilt :**

- `DataModule` : configure toutes les dépendances du module data (Retrofit, Room, OkHttp, etc.)
- `AppModule` : configure les dépendances spécifiques à l'application (AnalyticsHelper, etc.)

## Persistance Locale

### Room

**Pourquoi Room ?**

Room est la bibliothèque de persistance recommandée par Google pour Android.
Elle fournit une couche d'abstraction, ce qui simplifie grandement l'accès aux données tout en
offrant des fonctionnalités avancées

**Avantages :**

- Validation à la compilation : les requêtes SQL sont vérifiées à la compilation, ce qui évite les
  erreurs runtime
- Support des Flow : Room peut retourner des Flow, ce qui permet une réactivité automatique. Quand
  les données changent dans la base, les Flow émettront automatiquement les nouvelles valeurs
- Migrations facilitées : Room gère les migrations de schéma de base de données de manière
  structurée et sûre
- Type safe : Room utilise des annotations et génère du code type safe

**Utilisation dans le projet :**

Room est utilisé pour persister les albums localement, permettant ainsi un fonctionnement offline.
La base de données est définie avec une migration pour ajouter le champ `isFavorite` :

```kotlin
@Database(entities = [AlbumEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun albumDao(): AlbumDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE albums ADD COLUMN isFavorite INTEGER NOT NULL DEFAULT 0"
                )
            }
        }
    }
}
```

Le DAO utilise des Flow pour la réactivité :

```kotlin
@Dao
interface AlbumDao {
    @Query("SELECT * FROM albums")
    fun getAllAlbums(): Flow<List<AlbumEntity>>

    @Query("SELECT * FROM albums WHERE isFavorite = 1")
    fun getFavoriteAlbums(): Flow<List<AlbumEntity>>
}
```

Quand les données changent dans la base (par exemple, après un toggle favorite), Room émet
automatiquement les nouvelles valeurs via Flow, ce qui met à jour l'UI automatiquement

## Réseau

### Retrofit

**Pourquoi Retrofit ?**

Retrofit est la bibliothèque standard pour les appels réseau HTTP sur Android. Elle transforme les
interfaces en appels HTTP de manière type safe.

**Avantages :**

- Type safe : les interfaces Retrofit sont compilées et vérifiées, ce qui évite les erreurs de
  typage
- Support des coroutines : Retrofit supporte nativement les fonctions suspend de Kotlin, ce qui
  simplifie les appels asynchrones
- Extensible : Retrofit peut être étendu avec des interceptors (logging, authentification, etc.)
- Performance : Retrofit est optimisé et utilise OkHttp

**Utilisation dans le projet :**

L'interface API est définie de manière déclarative :

```kotlin
interface AlbumApiService {
    companion object {
        const val BASE_URL = "https://static.leboncoin.fr/img/shared/"
    }

    @GET("technical-test.json")
    suspend fun getAlbums(): List<AlbumDto>
}
```

Retrofit est configuré dans `DataModule` avec Kotlinx Serialization comme convertisseur :

```kotlin
@Provides
@Singleton
fun provideRetrofit(
    okHttpClient: OkHttpClient,
    json: Json
): Retrofit {
    val contentType = "application/json".toMediaType()
    return Retrofit.Builder()
        .baseUrl(AlbumApiService.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory(contentType))
        .build()
}
```

### OkHttp

**Pourquoi OkHttp ?**

OkHttp est la bibliothèque HTTP sous jacente utilisée par Retrofit.
Elle est également utilisée directement pour configurer des interceptors, notamment pour le logging
en mode debug

**Avantages :**

- Intercepteurs : permet d'ajouter facilement du logging, de l'authentification, etc.
- Performance : OkHttp est hautement optimisé pour les performances réseau
- Gestion des connexions : gère efficacement le pooling de connexions et la réutilisation

**Utilisation dans le projet :**

Un interceptor de logging est ajouté en mode debug pour faciliter le développement :

```kotlin
@Provides
@Singleton
fun provideOkHttpClient(): OkHttpClient {
    val builder = OkHttpClient.Builder()
    if (BuildConfig.DEBUG) {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        builder.addInterceptor(loggingInterceptor)
    }
    return builder.build()
}
```

### Kotlinx Serialization

**Pourquoi Kotlinx Serialization ?**

Kotlinx Serialization est la bibliothèque de sérialisation native de Kotlin
Elle est native Kotlin et offre de meilleures performances

**Avantages :**

- Natif Kotlin : pas de dépendance externe, intégré dans l'écosystème Kotlin
- Performance : plus rapide que Gson grâce à la génération de code à la compilation
- Type safe** : support des types Kotlin (data classes, sealed classes, etc.)
- Validation à la compilation : les erreurs de sérialisation sont détectées à la compilation

**Utilisation dans le projet :**

Les DTOs sont annotés avec `@Serializable` :

```kotlin
@Serializable
data class AlbumDto(
    val id: Int,
    val albumId: Int,
    val title: String,
    val url: String,
    @SerialName("thumbnailUrl") val thumbnailUrl: String
)
```

Le convertisseur est configuré dans Retrofit pour utiliser Kotlinx Serialization

## Interface Utilisateur

### Jetpack Compose

**Pourquoi Jetpack Compose ?**

Jetpack Compose est le framework UI moderne recommandé par Google pour Android.
Il remplace le système XML traditionnel par une approche déclarative et réactive

**Avantages :**

- Déclaratif : le code UI est plus lisible et maintenable que XML
- Réactif : l'UI se met à jour automatiquement quand l'état change
- Moins de code : Compose nécessite généralement moins de code que XML + ViewBinding/DataBinding
- Preview en temps réel : les composants peuvent être prévisualisés directement dans Android Studio
- Performance : Compose est optimisé pour les performances et ne recrée que les composants
  nécessaires.

**Utilisation dans le projet :**

Tous les écrans sont implémentés avec Compose :

- `AlbumsScreen` : Affiche la liste des albums en grille
- `DetailScreen` : Affiche les détails d'un album
- Composants réutilisables : `LoadingIndicator`, `ErrorMessage`, etc.

### Spark Design System

**Pourquoi Spark ?**

Spark est le design system d'Adevinta (leboncoin), que j'ai découvert durant le test.
Je l'ai utilisé car il a été fourni dans le test technique, et il garantit une cohérence

**Avantages :**

- Cohérence : assure une cohérence visuelle
- Composants prêts à l'emploi : fournit des composants UI pré-construits

**Utilisation dans le projet :**

Le thème de l'application est basé sur Spark avec des couleurs personnalisées :

```kotlin
val MusicColorsLight = lightSparkColors().copy(
    main = Color(0xFF6C5CE7),        // Violet principal
    onMain = Color.White,
    support = Color(0xFFA29BFE),      // Violet secondaire
    surface = Color(0xFFF8F9FA),      // Fond clair
    // ...
)
```

Les composants Spark utilisés incluent :

- `SparkTheme` : Thème de base
- `Spinner` : Indicateur de chargement
- `Text` : Texte stylisé
- `Scaffold` : Structure de base des écrans

## Asynchrone et Réactivité

### Kotlin Coroutines

**Pourquoi Kotlin Coroutines ?**

Les coroutines sont la solution native de Kotlin pour la programmation asynchrone. Elles remplacent
les callbacks et les RxJava pour une approche plus simple et plus lisible

**Avantages :**

- Natif Kotlin : intégré dans le langage, pas de dépendance externe
- Code séquentiel : le code asynchrone ressemble à du code synchrone, ce qui le rend plus
  lisible
- Gestion des erreurs : les exceptions sont gérées de manière naturelle avec try-catch
- Intégration : s'intègre parfaitement avec Flow, Room, Retrofit, etc

**Utilisation dans le projet :**

Les coroutines sont utilisées partout pour les opérations asynchrones :

- Dans les ViewModels avec `viewModelScope`
- Dans les Use Cases pour les opérations suspend
- Dans le Repository pour les appels réseau et base de données

**Amélioration par rapport au projet initial :**

Le projet initial utilisait `GlobalScope`, ce qui est un anti pattern car il peut causer des fuites
mémoire. J'ai utilisé `viewModelScope`, qui est automatiquement annulé quand le ViewModel est
détruit

### Flow (Kotlin Coroutines)

**Pourquoi Flow ?**

Flow est le système de streams réactifs de Kotlin. Je l'ai utilisé pour représenter des séquences de
valeurs qui peuvent être émises de manière asynchrone

**Avantages :**

- Réactivité : permet une réactivité automatique. Quand les données changent, l'UI se met à jour
  automatiquement
- Composable : les Flow peuvent être combinés, transformés, filtrés, etc
- Intégration : s'intègre parfaitement avec Room, qui peut retourner des Flow
- Gestion du cycle de vie : `collectAsStateWithLifecycle()` respecte automatiquement le cycle de
  vie

**Utilisation dans le projet :**

Les ViewModels exposent des StateFlow qui sont collectés dans les Composables :

```kotlin
// ViewModel
private val _uiState = MutableStateFlow<UiState<List<Album>>>(UiState.Initial)
val uiState: StateFlow<UiState<List<Album>>> = _uiState.asStateFlow()

// Composable
val uiState by viewModel.uiState.collectAsStateWithLifecycle()
```

Quand le `_uiState` change dans le ViewModel, le Composable se recompose automatiquement

## Tests

### Mockito

**Pourquoi Mockito ?**

Mockito est la bibliothèque standard pour créer des mocks dans les tests. Elle permet
de simuler des dépendances pour isoler les unités testées.

**Avantages :**

- **Support Kotlin** : `mockito-kotlin` fournit des extensions pour Kotlin.
- **Flexible** : permet de mocker n'importe quelle dépendance.

**Utilisation dans le projet :**

Les Use Cases sont mockés dans les tests de ViewModel et les tests des Use Cases mockent le
Repository :

```kotlin
// Dans les tests ViewModel
val getAllAlbumsUseCase = mock<GetAllAlbumsUseCase>()
whenever(getAllAlbumsUseCase()).thenReturn(flowOf(albums))

// Dans les tests Use Cases
val repository = mock<AlbumRepository>()
whenever(repository.getAllAlbums()).thenReturn(flowOf(albums))
```

### Kotlinx Coroutines Test

**Pourquoi Kotlinx Coroutines Test ?**

Kotlinx Coroutines Test fournit des utilitaires pour tester le code asynchrone basé sur les
coroutines

**Avantages :**

- Scope de test : `runTest` fournit un scope de coroutines de test qui permet de tester le code
  asynchrone de manière synchrone
- Contrôle du temps : permet de contrôler le temps virtuel pour les tests
- Intégration : s'intègre parfaitement avec les ViewModels et les Use Cases qui utilisent des
  coroutines

**Utilisation dans le projet :**

Tous les tests qui utilisent des coroutines utilisent `runTest` :

```kotlin
@Test
fun testAsyncOperation() = runTest {
    // Code de test avec coroutines
    val result = useCase()
    assertEquals(expected, result)
}
```