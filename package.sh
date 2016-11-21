rm -rf ../scalechain-package
mkdir ../scalechain-package
mkdir ../scalechain-package/target
sbt clean assembly
cp -r config ../scalechain-package
cp -r bin ../scalechain-package
cp scalechain-cli/target/scala-2.12/scalechain-cli-assembly-0.7.jar ../scalechain-package/bin
cp run-assembly.sh ../scalechain-package
cp .env ../scalechain-package
cp README-INSTALL.md ../scalechain-package
pushd .
cd ..
tar cvfz scalechain-package.tar.gz scalechain-package
popd  
