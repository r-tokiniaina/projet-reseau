# TODO List
Les choses à faire sont :

## Back-end
- [ ] Établissement d’une connexion (utilisation de l’UDP)
    - [ ] Envoi d’un signal de demande de connexion
        - [ ] Envoi de la commande CONNECT
    - [ ] Écoute des signaux de demande de connexion
        - [ ] À la réception d’une commande CONNECT, demande à l’utilisateur s’il accepte la demande
            - [ ] Si OUI, envoi de la réponse CONNECTED
            - [ ] Si NON, ajouter en tant qu’UTILISATEUR REFUSÉ
    - [ ] Écoute des signaux d’acceptation de connexion
        - [ ] À la réception d’une réponse CONNECTED, enregistrement de l’utilisateur en tant que UTILISATEUR CONNECTÉ
- [ ] Récupération des fichiers
    - [ ] Lire [le protocole](protocole.md)
- [ ]  des fichiers
    - [ ] Lire [le protocole](protocole.md)
- [ ] Récupération des fichiers
    - [ ] Lire [le protocole](protocole.md)

## Front-end
- [ ] Page d’accueil
    - [ ] Liste de tous les utilisateurs (en tant réel)
- [ ] Page de répertoire
    - [ ] Pour un répertoire donné, liste de tous les fichiers/répertoires dedans

...

- enregistrement des connexions déjà effectuées pour ne pas à réaccepter plus tard
- parametre utilisateur : refuser
