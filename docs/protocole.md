# Protocole P2P - Partage de fichiers

Ce document décrit le protocole de communication utilisé dans l'application de partage de fichiers P2P.

Le protocole utilise **deux couches** :
- **UDP** pour la découverte automatique des pairs sur le réseau local
- **TCP** pour les opérations de transfert et de gestion de fichiers

Tous les utilisateurs (pairs) sont identifiés par leur **nom**, leur **adresse IP** et leur **port TCP**.

---

## 1. Découverte des pairs (UDP)

### 1.1 Broadcast périodique

Chaque pair diffuse périodiquement (toutes les 3 secondes) un message d'annonce en broadcast UDP sur le port configuré (par défaut 8888).

**Format du message :**
```
ANNOUNCE|<PC_NAME>|<TCP_PORT>|<IP_ADDRESS>
```

**Exemple :**
```
ANNOUNCE|Laptop-Alice|5000|192.168.1.42
```

**Champs :**
- `ANNOUNCE` : Type de message (constante)
- `PC_NAME` : Nom de l'ordinateur/utilisateur
- `TCP_PORT` : Port TCP où le serveur de fichiers écoute
- `IP_ADDRESS` : Adresse IP de l'émetteur

### 1.2 Réception et enregistrement

Chaque pair écoute sur le port UDP et enregistre les pairs découverts dans un fichier local avec un **TTL de 12 secondes**. Les pairs qui n'ont pas envoyé d'annonce pendant 12 secondes sont automatiquement retirés de la liste.

### 1.3 Consultation des pairs

Les pairs découverts peuvent être consultés via la commande :
```bash
./p2p_udp_discovery.sh peers
```

**Format de sortie :**
```
<PC_NAME>|<IP_ADDRESS>|<TCP_PORT>
```

**Exemple :**
```
Laptop-Alice|192.168.1.10|5000
Desktop-Bob|192.168.1.20|5000
```

---

## 2. Opérations sur fichiers (TCP)

Le protocole TCP utilise un format **texte simple** avec des lignes terminées par `\n`.

### 2.1 Format général des requêtes

Chaque requête suit ce format :
```
<COMMAND>\n
<PATH>\n
[<DATA>]
```

- `COMMAND` : Nom de la commande (LIST, CREATE, DELETE, UPLOAD, DOWNLOAD)
- `PATH` : Chemin relatif au répertoire partagé (commence par `/`)
- `DATA` : Données binaires (uniquement pour UPLOAD)

### 2.2 Format général des réponses

Les réponses ont deux formats principaux :

**Succès :**
```
OK\n
[<PAYLOAD>]
```

**Erreur :**
```
ERROR <raison>\n
```

**Raisons d'erreur possibles :**
- `invalid_path` : Le chemin sort du répertoire partagé
- `not_directory` : Le chemin n'est pas un répertoire
- `not_found` : Le fichier/répertoire n'existe pas
- `unknown_command` : Commande non reconnue

---

## 3. Commandes disponibles

### 3.1 LIST - Lister le contenu d'un répertoire

**Requête client :**
```
LIST\n
<PATH>\n
```

**Réponse serveur (succès) :**
```
OK\n
ENTRY|<TYPE>|<NAME>\n
ENTRY|<TYPE>|<NAME>\n
...
END\n
```

Où `<TYPE>` peut être :
- `FILE` : Fichier
- `DIRECTORY` : Répertoire

**Exemple :**
```
OK
ENTRY|DIRECTORY|documents
ENTRY|FILE|readme.txt
ENTRY|FILE|photo.jpg
END
```

**Réponse serveur (erreur) :**
```
ERROR invalid_path
END
```
ou
```
ERROR not_directory
END
```

---

### 3.2 CREATE - Créer un répertoire

**Requête client :**
```
CREATE\n
<PATH>\n
```

**Réponse serveur (succès) :**
```
OK\n
```

Le répertoire et tous les répertoires parents manquants sont créés automatiquement.

**Réponse serveur (erreur) :**
```
ERROR invalid_path\n
```

---

### 3.3 DELETE - Supprimer un fichier ou répertoire

**Requête client :**
```
DELETE\n
<PATH>\n
```

**Réponse serveur (succès) :**
```
OK\n
```

La suppression est récursive pour les répertoires. Si le fichier/répertoire n'existe pas, la commande retourne quand même `OK`.

**Réponse serveur (erreur) :**
```
ERROR invalid_path\n
```

---

### 3.4 UPLOAD - Téléverser un fichier

**Requête client :**
```
UPLOAD\n
<PATH>\n
<BINARY_DATA>
```

Le client envoie d'abord la commande et le chemin, puis les données binaires complètes du fichier.

**Réponse serveur (succès) :**
```
OK\n
```

Le répertoire parent est créé automatiquement s'il n'existe pas.

**Réponse serveur (erreur) :**
```
ERROR invalid_path\n
```

---

### 3.5 DOWNLOAD - Télécharger un fichier

**Requête client :**
```
DOWNLOAD\n
<PATH>\n
```

**Réponse serveur (succès) :**
```
OK\n
<BINARY_DATA>
```

Le serveur envoie d'abord `OK\n`, puis le contenu binaire complet du fichier.

**Réponse serveur (erreur) :**
```
ERROR invalid_path\n
```
ou
```
ERROR not_found\n
```

---

## 4. Sécurité et validation des chemins

Tous les chemins sont validés pour s'assurer qu'ils ne sortent pas du répertoire partagé configuré. Les tentatives d'accès en dehors du répertoire partagé (par exemple avec `../`) retournent `ERROR invalid_path`.

La résolution des chemins utilise la normalisation pour détecter les tentatives de traversée de répertoires.

---

## 5. Configuration

La configuration se fait via le fichier `app.conf` au format `key=value` :

```
udp_port=8888
tcp_port=5000
pc_name=User-PC
upload_dir=/home/user/p2p_shared
```

**Paramètres :**
- `udp_port` : Port UDP pour la découverte (défaut: 8888)
- `tcp_port` : Port TCP pour les opérations fichiers (défaut: 5000)
- `pc_name` : Nom de l'utilisateur/ordinateur (défaut: hostname)
- `upload_dir` : Répertoire racine partagé (défaut: ~/p2p_shared)

---

## 6. Exemples d'utilisation

### Démarrer les services
```bash
# Démarrer la découverte UDP
./scripts/network/p2p_udp_discovery.sh start

# Démarrer le serveur TCP
./scripts/network/p2p_tcp_server.sh start
```

### Lister les pairs
```bash
./scripts/network/p2p_udp_discovery.sh peers
```

### Opérations sur fichiers
```bash
# Lister le contenu du répertoire racine
./scripts/network/p2p_tcp_client.sh 192.168.1.10 5000 LIST /

# Créer un répertoire
./scripts/network/p2p_tcp_client.sh 192.168.1.10 5000 CREATE /documents

# Téléverser un fichier
./scripts/network/p2p_tcp_client.sh 192.168.1.10 5000 UPLOAD /test.txt /local/file.txt

# Télécharger un fichier
./scripts/network/p2p_tcp_client.sh 192.168.1.10 5000 DOWNLOAD /test.txt /local/destination.txt

# Supprimer un fichier
./scripts/network/p2p_tcp_client.sh 192.168.1.10 5000 DELETE /test.txt
```

### Arrêter les services
```bash
./scripts/network/p2p_udp_discovery.sh stop
./scripts/network/p2p_tcp_server.sh stop
```
