# Utils et Composants Réutilisables

## Introduction

Ce document détaille tous les utilitaires et composants réutilisables implémentés dans le projet,
explique leur utilité, leur implémentation, et pourquoi ils ont été créés.
Ces composants et utilitaires améliorent la maintenabilité, la réutilisabilité, et la cohérence du
code.

## Utilitaires (Utils)

### UiState

**Localisation :** `app/src/main/java/fr/leboncoin/androidrecruitmenttestapp/utils/UiState.kt`

**Description :**

`UiState` est une sealed class qui représente tous les états possibles de l'interface utilisateur.
Elle est utilisée dans tous les ViewModels pour gérer de manière type-safe les différents états de
l'application

**Implémentation :**

```kotlin
sealed class UiState<out T> {
    data object Initial : UiState<Nothing>()
    data object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}
```

**Pourquoi cette Implémentation ?**

1. Type-safety : les sealed classes garantissent que tous les cas sont gérés. Le compilateur force
   la gestion de tous les cas dans les expressions `when`

2. Clarté : les noms des états sont explicites et clairs. On sait immédiatement quel état on gère

3. Réutilisabilité : cette classe peut être utilisée pour n'importe quel type de données (
   List<Album>, Album, etc.) grâce au générique `<T>`

4. Exhaustivité : les `when` expressions doivent gérer tous les cas, ce qui évite les bugs où un
   état serait oublié

**Utilisation :**

Les ViewModels utilisent `UiState` pour exposer l'état :

```kotlin
private val _uiState = MutableStateFlow<UiState<List<Album>>>(UiState.Initial)
val uiState: StateFlow<UiState<List<Album>>> = _uiState.asStateFlow()
```

Les Composables utilisent `when` pour gérer tous les états :

```kotlin
when (val state = uiState) {
    is UiState.Initial, is UiState.Loading -> LoadingIndicator()
    is UiState.Success -> AlbumsList(albums = state.data)
    is UiState.Error -> ErrorMessage(message = state.message)
}
```

**Avantages :**

- Cohérence : tous les écrans gèrent les états de la même manière
- Maintenabilité : les changements dans les états sont centralisés
- Documentation : a sealed class documente clairement tous les états possibles

### FlowExtensions

**Localisation :**
`app/src/main/java/fr/leboncoin/androidrecruitmenttestapp/utils/FlowExtensions.kt`

**Description :**

Ce fichier contient des extensions pour Flow qui facilitent la transformation des Flow en `UiState`
avec gestion d'erreurs

**Implémentation :**

```kotlin
fun <T> Flow<T>.toResultFlow(context: Context): Flow<UiState<T>> {
    return flow {
        val isInternetConnected = hasInternetConnection(context)
        if (!isInternetConnected) {
            emit(UiState.Error("No Internet Connection"))
            return@flow
        }

        emit(UiState.Loading)
        this@toResultFlow
            .catch { e ->
                emit(UiState.Error(e.message ?: "Error occurred"))
            }
            .collect { data ->
                emit(UiState.Success(data))
            }
    }.flowOn(Dispatchers.IO)
}
```

**Pourquoi cette Extension ?**

1. Réduction de la duplication : au lieu de répéter la logique de transformation Flow → UiState dans
   chaque ViewModel, cette extension centralise cette logique

2. Gestion d'erreurs centralisée : la gestion des erreurs est centralisée dans cette extension,
   garantissant une cohérence dans toute l'application

3. Vérification de connexion : l'extension vérifie la connexion Internet avant d'émettre des
   données, permettant une gestion proactive des erreurs réseau

4. Thread-safety : l'utilisation de `flowOn(Dispatchers.IO)` garantit que les opérations réseau se
   font sur le bon thread

**Utilisation :**

Bien que cette extension soit disponible, le projet utilise actuellement une approche plus explicite
dans les ViewModels pour plus de clarté.
Cependant, cette extension pourrait être utilisée pour simplifier le code si nécessaire.

### NetworkUtils

**Localisation :** `app/src/main/java/fr/leboncoin/androidrecruitmenttestapp/utils/NetworkUtils.kt`

**Description :**

Ce fichier contient des utilitaires pour vérifier l'état de la connexion réseau

**Implémentation :**

```kotlin
fun hasInternetConnection(context: Context?): Boolean {
    try {
        if (context == null) return false

        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE)
                as? ConnectivityManager ?: return false

        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(activeNetwork)
            ?: return false

        return when {
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    } catch (e: Exception) {
        return false
    }
}
```

**Pourquoi cette Fonction ?**

1. API moderne : utilise `NetworkCapabilities` au lieu de l'ancienne API `NetworkInfo` qui est
   dépréciée

2. Vérification complète : vérifie plusieurs types de transport (WiFi, cellulaire) pour une
   détection précise

3. Gestion d'erreurs : gère les cas où le contexte est null ou où une exception se produit

4. Réutilisabilité : cette fonction peut être utilisée partout dans l'application pour vérifier la
   connexion

**Utilisation :**

