#!/usr/bin/env bash 
set -e
set -o pipefail
echo "installing Helm..."

curl -fsSL -o get_helm.sh https://raw.githubusercontent.com/helm/helm/master/scripts/get-helm-3
chmod 700 get_helm.sh
./get_helm.sh

kubectl config current-context

helm uninstall my-release

if [[ $(helm list | tail -n +2 | grep spring-cloud-dataflow | awk '{print $8}') != "deployed" ]]; then
  echo "installing Spring Cloud Dataflow..."
  helm repo add bitnami https://charts.bitnami.com/bitnami
  helm install --set server.service.type=LoadBalancer my-release bitnami/spring-cloud-dataflow
else
  echo $(kubectl get services --namespace default my-release-spring-cloud-dataflow-server -o yaml)
fi

exit 0

echo "building and pushing app image ..."

export PROJECT_ID=${GCLOUD_PROJECT:-pgtm-jlong}
export ROOT_DIR=$(cd $(dirname $0) && pwd)

cd $ROOT_DIR
cd ../..
pwd

export APP_NAME=points-calculator-sink
export GCR_IMAGE_NAME=gcr.io/${PROJECT_ID}/${APP_NAME}
#docker rmi $(docker images -a -q)
mvn clean package spring-boot:build-image

image_id=$(docker images -q $APP_NAME)

echo "tagging ${GCR_IMAGE_NAME}"
docker tag "${image_id}" $GCR_IMAGE_NAME

echo "pushing ${image_id} to $GCR_IMAGE_NAME "
docker push $GCR_IMAGE_NAME
