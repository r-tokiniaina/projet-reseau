#!/usr/bin/env bash
set -euo pipefail

APP_CONF="${APP_CONF:-app.conf}"
RUNTIME_DIR="${P2P_RUNTIME_DIR:-.p2p_runtime}"
PEERS_FILE="$RUNTIME_DIR/udp_peers.txt"
LISTENER_PID_FILE="$RUNTIME_DIR/udp_listener.pid"
BROADCAST_PID_FILE="$RUNTIME_DIR/udp_broadcast.pid"
TTL_SECONDS=12

load_config() {
  if [[ ! -f "$APP_CONF" ]]; then
    echo "Missing config: $APP_CONF" >&2
    exit 1
  fi
  local key value
  while IFS='=' read -r key value; do
    [[ -z "${key:-}" ]] && continue
    [[ "${key:0:1}" == "#" ]] && continue
    case "$key" in
      udp_port) UDP_PORT="$value" ;;
      tcp_port) TCP_PORT="$value" ;;
      pc_name) PC_NAME="$value" ;;
    esac
  done < "$APP_CONF"
  UDP_PORT="${UDP_PORT:-8888}"
  TCP_PORT="${TCP_PORT:-5000}"
  PC_NAME="${PC_NAME:-$(hostname)}"
}

local_ip() {
  ip route get 1.1.1.1 2>/dev/null | awk '{for(i=1;i<=NF;i++) if($i=="src") {print $(i+1); exit}}'
}

cleanup_peers() {
  local now
  now="$(date +%s)"
  [[ -f "$PEERS_FILE" ]] || return 0
  awk -F'|' -v now="$now" -v ttl="$TTL_SECONDS" 'NF==4 && (now-$4)<=ttl {print $0}' "$PEERS_FILE" > "$PEERS_FILE.tmp" || true
  mv "$PEERS_FILE.tmp" "$PEERS_FILE"
}

record_peer() {
  local name="$1" ip_addr="$2" tcp="$3"
  local now
  now="$(date +%s)"
  mkdir -p "$RUNTIME_DIR"
  touch "$PEERS_FILE"
  awk -F'|' -v n="$name" -v ip="$ip_addr" '!(NF>=2 && $2==ip && $1==n)' "$PEERS_FILE" > "$PEERS_FILE.tmp" || true
  printf '%s|%s|%s|%s\n' "$name" "$ip_addr" "$tcp" "$now" >> "$PEERS_FILE.tmp"
  mv "$PEERS_FILE.tmp" "$PEERS_FILE"
}

broadcast_loop() {
  local ip_addr
  ip_addr="$(local_ip)"
  while true; do
    local msg="ANNOUNCE|$PC_NAME|$TCP_PORT|$ip_addr"
    printf '%s\n' "$msg" | nc -u -b -w 1 255.255.255.255 "$UDP_PORT" >/dev/null 2>&1 || true
    sleep 3
  done
}

listen_loop() {
  local self_ip
  self_ip="$(local_ip)"
  mkdir -p "$RUNTIME_DIR"
  touch "$PEERS_FILE"
  while true; do
    local msg
    msg="$(nc -u -l -p "$UDP_PORT" -w 2 2>/dev/null || true)"
    [[ -z "$msg" ]] && { cleanup_peers; continue; }

    IFS='|' read -r kind name tcp ip_addr <<< "$msg"
    if [[ "$kind" == "ANNOUNCE" && -n "${ip_addr:-}" && -n "${tcp:-}" ]]; then
      if [[ "$ip_addr" != "$self_ip" && "$name" != "$PC_NAME" ]]; then
        record_peer "$name" "$ip_addr" "$tcp"
      fi
    fi
    cleanup_peers
  done
}

start() {
  mkdir -p "$RUNTIME_DIR"
  if [[ -f "$LISTENER_PID_FILE" ]] && kill -0 "$(cat "$LISTENER_PID_FILE")" 2>/dev/null; then
    echo "already_running"
    exit 0
  fi
  (listen_loop) &
  echo $! > "$LISTENER_PID_FILE"
  (broadcast_loop) &
  echo $! > "$BROADCAST_PID_FILE"
  echo "started"
}

stop() {
  if [[ -f "$LISTENER_PID_FILE" ]]; then
    kill "$(cat "$LISTENER_PID_FILE")" 2>/dev/null || true
    rm -f "$LISTENER_PID_FILE"
  fi
  if [[ -f "$BROADCAST_PID_FILE" ]]; then
    kill "$(cat "$BROADCAST_PID_FILE")" 2>/dev/null || true
    rm -f "$BROADCAST_PID_FILE"
  fi
  echo "stopped"
}

peers() {
  cleanup_peers
  [[ -f "$PEERS_FILE" ]] || exit 0
  awk -F'|' 'NF>=3 {print $1"|"$2"|"$3}' "$PEERS_FILE"
}

load_config
case "${1:-}" in
  start) start ;;
  stop) stop ;;
  peers) peers ;;
  *) echo "Usage: $0 {start|stop|peers}" >&2; exit 1 ;;
esac
