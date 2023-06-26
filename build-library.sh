rm ./build/library/*.aar

./gradlew :live-location:assemble
./gradlew :live-location:assembleDebug

mkdir build
mkdir build/library

mv ./live-location/build/outputs/aar/* ./build/library/

clear

echo "Build Finished"
echo "Your library is in folder build/library/"
