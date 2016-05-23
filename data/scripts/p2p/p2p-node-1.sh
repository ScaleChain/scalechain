pushd .
cd ../../..
sbt "project scalechain-cli" "run-main io.scalechain.blockchain.cli.ScaleChainPeer -p 7643 -c 8080"
popd
