# Sambashare en P2P
Le projet de Réseau avec Mr Haga.

Pour info, nous sommes le **Groupe 3**.


# Objectif
Samba est un moyen de partager des fichiers. Son principe se base sur le fait que, au sein
d’un ordinateur, il y a des répertoires, appelés _shares_, qui sont accessibles via le réseau par le
protocole SMB. Ils sont configurés de telle sorte que seuls certains utilisateurs et/ou groupes (de
la machine) puissent y accéder.

Notre système s’inspire de ce mécanisme. Sauf qu’au lieu de stocker ces répertoires au sein d’un
seul ordinateur hôte, chaque utilisateur conserve leur _répertoire partagé_ au sein de leur propre
ordinateur. Une fois connecté sur un même réseau, il est possible d’accéder aux répertoires
partagées des autres utilisateurs et d’effectuer des actions (tels que le téléchargement, le
téléversement, la suppression, etc.) selon les permissions déjà définies au préalable.


# Protocole
Plus d’informations sur le protocole utilisé sont disponibles dans : [docs/protocole.md](docs/protocole.md).
