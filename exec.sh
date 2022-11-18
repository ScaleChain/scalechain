ENV_FILE=.env
if [ ! -f $ENV_FILE ]; then
   echo "Copy the .env-template to .env, and edit .env file for your configuration."
   exit 
fi

source $ENV_FILE

P2P_PORT=$[$1+$P2P_PORT_BASE]
RPC_PORT=$[$1+$RPC_PORT_BASE]

echo "Using P2P port $P2P_PORT, RPC port $RPC_PORT"

LOG_FILE="build/exec-output$1.log"
EXCEPTION_FILE="build/exec-error$1.log"

rm $LOG_FILE
rm $EXCEPTION_FILE

SCALECHAIN_HOME=`pwd`
gradle runExec -Drun.args="-d $SCALECHAIN_HOME  -p $RPC_PORT -u kmkim -w 1234 multithreadtestrpc 5 10 x" | tee $LOG_FILE
