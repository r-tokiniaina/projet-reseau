#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
APP_CONF="${APP_CONF:-app.conf}"
RUNTIME_DIR="${P2P_RUNTIME_DIR:-.p2p_runtime}"

CLIENT_SCRIPT="$SCRIPT_DIR/p2p_tcp_client.sh"
DISCOVERY_SCRIPT="$SCRIPT_DIR/p2p_udp_discovery.sh"

CURRENT_PEER=""
CURRENT_HOST=""
CURRENT_PORT=""
CURRENT_PATH="/"

load_config() {
  if [[ -f "$APP_CONF" ]]; then
    local key value
    while IFS='=' read -r key value; do
      [[ -z "${key:-}" ]] && continue
      [[ "${key:0:1}" == "#" ]] && continue
      case "$key" in
        tcp_port) DEFAULT_PORT="$value" ;;
      esac
    done < "$APP_CONF"
  fi
  DEFAULT_PORT="${DEFAULT_PORT:-5000}"
}

print_header() {
  echo ""
  echo "═══════════════════════════════════════════════════════"
  echo "  P2P File Sharing - Interactive Client"
  echo "═══════════════════════════════════════════════════════"
  echo ""
}

print_status() {
  if [[ -n "$CURRENT_PEER" ]]; then
    echo "📡 Connected to: $CURRENT_PEER ($CURRENT_HOST:$CURRENT_PORT)"
    echo "📂 Current path: $CURRENT_PATH"
  else
    echo "📡 Not connected to any peer"
  fi
  echo ""
}

list_peers() {
  echo "🔍 Discovering peers..."
  if [[ ! -x "$DISCOVERY_SCRIPT" ]]; then
    echo "❌ Discovery script not found: $DISCOVERY_SCRIPT"
    return 1
  fi

  local peers
  peers=$("$DISCOVERY_SCRIPT" peers 2>/dev/null || true)

  if [[ -z "$peers" ]]; then
    echo "⚠️  No peers found. Make sure UDP discovery is running."
    echo "   Start it with: $DISCOVERY_SCRIPT start"
    return 1
  fi

  echo ""
  echo "Available peers:"
  echo "───────────────────────────────────────────────────────"
  local i=1
  while IFS='|' read -r name ip port; do
    printf "%2d. %-20s %s:%s\n" "$i" "$name" "$ip" "$port"
    ((i++))
  done <<< "$peers"
  echo "───────────────────────────────────────────────────────"
  echo ""
}

connect_peer() {
  local peers peer_count choice
  peers=$("$DISCOVERY_SCRIPT" peers 2>/dev/null || true)

  if [[ -z "$peers" ]]; then
    echo "❌ No peers available"
    return 1
  fi

  peer_count=$(wc -l <<< "$peers")

  echo ""
  local i=1
  while IFS='|' read -r name ip port; do
    printf "%2d. %-20s %s:%s\n" "$i" "$name" "$ip" "$port"
    ((i++))
  done <<< "$peers"
  echo ""

  read -p "Select peer number (1-$peer_count): " choice

  if [[ ! "$choice" =~ ^[0-9]+$ ]] || [[ "$choice" -lt 1 ]] || [[ "$choice" -gt "$peer_count" ]]; then
    echo "❌ Invalid selection"
    return 1
  fi

  local selected
  selected=$(sed -n "${choice}p" <<< "$peers")
  IFS='|' read -r CURRENT_PEER CURRENT_HOST CURRENT_PORT <<< "$selected"
  CURRENT_PATH="/"

  echo "✅ Connected to $CURRENT_PEER ($CURRENT_HOST:$CURRENT_PORT)"
}

connect_manual() {
  read -p "Enter peer name: " CURRENT_PEER
  read -p "Enter host/IP: " CURRENT_HOST
  read -p "Enter port [$DEFAULT_PORT]: " CURRENT_PORT
  CURRENT_PORT="${CURRENT_PORT:-$DEFAULT_PORT}"
  CURRENT_PATH="/"

  echo "✅ Connected to $CURRENT_PEER ($CURRENT_HOST:$CURRENT_PORT)"
}

