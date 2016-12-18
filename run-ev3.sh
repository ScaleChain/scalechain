ENV_FILE=./.env
if [ ! -f $ENV_FILE ]; then
   echo "Copy the .env-template to .env, and edit .env file for your configuration."
   exit 
fi

source $ENV_FILE

P2P_PORT=$P2P_PORT_BASE
RPC_PORT=$RPC_PORT_BASE

echo "Deleting all test files."
rm -rf target/*-$P2P_PORT

echo "Using P2P port $P2P_PORT, RPC port $RPC_PORT"
echo "SBT_OPTS=$JAVA_OPTIONS"
echo "MINER_INITIAL_DELAY_MS=$MINER_INITIAL_DELAY_MS"
echo "MINER_HASH_DELAY_MS=$MINER_HASH_DELAY_MS"

LOG_FILE="target/sc$1.log"
EXCEPTION_FILE="target/ex$1.log"

rm $LOG_FILE
rm $EXCEPTION_FILE
rm config/currentView 

java -cp bin/scalechain-cli.jar io.scalechain.blockchain.cli.ScaleChainPeer -p $P2P_PORT -c $RPC_PORT --minerInitialDelayMS $MINER_INITIAL_DELAY_MS --minerHashDelayMS $MINER_HASH_DELAY_MS | tee $LOG_FILE 

