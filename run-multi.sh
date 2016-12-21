sbt compile
rm target/run-multi.log
rm target/sc*.log

./run.sh 0 &

./run.sh 1 &

./run.sh 2 &

./run.sh 3 &

while [ `grep "Height : " target/sc0.log | wc -l` -lt 1 ]
do
   echo "Waiting for the first block to be mined"
   sleep 1
done

echo "Starting test process" 

./run-multithreadtests-rpc.sh &

read -p "Press any key to stop all processes... " -n1 -s

./kill-all.sh

