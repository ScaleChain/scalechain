ENV_FILE=.env
if [ ! -f $ENV_FILE ]; then
   echo "Copy the .env-template to .env, and edit .env file for your configuration."
   exit 
fi

source $ENV_FILE

echo "SBT_OPTS=$JAVA_OPTIONS"

SBT_OPTS="$JAVA_OPTIONS" sbt "project scalechain-wallet" "test-only io.scalechain.wallet.WalletPerformanceSpec"
