echo "Creating serviceaccount"
kubectl apply -f ./windup.serviceaccount.yaml
echo "Creating role"
kubectl apply -f ./windup.role.yaml
echo "Creating rolebinding"
kubectl apply -f ./windup.rolebinding.yaml
echo "Creating cluster role"
kubectl apply -f ./windup.clusterrole.yaml
echo "Creating cluster rolebinding"
kubectl apply -f ./windup.clusterrolebinding.yaml
echo "Creating operator deployment"
kubectl apply -f ./windup.deployment.yaml
echo "Creating CRD"
kubectl apply -f ./windup.crd.yaml

