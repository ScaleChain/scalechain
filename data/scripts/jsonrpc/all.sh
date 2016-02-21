for command in getbestblockhash getblock getblockhash help submitblock getpeerinfo decoderawtransaction getrawtransaction sendrawtransaction signrawtransaction getaccount getaccountaddress getnewaddress getreceivedbyaddress listtransactions listunspent sendfrom 
do
   echo ""
   echo "========================================================================"
   echo $command 
   echo "========================================================================"

   ./${command}.sh
done
