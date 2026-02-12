#!/usr/bin/env bash
set -euo pipefail

find src/java -name "*.java" > sources.txt
javac -d build/classes @sources.txt
java -cp "build/classes" p2pshare.MainGUI
