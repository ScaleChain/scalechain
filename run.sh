ENV_FILE=.env
if [ ! -f $ENV_FILE ]; then
   echo "Copy the .env-template to .env, and edit .env file for your configuration."
   exit 
fi

source $ENV_FILE

P2P_PORT=$[$1+$P2P_PORT_BASE]
RPC_PORT=$[$1+$RPC_PORT_BASE]
CASSANDRA_IP_LAST=$[$1+$CASSANDRA_IP_LAST_BASE]
CASSANDRA_IP="192.168.99.${CASSANDRA_IP_LAST}"

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

SBT_OPTS="$JAVA_OPTIONS" sbt "project scalechain-cli" "run-main io.scalechain.blockchain.cli.ScaleChainPeer -p $P2P_PORT -c $RPC_PORT --minerInitialDelayMS $MINER_INITIAL_DELAY_MS --minerHashDelayMS $MINER_HASH_DELAY_MS --cassandraAddress $CASSANDRA_IP" | tee $LOG_FILE | grep "Exception" | tee $EXCEPTION_FILE 2>&1

