# Implémentation de l'Interface Utilisateur

## Introduction

Je me suis inspiré de quelques design sur Dribble pour avoir une idée.
Ce document détaille l'implémentation de l'interface utilisateur de l'application, explique les
choix de design, la structure des composants, le thème personnalisé, et pourquoi l'UI a été
implémentée de cette manière.
L'application utilise Jetpack Compose avec le design system Spark d'Adevinta, combiné à un thème
personnalisé pour créer une expérience utilisateur moderne et cohérente.

## Structure de l'Interface Utilisateur

### Écran Principal : AlbumsScreen

L'écran principal (`AlbumsScreen`) affiche la liste des albums dans une grille. Il est conçu pour
être réactif et gérer tous les états possibles de l'application.

**Gestion des États :**

L'écran gère quatre états différents via la sealed class `UiState` :

1. État Initial : affiché au premier lancement, avant que les données ne soient chargées
2. État Loading : affiché pendant le chargement des données
3. État Success : affiche la liste des albums quand les données sont disponibles
4. État Error : affiche un message d'erreur si quelque chose s'est mal passé

```kotlin
when (val state = uiState) {
    is UiState.Initial, is UiState.Loading -> {
        LoadingIndicator()
    }
    is UiState.Error -> {
        ErrorMessage(message = state.message)
    }
    is UiState.Success -> {
        // Affiche la liste des albums
    }
}
```

Cette approche garantit que l'utilisateur voit toujours un état approprié, que les données soient en
cours de chargement, disponibles, ou qu'une erreur se soit produite.

**Pull-to-Refresh :**

J'ai implémenté un mécanisme de pull-to-refresh pour permettre à l'utilisateur de rafraîchir
manuellement les données. Cela utilise le composant `PullToRefreshBox` de Material3 :

```kotlin
PullToRefreshBox(
    isRefreshing = isRefreshing,
    onRefresh = { viewModel.refreshAlbums() },
    modifier = Modifier.fillMaxSize()
) {
    // Contenu de l'écran
}
```

Quand l'utilisateur tire vers le bas, le ViewModel est notifié et lance un rafraîchissement des
données depuis l'API.

**Filtre Favoris :**

L'écran inclut une barre supérieure (`AlbumsTopBar`) qui permet de basculer entre l'affichage de
tous les albums et uniquement les favoris.
Cette fonctionnalité est implémentée avec un bouton toggle qui change l'état `showFavoritesOnly`dans
le ViewModel.

**Grille d'Albums :**

Les albums sont affichés dans une grille responsive utilisant `LazyVerticalGrid` :

```kotlin
LazyVerticalGrid(
    columns = GridCells.Adaptive(minSize = 160.dp),
    contentPadding = PaddingValues(16.dp),
    horizontalArrangement = Arrangement.spacedBy(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
) {
    items(albums, key = { it.id }) { album ->
        AlbumGridCard(
            album = album,
            onClick = { onAlbumClick(album.id) },
            onToggleFavorite = { onToggleFavorite(album.id) }
        )
    }
}
```

La grille s'adapte automatiquement à la taille de l'écran avec `GridCells.Adaptive`, garantissant
que chaque élément a une largeur minimale de 160dp tout en utilisant efficacement l'espace
disponible.

### Écran de Détail : DetailScreen

L'écran de détail (`DetailScreen`) affiche les informations complètes d'un album avec une mise en
page immersive et moderne.

**Design Immersif :**

L'écran utilise un design immersif avec une grande image en arrière plan floutée et un dégradé (
Brush.verticalGradient) pour créer un effet visuel attrayant :

```kotlin
Box(modifier = Modifier.fillMaxWidth().height(500.dp)) {
    // Image floutée en arrière-plan
    AsyncImage(
        modifier = Modifier.fillMaxSize().blur(50.dp),
        // ...
    )

    // Dégradé pour améliorer la lisibilité
    Box(
        modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(
                colors = listOf(
                    SparkTheme.colors.surface.copy(alpha = 0.3f),
                    SparkTheme.colors.surface.copy(alpha = 0.8f),
                    SparkTheme.colors.surface
                )
            )
        )
    )

    // Image principale centrée
    AsyncImage(
        modifier = Modifier.size(280.dp).align(Alignment.Center),
        // ...
    )
}
```

