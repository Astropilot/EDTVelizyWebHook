# Présentation

**EDTVelizyWebHook** est un programme écrit en Java 8 permettant de vérifier régulièrement les modifications d'emploi du temps de l'université technologique de Vélizy pour la filière Informatique et d'envoyer les changements sur un channel discord via un WebHook.

# Utilisation

Pour l'utiliser il faut avant tout personnaliser les groupes sur lequels vérifier l'emploi du temps.
Pour cela il faut modifier la classe *Data* en reprenant les exemples commentés de la classe.
Ensuite il faut modifier la classe *Main* pour définir les *Workers* pour chacun des groupes.

# Versions & Bugs

## TODO

* Améliorer le système de tri des jours afin d'instancier moins d'objets et évituer de polluer la mémoire
* Améliorer la gestion des rapports de plantage

## Les versions

* Version **1.0**:
  * Vérification fonctionnelle des emplois du temps
  * Génération de messages riches Discord sans API extérieure
