#!/usr/bin/env bash
set -euo pipefail

APP_CONF="${APP_CONF:-app.conf}"
RUNTIME_DIR="${P2P_RUNTIME_DIR:-.p2p_runtime}"
PID_FILE="$RUNTIME_DIR/p2p_tcp_server.pid"

load_config() {
  if [[ ! -f "$APP_CONF" ]]; then
    echo "Missing config: $APP_CONF" >&2
    exit 1
  fi
  local line key value
  while IFS='=' read -r key value; do
    [[ -z "${key:-}" ]] && continue
    [[ "${key:0:1}" == "#" ]] && continue
    case "$key" in
      tcp_port) TCP_PORT="$value" ;;
      upload_dir) UPLOAD_DIR="$value" ;;
    esac
  done < "$APP_CONF"

  TCP_PORT="${TCP_PORT:-5000}"
  UPLOAD_DIR="${UPLOAD_DIR:-$HOME/p2p_shared}"
  mkdir -p "$UPLOAD_DIR"
}

resolve_path() {
  local raw="$1"
  local trimmed="${raw#/}"
  local target="$UPLOAD_DIR/$trimmed"
  printf '%s' "$target"
}

handle_request() {
  local req_fifo="$1"
  local resp_fifo="$2"

  local cmd path abs target_dir
  IFS= read -r cmd < "$req_fifo" || true
  case "$cmd" in
    LIST)
      IFS= read -r path < "$req_fifo" || path="/"
      if ! abs="$(resolve_path "$path")"; then
        printf 'ERROR invalid_path\nEND\n' > "$resp_fifo"
        return
      fi
      if [[ ! -d "$abs" ]]; then
        printf 'ERROR not_directory\nEND\n' > "$resp_fifo"
        return
      fi
      {
        printf 'OK\n'
        while IFS= read -r entry; do
          [[ -z "$entry" ]] && continue
          if [[ -d "$abs/$entry" ]]; then
            printf 'ENTRY|DIRECTORY|%s\n' "$entry"
          else
            printf 'ENTRY|FILE|%s\n' "$entry"
          fi
        done < <(ls -1A "$abs" 2>/dev/null || true)
        printf 'END\n'
      } > "$resp_fifo"
      ;;
    CREATE)
      IFS= read -r path < "$req_fifo" || true
      if ! abs="$(resolve_path "$path")"; then
        printf 'ERROR invalid_path\n' > "$resp_fifo"
        return
      fi
      mkdir -p "$abs"
      printf 'OK\n' > "$resp_fifo"
      ;;
    DELETE)
      IFS= read -r path < "$req_fifo" || true
      if ! abs="$(resolve_path "$path")"; then
        printf 'ERROR invalid_path\n' > "$resp_fifo"
        return
      fi
      if [[ -e "$abs" ]]; then
        rm -rf "$abs"
      fi
      printf 'OK\n' > "$resp_fifo"
      ;;
    UPLOAD)
      IFS= read -r path < "$req_fifo" || true
      if ! abs="$(resolve_path "$path")"; then
        printf 'ERROR invalid_path\n' > "$resp_fifo"
        return
      fi
      target_dir="$(dirname "$abs")"
      mkdir -p "$target_dir"
      cat < "$req_fifo" > "$abs"
      printf 'OK\n' > "$resp_fifo"
      ;;
    DOWNLOAD)
      IFS= read -r path < "$req_fifo" || true
      if ! abs="$(resolve_path "$path")"; then
        printf 'ERROR invalid_path\n' > "$resp_fifo"
        return
      fi
      if [[ ! -f "$abs" ]]; then
        printf 'ERROR not_found\n' > "$resp_fifo"
        return
      fi
      {
        printf 'OK\n'
        cat "$abs"
      } > "$resp_fifo"
      ;;
    *)
      printf 'ERROR unknown_command\n' > "$resp_fifo"
      ;;
  esac
}

run_server() {
  mkdir -p "$RUNTIME_DIR"
  while true; do
    local req_fifo="$RUNTIME_DIR/tcp_req.$$"
    local resp_fifo="$RUNTIME_DIR/tcp_resp.$$"
    rm -f "$req_fifo" "$resp_fifo"
    mkfifo "$req_fifo" "$resp_fifo"

    nc -l -p "$TCP_PORT" < "$resp_fifo" > "$req_fifo" 2>/dev/null &
    local nc_pid=$!

    handle_request "$req_fifo" "$resp_fifo" || true

    wait "$nc_pid" 2>/dev/null || true
    rm -f "$req_fifo" "$resp_fifo"
  done
}

start_server() {
  mkdir -p "$RUNTIME_DIR"
  if [[ -f "$PID_FILE" ]] && kill -0 "$(cat "$PID_FILE")" 2>/dev/null; then
    echo "already_running"
    exit 0
  fi
  (run_server) &
  echo $! > "$PID_FILE"
  echo "started"
}

stop_server() {
  if [[ -f "$PID_FILE" ]]; then
    local pid
    pid="$(cat "$PID_FILE")"
    kill "$pid" 2>/dev/null || true
    rm -f "$PID_FILE"
  fi
  echo "stopped"
}

load_config
case "${1:-run}" in
  start) start_server ;;
  stop) stop_server ;;
  run) run_server ;;
  *) echo "Usage: $0 {start|stop|run}" >&2; exit 1 ;;
esac
