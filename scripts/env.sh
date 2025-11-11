BUILD_ENV_FILE="./scripts/target/build-env.sh"
if [[ "$OSTYPE" == "darwin"* ]]; then
    # macOS
    get_mtime() { stat -f %m "$1"; }
else
    # Linux and others
    get_mtime() { stat -c %Y "$1"; }
fi
