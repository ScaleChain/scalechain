curl --verbose --data-binary '{"jsonrpc": "1.0", "id":1, "method": "listtransactions", "params": ["someone else address2", 1, 0, true] }' -H 'Content-Type: application/json' http://127.0.0.1:$PORT/
