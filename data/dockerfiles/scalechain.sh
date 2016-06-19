if [ $# -ne 3 ] 
then
       Usage "$0 {build|remove}"
       Usage "$0 {remove|start|shell|stop} ID"
       Example> "$0 build"
       Example> "$0 start 1"
       Example> "$0 start 2"
       Example> "$0 start 3"
       Example> "$0 shell 2"
       Example> "$0 stop 1"
       Example> "$0 stop 2"
       Example> "$0 stop 3"
       Example> "$0 remove"
       exit -1
fi


ID=$2
CONT_NAME=scalechain-${ID}

case "$1" in
        build)
                ./build.sh scalechain
                ;;
        remove)
                ./remove.sh scalechain:latest
                ;;
        start)
                docker run --name $CONT_NAME -d -p 8080:8080 -p 7643:7643 scalechain
                ;;
        shell)
                ./shell.sh $CONT_NAME
                ;;
        stop)
                ./stop.sh $CONT_NAME
                ;;
        *)
                echo "Invalid command. Only build,remove,start,shell, and stop is supported"
                exit -1
esac
