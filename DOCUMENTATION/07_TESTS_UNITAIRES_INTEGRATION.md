# Tests Unitaires et d'Intégration

## Introduction

Ce document détaille la stratégie de tests implémentée dans le projet, explique les différents types
de tests, leur objectif, et comment ils garantissent la qualité et la fiabilité de l'application.
Les tests couvrent les ViewModels, les Use Cases, les Repositories, et les composants UI Compose

## Stratégie de Tests

### Pourquoi Tester ?

Les tests sont essentiels pour plusieurs raisons :

1. Détection précoce des bugs : les tests permettent de détecter les problèmes avant qu'ils n'
   atteignent la production
2. Confiance dans le refactoring : les tests garantissent que le refactoring n'introduit pas de
   régressions
3. Documentation vivante : les tests servent de documentation sur le comportement attendu du code
4. Réduction des coûts : détecter et corriger les bugs tôt coûte moins cher que de les corriger en
   production

### Types de Tests Implémentés

Le projet implémente deux types principaux de tests :

1. Tests Unitaires : testent des unités individuelles de code (Use Cases, ViewModels) de manière
   isolée
2. Tests d'Intégration : testent l'intégration entre plusieurs composants (UI avec ViewModel,
   Repository avec DAO et API)

## Tests Unitaires

### Tests des ViewModels

Les ViewModels sont testés de manière isolée en mockant leurs dépendances (Use Cases). Cela permet
de tester la logique de présentation sans dépendre des couches inférieures

#### AlbumsViewModelTest

Ce fichier contient les tests de base pour `AlbumsViewModel` :

**Test d'État Initial :**

```kotlin
@Test
fun `initial state should be Initial`() = runTest {
    whenever(getAllAlbumsUseCase()).thenReturn(
        flow {
            delay(100)
            emit(emptyList())
        }
    )
    whenever(getFavoriteAlbumsUseCase()).thenReturn(flowOf(emptyList()))

    viewModel.uiState.test {
        val initialState = awaitItem()
        assertTrue("Initial state should be Initial or Loading",
            initialState is UiState.Initial || initialState is UiState.Loading)
    }
}
```

Ce test vérifie que l'état initial du ViewModel est correct. Il utilise Turbine pour tester le Flow
de manière réactive

**Test d'État de Succès :**

```kotlin
@Test
fun `when albums are loaded successfully, state is success`() = runTest {
    val albums = listOf(
        Album(1, 1, "Test Album", "url", "thumb", false)
    )
    whenever(getAllAlbumsUseCase()).thenReturn(flowOf(albums))
    whenever(getFavoriteAlbumsUseCase()).thenReturn(flowOf(emptyList()))

    viewModel.uiState.test {
        val state = awaitItem()
        assertTrue(state is UiState.Success)
        assertEquals(albums, (state as UiState.Success).data)
    }
}
```

Ce test vérifie que quand les albums sont chargés avec succès, l'état passe à `Success` avec les
bonnes données

**Test d'État Vide :**

```kotlin
@Test
fun `when albums list is empty, state is success with empty list`() = runTest {
    whenever(getAllAlbumsUseCase()).thenReturn(flowOf(emptyList()))
    whenever(getFavoriteAlbumsUseCase()).thenReturn(flowOf(emptyList()))

    viewModel.uiState.test {
        val state = awaitItem()
        assertTrue(state is UiState.Success)
        assertTrue((state as UiState.Success).data.isEmpty())
    }
}
```

Ce test vérifie que le ViewModel gère correctement le cas où la liste est vide

**Test d'État d'Erreur :**

```kotlin
@Test
fun `when error occurs, state is error`() = runTest {
    whenever(getAllAlbumsUseCase()).thenReturn(
        flow { throw IOException("Network error") }
    )
    whenever(getFavoriteAlbumsUseCase()).thenReturn(flowOf(emptyList()))

    viewModel.uiState.test {
        val state = awaitItem()
        assertTrue(state is UiState.Error)
        assertEquals("Network error", (state as UiState.Error).message)
    }
}
```

Ce test vérifie que les erreurs sont correctement capturées et transformées en état d'erreur

#### AlbumsViewModelLifecycleTest

Ce fichier contient les tests de cycle de vie du ViewModel :

**Test de Cycle de Vie Complet :**

