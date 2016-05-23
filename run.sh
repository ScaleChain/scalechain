source .env

P2P_PORT=$[$1+$P2P_PORT_BASE]
RPC_PORT=$[$1+$RPC_PORT_BASE]


echo "Deleting all test files."
rm -rf target/*-$P2P_PORT

echo "Using P2P port $P2P_PORT, RPC port $RPC_PORT"
echo "JAVA_OPTS=$JAVA_OPTIONS"
echo "MINER_INITIAL_DELAY_MS=$MINER_INITIAL_DELAY_MS"
echo "MINER_HASH_DELAY_MS=$MINER_HASH_DELAY_MS"

JAVA_OPTS=$JAVA_OPTIONS sbt "project scalechain-cli" "run-main io.scalechain.blockchain.cli.ScaleChainPeer -p $P2P_PORT -c $RPC_PORT --minerInitialDelayMS $MINER_INITIAL_DELAY_MS --minerHashDelayMS $MINER_HASH_DELAY_MS "