Cette fonction est utilisée dans `FlowExtensions` pour vérifier la connexion avant d'émettre des
données.
Elle pourrait également être utilisée dans les ViewModels pour afficher des messages d'erreur plus
spécifiques

## Composants UI Réutilisables

### LoadingIndicator

**Localisation :**
`app/src/main/java/fr/leboncoin/androidrecruitmenttestapp/coreui/components/LoadingIndicator.kt`

**Description :**

Le composant `LoadingIndicator` affiche un spinner de chargement centré à l'écran.
Il est utilisé dans tous les écrans pendant le chargement des données

**Implémentation :**

```kotlin
@Composable
fun LoadingIndicator(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Spinner(
            modifier = Modifier.size(28.dp),
        )
    }
}
```

**Pourquoi ce Composant ?**

1. Réutilisabilité : utilisé dans `AlbumsScreen`, `DetailScreen`, et potentiellement d'autres écrans
   futurs

2. Cohérence : garantit que l'indicateur de chargement a toujours la même apparence dans toute l'
   application

3. Simplicité : composant simple et facile à utiliser

4. Design System : utilise le `Spinner` de Spark pour maintenir la cohérence avec le design system

**Utilisation :**

```kotlin
when (val state = uiState) {
    is UiState.Loading -> LoadingIndicator()
    // ...
}
```

### ErrorMessage

**Localisation :**
`app/src/main/java/fr/leboncoin/androidrecruitmenttestapp/coreui/components/ErrorMessage.kt`

**Description :**

Le composant `ErrorMessage` affiche un message d'erreur centré à l'écran.
Il utilise la couleur d'erreur du thème pour une cohérence visuelle

**Implémentation :**

```kotlin
@Composable
fun ErrorMessage(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = SparkTheme.typography.body1,
            color = SparkTheme.colors.error,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(16.dp)
        )
    }
}
```

**Pourquoi ce Composant ?**

1. Cohérence : tous les messages d'erreur ont la même apparence (couleur, style, position).

2. Accessibilité : le message est centré et bien visible, avec un padding approprié pour la
   lisibilité

3. Réutilisabilité : utilisé dans tous les écrans pour afficher les erreurs

4. Thème : utilise les couleurs et la typographie du thème pour la cohérence

**Utilisation :**

```kotlin
when (val state = uiState) {
    is UiState.Error -> ErrorMessage(message = state.message)
    // ...
}
```

### AlbumGridCard

**Localisation :**
`app/src/main/java/fr/leboncoin/androidrecruitmenttestapp/coreui/components/AlbumGridCard.kt`

**Description :**

Le composant `AlbumGridCard` est le composant principal pour afficher un album dans la grille.
Il combine l'image, le titre, et le bouton favoris dans un design cohérent

**Implémentation Détaillée :**

Le composant est structuré en plusieurs parties :

1. Animation : animation subtile au clic pour améliorer le feedback utilisateur
2. Image : image de l'album avec un ratio et coins arrondis
3. Bouton Favoris : bouton flottant en bas à droite de l'image
4. Titre : titre de l'album avec un maximum de 2 lignes
5. Sous titre : numéro d'album avec une couleur plus claire

**Pourquoi ce Composant ?**

1. Encapsulation : encapsule toute la logique d'affichage d'un album dans la grille

2. Réutilisabilité : utilisé dans `AlbumsList` pour afficher chaque album

3. Maintenabilité : les changements dans l'affichage des albums sont centralisés dans ce composant

4. Expérience utilisateur : inclut des animations et des interactions pour améliorer l'expérience
   utilisateur

**Caractéristiques Avancées :**

- Animation au clic : animation de scale pour le feedback utilisateur
- Gestion des images : utilise Coil avec crossfade pour des transitions fluides
- Bouton favoris intégré : le bouton favoris est intégré dans le design de la carte
- Gestion du texte : limite le titre à 2 lignes avec ellipsis pour éviter les débordements

**Utilisation :**

```kotlin
AlbumGridCard(
    album = album,
    onClick = { onAlbumClick(album.id) },
    onToggleFavorite = { onToggleFavorite(album.id) }
)
```

## Pourquoi Séparer les Composants ?

### Avantages de la Séparation

1. Réutilisabilité : les composants peuvent être réutilisés dans différents écrans sans duplication
   de code

2. Maintenabilité : les changements dans un composant sont centralisés. Si on veut changer
   l'apparence du `LoadingIndicator`, on le fait une seule fois

3. Testabilité : chaque composant peut être testé indépendamment

4. Cohérence : garantit une apparence et un comportement cohérents dans toute l'application

5. Séparation des responsabilités : chaque composant a une responsabilité claire et bien définie

### Structure du Package coreui

Les composants sont organisés dans le package `coreui` pour indiquer qu'ils font partie du système
de design de l'application :

```
coreui/
├── components/          # Composants UI réutilisables
│   ├── LoadingIndicator.kt
│   ├── ErrorMessage.kt
│   └── AlbumGridCard.kt
└── theme/              # Thème personnalisé
    └── MusicTheme.kt
```

Cette organisation facilite la navigation et la maintenance du code.
