# Nastavení IntelliJ IDEA pro správu tenant praxe2026

Tento průvodce popisuje jak nastavit IntelliJ IDEA na Windows pro správu Kubernetes
namespace `praxe2026` na clusteru bacprac2026 — bez nutnosti WSL.

---

## Co budeš potřebovat

| Nástroj | Účel |
|---------|------|
| IntelliJ IDEA (Ultimate nebo Community) | IDE + terminál + Kubernetes plugin |
| Git for Windows | Klonování repozitáře |
| kubectl (Windows) | Příkazy do Kubernetes clusteru |
| k9s (Windows) | TUI dashboard pro Kubernetes |
| kubeconfig | Přihlašovací soubor pro tenant praxe2026 |

---

## Step 01 — Instalace Git for Windows

Stáhni a nainstaluj z: https://git-scm.com/download/win

Při instalaci zvol:
- **Default editor:** dle preference (VS Code nebo Notepad++)
- **Git Bash:** zaškrtnout (dostaneš bash terminál ve Windows)

---

## Step 02 — Instalace kubectl pro Windows

**Možnost A — winget (doporučeno):**

Otevři PowerShell a spusť:
```powershell
winget install -e --id Kubernetes.kubectl
```

**Možnost B — manuální stažení:**
```powershell
# Stáhnout kubectl.exe
curl.exe -LO "https://dl.k8s.io/release/v1.32.0/bin/windows/amd64/kubectl.exe"

# Přesunout do složky v PATH, např.:
mkdir C:\kubectl
move kubectl.exe C:\kubectl\
# Přidat C:\kubectl do PATH v System Environment Variables
```

Ověřit instalaci (Git Bash nebo PowerShell):
```bash
kubectl version --client
```

---

## Step 03 — Instalace k9s pro Windows

```powershell
# Přes winget
winget install -e --id Derailed.k9s
```

Nebo stáhni `k9s_Windows_amd64.zip` z:
https://github.com/derailed/k9s/releases/latest

Rozbal a přidej `k9s.exe` do PATH.

---

## Step 04 — Nastavení kubeconfig pro tenant praxe2026

Dostaneš soubor `kubeconfig-praxe-user-1.yaml` od správce clusteru.

```powershell
# Vytvořit složku .kube
mkdir C:\Users\<TvojeJmeno>\.kube

# Zkopírovat kubeconfig
copy kubeconfig-praxe-user-1.yaml C:\Users\<TvojeJmeno>\.kube\praxe2026
```

Nastavit jako výchozí kubeconfig (PowerShell):
```powershell
# Dočasně (jen pro aktuální session)
$env:KUBECONFIG = "C:\Users\<TvojeJmeno>\.kube\praxe2026"

# Trvale — přidat do System Environment Variables
[System.Environment]::SetEnvironmentVariable("KUBECONFIG", "C:\Users\<TvojeJmeno>\.kube\praxe2026", "User")
```

---

## Step 05 — Ověřit připojení ke clusteru

Otevři Git Bash nebo PowerShell:

```bash
# Aktuální kontext
kubectl config current-context
# Expected: praxe-user-1@bacprac2026

# Zobrazit namespace
kubectl get pods -n praxe2026
# Expected: No resources found

# Ověřit kvótu
kubectl describe resourcequota praxe2026-quota -n praxe2026

# Ověřit izolaci — musí vrátit "Forbidden"
kubectl get pods -n team-a      # Forbidden ✓
kubectl get pods -n kube-system # Forbidden ✓
```

---

## Step 06 — Instalace IntelliJ IDEA

Stáhni z: https://www.jetbrains.com/idea/download/

> Community edition je zdarma a postačuje pro správu Kubernetes.
> Ultimate edition přidává databázové nástroje, Spring Boot support atd.

---

## Step 07 — Otevřít projekt v IntelliJ

