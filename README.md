# 🚢 BATAILLE JAVALE Ma Salive 🌊

<p align="center">
  <img src="https://img.shields.io/badge/Java%2025-ED8B00?style=for-the-badge&logo=java&logoColor=white" alt="Java"/>
  <img src="https://img.shields.io/badge/JavaFX-FF0000?style=for-the-badge&logo=java&logoColor=white" alt="JavaFX"/>
  <img src="https://img.shields.io/badge/FXGL%20Engine-0055FF?style=for-the-badge&logo=nintendo-gamecube&logoColor=white" alt="FXGL"/>
</p>

> **"STATION DE COMMANDEMENT ACTIVÉE. CALIBRAGE DES SYSTÈMES TACTIQUES..."**
> *Bataille Javale* est une simulation de combat naval cyberpunk développée avec le moteur FXGL. Le projet met l'accent sur la personnalisation extrême de l'expérience de jeu et une immersion visuelle via un moteur d'anomalies dynamiques.

---

## 🚀 Fonctionnalités "Hard" & Spécifications

Nous avons implémenté des mécaniques de personnalisation et de gameplay avancées pour répondre aux exigences du cahier des charges :

* **🔄 Mode Salve Tactique (Hard) :** Puissance de feu indexée sur la survie de votre flotte (1 navire opérationnel = 1 tir). Un tournant stratégique majeur par rapport au jeu classique.
* **☢️ Moteur d'Événements Aléatoires :**
  * **Brouillage Radar** : Masquage des données de tir/historique.
  * **Pluie de Météores** : Frappes environnementales synchronisées sur les deux plateaux.
  * **Protocole Apocalypse** : Déclenchement automatique au tour 30 (changement d'atmosphère et intensité critique).
  * **Salve Boostée** : Événement augmentant temporairement la puissance de feu.
  * **Bonus Passifs** : Ravitaillement gratuit et capacités bloquées intégrés au flux.
* **🏅 Archives & Carrière (Persistance) :**
  * Registre de succès complet avec **sauvegarde locale permanente** (`.dat`).
  * Interface dédiée avec **barres de progression segmentées** et rendu conditionnel (visuels néons débloqués).
  * Bouton de réinitialisation complète des données (reset) intégré.
* **📐 Flexibilité Tactique Totale :**
  * **Grille Redimensionnable** : Support complet du 5x5 jusqu'au 26x26 (A-Z).
  * **Persistance de Session** : Le système conserve l'intégralité de vos réglages de la partie à laquelle vous venez de jouer (taille, difficulté, mode, événements, arsenal) via le bouton "Rejouer".
  * **Customisation Totale** : Difficulté de l'IA, nombre de navires, activation modulaire des événements et de l'Apocalypse.

---

## 🛠️ Technique & UI

* **Architecture MVC** : Séparation stricte entre la logique métier (`BattleEngine`), l'orchestrateur de combat (`CombatOrchestrator`) et les vues JavaFX.
* **Responsive UI** : Utilisation de `Transforms.Scale` pour garantir que les grilles géantes (26x26) restent parfaitement lisibles et centrées sur n'importe quel écran.
* **VFX & Audio** : Système d'overlay indépendant pour les secousses de caméra (Shake), particules et gestionnaire audio mixant ambiances et SFX en temps réel.

---

## 📦 Installation et Déploiement

Le projet utilise **Maven** pour la gestion des dépendances.
```
# Compiler le projet
   mvn clean install
   
# Lancer l'opération
   mvn javafx:run
```
---

## 👨‍💻 Projet développé par **Adam & Lucie**.

> **Note Technique :**
> * **Ravitaillement** : Implémenté à 95% avec gestion des munitions et cooldowns, mais désactivé dans cette version finale suite à un bug critique de dernière minute afin de garantir la stabilité de la correction.
> * **Base de données** : Structure présente (`DatabaseManager`) mais non-exploitée au profit de la persistance locale plus robuste.