Cette approche crée une expérience visuelle riche qui met en valeur l'album tout en maintenant une
bonne lisibilité du texte.

**Scroll Vertical :**

Le contenu de l'écran est scrollable verticalement pour permettre l'affichage de toutes les
informations même sur de petits écrans :

```kotlin
Column(
    modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState())
) {
    // Contenu scrollable
}
```

**Bouton Favoris :**

Un bouton favoris est affiché en haut à droite de l'écran, permettant à l'utilisateur de basculer
rapidement le statut favori de l'album.

## Composants UI Réutilisables

### Pourquoi Séparer les Composants ?

J'ai séparé les composants UI dans le package `coreui.components` pour plusieurs raisons
importantes :

1. Réutilisabilité : les composants peuvent être réutilisés dans différents écrans
2. Maintenabilité : les changements dans un composant sont centralisés
3. Testabilité : chaque composant peut être testé indépendamment
4. Cohérence : garantit une apparence et un comportement cohérents dans toute l'application

### LoadingIndicator

Le composant `LoadingIndicator` affiche un spinner de chargement centré à l'écran. Il est utilisé
dans tous les écrans pendant le chargement des données.

```kotlin
@Composable
fun LoadingIndicator(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Spinner(modifier = Modifier.size(28.dp))
    }
}
```

Ce composant utilise le `Spinner` de Spark pour maintenir la cohérence avec le design system. Il est
simple, réutilisable, et fournit un feedback visuel clair à l'utilisateur pendant le chargement.

### ErrorMessage

Le composant `ErrorMessage` affiche un message d'erreur centré à l'écran. Il utilise la couleur
d'erreur du thème pour une cohérence visuelle.

```kotlin
@Composable
fun ErrorMessage(message: String, modifier: Modifier = Modifier) {
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

Ce composant garantit que les erreurs sont toujours affichées de manière cohérente dans toute
l'application, avec un style uniforme et une bonne lisibilité.

### AlbumGridCard

Le composant `AlbumGridCard` est le composant principal pour afficher un album dans la grille. Il
combine l'image, le titre, et le bouton favoris dans un design cohérent.

**Structure du Composant :**

Le composant est structuré en plusieurs parties :

1. Image : image de l'album avec un ratio 1:1 (carré)
2. Bouton Favoris : bouton flottant en bas à droite de l'image
3. Titre : titre de l'album avec un maximum de 2 lignes
4. Sous titre : numéro d'album avec une couleur plus claire

**Animations :**

Le composant inclut une animation subtile au clic pour améliorer le feedback utilisateur :

```kotlin
val scale by animateFloatAsState(
    targetValue = 1f,
    animationSpec = tween(300),
    label = "scale"
)
```

**Gestion des Images :**

Les images sont chargées avec Coil, qui gère automatiquement le cache et les transitions :

```kotlin
AsyncImage(
    model = ImageRequest.Builder(LocalContext.current)
        .data(album.thumbnailUrl)
        .httpHeaders(
            NetworkHeaders.Builder()
                .add("User-Agent", "LeboncoinApp/1.0")
                .build()
        )
        .crossfade(300)
        .build(),
    contentDescription = album.title,
    modifier = Modifier
        .fillMaxSize()
        .clip(RoundedCornerShape(12.dp)),
    contentScale = ContentScale.Crop
)
```

Le `crossfade(300)` ajoute une transition fluide de 300ms lors du chargement de l'image.

## Thème Personnalisé : MusicTheme

### Pourquoi un Thème Personnalisé ?

Bien que l'application utilise le design system Spark, j'ai personnalisé un thème (`MusicTheme`)pour
adapter les couleurs à une application de musique, créant une identité visuelle unique
tout en respectant les guidelines Spark.

### Implémentation du Thème

Le thème est basé sur Spark avec des couleurs personnalisées :

**Couleurs Claires (Light Mode) :**

```kotlin
val MusicColorsLight = lightSparkColors().copy(
    main = Color(0xFF6C5CE7),        // Violet principal (couleur de marque)
    onMain = Color.White,             // Texte sur couleur principale
    support = Color(0xFFA29BFE),      // Violet secondaire (couleur de support)
    onSupport = Color.White,          // Texte sur couleur de support
    surface = Color(0xFFF8F9FA),      // Fond de surface clair
    onSurface = Color(0xFF2D3436),    // Texte sur surface
    backgroundVariant = Color(0xFFE9ECEF), // Variante de fond
    error = Color(0xFFE74C3C)         // Couleur d'erreur
)
```

**Couleurs Sombres (Dark Mode) :**

```kotlin
val MusicColorsDark = darkSparkColors().copy(
    main = Color(0xFF8B7ED8),         // Violet principal plus clair pour dark mode
    onMain = Color(0xFF2D3436),       // Texte sur couleur principale
    support = Color(0xFFB8B3FF),      // Violet secondaire plus clair
    onSupport = Color(0xFF2D3436),    // Texte sur couleur de support
    surface = Color(0xFF1A1A1A),      // Fond de surface sombre
    onSurface = Color(0xFFE9ECEF),    // Texte sur surface (clair pour contraste)
    backgroundVariant = Color(0xFF2D3436), // Variante de fond
    error = Color(0xFFE74C3C)         // Couleur d'erreur (identique)
)
```

**Application du Thème :**

Le thème est appliqué au niveau de l'application dans `MainActivity` et `DetailsActivity` :

```kotlin
setContent {
    MusicTheme {
        AlbumsScreen(onAlbumClick = { /* ... */ })
    }
}
```

Le thème détecte automatiquement le mode sombre du système via `isSystemInDarkTheme()` et applique
les couleurs appropriées.

### Configuration du Thème XML

Pour que le thème Compose fonctionne correctement avec le système Android, il est nécessaire de
configurer un thème XML dans les ressources. Ce thème XML gère uniquement la barre de statut système
et la barre de navigation, tandis que le contenu UI est entièrement géré par Compose avec Spark.

**Thème XML pour le mode clair** (`values/themes.xml`) :

```xml

