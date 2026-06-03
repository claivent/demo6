# da-mysql.md — MySQL pro demo6: lokálně i v Kubernetes

Tento průvodce popisuje, jak přidat MySQL databázi do demo6 projektu — nejdřív lokálně přes Docker Compose, pak nasazení v Kubernetes s PVC.

---

## Přehled architektury

```
[Angular frontend :4200]
        ↓
[Spring Boot backend :9111]
        ↓
[MySQL :3306]
   database: demo6db
   user:     demo6admin
   password: Demo6Pass1!
```

Nový endpoint **`GET /api/db/users`** vrací uživatele uložené v MySQL tabulce `db_users`.
Původní endpoint **`GET /api/users`** zůstal nezměněn (hardcoded data).

---

## Co bylo přidáno do projektu

### pom.xml — nové závislosti
```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
  <groupId>com.mysql</groupId>
  <artifactId>mysql-connector-j</artifactId>
  <scope>runtime</scope>
</dependency>
```

### application.yaml — konfigurace datasource
```yaml
spring:
  datasource:
    url: ${SPRING_DATASOURCE_URL:jdbc:mysql://localhost:3306/demo6db}
    username: ${SPRING_DATASOURCE_USERNAME:demo6admin}
    password: ${SPRING_DATASOURCE_PASSWORD:Demo6Pass1!}
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: none
```

Hodnoty jsou přepsatelné env proměnnými — výchozí hodnoty fungují lokálně, env proměnné se nastaví v K8s.

### Nové Java soubory
| Soubor | Účel |
|---|---|
| `model/DbUser.java` | JPA entita mapovaná na tabulku `db_users` |
| `repository/DbUserRepository.java` | Spring Data JPA repository |
| `DbUserController.java` | REST endpoint `GET /api/db/users` |

### MySQL init SQL
`mysql/init/01-schema.sql` — spustí se automaticky při prvním startu MySQL kontejneru:
- vytvoří databázi `demo6db`
- vytvoří uživatele `demo6admin`
- vytvoří tabulku `db_users`
- vloží 3 testovací záznamy

---

## Část 1 — Lokálně s Docker Compose

### Spuštění

```bash
cd /home/claiv/demo6

# Spustit MySQL + backend
docker compose up mysql backend -d

# Nebo jen MySQL (pokud backend běží přes mvn spring-boot:run)
docker compose up mysql -d
```

### Ověření MySQL

```bash
# Připojení přes docker exec
docker exec -it demo6-mysql mysql -u demo6admin -pDemo6Pass1! demo6db

# Zkontrolovat data
SELECT * FROM db_users;
```

Očekávaný výstup:
```
+----+------------------+------------------+
| id | name             | email            |
+----+------------------+------------------+
|  1 | Alice Nováková   | alice@demo6.com  |
|  2 | Bob Procházka    | bob@demo6.com    |
|  3 | Carol Dvořáková  | carol@demo6.com  |
+----+------------------+------------------+
```

### Ověření endpointu

```bash
curl http://localhost:9111/api/db/users
```

Očekávaný výstup:
```json
[
  {"id":1,"name":"Alice Nováková","email":"alice@demo6.com"},
  {"id":2,"name":"Bob Procházka","email":"bob@demo6.com"},
  {"id":3,"name":"Carol Dvořáková","email":"carol@demo6.com"}
]
```

### Spuštění backendu lokálně (bez Dockeru)

Pokud MySQL běží v Dockeru na portu 3306, backend lze spustit přímo:

```bash
cd /home/claiv/demo6
mvn spring-boot:run
# → aplikace se připojí na localhost:3306 (výchozí hodnoty v application.yaml)
```

### Dev mode (backend v Dockeru přes Maven)

```bash
docker compose up mysql backend-dev -d
docker compose logs -f backend-dev
```

### Zastavení a vyčištění

```bash
# Zastavit, zachovat MySQL data (volume mysql-data)
docker compose down

# Zastavit a smazat data (fresh start)
docker compose down -v
```

---

## Část 2 — Kubernetes s PVC

### Struktura souborů

```
__demo6-gitops/argocd-deploy-kustomization/
├── kustomization.yaml          ← přidány mysql/ resources
├── patch-env.yaml              ← přidány SPRING_DATASOURCE_* env vars
└── mysql/
    ├── kustomization.yaml
    ├── mysql-secret.yaml       ← credentials (base64)
    ├── mysql-pvc.yaml          ← PersistentVolumeClaim 2Gi
    ├── mysql-configmap.yaml    ← init SQL (01-schema.sql)
    └── mysql-deploy.yaml       ← Deployment + Service
```

