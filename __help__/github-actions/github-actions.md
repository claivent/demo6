# GitHub Actions — návod pro demo6
**Jak automaticky sestavit a nahrát Docker image po každém commitu**

Tento repozitář má připravený workflow soubor v `.github/workflows/build-and-push.yml`.  
Po každém `git push` do větve `main` GitHub Actions automaticky:
1. Sestaví Docker image z `Dockerfile` v kořeni projektu
2. Nahraje ho do GitHub Container Registry jako `ghcr.io/<username>/praxe-demo6:latest`

Vše **zdarma** na GitHub Free plánu.

---

## Co je již připraveno

```
demo6/
├── .github/
│   └── workflows/
│       └── build-and-push.yml   ✓ připraveno
├── src/
├── pom.xml
└── Dockerfile                   ✓ připraveno
```

**Dockerfile** — multi-stage build (Maven → JRE):
```dockerfile
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

---

## Co musíš udělat — jen jednou

### Krok 1 — Vytvoř Personal Access Token (PAT)

1. Přihlas se na **github.com**
2. Avatar vpravo nahoře → **Settings**
3. Vlevo dole → **Developer settings**
4. **Personal access tokens** → **Tokens (classic)**
5. **Generate new token (classic)**
6. Vyplň:
   - **Note:** `github-actions-packages`
   - **Expiration:** `90 days`
   - **Scopes:** zaškrtni `write:packages`
7. Klikni **Generate token**
8. **Zkopíruj token hned** — zobrazí se jen jednou!

### Krok 2 — Ulož token jako Secret v repozitáři

1. Jdi do svého repozitáře na GitHubu
2. Záložka **Settings**
3. Vlevo → **Secrets and variables** → **Actions**
4. **New repository secret**
5. Vyplň:
   - **Name:** `GHCR_TOKEN`
   - **Secret:** vlož zkopírovaný PAT token
6. **Add secret**

> Bez tohoto kroku workflow selže s chybou `denied`.

---

## Jak spustit workflow

Stačí odeslat commit do větve `main`:

```bash
git add .
git commit -m "tvoje zpráva"
git push
```

Workflow se spustí automaticky. Průběh sleduj na GitHubu → záložka **Actions**.

---

## Výsledný image

Po úspěšném buildu je image dostupný jako:

```
ghcr.io/<tvuj-github-username>/praxe-demo6:latest
ghcr.io/<tvuj-github-username>/praxe-demo6:<commit-sha>
```

Najdeš ho na: **github.com → tvůj profil → záložka Packages**

---

## Jak workflow funguje — vizuálně

```
git push do main
        │
        ▼
GitHub spustí runner (Ubuntu, zdarma)
        │
        ├── 1. Checkout kódu
        ├── 2. Login do ghcr.io pomocí GHCR_TOKEN
        ├── 3. Získání SHA commitu (např. "a1b2c3d")
        ├── 4. docker build -t ghcr.io/.../praxe-demo6:a1b2c3d .
        └── 5. docker push ghcr.io/.../praxe-demo6:a1b2c3d
                        │
                        ▼
        ghcr.io/<username>/praxe-demo6:a1b2c3d  ✓
        ghcr.io/<username>/praxe-demo6:latest   ✓
```

---

## Workflow soubor — `.github/workflows/build-and-push.yml`

```yaml
name: Build and Push Docker Image

on:
  push:
    branches:
      - main

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
          context: .
          push: true
          tags: |
            ghcr.io/${{ github.repository_owner }}/praxe-demo6:${{ env.TAG }}
            ghcr.io/${{ github.repository_owner }}/praxe-demo6:latest
```

---

## Časté chyby

| Chyba | Příčina | Řešení |
|-------|---------|--------|
| `denied` při push | Secret `GHCR_TOKEN` chybí nebo nemá `write:packages` | Vytvoř PAT s `write:packages` a ulož jako Secret |
| Workflow se nespustí | Větev se nejmenuje `main` | Změň `branches: - main` na `- master` |
| `Dockerfile not found` | Dockerfile není v kořeni projektu | Zkontroluj že `Dockerfile` existuje vedle `pom.xml` |
| Build selže na `mvn package` | Chyba v kódu | Zkontroluj logy v záložce Actions → klikni na failed krok |

---

## Zdarma — kolik minut máš

| Repozitář | Minuty/měsíc |
|-----------|-------------|
| Veřejný | Neomezeno |
| Soukromý (Free účet) | 2 000 min |

Jeden build trvá přibližně **2–4 minuty** → na Free plánu zvládneš ~500 buildů měsíčně.
