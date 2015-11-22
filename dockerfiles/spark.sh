case "$1" in
        build)
                ./build.sh spark
                ;;
        remove)
                ./remove.sh spark:latest
                ;;
        start)
                docker run -d --name spark
                ;;
        shell)
                docker run -it -p 8088:8088 -p 8042:8042 --name spark bash
                ;;
        stop)
                ./stop.sh spark
                ;;
        *)
                echo $"Usage: $0 {build|remove|start|shell|stop}"
                exit 1
esac
