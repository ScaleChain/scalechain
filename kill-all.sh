#ps -ef | grep ScaleChainPeer | grep -v "grep ScaleChainPeer" | awk -F ' ' '{print $2}' | xargs kill -9
ps -ef | grep java | grep -v "grep java" | awk -F ' ' '{print $2}' | xargs kill -9