<style name="Theme.AndroidRecruitmentTestApp" parent="android:Theme">
    <item name="android:windowNoTitle">true</item>
    <item name="android:windowActionBar">false</item>
    <item name="android:windowFullscreen">false</item>
    <item name="android:windowContentOverlay">@null</item>
</style>
```

**Thème XML pour le mode sombre** (`values-night/themes.xml`) :

```xml

<style name="Theme.AndroidRecruitmentTestApp" parent="android:Theme">
    <item name="android:windowNoTitle">true</item>
    <item name="android:windowActionBar">false</item>
    <item name="android:windowFullscreen">false</item>
    <item name="android:windowContentOverlay">@null</item>
</style>
```

**Pourquoi `android:Theme` ?**

Le thème XML utilise `android:Theme` comme parent, qui est le thème de base le plus simple
disponible dans Android. Cette approche est nécessaire car :

1. Compatibilité : `android:Theme` est disponible dans tous les SDK Android, garantissant la
   compatibilité
2. Séparation des responsabilités : Le thème XML ne gère que la configuration système (barre de
   statut, barre de navigation), pas le contenu UI
3. Compose gère l'UI : Tout le contenu visuel est géré par Compose avec `MusicTheme` et Spark,
   donc le thème XML peut être minimal
4. Support DayNight : Les dossiers `values/` et `values-night/` permettent à Android de
   sélectionner automatiquement le bon thème selon le mode système

**Détection automatique du thème système**

Le thème Compose utilise `LocalConfiguration.current.uiMode` comme clé pour forcer la recomposition
quand le mode UI change :

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

Cette approche garantit que :

- Le thème se met à jour automatiquement quand le système change de thème
- Les couleurs appropriées sont appliquées immédiatement

## Pourquoi cette Implémentation UI ?

### Séparation des Responsabilités

L'UI est structurée de manière à séparer clairement les responsabilités :

- Composables d'écran (`AlbumsScreen`, `DetailScreen`) : gèrent la structure globale et la
  navigation entre les états
- Composants réutilisables (`LoadingIndicator`, `ErrorMessage`, etc.) : encapsulent des
  fonctionnalités spécifiques
- Thème : centralise les couleurs et le style
