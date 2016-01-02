pushd .
cd ../..
sbt "run-main io.scalechain.blockchain.DumpChain $1 $2 $3"
popd
