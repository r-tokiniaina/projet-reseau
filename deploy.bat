mkdir -p build/classes
javac -cp "libs/gson-2.13.2.jar;src/java" -d build/classes src/java/logging/Log.java src/java/utils/FileUtils.java src/java/model/*.java src/java/networking/*.java src/java/p2pshare/config/AppConfig.java src/java/p2pshare/model/Peer.java src/java/p2pshare/network/udp/UdpDiscoveryService.java src/java/p2pshare/App.java src/java/p2pshare/Main.java
javac -cp "libs/gson-2.13.2.jar;src/java" -d build/classes src/java/logging/Log.java
javac -cp "libs/gson-2.13.2.jar;src/java;build/classes" -d build/classes src/java/utils/FileUtils.java
javac -cp "libs/gson-2.13.2.jar;src/java;build/classes" -d build/classes src/java/model/*.java
javac -cp "libs/gson-2.13.2.jar;src/java;build/classes" -d build/classes src/java/networking/*.java
javac -cp "libs/gson-2.13.2.jar;src/java;build/classes" -d build/classes src/java/p2pshare/config/AppConfig.java
javac -cp "libs/gson-2.13.2.jar;src/java;build/classes" -d build/classes src/java/p2pshare/model/Peer.java
javac -cp "libs/gson-2.13.2.jar;src/java;build/classes" -d build/classes src/java/p2pshare/network/udp/UdpDiscoveryService.java
javac -cp "libs/gson-2.13.2.jar;src/java;build/classes" -d build/classes src/java/p2pshare/App.java
javac -cp "libs/gson-2.13.2.jar;src/java;build/classes" -d build/classes src/java/p2pshare/Main.java

java -cp "build/classes;libs/gson-2.13.2.jar" p2pshare.Main