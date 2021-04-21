#!/usr/bin/env bash 
set -e
set -o pipefail
echo "installing Helm..."

curl -fsSL -o get_helm.sh https://raw.githubusercontent.com/helm/helm/master/scripts/get-helm-3
chmod 700 get_helm.sh
./get_helm.sh

if [[ $(helm list | tail -n +2 | grep spring-cloud-dataflow | awk '{print $8}') != "deployed" ]]; then
  echo "installing Spring Cloud Dataflow..."
  helm repo add bitnami https://charts.bitnami.com/bitnami
  helm install --set server.service.type=LoadBalancer my-release bitnami/spring-cloud-dataflow
else
  kubectl get endpoints my-release-spring-cloud-dataflow-server
  kubectl get service my-release-spring-cloud-dataflow-server -o yaml
  export SERVICE_PORT=$(kubectl get --namespace default -o jsonpath="{..spec.ports[0].port}" services my-release-spring-cloud-dataflow-server)
  export SERVICE_IP=$(kubectl get svc --namespace default my-release-spring-cloud-dataflow-server -o jsonpath="{..status.loadBalancer.ingress[0].ip}")
  echo "SCDF dashboard: http://${SERVICE_IP}:${SERVICE_PORT}/dashboard"
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
