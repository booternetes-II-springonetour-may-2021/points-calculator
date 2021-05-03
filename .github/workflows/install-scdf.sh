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
