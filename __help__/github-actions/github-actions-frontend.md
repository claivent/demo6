# GitHub Actions — Frontend (Angular)
**Jak automaticky sestavit a nahrát Docker image frontendu po každém commitu**

Po každém `git push` do větve `master` kde jsou změny ve složce `frontend/` GitHub Actions automaticky:
1. Sestaví Docker image Angular aplikace
2. Nahraje ho do GitHub Container Registry jako `ghcr.io/<username>/praxe-demo6-frontend:latest`

---

## Co je připraveno

```
demo6/
├── .github/
│   └── workflows/
│       ├── build-and-push.yml           ✓ backend
│       └── build-and-push-frontend.yml  ✓ frontend (nový)
└── frontend/
    ├── src/
    ├── package.json
    ├── angular.json
    ├── Dockerfile                        ✓ připraveno
    └── .dockerignore                     ✓ připraveno
```

**Dockerfile** — Angular dev server (Node 20):
```dockerfile
FROM node:20-alpine
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
EXPOSE 4200
CMD ["npm", "start", "--", "--host", "0.0.0.0"]
```

**`.dockerignore`** — vylučuje `node_modules/` z build contextu (zrychluje build).

---

## Co musíš udělat — jen jednou

Secret `GHCR_TOKEN` je **stejný** jako pro backend — pokud jsi ho již nastavil, není třeba nic dělat.

Pokud secret ještě nemáš:

1. Přihlas se na **github.com** → Avatar → **Settings**
2. Vlevo dole → **Developer settings**
3. **Personal access tokens** → **Tokens (classic)**
4. **Generate new token (classic)**  
   - **Note:** `github-actions-packages`
   - **Scopes:** zaškrtni `write:packages`
5. Zkopíruj token
6. Jdi do repozitáře → **Settings** → **Secrets and variables** → **Actions**
7. **New repository secret** → Name: `GHCR_TOKEN` → vlož token → **Add secret**

---

## Jak spustit workflow

Workflow se spustí **automaticky** když změníš cokoliv ve složce `frontend/` a pushneš:

```bash
# uprav něco v frontend/
git add frontend/
git commit -m "feat: update angular component"
git push
```

Průběh sleduj na GitHubu → záložka **Actions** → `Build and Push Frontend Docker Image`.

> Pokud pushneš změny mimo `frontend/` (např. jen backend), frontend workflow se **nespustí** — šetří minuty.

---

## Výsledný image

```
ghcr.io/<tvuj-github-username>/praxe-demo6-frontend:latest
ghcr.io/<tvuj-github-username>/praxe-demo6-frontend:<commit-sha>
```

Najdeš ho na: **github.com → tvůj profil → záložka Packages**

---

## Spuštění kontejneru lokálně

```bash
docker pull ghcr.io/<username>/praxe-demo6-frontend:latest
docker run -p 4200:4200 ghcr.io/<username>/praxe-demo6-frontend:latest
```

Aplikace běží na: `http://localhost:4200`

---

## Workflow soubor — `.github/workflows/build-and-push-frontend.yml`

```yaml
name: Build and Push Frontend Docker Image

on:
  push:
    branches:
      - master
    paths:
      - 'frontend/**'         # spustí se pouze při změně souborů ve frontend/

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Log in to GitHub Container Registry
        uses: docker/login-action@v3
        with:
          registry: ghcr.io
          username: ${{ github.actor }}
          password: ${{ secrets.GHCR_TOKEN }}

      - name: Set image tag
        run: echo "TAG=$(git rev-parse --short HEAD)" >> $GITHUB_ENV

      - name: Build and push Docker image
        uses: docker/build-push-action@v5
        with:
          context: ./frontend    # Dockerfile je ve složce frontend/
          push: true
          tags: |
            ghcr.io/${{ github.repository_owner }}/praxe-demo6-frontend:${{ env.TAG }}
            ghcr.io/${{ github.repository_owner }}/praxe-demo6-frontend:latest
```

---

## Rozdíl mezi backend a frontend workflow

| | Backend | Frontend |
|--|---------|----------|
| Workflow soubor | `build-and-push.yml` | `build-and-push-frontend.yml` |
| Spouští se při změně | cokoliv | pouze `frontend/**` |
| Dockerfile | `./Dockerfile` | `./frontend/Dockerfile` |
| Base image | `maven` + `eclipse-temurin:21-jre` | `node:20-alpine` |
| Port | 8080 | 4200 |
| Image name | `praxe-demo6` | `praxe-demo6-frontend` |

---

## Časté chyby

| Chyba | Příčina | Řešení |
|-------|---------|--------|
| `denied` při push | Secret `GHCR_TOKEN` chybí | Nastav Secret v repozitáři (viz výše) |
| Workflow se nespustí | Změny nebyly ve `frontend/` složce | Zkontroluj že jsi upravil soubory uvnitř `frontend/` |
| `npm install` selže | Chyba v `package.json` | Zkontroluj logy v Actions → failed krok |
| `Cannot GET /` v prohlížeči | Angular dev server ještě startuje | Počkej ~30s po spuštění kontejneru |
