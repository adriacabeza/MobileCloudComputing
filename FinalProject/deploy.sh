(cd ./frontend && ./gradlew --info --stacktrace clean assembleRelease)
gcloud auth activate-service-account --key-file=./server/app/engine_account.json
gcloud config set project mcc-fall-2019-g10
(cd ./server/app && bash deploy_app.sh)
#(cd ./server/cloud_functions && bash deploy_cloud_functions.sh)
