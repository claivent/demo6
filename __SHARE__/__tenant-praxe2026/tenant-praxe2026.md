# Tenant Isolation — praxe2026

Tento soubor slouží jako šablona pro izolaci dalších tenantů v Kubernetes clusteru.
Pro každý nový tenant stačí zkopírovat tuto složku, přejmenovat `praxe2026` → název nového tenantu
a upravit CPU/RAM limity v `step02-resourcequota.yaml`.

---

## Přehled kroků

| Krok | Soubor | Popis |
|------|--------|-------|
| Step 01 | `step01-namespace.yaml` | Vytvořit namespace |
| Step 02 | `step02-resourcequota.yaml` | Nastavit CPU/RAM kvóty (10 CPU, 20Gi RAM) |
| Step 03 | `step03-limitrange.yaml` | Nastavit výchozí limity na kontejner |
| Step 04 | `step04-serviceaccounts.yaml` | Vytvořit ServiceAccounts pro 2 uživatele |
| Step 05 | `step05-role.yaml` | Vytvořit Role (oprávnění jen v tomto namespace) |
| Step 06 | `step06-rolebindings.yaml` | Přiřadit Role uživatelům |
| Step 07 | `step07-tokens.yaml` | Vytvořit tokeny pro přihlášení |
| Step 08 | *(příkazy níže)* | Vygenerovat kubeconfig pro každého uživatele |
| Step 09 | *(příkazy níže)* | Ověřit izolaci — uživatel nesmí vidět jiné namespacey |

---

## Architektura izolace

```
Cluster
├── team-a (demo)           ← uživatelé praxe2026 NEMAJÍ přístup
├── team-b (paygate)        ← uživatelé praxe2026 NEMAJÍ přístup
├── team-c (config-server)  ← uživatelé praxe2026 NEMAJÍ přístup
├── platform-data (MySQL)   ← uživatelé praxe2026 NEMAJÍ přístup
└── praxe2026               ← uživatelé MAJÍ přístup POUZE sem
    ├── ResourceQuota: 10 CPU, 20Gi RAM
    ├── LimitRange: výchozí 100m CPU / 128Mi RAM na kontejner
    ├── praxe-user-1 (ServiceAccount + token)
    └── praxe-user-2 (ServiceAccount + token)
```

> Kubernetes RBAC je ve výchozím stavu **deny-all**. Pokud uživatel nemá ClusterRoleBinding
> ani RoleBinding v jiném namespace, automaticky k němu nemá přístup.

---

## Step 01 — Aplikovat vše najednou

```bash
kubectl apply -k __tenant-praxe2026/
```

Nebo krok po kroku:

```bash
kubectl apply -f __tenant-praxe2026/step01-namespace.yaml
kubectl apply -f __tenant-praxe2026/step02-resourcequota.yaml
kubectl apply -f __tenant-praxe2026/step03-limitrange.yaml
kubectl apply -f __tenant-praxe2026/step04-serviceaccounts.yaml
kubectl apply -f __tenant-praxe2026/step05-role.yaml
kubectl apply -f __tenant-praxe2026/step06-rolebindings.yaml
kubectl apply -f __tenant-praxe2026/step07-tokens.yaml
```

### Ověřit namespace a kvótu

```bash
kubectl get namespace praxe2026
kubectl describe resourcequota praxe2026-quota -n praxe2026
kubectl describe limitrange praxe2026-limitrange -n praxe2026
```

---

## Step 08 — Vygenerovat kubeconfig pro uživatele

Každý uživatel dostane vlastní `kubeconfig` soubor. Obsahuje:
- adresu clusteru
- CA certifikát
- token ServiceAccountu

```bash
# ── User 1 ──────────────────────────────────────────────────────────────────
TOKEN_1=$(kubectl get secret praxe-user-1-token -n praxe2026 \
  -o jsonpath='{.data.token}' | base64 -d)

CA_DATA=$(kubectl get secret praxe-user-1-token -n praxe2026 \
  -o jsonpath='{.data.ca\.crt}')

# Server URL clusteru (API server)
SERVER="https://178.18.244.197:6443"

cat > kubeconfig-praxe-user-1.yaml <<EOF
apiVersion: v1
kind: Config
clusters:
  - name: bacprac2026
    cluster:
      server: ${SERVER}
      certificate-authority-data: ${CA_DATA}
contexts:
  - name: praxe-user-1@bacprac2026
    context:
      cluster: bacprac2026
      namespace: praxe2026
      user: praxe-user-1
current-context: praxe-user-1@bacprac2026
users:
  - name: praxe-user-1
    user:
      token: ${TOKEN_1}
EOF

echo "Kubeconfig uložen: kubeconfig-praxe-user-1.yaml"
```

