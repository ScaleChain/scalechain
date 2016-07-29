ENV_FILE=.env
if [ ! -f $ENV_FILE ]; then
   echo "Copy the .env-template to .env, and edit .env file for your configuration."
   exit 
fi

source $ENV_FILE

echo "SBT_OPTS=$JAVA_OPTIONS"

# multithreadtestlayers transaction-group-count 
SBT_OPTS="$JAVA_OPTIONS" sbt "project scalechain-cli" "run-main io.scalechain.blockchain.cli.command.CommandExecutor multithreadtestlayers 40"
