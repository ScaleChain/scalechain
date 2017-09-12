ENV_FILE=.env
if [ ! -f $ENV_FILE ]; then
   echo "Copy the .env-template to .env, and edit .env file for your configuration."
   exit 
fi

source $ENV_FILE

P2P_PORT=$[$1+$P2P_PORT_BASE]
RPC_PORT=$[$1+$RPC_PORT_BASE]

echo "Deleting all test files."
rm -rf build/*-$P2P_PORT

echo "Using P2P port $P2P_PORT, RPC port $RPC_PORT"

LOG_FILE="build/sc$1.log"
EXCEPTION_FILE="build/ex$1.log"

rm $LOG_FILE
rm $EXCEPTION_FILE

#java -classpath /Users/kangmo/crypto/scalechain/scalechain-cli/build/libs/scalechain-cli.jar io.scalechain.blockchain.cli.ScaleChainPeer -p $P2P_PORT -c $RPC_PORT  | tee $LOG_FILE
#java $JAVA_OPTIONS -classpath ./tmp io.scalechain.blockchain.cli.ScaleChainPeer -p $P2P_PORT -c $RPC_PORT  | tee $LOG_FILE

gradle run -PappArgs="['-p', $P2P_PORT, '-c', $RPC_PORT]" -PrunWorkingDir=`pwd` | tee $LOG_FILE
