#!/usr/bin/env bash
# Reproduces the exact demo steps from 09-cloud slides.
# Run this script from the project root: bash run.sh
set -e

echo "=== Step 1: Start local K8s cluster ==="
minikube start

echo ""
echo "=== Step 2: Point Docker to minikube's internal daemon ==="
eval $(minikube docker-env)

echo ""
echo "=== Step 3: Build image with Jib (no Dockerfile needed) ==="
mvn compile jib:dockerBuild

echo ""
echo "=== Step 4: Deploy to K8s ==="
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml

echo ""
echo "=== Step 5: Wait for pod to be ready ==="
kubectl rollout status deployment/echo-boot

echo ""
echo "=== Step 6: Get service URL and test ==="
URL=$(minikube service echo-boot --url)
echo "Service URL: $URL"
echo ""
echo "Testing GET /:"
curl -s "$URL/" | python3 -m json.tool
echo ""
echo "Testing GET /echo?msg=K8s:"
curl -s "$URL/echo?msg=K8s" | python3 -m json.tool

echo ""
echo "=== Step 7: Scale to 2 replicas and observe load balancing ==="
kubectl scale deployment echo-boot --replicas=2
kubectl rollout status deployment/echo-boot
echo ""
echo "Send 4 requests — watch the 'pod' field change between two hostnames:"
for i in 1 2 3 4; do
  curl -s "$URL/echo?msg=request-$i" | python3 -m json.tool
  echo "---"
done

echo ""
echo "=== Step 8: Rolling update to v2 (zero downtime) ==="
echo "Edit k8s/deployment.yaml: change image tag to echo-boot:2.0.0"
echo "Then run: kubectl apply -f k8s/deployment.yaml"
echo "Watch pods being replaced: kubectl get pods -w"
