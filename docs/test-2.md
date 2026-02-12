# Test
Voici un exemple de moyen de tester le projet.

Il est supposé que le répertoire actuel est à la racine du projet.

## Compilation
```bash
rm -r dist
find src/java -name '*.java' > classes.txt
javac -Xlint:unchecked -cp libs/gson-2.13.2.jar -d dist @classes.txt
rm classes.txt
```

## Lancement du serveur
Pour exécuter du java intéractivement:
```bash
jshell --class-path "libs/gson-2.13.2.jar:dist"
```

```java
networking.AppServer s = new networking.AppServer();
s.run();
```

## Lancement du client
### En Java
Pour exécuter du java intéractivement:
```bash
jshell --class-path "libs/gson-2.13.2.jar:dist"
```

```java
model.Peer target = new model.Peer("Username", java.net.InetAddress.getByAddress(new byte[] {127,0,0,1}), 18003);

model.File file = new model.File("/", model.File.Type.DIRECTORY);

networking.AppClient client = new networking.AppClient(target);
java.util.List<model.File> files = client.sendListRequest(file);
client.close();

for (model.File f : files) {
    System.out.println(f.getName() + "\t" + f.getType());
}
```

### En bash
```bash
command='{"command":"LIST","path":"/"}'

IP='127.0.0.1'
PORT=18003
exec 5<>/dev/tcp/$IP/$PORT

printf '%08X' "${#command}" | xxd -r -p >&5
printf '%s' "$command" >&5

response=$(cat <&5 | dd bs=1 skip=4 2>/dev/null)

exec 5<>-

echo "$response" | jq
```
