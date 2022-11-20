ENV_FILE=.env
if [ ! -f $ENV_FILE ]; then
   echo "Copy the .env-template to .env, and edit .env file for your configuration."
   exit 
fi

source $ENV_FILE

P2P_PORT=$P2P_PORT_BASE
RPC_PORT=$RPC_PORT_BASE

echo "Using P2P port $P2P_PORT, RPC port $RPC_PORT"

LOG_FILE="build/exec-output-$P2P_PORT.log"
EXCEPTION_FILE="build/exec-error-$P2P_PORT.log"

rm $LOG_FILE
rm $EXCEPTION_FILE

SCALECHAIN_HOME=`pwd`
gradle runExec -Drun.args="-homedir $SCALECHAIN_HOME  -rpcport $RPC_PORT -rpcuser kmkim -rpcpassword 1234 $1 $2 $3 $4 $5 $6 $7 $8 $9" | tee $LOG_FILE