list_files() {
  if [[ -z "$CURRENT_HOST" ]]; then
    echo "❌ Not connected to any peer. Use 'connect' first."
    return 1
  fi

  local path="${1:-$CURRENT_PATH}"

  echo "📂 Listing: $path"
  echo ""

  local result
  result=$("$CLIENT_SCRIPT" "$CURRENT_HOST" "$CURRENT_PORT" LIST "$path" 2>&1 || true)

  if grep -q "^ERROR" <<< "$result"; then
    echo "❌ $result"
    return 1
  fi

  if grep -q "^OK" <<< "$result"; then
    echo "Type       Name"
    echo "───────────────────────────────────────────────────────"

    while IFS='|' read -r marker type name; do
      if [[ "$marker" == "ENTRY" ]]; then
        if [[ "$type" == "DIRECTORY" ]]; then
          printf "📁 DIR     %s\n" "$name"
        else
          printf "📄 FILE    %s\n" "$name"
        fi
      fi
    done <<< "$result"

    echo ""
  else
    echo "❌ Unexpected response"
    return 1
  fi
}

change_directory() {
  if [[ -z "$CURRENT_HOST" ]]; then
    echo "❌ Not connected to any peer"
    return 1
  fi

  local target="$1"

  if [[ "$target" == ".." ]]; then
    if [[ "$CURRENT_PATH" == "/" ]]; then
      echo "⚠️  Already at root"
      return 0
    fi
    CURRENT_PATH="$(dirname "$CURRENT_PATH")"
    [[ "$CURRENT_PATH" == "." ]] && CURRENT_PATH="/"
  elif [[ "$target" == "/" ]]; then
    CURRENT_PATH="/"
  elif [[ "${target:0:1}" == "/" ]]; then
    CURRENT_PATH="$target"
  else
    if [[ "$CURRENT_PATH" == "/" ]]; then
      CURRENT_PATH="/$target"
    else
      CURRENT_PATH="$CURRENT_PATH/$target"
    fi
  fi

  echo "📂 Changed to: $CURRENT_PATH"
}

create_directory() {
  if [[ -z "$CURRENT_HOST" ]]; then
    echo "❌ Not connected to any peer"
    return 1
  fi

  local dirname="$1"
  local fullpath

  if [[ "${dirname:0:1}" == "/" ]]; then
    fullpath="$dirname"
  elif [[ "$CURRENT_PATH" == "/" ]]; then
    fullpath="/$dirname"
  else
    fullpath="$CURRENT_PATH/$dirname"
  fi

  echo "📁 Creating directory: $fullpath"

  local result
  result=$("$CLIENT_SCRIPT" "$CURRENT_HOST" "$CURRENT_PORT" CREATE "$fullpath" 2>&1 || true)

  if grep -q "^OK" <<< "$result"; then
    echo "✅ Directory created successfully"
  else
    echo "❌ $result"
    return 1
  fi
}

upload_file() {
  if [[ -z "$CURRENT_HOST" ]]; then
    echo "❌ Not connected to any peer"
    return 1
  fi

  local source="$1"
  local dest="${2:-$(basename "$source")}"
  local fullpath

  if [[ ! -f "$source" ]]; then
    echo "❌ Source file not found: $source"
    return 1
  fi

  if [[ "${dest:0:1}" == "/" ]]; then
    fullpath="$dest"
  elif [[ "$CURRENT_PATH" == "/" ]]; then
    fullpath="/$dest"
  else
    fullpath="$CURRENT_PATH/$dest"
  fi

  echo "📤 Uploading: $source → $fullpath"

  local result
  result=$("$CLIENT_SCRIPT" "$CURRENT_HOST" "$CURRENT_PORT" UPLOAD "$fullpath" "$source" 2>&1 || true)

  if grep -q "^OK" <<< "$result"; then
    echo "✅ File uploaded successfully"
  else
    echo "❌ $result"
    return 1
  fi
}

download_file() {
  if [[ -z "$CURRENT_HOST" ]]; then
    echo "❌ Not connected to any peer"
    return 1
  fi

  local source="$1"
  local dest="${2:-$(basename "$source")}"
  local fullpath

  if [[ "${source:0:1}" == "/" ]]; then
    fullpath="$source"
  elif [[ "$CURRENT_PATH" == "/" ]]; then
    fullpath="/$source"
  else
    fullpath="$CURRENT_PATH/$source"
  fi

  echo "📥 Downloading: $fullpath → $dest"

  local result
  result=$("$CLIENT_SCRIPT" "$CURRENT_HOST" "$CURRENT_PORT" DOWNLOAD "$fullpath" "$dest" 2>&1 || true)

  if grep -q "^OK" <<< "$result"; then
    echo "✅ File downloaded successfully"
  else
    echo "❌ $result"
    return 1
  fi
}

