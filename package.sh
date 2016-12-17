rm -rf ../scalechain-package
mkdir ../scalechain-package
mkdir ../scalechain-package/target
gradle clean build
cp -r config ../scalechain-package
cp -r scalechain-cli/unittest/config ../scalechain-package/config.one-node
cp -r bin ../scalechain-package
cp ./scalechain-cli/build/libs/scalechain-cli.jar ../scalechain-package/bin
cp run-assembly.sh ../scalechain-package
cp .env ../scalechain-package
cp README-INSTALL.md ../scalechain-package
pushd .
cd ..
tar cvfz scalechain-package.tar.gz scalechain-package
popd  
