case "$1" in
        build)
                ./build.sh spark
                ;;
        remove)
                ./remove.sh spark:latest
                ;;
        start)
                docker run --name spark -d -p 8088:8088 -p 8042:8042 spark
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
