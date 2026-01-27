# Protocole
Afin d’éviter de faire un projet trop simple, il est nécessaire de définir notre propre protocole.

Le format JSON est le format qui est utilisé lors des transferts.

Pour simplifier la suite, on appellera **utilisateur**, chaque ordinateur sur le réseau. Ils sont
tous capables d’envoyer/recevoir des requêtes et des réponses. Ils sont identifiés par le couple
**IP:PORT**, récupéré lors de la réception du socket.

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
  "command": "CONNECT"
}
```

Chaque ordinateur qui ont reçu (et compris) la requête précédente répond :
```json
{
  "response": "CONNECTED",
  "name": "Nom de l’utilisateur"
}
```

### Récupération des fichiers/répertoires dans un répertoire d’un utilisateur
On peut aussi l’utiliser pour récupérer les fichiers/répertoires dans le répertoire partagé d’un
utilisateur, en utilisant `"path": "/"`.

Un utilisateur envoye à un utilisateur cible :
```json
{
  "command": "LIST",
  "path": "/path/vers/dossier"
}
```

Le cible répond :
```json
{
  "response": "SUCCESS",
  "path": "/path/vers/dossier",
  "files": [
    { "name": "fichier1.txt", "type": "file", "permission": "rwd" },
    { "name": "fichier2.txt", "type": "file", "permission": "rwd" }
  ]
}
```

Si la permission `r` n’est pas définie :
```json
{
  "response": "ERROR",
  "error": "No read permission"
}
```

Si le nom indiqué n’est pas un répertoire :
```json
{
  "response": "ERROR",
  "error": "Unknown directory"
}
```

### Suppression d’un fichier/répertoire
Un utilisateur envoye à un utilisateur cible :
```json
{
  "command": "DELETE",
  "path": "/path/vers/fichier/ou/dossier"
}
```

Le cible répond :
```json
{
  "response": "SUCCESS"
}
```

Si la permission `d` n’est pas définie :
```json
{
  "response": "ERROR",
  "error": "No delete permission"
}
```

Si le nom indiqué n’est ni un fichier, ni un répertoire :
```json
{
  "response": "ERROR",
  "error": "Unknown file or directory"
}
```

### Création d’un répertoire
Un utilisateur envoye à un utilisateur cible :
```json
{
  "command": "CREATE",
  "path": "/path/vers/nouveau/dossier"
}
```

Le cible répond :
```json
{
  "response": "SUCCESS"
}
```

Si la permission `w` n’est pas définie :
```json
{
  "response": "ERROR",
  "error": "No write permission"
}
```

### Téléversement d’un fichier
Un utilisateur envoye à un utilisateur cible :
```json
{
  "command": "UPLOAD",
  "path": "/path/vers/futur/fichier",
  "checksum": "a1b2c3d4e5f67890"
}
```

Le cible répond :
```json
{
  "response": "WAITING"
}
```
ou :
```json
{
  "response": "ERROR",
  "error": "No write permission"
}
```
ou :
```json
{
  "response": "ERROR",
  "error": "Unvalid path specified"
}
```

S’il n’y a pas d’erreur, l’utilisateur envoie les binaires du fichier.

Le cible répond :
```json
{
  "response": "SUCCESS"
}
```
ou :
```json
{
  "response": "ERROR",
  "error": "Corrupted file"
}
```

### Téléchargement d’un fichier
Un utilisateur envoye à un utilisateur cible :
```json
{
  "command": "DOWNLOAD",
  "path": "/path/vers/fichier/a/telecharger",
  "checksum": "a1b2c3d4e5f67890"
}
```

Le cible répond :
```json
{
  "response": "SENDING",
  "checksum": "a1b2c3d4e5f67890"
}
```
ou :
```json
{
  "response": "ERROR",
  "error": "No read permission"
}
```

L’utilisateur répond :
```json
{
  "response": "WAITING"
}
```
Le cible envoie les binaires du fichier.

L’utilisateur répond :
```json
{
  "response": "SUCCESS"
}
```
ou :
```json
{
  "response": "ERROR",
  "error": "Corrupted file"
}
```
