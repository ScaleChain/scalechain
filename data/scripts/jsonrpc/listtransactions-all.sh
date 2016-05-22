curl --verbose --data-binary '{"jsonrpc": "1.0", "id":1, "method": "listtransactions", "params": ["_FOR_TEST_ONLY", 100, 0, true] }' -H 'Content-Type: application/json' http://127.0.0.1:8080/ 
