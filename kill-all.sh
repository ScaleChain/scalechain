ps -ef | grep java | grep -v "grep java" | awk -F ' ' '{print $2}' | xargs kill -9
