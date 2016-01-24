#url --verbose --data-binary '{"jsonrpc": "1.0", "id":"curltest", "method": "getinfo", "params": [] }' -H 'Content-Type: text/plain;' http://127.0.0.1:8080/ 
curl --verbose --data-binary '{"jsonrpc": "1.0", "id":"curltest", "method": "getinfo", "params": [] }' -H 'Content-Type: application/json' http://127.0.0.1:8080/ 
