rm -rf ./tmp
gradle clean distZip
mkdir tmp
cd tmp
unzip ../scalechain-cli/build/distributions/scalechain-cli.zip 
cd ..

