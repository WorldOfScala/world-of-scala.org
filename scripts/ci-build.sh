#!/usr/bin/env bash
set -e
# Import the project environment variables
. ./scripts/env.sh

./scripts/setup.sh

MOD=Docker sbt -mem 4096 "server/compile"
cd modules/client

npm run build

