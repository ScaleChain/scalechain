curl --verbose --data-binary "{\"jsonrpc\": \"1.0\", \"id\":1, \"method\": \"listunspent\", \"params\": [0, 99999999, [\"$1\"] ] }" -H 'Content-Type: application/json' http://127.0.0.1:$PORT/
