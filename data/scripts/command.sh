pushd .
cd ../..
sbt "project scalechain-cli" "run-main io.scalechain.blockchain.cli.api.CommandExecutor $1 $2 $3 $4 $5 $6 $7 $8 $9"
popd