### Credentials (mysql-secret.yaml)

| Klíč | Hodnota |
|---|---|
| `root-password` | `RootPass1!` |
| `username` | `demo6admin` |
| `password` | `Demo6Pass1!` |
| `database` | `demo6db` |

Pokud chceš změnit hesla, vygeneruj nové base64 hodnoty:
```bash
echo -n 'NoveHeslo123!' | base64
```

### Nasazení do clusteru

#### Možnost A — přes ArgoCD (doporučeno)

ArgoCD aplikace `mentors` sleduje `__demo6-gitops/argocd-deploy-kustomization/`.
Stačí commitnout a pushnout změny:

```bash
cd /home/claiv/demo6
git add __demo6-gitops/
git commit -m "add mysql with pvc"
git push

# ArgoCD auto-sync do ~3 minut, nebo ruční sync:
argocd app sync mentors
```

#### Možnost B — přímé kubectl apply

```bash
# Pouze MySQL resources
kubectl apply -k __demo6-gitops/argocd-deploy-kustomization/mysql/

# Nebo celý stack
kubectl apply -k __demo6-gitops/argocd-deploy-kustomization/
```

### Ověření nasazení

```bash
# Zkontrolovat PVC
kubectl get pvc -n mentors
# NAME        STATUS   VOLUME   CAPACITY   ACCESS MODES
# mysql-pvc   Bound    ...      2Gi        RWO

# Zkontrolovat pody
kubectl get pods -n mentors
# NAME                            READY   STATUS    RESTARTS
# mentor-mysql-xxx                1/1     Running   0
# mentor-xxx (backend)            1/1     Running   0

# Logy MySQL podu
kubectl logs -n mentors deploy/mentor-mysql

# Připojit se do MySQL v K8s
kubectl exec -n mentors deploy/mentor-mysql -- \
  mysql -u demo6admin -pDemo6Pass1! demo6db -e "SELECT * FROM db_users;"
```

### Ověření endpointu v K8s

```bash
# Přes NodePort nebo Traefik
curl https://app.baprace.online/api/db/users

# Nebo přes port-forward
kubectl port-forward -n mentors svc/mentor-backend 9111:80
curl http://localhost:9111/api/db/users
```

---

## Přidání nových uživatelů do databáze

### Lokálně

```bash
docker exec -it demo6-mysql mysql -u demo6admin -pDemo6Pass1! demo6db -e \
  "INSERT INTO db_users (name, email) VALUES ('Nový Uživatel', 'novy@demo6.com');"
```

### V Kubernetes

```bash
kubectl exec -n mentors deploy/mentor-mysql -- \
  mysql -u demo6admin -pDemo6Pass1! demo6db -e \
  "INSERT INTO db_users (name, email) VALUES ('Nový Uživatel', 'novy@demo6.com');"
```

Změny v databázi jsou trvalé díky PVC — přežijí restart podu.

---

## Troubleshooting

### Backend se nepřipojí k MySQL

**Příznaky:** `Communications link failure`, `Access denied`

**Postup:**
```bash
# Lokálně — ověřit že MySQL kontejner běží a je healthy
docker compose ps
docker compose logs mysql

# K8s — ověřit readiness probe
kubectl describe pod -n mentors -l app=mentor-mysql
kubectl logs -n mentors deploy/mentor-mysql | tail -20
```

**Nejčastější příčiny:**
- MySQL ještě není připraven (backend se spustil dřív) → počkej 20s a restartuj backend pod
- Špatné heslo v secretu → ověř `kubectl get secret mysql-secret -n mentors -o yaml`

### Init SQL se nespustil

Init SQL se spustí **pouze při prvním startu** (prázdný volume). Pokud data chybí:

```bash
# Lokálně — smazat volume a znovu spustit
docker compose down -v
docker compose up mysql -d

# K8s — smazat PVC (ZTRATÍŠ DATA) a znovu nasadit
kubectl delete pvc mysql-pvc -n mentors
kubectl apply -k __demo6-gitops/argocd-deploy-kustomization/mysql/
```

### Resetovat heslo root (K8s)

```bash
kubectl exec -n mentors deploy/mentor-mysql -- \
  mysql -u root -pRootPass1! -e \
  "ALTER USER 'demo6admin'@'%' IDENTIFIED BY 'Demo6Pass1!';"
```

---

## Přehled endpointů

| Endpoint | Zdroj dat | Popis |
|---|---|---|
| `GET /api/users` | hardcoded v kódu | 3 přednastavení uživatelé (Jan, Petra, Tomáš) |
| `GET /api/db/users` | MySQL `db_users` | Uživatelé z databáze (Alice, Bob, Carol) |
