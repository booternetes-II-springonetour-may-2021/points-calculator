#!/usr/bin/env bash 

echo "installing Helm.."

curl -fsSL -o get_helm.sh https://raw.githubusercontent.com/helm/helm/master/scripts/get-helm-3
chmod 700 get_helm.sh
./get_helm.sh

 [[ $(helm list | tail -n +2 | grep spring-cloud-dataflow | awk '{print $8}') != "deployed" ]]; then
  echo "installing Spring Cloud Dataflow..."
  helm repo add bitnami https://charts.bitnami.com/bitnami
  helm install my-release bitnami/spring-cloud-dataflow
fi


