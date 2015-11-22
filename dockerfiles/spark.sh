case "$1" in
        build)
                ./build.sh spark
                ;;
        remove)
                ./remove.sh spark:latest
                ;;
        start)
                ./start.sh spark 8855
                ;;
        shell)
                ./shell.sh spark
                ;;
        stop)
                ./stop.sh spark
                ;;
        *)
                echo $"Usage: $0 {build|remove|start|shell|stop}"
                exit 1
esac
