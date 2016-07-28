ENV_FILE=.env
if [ ! -f $ENV_FILE ]; then
   echo "Copy the .env-template to .env, and edit .env file for your configuration."
   exit 
fi

source $ENV_FILE

echo "SBT_OPTS=$JAVA_OPTIONS"

# generaterawtransactions private-key split-output-count transaction-group-count transaction-count-per-group
SBT_OPTS="$JAVA_OPTIONS" sbt "project scalechain-cli" "run-main io.scalechain.blockchain.cli.command.CommandExecutor generaterawtransactions 92oNzCxNqAub3PReRavU7pDYwfQibmg4aoN5q2dB8rC9vMQEdhT 2 2 10000"