1. Spusť IntelliJ IDEA
2. Na úvodní obrazovce klikni **Open**
3. Vyber složku s obdrženými soubory
4. Klikni **OK**

---

## Step 08 — Instalace Kubernetes pluginu

1. Otevři **File → Settings** (`Ctrl+Alt+S`)
2. Jdi na **Plugins → Marketplace**
3. Vyhledej `Kubernetes` od JetBrains
4. Klikni **Install** a restartuj IDE

---

## Step 09 — Konfigurace Kubernetes pluginu

Po instalaci pluginu:

1. Otevři **File → Settings → Tools → Kubernetes**
2. V sekci **Kubectl executable** ověř cestu k `kubectl.exe`
3. V sekci **Kubeconfig** přidej cestu: `C:\Users\<TvojeJmeno>\.kube\praxe2026`
4. Klikni **OK**

Zobrazit Kubernetes panel:
- **View → Tool Windows → Services** (nebo `Alt+8`)
- Uvidíš sekci **Kubernetes** s clusterem a namespace `praxe2026`

---

## Step 10 — Práce s YAML manifesty v IntelliJ

IntelliJ s Kubernetes pluginem nabízí:
- **Autocomplete** pro Kubernetes YAML soubory
- **Apply to cluster** — pravý klik na YAML soubor → `Apply to Kubernetes Cluster`
- **Diff** — porovnání lokálního YAML s live stavem v clusteru
- **Logy** — klikni na pod v Services panelu → View Logs

### Jak nasadit manifest z IntelliJ:

1. Otevři soubor YAML (např. `step01-namespace.yaml`)
2. Pravý klik → **Apply to Kubernetes Cluster**
3. Ověř v Services panelu nebo terminálu:
   ```bash
   kubectl get pods -n praxe2026
   ```

---

## Step 11 — Terminál v IntelliJ pro kubectl

IntelliJ má zabudovaný terminál:
- `Alt+F12` → otevře terminál (Git Bash nebo PowerShell)
- KUBECONFIG je automaticky načten z Environment Variables

```bash
# Příklady příkazů přímo v IntelliJ terminálu:
kubectl get all -n praxe2026
kubectl describe resourcequota praxe2026-quota -n praxe2026
k9s -n praxe2026
```

---

## Step 12 — Spuštění k9s

Otevři terminál (Git Bash nebo IntelliJ terminál):

```bash
k9s -n praxe2026

# Klávesové zkratky:
# :po      → pody
# :deploy  → deploymenty
# :svc     → services
# l        → logy podu
# d        → describe
# e        → edit YAML
# Ctrl+D   → smazat
# ?        → nápověda
```

---

## Přístupové údaje

Veškerá potřebná data (kubeconfig, přístupové údaje) vám budou zaslána elektronicky.

---

## Shrnutí — co máš po nastavení

```
Windows
├── Git for Windows (Git Bash)
├── kubectl.exe          → správa clusteru z příkazové řádky
├── k9s.exe              → TUI dashboard
├── %USERPROFILE%\.kube\praxe2026  → kubeconfig pro tenant
└── IntelliJ IDEA
    ├── Kubernetes plugin → UI pro namespace praxe2026
    │   ├── YAML autocomplete + apply
    │   ├── Services panel (pody, deploymenty, logy)
    │   └── Diff local vs cluster
    └── Terminál (Alt+F12) → kubectl + k9s
```

---

## Srovnání WSL (VS Code) vs IntelliJ

| | WSL + VS Code | IntelliJ |
|---|---|---|
| Prostředí | Linux (WSL2) | Windows nativně |
| Editor | VS Code | IntelliJ IDEA |
| Kubernetes UI | Kubernetes extension | Kubernetes plugin |
| Terminál | Ubuntu bash | Git Bash / PowerShell |
| Maven build | Nativní Linux Maven | IntelliJ zabudovaný Maven |
| Doporučeno pro | Obecný vývoj | Java/Spring Boot vývoj |
