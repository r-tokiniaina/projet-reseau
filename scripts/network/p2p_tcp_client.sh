#!/usr/bin/env bash
set -euo pipefail

if [[ $# -lt 4 ]]; then
  echo "Usage: $0 <host> <port> <command> <path> [extra]" >&2
  exit 1
fi

HOST="$1"
PORT="$2"
COMMAND="$3"
PATH_ARG="$4"

run_list() {
  printf 'LIST\n%s\n' "$PATH_ARG" | nc "$HOST" "$PORT" -w 10
}

run_create() {
  printf 'CREATE\n%s\n' "$PATH_ARG" | nc "$HOST" "$PORT" -w 10
}

run_delete() {
  printf 'DELETE\n%s\n' "$PATH_ARG" | nc "$HOST" "$PORT" -w 10
}

run_upload() {
  local source="${5:-}"
  if [[ -z "$source" || ! -f "$source" ]]; then
    echo "ERROR source_not_found"
    exit 1
  fi
  {
    printf 'UPLOAD\n%s\n' "$PATH_ARG"
    cat "$source"
  } | nc "$HOST" "$PORT" -w 20
}

run_download() {
  local destination="${5:-}"
  if [[ -z "$destination" ]]; then
    echo "ERROR destination_required"
    exit 1
  fi

  mkdir -p "$(dirname "$destination")"

  exec 3< <(printf 'DOWNLOAD\n%s\n' "$PATH_ARG" | nc "$HOST" "$PORT" -w 20)
  local header
  IFS= read -r header <&3 || true

  if [[ "$header" != "OK" ]]; then
    echo "$header"
    return 1
  fi

  cat <&3 > "$destination"
  echo "OK|$destination"
}

case "$COMMAND" in
  LIST) run_list ;;
  CREATE) run_create ;;
  DELETE) run_delete ;;
  UPLOAD) run_upload "${5:-}" ;;
  DOWNLOAD) run_download "${5:-}" ;;
  *) echo "ERROR unknown_command" >&2; exit 1 ;;
esac
