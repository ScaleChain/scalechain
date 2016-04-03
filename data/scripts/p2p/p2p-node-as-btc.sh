pushd .
cd ../../..
# connect to Bitcoin core installed on the local machine. 
# The address is localhost:8333
sbt -mem 4096 "project scalechain-cli" "run-main io.scalechain.blockchain.cli.ScaleChainPeer -a localhost -x 8333"
popd
