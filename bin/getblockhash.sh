curl --data-binary "{\"jsonrpc\": \"1.0\", \"id\":1, \"method\": \"getblockhash\", \"params\": [$1] }" -H 'Content-Type: application/json' http://127.0.0.1:$PORT/
