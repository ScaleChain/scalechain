pushd .
cd ../..
sbt "project scalechain-api" "~run-main io.scalechain.blockchain.api.JsonRpcMicroservice"
popd
