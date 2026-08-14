#!/usr/bin/env bash
# Lists the distinct To/Cc email addresses found in one or more mbox files.
# Usage: ./list-addresses.sh [mailbox-file...]
# With no arguments, uses the accounts configured under config.properties' mailscan.accounts.
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

if [ "$#" -ge 1 ]; then
  joined=$(IFS=,; echo "$*")
  CONFIRM_LIST_ADDRESSES=1 mvn -q -f "$SCRIPT_DIR/pom.xml" test -Dtest=ListMailboxAddressesTest -DfailIfNoTests=false -Dmailbox.paths="$joined"
else
  CONFIRM_LIST_ADDRESSES=1 mvn -q -f "$SCRIPT_DIR/pom.xml" test -Dtest=ListMailboxAddressesTest -DfailIfNoTests=false
fi
