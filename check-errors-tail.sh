tail -f target/sc*.log | grep "Exception\|AssertionError\|NodeServerHandler\|NodeClientHandler\|ERROR\|WANR" | grep -v "Connection accepted from" | grep -v "Failed to connect"
