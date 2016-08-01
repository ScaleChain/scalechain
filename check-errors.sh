grep "Exception\|AssertionError\|NodeServerHandler\|NodeClientHandler\|ERROR\|WANR" target/sc*.log | grep -v "Connection accepted from" | grep -v "Failed to connect"
