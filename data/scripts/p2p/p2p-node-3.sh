pushd .
cd ../../..
sbt "project scalechain-cli" "run-main io.scalechain.blockchain.cli.ScaleChainPeer -p 7645 -c 8082"
popd
