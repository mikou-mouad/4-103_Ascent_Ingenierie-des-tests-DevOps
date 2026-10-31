# La présentation

53 slides : pour chaque chapitre, l'incident chez Iroise Traversées, le concept, puis le TP.
Les pauses, les déjeuners, les débuts et fins de journée suivent les horaires de `programme-2-jours.md`
(9h00 – 12h00, 13h00 – 16h00, pauses à 10h30 et 14h30).

## Présenter

Ouvrez `index.html` dans un navigateur.

| Touche | Action |
|--------|--------|
| → / Espace / clic | slide suivante |
| ← | slide précédente |
| Début / Fin | première / dernière slide |
| N | afficher les notes du formateur |
| F | plein écran |

Les polices viennent de Google Fonts (Bitter, Source Sans 3, JetBrains Mono) : sans Internet, le navigateur utilise des polices de remplacement.
Les icônes sont remplacées par des symboles simples.

## Les fichiers

- `deck.json` : l'ordre des slides et les chapitres
- `slides/<id>.html` : une slide par fichier (1920 × 1080, styles en ligne), avec les notes du formateur dans `<aside>`
- `construire.py` : assemble `index.html` à partir des slides

Après une modification d'une slide, depuis la racine du dépôt :

```bash
python presentation/construire.py
```

## La charte « granite et phare »

| Usage | Couleur |
|-------|---------|
| Fond sombre (couverture, incidents, fins de journée) | granite `#102A2E` |
| Texte sur fond sombre | `#F2EFE9`, secondaire `#B9C7C9` |
| Accent principal (pauses café) | rouge phare `#D7263D` |
| Accent sur fond sombre | feu jaune `#F4C95D` |
| Fonds clairs | `#F2EFE9`, `#FAF8F4`, cartes `#E4E0D8` |
| Texte sur fond clair | `#1A2326`, secondaire `#55616A` |
| Concept, cadres techniques | `#2E8B8B` ; slides TP et déjeuners `#1F4E5A` |
| Code | fond `#0B1D21`, surlignage `#F4C95D` et `#8FD3D3`, commentaires `#7F9496` |
