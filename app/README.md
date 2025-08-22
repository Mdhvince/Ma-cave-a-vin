# Ma cave a vin

Une application android minimalist et un design ultra moderne, une UX de haut niveau permettant:

### Setup
1. Choisir une configuration de la cave a vin (i.e 4x4)

### Ajout
1. prendre la photo d'une etiquette de vin
2. Associer le vin (l'etiquette) a un endroit de la cave

### Consultation
1. Visualiser la cave à vin
2. Afficher les détails d'un vin
3. Rechercher et Filtrer les vins (avec mise en évidence en temps réel des emplacements correspondants dans la cave)

### Update
1. Modifier les informations d'un vin (ex: millésime, commentaire, note)
2. Déplacer un vin dans la cave

### Suppression
1. Supprimer un vin de la cave

## Modernisation (Phase 1)

Cette application est en cours de refonte vers Material Design 3 (Jetpack Compose).

Implémenté dans cette phase:
- Material Design 3 + theming dynamique (Android 12+) + mode sombre/clair automatique
- Palette statique inspirée du vin (bourgogne profond, accents or) en fallback
- Typographie Material 3
- FAB contextuel (Home: ajouter une cave, Cellar: ajouter un compartiment)
- Pull-to-refresh sur l’écran d’accueil (liste des caves)
- Quick actions: appui long sur une carte de cave pour supprimer (avec haptique et confirmation)
- États de chargement image: placeholder « skeleton » pour les images hero (détails du vin)
- Recherches débouncées pour de meilleures performances
- Navigation: barre de navigation inférieure (Bottom Navigation), suppression des tiroirs (drawers)

Déjà présent:
- Bottom Sheet pour les actions secondaires (configuration, cave)
- Cartes avec elevation et coins arrondis

Prochaines étapes (proposées):
- Gestes de swipe (supprimer/archiver), états vides/erreur enrichis
- Transitions animées entre écrans (shared elements / animations)
- Offline-first + Room + WorkManager (sync)
- Pagination/LazyLoading avancée, cache image

Tech:
- UI: Jetpack Compose + Material3
- Image: Coil Compose
- Navigation: Navigation Compose
