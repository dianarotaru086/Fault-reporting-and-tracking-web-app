# Gestion des pannes

Application web de signalement et de suivi des pannes dans un établissement.
Équipe QWERTY : ROTARU Diana Maria, BARBIER Ileana Geneviève (groupe 1241FA).

## Technologies

- Interface : HTML, CSS, JavaScript (dossier `src/main/resources/static`)
- Serveur : Java 25, Spring Boot 4 (API REST, validation, règles métier)
- Base de données : MySQL 8.4

## Lancer l'application

1. Installer MySQL Server 8.4 et noter le mot de passe de l'utilisateur `root`.
2. Dans `src/main/resources/application.properties`, remplacer `changeme` par ce mot de passe
   (ou définir la variable d'environnement `DB_PASSWORD`).
3. Ouvrir le projet dans IntelliJ IDEA (fichier `pom.xml` → *Open as Project*).
4. Lancer la classe `GestionPannesApplication`.
5. Ouvrir http://localhost:8080 dans le navigateur.

La base `gestion_pannes` et ses tables sont créées automatiquement au premier démarrage,
puis des données d'exemple sont chargées.

## Comptes de démonstration

Mot de passe pour tous les comptes : `demo123`

| E-mail | Rôle |
|---|---|
| maria.ionescu@exemple.fr | Utilisateur |
| andrei.popescu@exemple.fr | Utilisateur |
| ion.georgescu@exemple.fr | Responsable technique |
| elena.dumitru@exemple.fr | Responsable technique |

Pour repartir de zéro : supprimer la base (`DROP DATABASE gestion_pannes;`) puis relancer.
