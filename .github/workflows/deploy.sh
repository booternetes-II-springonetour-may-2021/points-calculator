#!/usr/bin/env bash 
set -e
set -o pipefail
echo "installing Helm..."

curl -fsSL -o get_helm.sh https://raw.githubusercontent.com/helm/helm/master/scripts/get-helm-3
chmod 700 get_helm.sh
./get_helm.sh

NAMESPACE=bk
HELM_ID=demo

if [[ $(helm list --namespace $NAMESPACE | tail -n +2 | grep spring-cloud-dataflow | awk '{print $8}') != "deployed" ]]; then
  echo "installing Spring Cloud Dataflow..."
  helm repo add bitnami https://charts.bitnami.com/bitnami
  helm install --set server.service.type=LoadBalancer $HELM_ID bitnami/spring-cloud-dataflow --namespace $NAMESPACE
else
  export SERVICE_PORT=$(kubectl get --namespace $NAMESPACE -o jsonpath="{..spec.ports[0].port}" services $HELM_ID-spring-cloud-dataflow-server)
  export SERVICE_IP=$(kubectl get svc --namespace $NAMESPACE $HELM_ID-spring-cloud-dataflow-server -o jsonpath="{..status.loadBalancer.ingress[0].ip}")
  echo "SCDF dashboard: http://${SERVICE_IP}:${SERVICE_PORT}/dashboard"
fi

echo "building and pushing app image ..."

export PROJECT_ID=${GCLOUD_PROJECT:-pgtm-jlong}
export ROOT_DIR=$(cd $(dirname $0) && pwd)

cd $ROOT_DIR
cd ../..
pwd

export APP_NAME=points-calculator-sink
export IMAGE_NAME=docker.io/dturanski/${APP_NAME}
#docker rmi $(docker images -a -q)
mvn clean package spring-boot:build-image

echo "pushing ${IMAGE_NAME}"
docker login -u ${secrets.docker_user} -p ${secrets.docker_password}
docker push $IMAGE_NAME
