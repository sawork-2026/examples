#!/usr/bin/env bash
# Same demo as run.sh, but targets OrbStack's built-in Kubernetes instead of minikube.
# OrbStack shares its Docker daemon with its K8s cluster, so no docker-env switch is needed.
set -e

echo "=== Step 1: Verify OrbStack K8s is the active context ==="
kubectl config use-context orbstack
kubectl cluster-info | head -2

echo ""
echo "=== Step 2: Build image with Jib into the local Docker daemon ==="
mvn compile jib:dockerBuild

echo ""
echo "=== Step 3: Deploy to K8s ==="
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml

echo ""
echo "=== Step 4: Wait for pod to be ready ==="
kubectl rollout status deployment/echo-boot

echo ""
echo "=== Step 5: Port-forward service and test ==="
# Run port-forward in background so we can curl it
kubectl port-forward svc/echo-boot 18080:8080 >/tmp/echo-boot-pf.log 2>&1 &
PF_PID=$!
trap "kill $PF_PID 2>/dev/null || true" EXIT
sleep 3

URL="http://localhost:18080"
echo "Service URL: $URL"
echo ""
echo "Testing GET /:"
curl -s "$URL/" | python3 -m json.tool
echo ""
echo "Testing GET /echo?msg=K8s:"
curl -s "$URL/echo?msg=K8s" | python3 -m json.tool

echo ""
echo "=== Step 6: Scale to 2 replicas and observe load balancing ==="
kubectl scale deployment echo-boot --replicas=2
kubectl rollout status deployment/echo-boot
echo ""
echo "Send 4 requests — watch the 'pod' field change between two hostnames:"
for i in 1 2 3 4; do
  curl -s "$URL/echo?msg=request-$i" | python3 -m json.tool
  echo "---"
done

echo ""
echo "=== Step 7: Rolling update to v2 (zero downtime) ==="
echo "Edit k8s/deployment.yaml: change image tag to echo-boot:2.0.0"
echo "Then run: kubectl apply -f k8s/deployment.yaml"
echo "Watch pods being replaced: kubectl get pods -w"
echo ""
echo "(Port-forward will be killed on exit.)"
