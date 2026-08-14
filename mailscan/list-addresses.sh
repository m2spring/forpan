#!/usr/bin/env bash
# Lists the distinct To/Cc email addresses found in one or more mbox files.
# Usage: ./list-addresses.sh <mailbox-file> [more-mailbox-files...]
set -euo pipefail

if [ "$#" -lt 1 ]; then
  echo "usage: $(basename "$0") <mailbox-file> [more-mailbox-files...]" >&2
  exit 1
fi

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

joined=$(IFS=,; echo "$*")

mvn -q -f "$SCRIPT_DIR/pom.xml" test -Dtest=ListMailboxAddressesTest -DfailIfNoTests=false -Dmailbox.paths="$joined"
