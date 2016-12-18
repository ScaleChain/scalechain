rm -rf ../scalechain-package
mkdir ../scalechain-package
mkdir ../scalechain-package/target
gradle clean build
cp -r config ../scalechain-package
cp -r config.four-nodes ../scalechain-package/config.four-nodes
cp -r bin ../scalechain-package
cp ./scalechain-cli/build/libs/scalechain-cli.jar ../scalechain-package/bin
cp run-assembly.sh ../scalechain-package
cp run-ev3.sh ../scalechain-package
cp .env ../scalechain-package
cp README-INSTALL.md ../scalechain-package
pushd .
cd ..
tar cvfz scalechain-package.tar.gz scalechain-package
popd  