delete_file() {
  if [[ -z "$CURRENT_HOST" ]]; then
    echo "❌ Not connected to any peer"
    return 1
  fi

  local target="$1"
  local fullpath

  if [[ "${target:0:1}" == "/" ]]; then
    fullpath="$target"
  elif [[ "$CURRENT_PATH" == "/" ]]; then
    fullpath="/$target"
  else
    fullpath="$CURRENT_PATH/$target"
  fi

  read -p "⚠️  Delete $fullpath? [y/N]: " confirm

  if [[ ! "$confirm" =~ ^[Yy]$ ]]; then
    echo "❌ Cancelled"
    return 0
  fi

  echo "🗑️  Deleting: $fullpath"

  local result
  result=$("$CLIENT_SCRIPT" "$CURRENT_HOST" "$CURRENT_PORT" DELETE "$fullpath" 2>&1 || true)

  if grep -q "^OK" <<< "$result"; then
    echo "✅ Deleted successfully"
  else
    echo "❌ $result"
    return 1
  fi
}

show_help() {
  cat << 'EOF'
Available commands:
───────────────────────────────────────────────────────
  peers                      - List all discovered peers
  connect                    - Connect to a peer from list
  manual <name> <host> <port> - Manually connect to a peer
  disconnect                 - Disconnect from current peer

  ls [path]                  - List files in directory
  cd <directory>             - Change directory (.. for parent, / for root)
  pwd                        - Print current directory

  mkdir <name>               - Create directory
  upload <local> [remote]    - Upload file
  download <remote> [local]  - Download file
  delete <name>              - Delete file or directory

  help                       - Show this help
  clear                      - Clear screen
  exit, quit                 - Exit client
───────────────────────────────────────────────────────
EOF
}

main() {
  load_config
  print_header

  if [[ ! -x "$CLIENT_SCRIPT" ]]; then
    echo "❌ Client script not found: $CLIENT_SCRIPT"
    exit 1
  fi

  show_help
  echo ""

  while true; do
    print_status
    read -p "p2p> " -a cmd

    [[ ${#cmd[@]} -eq 0 ]] && continue

    case "${cmd[0]}" in
      peers)
        list_peers
        ;;
      connect)
        connect_peer
        ;;
      manual)
        if [[ ${#cmd[@]} -ge 4 ]]; then
          CURRENT_PEER="${cmd[1]}"
          CURRENT_HOST="${cmd[2]}"
          CURRENT_PORT="${cmd[3]}"
          CURRENT_PATH="/"
          echo "✅ Connected to $CURRENT_PEER ($CURRENT_HOST:$CURRENT_PORT)"
        else
          connect_manual
        fi
        ;;
      disconnect)
        CURRENT_PEER=""
        CURRENT_HOST=""
        CURRENT_PORT=""
        CURRENT_PATH="/"
        echo "✅ Disconnected"
        ;;
      ls|list)
        list_files "${cmd[1]:-}"
        ;;
      cd)
        if [[ ${#cmd[@]} -lt 2 ]]; then
          echo "Usage: cd <directory>"
        else
          change_directory "${cmd[1]}"
        fi
        ;;
      pwd)
        echo "$CURRENT_PATH"
        ;;
      mkdir)
        if [[ ${#cmd[@]} -lt 2 ]]; then
          echo "Usage: mkdir <directory_name>"
        else
          create_directory "${cmd[1]}"
        fi
        ;;
      upload)
        if [[ ${#cmd[@]} -lt 2 ]]; then
          echo "Usage: upload <local_file> [remote_name]"
        else
          upload_file "${cmd[1]}" "${cmd[2]:-}"
        fi
        ;;
      download)
        if [[ ${#cmd[@]} -lt 2 ]]; then
          echo "Usage: download <remote_file> [local_name]"
        else
          download_file "${cmd[1]}" "${cmd[2]:-}"
        fi
        ;;
      delete|rm)
        if [[ ${#cmd[@]} -lt 2 ]]; then
          echo "Usage: delete <file_or_directory>"
        else
          delete_file "${cmd[1]}"
        fi
        ;;
      help|h|\?)
        show_help
        ;;
      clear|cls)
        clear
        print_header
        ;;
      exit|quit|q)
        echo ""
        echo "👋 Goodbye!"
        exit 0
        ;;
      "")
        ;;
      *)
        echo "❌ Unknown command: ${cmd[0]}"
        echo "Type 'help' for available commands"
        ;;
    esac
  done
}

main "$@"
