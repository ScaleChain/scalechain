pushd .
cd ../..
sbt "project scalechain-cli" "run-main io.scalechain.blockchain.cli.DumpChain $1 $2 $3"
popd
