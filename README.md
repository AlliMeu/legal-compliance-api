# legal-compliance-api

![CI](https://github.com/AlliMeu/legal-compliance-api/actions/workflows/ci.yml/badge.svg)

Une petite API REST Spring Boot de consultation de dossiers d'avocat, soumise à des règles de conformité. C'est un exercice de test délibéré, pas un vrai produit.

## Pourquoi ce projet

Je voulais pratiquer cinq compétences de test en même temps : la gestion des erreurs, les cas limites sur une petite fonction pure, la vérification d'un service avec Mockito, un calcul de date de conservation avec des données manquantes, et l'autorisation avec Spring Security. J'ai choisi un domaine que je connais grâce à mon premier métier : le droit.

## Ce que le projet montre

| Domaine | Où | Ce qui est testé |
|---|---|---|
| Réponses d'erreur | `exception/GlobalExceptionHandler.java` | Un dossier inconnu (404), un corps JSON illisible et les erreurs de validation Bean Validation renvoient chacun une erreur JSON avec un `errorId` clair. Les erreurs que Spring gère déjà (405, 415, chemin inconnu) gardent le statut de Spring. |
| Cas limites en test unitaire | `util/CaseReferenceMasker.java` | La référence d'un dossier est masquée avant d'arriver dans une ligne de log : les 3 premiers caractères restent visibles, une référence de 3 caractères ou moins est entièrement masquée. Testé avec une entrée longue, courte, vide et nulle. |
| Service avec un dépôt de données | `audit/CaseAccessAuditService.java` | Chaque accès à un dossier est enregistré dans une entrée de journal (dossier, utilisateur, catégorie, canal, date). Les tests vérifient qu'un dossier inconnu est rejeté avec un 404 sans rien enregistrer, et qu'un accès valide enregistre exactement l'entrée attendue (contrôlé avec un `ArgumentCaptor`). |
| Date de conservation avec données manquantes | `compliance/RetentionPeriodCalculator.java` | Un dossier peut relever de plusieurs règles belges (dossier d'avocat : 5 ans, anti-blanchiment : 10 ans). La date retenue est la fin de la mission plus le délai applicable le plus long. Exposé par `POST /compliance/retention-period`. Si la mission n'est pas terminée, aucune date n'est renvoyée plutôt qu'une date devinée. Testé avec une règle, plusieurs règles, une mission non terminée, aucune règle et une mission qui se termine un 29 février. |
| Autorisation par rôle | `config/SecurityConfig.java` | `GET /cases/**` exige `SCOPE_case:read`, `POST /cases/{ref}/access` exige `SCOPE_case:write`. Testé avec un mauvais droit (403), sans authentification et avec un mauvais mot de passe (401), et avec le bon droit (succès) via `MockMvc` avec les vrais utilisateurs de démonstration, puis sur du vrai HTTP via Karate. |

Le projet compte 39 tests, répartis volontairement sur trois niveaux :

- 15 tests unitaires (JUnit 5, Mockito, AssertJ) pour la logique isolée ;
- 17 tests `MockMvc` pour un retour rapide sur les contrôleurs et la sécurité ;
- 7 scénarios Karate (`src/test/resources/.../karate/cases.feature`) qui appellent les mêmes endpoints comme un client HTTP boîte noire, sur un contexte Spring démarré, avec la base de données simulée.

Le job CI `docker-e2e-smoke` contrôle ensuite des réponses clés sur la vraie pile (application et MongoDB, sans simulation).

## Stack

Java 17, Spring Boot 3, Spring Security, Spring Data MongoDB, JUnit 5, Mockito, AssertJ, Karate, Docker.

## Lancer l'application

```bash
docker compose up --build
```

L'API démarre sur le port `8080` avec une vraie base MongoDB. Deux utilisateurs de démonstration sont définis dans `SecurityConfig` pour les tests manuels (authentification Basic) : `paralegal` / `demo` (lecture seule) et `lawyer` / `demo` (lecture et écriture).

```bash
curl -u lawyer:demo http://localhost:8080/cases/REF-001
```

La base démarre vide (aucun dossier n'est préchargé). La réponse attendue est donc un 404 JSON propre renvoyé par le gestionnaire d'erreurs, pas une trace d'exécution :

```json
{"errorId":"NOT_FOUND","message":"No case found for reference REF-001"}
```

Pour obtenir un dossier, ajoutez-en d'abord un dans la base :

```bash
docker compose exec mongo mongosh legal_compliance --eval 'db.cases.insertOne({reference: "REF-001", subject: "Lease dispute", status: "OPEN"})'
```

## Lancer les tests

```bash
mvn test
```

Les tests exigent **JDK 17** : le moteur GraalJS embarqué par Karate n'est pas encore compatible avec les JDK plus récents (`sun.misc.Unsafe.ensureClassInitialized` a été retiré). Si `mvn test` échoue avec une erreur `GraalVM` ou `Unsafe`, faites pointer `JAVA_HOME` vers un JDK 17 et relancez. La CI est fixée sur JDK 17 pour la même raison.

## Notes sur le domaine

Les dates de conservation suivent deux règles du droit belge :

- L'avocat est déchargé de sa responsabilité professionnelle et de la conservation des pièces cinq ans après l'achèvement de sa mission (art. 2276bis de l'ancien Code civil), sauf s'il a été constitué expressément dépositaire de pièces déterminées.
- Lorsque les obligations anti-blanchiment s'appliquent, les données d'identification sont conservées dix ans à dater de la fin de la relation d'affaires ou de l'opération occasionnelle, et les pièces relatives aux opérations dix ans à dater de leur exécution (art. 60 de la loi du 18 septembre 2017).

Pour simplifier, tous ces points de départ sont assimilés à la fin de la mission. Les dossiers et les utilisateurs sont fictifs, et rien ici ne constitue un conseil juridique.
