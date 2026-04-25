#!/usr/bin/env bash
set -euo pipefail

GO_VERSION="${GO_VERSION:-1.23.6}"
GO_TAR="go${GO_VERSION}.linux-amd64.tar.gz"
PREFIX="${HOME}/.jitpack-golang"
TMP="${TMPDIR:-/tmp}/${GO_TAR}"

mkdir -p "${PREFIX}"

if command -v curl >/dev/null 2>&1; then
	curl -fsSL "https://go.dev/dl/${GO_TAR}" -o "${TMP}"
else
	wget -q "https://go.dev/dl/${GO_TAR}" -O "${TMP}"
fi

rm -rf "${PREFIX}/go"
tar -C "${PREFIX}" -xzf "${TMP}"
rm -f "${TMP}"

{
	echo "export GOROOT=\"${PREFIX}/go\""
	echo "export PATH=\"${PREFIX}/go/bin:\${PATH}\""
} >"${HOME}/.jitpack-go-env.sh"

"${PREFIX}/go/bin/go" version
