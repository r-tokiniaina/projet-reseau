# Depuis reseau-backup/
# javac -cp "libs/gson-2.13.2.jar:src/java" -d build/classes src/java/logging/Log.java src/java/utils/FileUtils.java src/java/model/*.java src/java/networking/*.java src/java/p2pshare/config/AppConfig.java src/java/p2pshare/model/Peer.java src/java/p2pshare/network/udp/UdpDiscoveryService.java src/java/p2pshare/App.java src/java/p2pshare/Main.java

# Ou avec find (compilation automatique)
find src/java -name "*.java" > sources.txt
javac -cp "libs/gson-2.13.2.jar" -d build/classes @sources.txt
java -cp "build/classes:libs/gson-2.13.2.jar" p2pshare.Main