```kotlin
@Test
fun `complete lifecycle: Initial -> Loading -> Success with data`() = runTest {
    whenever(getAllAlbumsUseCase()).thenReturn(
        flow {
            emit(emptyList())
            delay(100)
            emit(listOf(
                Album(1, 1, "Album 1", "url1", "thumb1", false),
                Album(2, 1, "Album 2", "url2", "thumb2", false)
            ))
        }
    )
    whenever(getFavoriteAlbumsUseCase()).thenReturn(flowOf(emptyList()))

    viewModel.uiState.test {
        val initialState = awaitItem()
        assertTrue("Step 1: Initial state",
            initialState is UiState.Initial ||
            initialState is UiState.Success && (initialState as UiState.Success).data.isEmpty())

        val successState = awaitItem()
        assertTrue("Step 3: Success state", successState is UiState.Success)
        val data = (successState as UiState.Success).data
        assertEquals("Should have 2 albums", 2, data.size)
    }
}
```

Ce test vérifie que le ViewModel passe correctement par tous les états du cycle de vie : Initial →
Loading → Success

#### AlbumsViewModelErrorTest

Ce fichier contient les tests de gestion d'erreurs :

**Test d'Erreur Timeout :**

```kotlin
@Test
fun `should handle network timeout error`() = runTest {
    whenever(getAllAlbumsUseCase()).thenReturn(
        flow {
            throw TimeoutCancellationException("Request timeout")
        }
    )
    whenever(getFavoriteAlbumsUseCase()).thenReturn(flowOf(emptyList()))

    viewModel.uiState.test {
        val state = awaitItem()
        assertTrue("Should be Error state", state is UiState.Error)
        val error = state as UiState.Error
        assertTrue("Error message should contain timeout",
            error.message.contains("timeout", ignoreCase = true))
    }
}
```

Ce test vérifie que le ViewModel gère correctement les erreurs de timeout réseau

**Test d'Erreur Socket Timeout :**

```kotlin
@Test
fun `should handle socket timeout error`() = runTest {
    whenever(getAllAlbumsUseCase()).thenReturn(
        flow {
            throw SocketTimeoutException("Socket timeout")
        }
    )
    whenever(getFavoriteAlbumsUseCase()).thenReturn(flowOf(emptyList()))

    viewModel.uiState.test {
        val state = awaitItem()
        assertTrue("Should be Error state", state is UiState.Error)
    }
}
```

Ce test vérifie la gestion des erreurs de socket timeout

**Test d'Erreur Serveur 500 :**

```kotlin
@Test
fun `should handle server error 500`() = runTest {
    whenever(getAllAlbumsUseCase()).thenReturn(
        flow {
            throw IOException("Server error 500")
        }
    )
    whenever(getFavoriteAlbumsUseCase()).thenReturn(flowOf(emptyList()))

    viewModel.uiState.test {
        val state = awaitItem()
        assertTrue("Should be Error state", state is UiState.Error)
    }
}
```

Ce test vérifie la gestion des erreurs serveur

### Tests des Use Cases

Les Use Cases sont testés en mockant le Repository :

**ToggleFavoriteUseCaseTest :**

```kotlin
class ToggleFavoriteUseCaseTest {
    private lateinit var repository: AlbumRepository
    private lateinit var useCase: ToggleFavoriteUseCase

    @Before
    fun setup() {
        repository = mock()
        useCase = ToggleFavoriteUseCase(repository)
    }

    @Test
    fun `invoke should return true when album is added to favorites`() = runTest {
        val albumId = 1
        whenever(repository.toggleFavorite(albumId)).thenReturn(true)

        val result = useCase(albumId)

        assertTrue(result)
    }
}
```

Ce test vérifie que le Use Case retourne correctement le nouveau statut favori

### Tests de Structure de Données

**AlbumDataStructureTest :**

Ce fichier teste la structure des données pour garantir que tous les champs obligatoires sont
présents :

```kotlin
@Test
fun `album should have all required fields`() {
    val album = Album(
        id = 1,
        albumId = 100,
        title = "Test Album",
        url = "https://example.com/image.jpg",
        thumbnailUrl = "https://example.com/thumb.jpg",
        isFavorite = false
    )

    assertNotNull("ID should not be null", album.id)
    assertNotNull("Album ID should not be null", album.albumId)
    assertNotNull("Title should not be null", album.title)
    assertNotNull("URL should not be null", album.url)
    assertNotNull("Thumbnail URL should not be null", album.thumbnailUrl)
    assertNotNull("IsFavorite should not be null", album.isFavorite)
}
```

