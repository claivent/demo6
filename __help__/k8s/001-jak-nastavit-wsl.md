# Nastavení WSL2 + VS Code pro správu tenant praxe2026

Tento průvodce popisuje jak nastavit vývojové prostředí na Windows pomocí WSL2 a VS Code
pro správu Kubernetes namespace `praxe2026` na clusteru bacprac2026.

---

## Co budeš potřebovat

| Nástroj | Účel |
|---------|------|
| WSL2 (Ubuntu 22.04) | Linux prostředí ve Windows |
| VS Code | Editor + terminál + Kubernetes UI |
| kubectl | Příkazy do Kubernetes clusteru |
| k9s | TUI dashboard pro Kubernetes |
| kubeconfig | Přihlašovací soubor pro tenant praxe2026 |

---

## Step 01 — Instalace WSL2

Otevři PowerShell jako **správce** a spusť:

```powershell
wsl --install -d Ubuntu-22.04
```

Po restartu se Ubuntu spustí a požádá o vytvoření uživatele a hesla.

Ověř instalaci:
```powershell
wsl --list --verbose
# NAME            STATE    VERSION
# Ubuntu-22.04    Running  2
```

> Pokud už WSL máš, ověř verzi: `wsl --set-version Ubuntu-22.04 2`

---

## Step 02 — Aktualizace Ubuntu

Otevři WSL terminál (Ubuntu) a spusť:

```bash
sudo apt update && sudo apt upgrade -y
```

---

## Step 03 — Instalace kubectl

```bash
# Stáhnout a nainstalovat kubectl
curl -LO "https://dl.k8s.io/release/$(curl -Ls https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
chmod +x kubectl
mkdir -p ~/.local/bin
mv kubectl ~/.local/bin/kubectl

# Přidat ~/.local/bin do PATH (pokud tam ještě není)
echo 'export PATH="$HOME/.local/bin:$PATH"' >> ~/.bashrc
source ~/.bashrc

# Ověřit instalaci
kubectl version --client
```

---

## Step 04 — Instalace k9s (TUI dashboard)

```bash
# Stáhnout nejnovější verzi k9s
K9S_VERSION=$(curl -s https://api.github.com/repos/derailed/k9s/releases/latest | grep tag_name | cut -d'"' -f4)
curl -LO "https://github.com/derailed/k9s/releases/download/${K9S_VERSION}/k9s_Linux_amd64.tar.gz"
tar -xzf k9s_Linux_amd64.tar.gz k9s
mv k9s ~/.local/bin/k9s
rm k9s_Linux_amd64.tar.gz

# Ověřit
k9s version
```

---

## Step 05 — Instalace helmu (volitelné)

```bash
curl https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3 | bash
helm version
```

---

## Step 06 — Nastavení kubeconfig pro tenant praxe2026

Dostaneš soubor `kubeconfig-praxe-user-1.yaml` (nebo user-2) od správce clusteru.
Zkopíruj ho do WSL:

```bash
# Vytvoř složku pro kubeconfig
mkdir -p ~/.kube

# Zkopíruj kubeconfig (z Windows do WSL — cesta ve WSL ke C: disku)
cp /mnt/c/Users/<TvojeWindowsJmeno>/Downloads/kubeconfig-praxe-user-1.yaml ~/.kube/praxe2026

# Nastavit jako výchozí kubeconfig
echo 'export KUBECONFIG=~/.kube/praxe2026' >> ~/.bashrc
source ~/.bashrc
```

Nebo pokud chceš přepínat mezi více kubeconfigs:

```bash
# Přidat do ~/.bashrc alias pro snadné přepínání
echo "alias kpraxe='export KUBECONFIG=~/.kube/praxe2026'" >> ~/.bashrc
source ~/.bashrc
kpraxe   # přepnout na tenant praxe2026
```

---

## Step 07 — Ověřit připojení ke clusteru

```bash
# Zobrazit aktuální kontext
kubectl config current-context
# Expected: praxe-user-1@bacprac2026

# Zobrazit pods v namespace praxe2026
kubectl get pods -n praxe2026
# Expected: No resources found (namespace je prázdný)

# Ověřit kvótu (10 CPU, 20Gi RAM)
kubectl describe resourcequota praxe2026-quota -n praxe2026

# Ověřit izolaci — tyto příkazy musí vrátit "Forbidden"
kubectl get pods -n team-a        # Forbidden ✓
kubectl get pods -n kube-system   # Forbidden ✓
```

---

## Step 08 — Instalace VS Code

Stáhni VS Code z: https://code.visualstudio.com/

Po instalaci přidej **Remote - WSL** extension:
1. Otevři VS Code
2. `Ctrl+Shift+X` → vyhledej `Remote - WSL`
3. Klikni **Install**

---

## Step 09 — Otevřít VS Code přes WSL

V WSL terminálu přejdi do složky s obdrženými soubory a otevři VS Code:

```bash
# Přejít do složky se soubory (upravit cestu dle umístění)
cd ~/praxe2026

# Otevřít ve VS Code přes WSL
code .
```

VS Code se otevře s WSL prostředím — terminál uvnitř VS Code je přímo Ubuntu.

---

## Step 10 — VS Code extensions pro Kubernetes

Po otevření VS Code přes WSL nainstaluj tyto extensions (`Ctrl+Shift+X`):

| Extension | ID | Účel |
|-----------|----|------|
| Kubernetes | `ms-kubernetes-tools.vscode-kubernetes-tools` | UI pro cluster, namespace, pody |
| YAML | `redhat.vscode-yaml` | Validace YAML souborů |
| GitLens | `eamodio.gitlens` | Git historie |

Po instalaci Kubernetes extension:
1. Klikni na ikonu Kubernetes v levém panelu
2. Extension automaticky najde kubeconfig z `~/.kube/praxe2026`
3. Uvidíš namespace `praxe2026` se všemi resources

---

## Step 11 — Spuštění k9s

```bash
# Spustit k9s dashboard
k9s -n praxe2026

# Klávesové zkratky v k9s:
# :ns      — přepnout namespace
# :po      — zobrazit pody
# :deploy  — zobrazit deploymenty
# d        — describe (detail objektu)
# l        — logy
# e        — edit YAML
# Ctrl+D   — smazat
# ?        — nápověda
```

---

## Přístupové údaje

Veškerá potřebná data (kubeconfig, přístupové údaje) vám budou zaslána elektronicky.

---

## Shrnutí — co máš po nastavení

```
Windows
└── WSL2 (Ubuntu 22.04)
    ├── kubectl          → správa clusteru
    ├── k9s              → dashboard
    ├── helm             → instalace charts
    ├── ~/.kube/praxe2026 → kubeconfig pro tenant
    └── VS Code (Remote WSL)
        ├── Kubernetes extension → UI pro namespace praxe2026
        └── YAML extension       → validace manifestů
```
