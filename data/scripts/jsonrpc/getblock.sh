curl --data-binary "{\"jsonrpc\": \"1.0\", \"id\":1, \"method\": \"getblock\", \"params\": [\"$1\", true] }" -H 'Content-Type: application/json' http://127.0.0.1:$PORT/
