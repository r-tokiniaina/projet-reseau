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
Afin d’éviter de faire un projet trop simple, il est nécessaire de définir notre propre protocole.

Le format JSON est le format qui est utilisé lors des transferts.

Pour simplifier la suite, on appellera **utilisateur**, chaque ordinateur sur le réseau. Ils sont
tous capables d’envoyer/recevoir des requêtes et des réponses. Ils sont identifiés par le couple
**IP:PORT**, récupéré lors de la réception du socket.


## Principe
Toutes les requêtes sont associées à un identifiant (`request-id`).
Toutes les réponses sont associées à un identifiant (`response-id`), et doivent aussi préciser
le `request-id` de la requête à répondre.
L’identifiant est un nombre aléatoire (l’unicité n’est pas nécessaire).

Les permissions sont:
- `r`: (_read_) ouvrir/télécharger
- `w`: (_write_) téléverser
- `d`: (_delete_) supprimer
- d’autres viendront peut-être


## Fonctionnalités

### Récupération de tous les utilisateurs connectés
Un utilisateur envoye cette requête à tous les ordinateurs sur le réseau actuel.
```json
{
  "command": "CONNECT",
  "request-id": 1234567890
}
```

Chaque ordinateur qui ont reçu (et compris) la requête précédente répond :
```json
{
  "response": "CONNECTED",
  "name": "Nom de l’utilisateur",
  "permission": "rwd",
  "request-id": 1234567890,
  "response-id": 1244458147
}
```

### Récupération des fichiers/répertoires dans un répertoire d’un utilisateur
On peut aussi l’utiliser pour récupérer les fichiers/répertoires dans le répertoire partagé d’un
utilisateur, en utilisant `"path": "/"`.

Un utilisateur envoye à un utilisateur cible :
```json
{
  "command": "LIST",
  "path": "/path/vers/dossier",
  "request-id": 1241474395389
}
```

Le cible répond :
```json
{
  "response": "SUCCESS",
  "path": "/path/vers/dossier",
  "files": [
    { "name": "fichier1.txt", "type": "file" },
    { "name": "fichier2.txt", "type": "file" }
  ],
  "request-id": 1241474395389,
  "response-id": 73219846345
}
```

Si la permission `r` n’est pas définie :
```json
{
  "response": "ERROR",
  "error": "No read permission",
  "request-id": 1241474395389,
  "response-id": 73219846345
}
```

Si le nom indiqué n’est pas un répertoire :
```json
{
  "response": "ERROR",
  "error": "Unknown directory",
  "request-id": 1241474395389,
  "response-id": 73219846345
}
```

### Suppression d’un fichier/répertoire
Un utilisateur envoye à un utilisateur cible :
```json
{
  "command": "DELETE",
  "path": "/path/vers/fichier/ou/dossier",
  "request-id": 72462873498325
}
```

Le cible répond :
```json
{
  "response": "SUCCESS",
  "request-id": 72462873498325,
  "response-id": 481749364189
}
```

Si la permission `d` n’est pas définie :
```json
{
  "response": "ERROR",
  "error": "No delete permission",
  "request-id": 72462873498325,
  "response-id": 481749364189
}
```

Si le nom indiqué n’est ni un fichier, ni un répertoire :
```json
{
  "response": "ERROR",
  "error": "Unknown file or directory",
  "request-id": 72462873498325,
  "response-id": 481749364189
}
```

### Création d’un répertoire
Un utilisateur envoye à un utilisateur cible :
```json
{
  "command": "CREATE",
  "path": "/path/vers/nouveau/dossier",
  "request-id": 72462873498325
}
```

Le cible répond :
```json
{
  "response": "SUCCESS",
  "request-id": 72462873498325,
  "response-id": 481749364189
}
```

Si la permission `w` n’est pas définie :
```json
{
  "response": "ERROR",
  "error": "No write permission",
  "request-id": 72462873498325,
  "response-id": 481749364189
}
```

### Téléversement d’un fichier
Un utilisateur envoye à un utilisateur cible :
```json
{
  "command": "UPLOAD",
  "path": "/path/vers/futur/fichier",
  "checksum": "a1b2c3d4e5f67890",
  "request-id": 479812643873
}
```

Le cible répond :
```json
{
  "response": "WAITING",
  "request-id": 479812643873,
  "response-id": 4714632788426
}
```
ou :
```json
{
  "response": "ERROR",
  "error": "No write permission",
  "request-id": 479812643873,
  "response-id": 4714632788426
}
```
ou :
```json
{
  "response": "ERROR",
  "error": "Unvalid path specified",
  "request-id": 479812643873,
  "response-id": 4714632788426
}
```

S’il n’y a pas d’erreur, l’utilisateur répond :
```json
{
  "response": "SENDING",
  "data": "plein de données binaires ici",
  "request-id": 479812643873,
  "response-id": 4714632788426
}
```

Le cible répond :
```json
{
  "response": "SUCCESS",
  "request-id": 479812643873,
  "response-id": 4714632788426
}
```
ou :
```json
{
  "response": "ERROR",
  "error": "Corrupted file",
  "request-id": 479812643873,
  "response-id": 4714632788426
}
```

En cas d’erreur, l’utilisateur peut renvoyer le fichier avec les mêmes `request-id` et `response-id`.

### Téléchargement d’un fichier
Un utilisateur envoye à un utilisateur cible :
```json
{
  "command": "DOWNLOAD",
  "path": "/path/vers/fichier/a/telecharger",
  "checksum": "a1b2c3d4e5f67890",
  "request-id": 954282765243
}
```

Le cible répond :
```json
{
  "response": "SENDING",
  "checksum": "a1b2c3d4e5f67890",
  "request-id": 954282765243,
  "response-id": 4817483674626
}
```
ou :
```json
{
  "response": "ERROR",
  "error": "No read permission",
  "request-id": 954282765243,
  "response-id": 4817483674626
}
```

L’utilisateur répond :
```json
{
  "response": "WAITING",
  "request-id": 954282765243,
  "response-id": 4817483674626
}
```
Le cible répond :
```json
{
  "response": "SENDING",
  "data": "plein de données binaires ici",
  "request-id": 954282765243,
  "response-id": 4817483674626
}
```

L’utilisateur répond :
```json
{
  "response": "SUCCESS",
  "request-id": 954282765243,
  "response-id": 4817483674626
}
```
ou :
```json
{
  "response": "ERROR",
  "error": "Corrupted file",
  "request-id": 954282765243,
  "response-id": 4817483674626
}
```

En cas d’erreur, le cible peut renvoyer le fichier avec les mêmes `request-id` et `response-id`.
