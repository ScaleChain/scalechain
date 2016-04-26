pushd .
cd ../../..
# connect to Bitcoin core installed on the local machine. 
# The address is localhost:8333
sbt "project scalechain-cli" "run-main io.scalechain.blockchain.cli.ScaleChainPeer -a localhost -x 18333"
popd