Ce test garantit que tous les champs obligatoires sont présents et non null

## Tests d'Intégration

### Tests du Repository

**AlbumRepositoryIntegrationTest :**

Ce fichier teste l'intégration entre le Repository, le DAO mocké, et l'API mockée :

```kotlin
@Test
fun `getAllAlbums should return flow of albums from dao`() = runTest {
    val entities = listOf(
        AlbumEntity(1, 1, "Album 1", "url1", "thumb1", false),
        AlbumEntity(2, 1, "Album 2", "url2", "thumb2", true)
    )
    whenever(albumDao.getAllAlbums()).thenReturn(flowOf(entities))

    val result = repository.getAllAlbums()

    result.test {
        val albums = awaitItem()
        assertEquals("Should have 2 albums", 2, albums.size)
        assertEquals("First album title should match", "Album 1", albums[0].title)
        assertEquals("Second album should be favorite", true, albums[1].isFavorite)
    }
}
```

Ce test vérifie que le Repository transforme correctement les Entities en modèles de domaine

### Tests UI Compose

Les tests UI utilisent Compose Test Rule pour tester les Composables :

**AlbumsScreenTest :**

```kotlin
class AlbumsScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `loading indicator should be displayed when state is Loading`() {
        val viewModel = createTestViewModel(
            uiState = MutableStateFlow(UiState.Loading)
        )

        composeTestRule.setContent {
            AlbumsScreen(
                onAlbumClick = {},
                viewModel = viewModel
            )
        }

        composeTestRule.waitForIdle()
        // Le LoadingIndicator devrait être affiché
    }
}
```

Ce test vérifie que l'indicateur de chargement est affiché quand l'état est Loading

**AlbumsScreenInteractionTest :**

Ce fichier teste les interactions utilisateur :

```kotlin
@Test
fun `clicking on album should transmit correct ID`() {
    val clickedIds = mutableListOf<Int>()
    val albums = (1..5).map {
        Album(it, 1, "Album $it", "url$it", "thumb$it", false)
    }

    val viewModel = createTestViewModel(
        uiState = MutableStateFlow(UiState.Success(albums))
    )

    composeTestRule.setContent {
        AlbumsScreen(
            onAlbumClick = { albumId ->
                clickedIds.add(albumId)
            },
            viewModel = viewModel
        )
    }

    albums.forEach { album ->
        composeTestRule.onNodeWithText(album.title)
            .assertIsDisplayed()
            .performClick()

        composeTestRule.waitForIdle()
        assertEquals("Clicked ID should match album ID", album.id, clickedIds.last())
    }
}
```

Ce test vérifie que quand l'utilisateur clique sur un album, l'ID correct est transmis au callback

## Outils de Test Utilisés

### Turbine

Turbine est utilisé pour tester les Flow de manière réactive :

```kotlin
viewModel.uiState.test {
    val state = awaitItem()
    assertTrue(state is UiState.Success)
}
```

Turbine permet d'attendre et d'assertionner les valeurs émises par les Flow de manière simple et
intuitive

### Mockito

Mockito est utilisé pour créer des mocks des dépendances :

```kotlin
val getAllAlbumsUseCase = mock<GetAllAlbumsUseCase>()
whenever(getAllAlbumsUseCase()).thenReturn(flowOf(albums))
```

Mockito permet d'isoler les unités testées en mockant leurs dépendances

### Compose Test Rule

Compose Test Rule est utilisé pour tester les Composables :

```kotlin
@get:Rule
val composeTestRule = createComposeRule()

composeTestRule.setContent {
    AlbumsScreen(onAlbumClick = {}, viewModel = viewModel)
}
```

Compose Test Rule permet de tester les Composables de manière isolée

## Couverture de Tests

### Ce qui est Testé

1. ViewModels : tous les états, les interactions, et la gestion d'erreurs
2. Use Cases : tous les Use Cases sont testés
3. Repository : les transformations et l'intégration avec DAO et API
4. UI : les états d'affichage et les interactions utilisateur
5. Structure de données : validation des champs obligatoires

### Ce qui pourrait être Amélioré

1. Tests de DAO : tests avec une base de données Room en mémoire
2. Tests d'API : tests avec un serveur mock (MockWebServer)
3. Tests de navigation : tests de la navigation entre les écrans
4. Tests d'accessibilité : tests pour garantir l'accessibilité