```bash
# ── User 2 ──────────────────────────────────────────────────────────────────
TOKEN_2=$(kubectl get secret praxe-user-2-token -n praxe2026 \
  -o jsonpath='{.data.token}' | base64 -d)

cat > kubeconfig-praxe-user-2.yaml <<EOF
apiVersion: v1
kind: Config
clusters:
  - name: bacprac2026
    cluster:
      server: ${SERVER}
      certificate-authority-data: ${CA_DATA}
contexts:
  - name: praxe-user-2@bacprac2026
    context:
      cluster: bacprac2026
      namespace: praxe2026
      user: praxe-user-2
current-context: praxe-user-2@bacprac2026
users:
  - name: praxe-user-2
    user:
      token: ${TOKEN_2}
EOF

echo "Kubeconfig uložen: kubeconfig-praxe-user-2.yaml"
```

### Použití kubeconfig

```bash
# Uživatel 1 — otestovat přihlášení
kubectl --kubeconfig=kubeconfig-praxe-user-1.yaml get pods -n praxe2026

# Nebo nastavit jako výchozí
export KUBECONFIG=kubeconfig-praxe-user-1.yaml
kubectl get pods
```

---

## Step 09 — Ověřit izolaci

Přepnout na kubeconfig uživatele a ověřit, že NEMÁ přístup do jiných namespaců:

```bash
export KUBECONFIG=kubeconfig-praxe-user-1.yaml

# Tyto příkazy musí vrátit "Forbidden" — to je správně
kubectl get pods -n team-a        # Forbidden ✓
kubectl get pods -n team-b        # Forbidden ✓
kubectl get pods -n platform-data # Forbidden ✓
kubectl get pods -n kube-system   # Forbidden ✓

# Tento příkaz musí fungovat
kubectl get pods -n praxe2026     # OK ✓

# Ověřit kvótu — uživatel vidí pouze svou kvótu
kubectl describe resourcequota -n praxe2026
```

---

## Step 10 — Ověřit ResourceQuota při deployi

Pokud pod překročí limity, Kubernetes ho odmítne:



```yaml
apiVersion: v1
kind: Pod
metadata:
  name: test-pod
  namespace: praxe2026
spec:
  containers:
    - name: nginx
      image: nginx:alpine
      resources:
        requests:
          cpu: 100m
          memory: 128Mi
        limits:
          cpu: 500m
          memory: 512Mi
```
````bash
kubectl apply -f test-pod.yaml

#check
kubectl get pod test-pod -n praxe2026
kubectl describe pod test-pod -n praxe2026
````

```bash

# running version
kubectl run test-pod \
  -n praxe2026 \
  --image=nginx:alpine \
  --overrides='
{
  "apiVersion": "v1",
  "spec": {
    "containers": [
      {
        "name": "test-pod",
        "image": "nginx:alpine",
        "resources": {
          "requests": {
            "cpu": "100m",
            "memory": "128Mi"
          },
          "limits": {
            "cpu": "500m",
            "memory": "512Mi"
          }
        }
      }
    ]
  }
}'

```

```bash


# Zkontrolovat aktuální využití kvóty
kubectl describe resourcequota praxe2026-quota -n praxe2026
# Výstup ukáže: Used vs Hard limity

# Smazat testovací pod
kubectl delete pod test-pod -n praxe2026
```

---

## Smazání tenantu

```bash
# Smazání namespace smaže VEŠKERÝ obsah (pody, PVC, role, tokeny...)
kubectl delete namespace praxe2026
```

---

## Šablona pro nový tenant

1. Zkopírovat složku: `cp -r __tenant-praxe2026 __tenant-NOVY_TENANT`
2. Hromadně přejmenovat: `sed -i 's/praxe2026/NOVY_TENANT/g' __tenant-NOVY_TENANT/*.yaml`
3. Upravit CPU/RAM v `step02-resourcequota.yaml`
4. Upravit jména uživatelů v `step04`, `step06`, `step07`
5. Aplikovat: `kubectl apply -k __tenant-NOVY_TENANT/`
6. Vygenerovat kubeconfig (Step 08)

---

## Troubleshooting

### Pod se nespustí — "exceeded quota"
```bash
kubectl describe resourcequota praxe2026-quota -n praxe2026
# Zkontroluj sloupec "Used" vs "Hard"
# Buď zvýšit kvótu v step02-resourcequota.yaml, nebo smazat nepotřebné pody
```

### Pod se nespustí — "must specify limits"
```bash
# LimitRange nastavuje výchozí limity, ale některé operátory je ignorují
# Přidat explicitně resources: requests/limits do Deployment specifikace
```

### Token nefunguje
```bash
kubectl get secret praxe-user-1-token -n praxe2026
# Pokud SECRET neexistuje, ServiceAccount možná ještě nebyl vytvořen
kubectl get serviceaccount -n praxe2026
```

### Ověřit oprávnění uživatele
```bash
# Jako admin — co může praxe-user-1 dělat?
kubectl auth can-i --list \
  --as=system:serviceaccount:praxe2026:praxe-user-1 \
  -n praxe2026

# Může praxe-user-1 přistupovat do team-a?
kubectl auth can-i get pods \
  --as=system:serviceaccount:praxe2026:praxe-user-1 \
  -n team-a
# Expected: no
```
