#!/usr/bin/env bash
set -euo pipefail

GO_VERSION="${GO_VERSION:-1.23.6}"
GO_TAR="go${GO_VERSION}.linux-amd64.tar.gz"

wget -q "https://go.dev/dl/${GO_TAR}"
sudo rm -rf /usr/local/go
sudo tar -C /usr/local -xzf "${GO_TAR}"
rm -f "${GO_TAR}"
/usr/local/go/bin/go version
