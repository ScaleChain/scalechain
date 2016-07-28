ENV_FILE=.env
if [ ! -f $ENV_FILE ]; then
   echo "Copy the .env-template to .env, and edit .env file for your configuration."
   exit 
fi

source $ENV_FILE

echo "SBT_OPTS=$JAVA_OPTIONS"

# generaterawtransactions private-key split-output-count transaction-group-count transaction-count-per-group
SBT_OPTS="$JAVA_OPTIONS" sbt "project scalechain-cli" "run-main io.scalechain.blockchain.cli.command.CommandExecutor generaterawtransactions 93DhXtxTX1r5wH1iVskskeQSVVH4JRMW14wR9aoFcDwczRtyhc9 2 1 10"
