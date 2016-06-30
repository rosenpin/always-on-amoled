sedir=$(git rev-parse --show-toplevel)
apikey=$(tr -d ' \r\n' < "/home/tomer/Projects/Android/AlwaysOnDisplayAmoled/scripts/crowdin.key")


if [ -z "/home/tomer/Projects/Android/AlwaysOnDisplayAmoled" -o -z "$apikey" ]; then
    echo 'API key missing'
    exit 1
fi

if [ -n "$(git status --porcelain /home/tomer/Projects/Android/AlwaysOnDisplayAmoled/app/src/main/res)" ]; then
    echo 'Outstanding changes:'
    git status --short "/home/tomer/Projects/Android/AlwaysOnDisplayAmoled/app/src/main/res"
    exit 1
fi

response=$(curl -sS "https://api.crowdin.com/api/project/always-on-amoled/export?key=$apikey" | grep '<success')
echo $response

if [ -n "$response" ]; then
    tempfile=$(mktemp)
    wget -qO "$tempfile" "https://api.crowdin.com/api/project/always-on-amoled/download/all.zip?key=$apikey"
    unzip -oqd "$basedir/$location" "$tempfile"
    rm "$tempfile"
    git --no-pager diff --stat --no-ext-diff "$basedir/$location"
fi
