# frontend-application-model.md

Popis architektury a struktury Angular frontendu pro projekt demo6.

---

## Technologie

| Položka | Verze |
|---|---|
| Angular | 19+ |
| TypeScript | 5+ |
| Stavební nástroj | Angular CLI (`ng build`) |
| HTTP | `HttpClient` (Angular built-in) |
| Formuláře | `FormsModule` (template-driven) |

---

## Struktura souborů

```
frontend/src/app/
├── app.component.html        ← hlavní layout: topbar + <router-outlet>
├── app.component.ts          ← root komponenta
├── app.component.css         ← (prázdné, styly jsou v styles.css)
├── app.config.ts             ← providers: Router, HttpClient, ZoneChangeDetection
├── app.routes.ts             ← definice všech routes
├── user.model.ts             ← interface User { id, name, email }
├── user.service.ts           ← HTTP volání na /api/db/*
└── users/users/
    ├── users.component.*         → /users       — seznam uživatelů
    ├── user.add.component/*      → /user/add    — přidání uživatele
    ├── user.del.component/*      → /user/delete — smazání uživatele
    └── user.modify.component/*   → /user/modify — úprava uživatele
```

---

## Routing (`app.routes.ts`)

| Path | Komponenta | Popis |
|---|---|---|
| `/users` | `UsersComponent` | Tabulka všech uživatelů z DB |
| `/user/add` | `UserAddComponent` | Formulář pro přidání |
| `/user/delete` | `UserDelComponent` | Formulář pro smazání |
| `/user/modify` | `UserModifyComponent` | Formulář pro úpravu |
| `` (prázdné) | redirect | Přesměruje na `/users` |

---

## UserService (`user.service.ts`)

Singleton service (`providedIn: 'root'`). Všechna volání míří na `/api/db/*`.

| Metoda | HTTP | Endpoint | Popis |
|---|---|---|---|
| `getUsers()` | GET | `/api/db/users` | Vrátí seznam všech uživatelů |
| `addUser(user)` | POST | `/api/db/add` | Přidá uživatele, vrátí objekt s přiděleným `id` |
| `deleteUser(id)` | DELETE | `/api/db/delete/{id}` | Smaže uživatele, vrátí `{ message }` |
| `modifyUser(id, patch)` | PATCH | `/api/db/modify/{id}` | Partial update — posílají se jen vyplněná pole |

---

## Komponenty

### UsersComponent (`/users`)

- Načte uživatele přes `UserService.getUsers()` v `ngOnInit`
- Zobrazí tabulku: ID / Jméno / Email
- Stavy: načítání, chyba, prázdný seznam

### UserAddComponent (`/user/add`)

- Formulář: Jméno + Email (ID není — generuje databáze AUTO_INCREMENT)
- Po úspěchu zobrazí přidělené ID a vyčistí formulář

### UserDelComponent (`/user/delete`)

Komponenta používá dvoustupňový modální tok — nejdřív výběr, pak potvrzení se třemi akcemi.

#### Postup

```
1. Uživatel klikne na "Vybrat pro smazání"
        ↓
2. Modal 1 — výběr uživatele
   → volá UserService.getUsers() (GET /api/db/users)
   → zobrazí tabulku uživatelů (hover červeně jako vizuální hint nebezpečí)
        ↓
3. Uživatel klikne na řádek
   → Modal 1 se zavře
   → Modal 2 se otevře — potvrzení
        ↓
4. Modal 2 — potvrzení smazání
   → zobrazí: ID, Jméno, Email vybraného uživatele
   → červené varování: "Tuto akci nelze vrátit zpět"
   → tři tlačítka:
       [Ano — vymazat]  → smaže uživatele, oba modály zavřeny
       [Storno]         → zruší akci, oba modály zavřeny
       [Vybrat jiného]  → zavře Modal 2, znovu otevře Modal 1
```

#### Stavový model komponenty

| Vlastnost | Typ | Popis |
|---|---|---|
| `listOpen` | `boolean` | Řídí viditelnost Modalu 1 (seznam) |
| `listUsers` | `User[]` | Uživatelé načtení pro Modal 1 |
| `listLoading` | `boolean` | Stav načítání v Modalu 1 |
| `listError` | `string` | Chybová zpráva v Modalu 1 |
| `confirmOpen` | `boolean` | Řídí viditelnost Modalu 2 (potvrzení) |
| `selected` | `User \| null` | Uživatel vybraný ke smazání |
| `message` | `string` | Zpráva o úspěšném smazání |
| `error` | `string` | Chybová zpráva po neúspěšném smazání |

#### Metody

| Metoda | Popis |
|---|---|
| `openList()` | Otevře Modal 1, načte uživatele, zavře Modal 2 |
| `pickUser(user)` | Uloží vybraného uživatele, zavře Modal 1, otevře Modal 2 |
| `confirmDelete()` | Volá `deleteUser(id)`, po úspěchu zavře Modal 2 a vyčistí stav |
| `cancel()` | Zavře Modal 2 bez smazání |
| `pickAnother()` | Zavře Modal 2, znovu zavolá `openList()` |

#### Vizuální odlišení od Modify

| Prvek | Modify | Delete |
|---|---|---|
| Trigger tlačítko | tmavé (`.btn-select`) | červené (`.btn-danger`) |
| Hover v seznamu | modrý | červený |
| Hlavička Modalu 2 | — | červená (`.danger`) |
| Druhý modal | není | potvrzení se třemi akcemi |

### UserModifyComponent (`/user/modify`)

Komponenta používá dvoustupňový tok: nejdřív výběr uživatele přes modální okno, pak editace formuláře.

#### Postup

```
1. Uživatel klikne na "Vybrat uživatele"
        ↓
2. Otevře se modální okno
   → volá UserService.getUsers() (GET /api/db/users)
   → zobrazí tabulku všech uživatelů
        ↓
3. Uživatel klikne na řádek
   → modal se zavře
   → formulář se předvyplní: id, name, email vybraného uživatele
   → zobrazí se badge "Vybraný uživatel: #ID"
        ↓
4. Uživatel upraví pole (obě nepovinná)
   → prázdné pole = nezměněno (neposílá se)
        ↓
5. Odeslání formuláře
   → volá UserService.modifyUser(id, patch) (PATCH /api/db/modify/{id})
   → zobrazí potvrzení nebo chybu
```

#### Stavový model komponenty

| Vlastnost | Typ | Popis |
|---|---|---|
| `id` | `number` | ID vybraného uživatele (0 = nevybráno) |
| `name` | `string` | Předvyplněné / upravené jméno |
| `email` | `string` | Předvyplněný / upravený email |
| `modalOpen` | `boolean` | Řídí viditelnost modálního okna |
| `modalUsers` | `User[]` | Seznam uživatelů načtený pro modal |
| `modalLoading` | `boolean` | Stav načítání v modalu |
| `modalError` | `string` | Chybová zpráva v modalu |
| `message` | `string` | Zpráva o úspěchu formuláře |
| `error` | `string` | Chybová zpráva formuláře |

#### Metody

| Metoda | Popis |
|---|---|
| `openModal()` | Otevře modal a načte uživatele přes `getUsers()` |
| `selectUser(user)` | Vyplní formulář daty uživatele a zavře modal |
| `closeModal()` | Zavře modal bez výběru (také backdrop klik) |
| `modifyUser()` | Sestaví patch objekt a odešle PATCH request |

#### Modal — chování

- Otevření: tlačítko **Vybrat uživatele**
- Zavření: klik na `✕`, klik na tmavý backdrop, nebo výběr uživatele
- Stavy v modalu: načítání / chyba / tabulka uživatelů
- Hover na řádku zvýrazní modrým pozadím jako vizuální hint pro kliknutí

#### Validace před odesláním

- `id` musí být > 0 (uživatel musí být vybrán přes modal)
- alespoň jedno z polí (`name` nebo `email`) musí být vyplněno
- prázdná pole se do `patch` objektu nepřidávají → backend je nezmění

---

## Design systém (`styles.css`)

Globální CSS bez externích frameworků.

### Barvy

| Proměnná (CSS třída) | Barva | Použití |
|---|---|---|
| `.topbar` | `#1a1a2e` (tmavě modrá) | Navigační lišta |
| `.btn-primary` | `#4361ee` (modrá) | Tlačítko Přidat |
| `.btn-danger` | `#e53e3e` (červená) | Tlačítko Smazat |
| `.btn-warning` | `#dd6b20` (oranžová) | Tlačítko Uložit změny |
| `.alert-success` | `#c6f6d5` (zelená) | Zpráva o úspěchu |
| `.alert-error` | `#fed7d7` (červená) | Chybová zpráva |

### CSS třídy

| Třída | Popis |
|---|---|
| `.topbar` | Horní lišta s názvem aplikace a navigací |
| `.topbar-title` | Název aplikace vlevo |
| `.topbar-version` | Verze aplikace (malý text vpravo od názvu) |
| `.page` | Centrovaný obsah (max 860px) |
| `.card` | Bílý box se stínem — obal formuláře nebo tabulky |
| `.form-group` | Wrapper pro label + input |
| `.btn` | Základní styl tlačítka |
| `.table-wrapper` | Horizontální scroll pro tabulku |
| `.alert` | Zpráva (success / error) |
| `.status-text` | Šedý text (Načítání...) |
| `.btn-select` | Tmavé tlačítko pro otevření modalu |
| `.selected-badge` | Modrý odznak s ID vybraného uživatele |
| `.modal-backdrop` | Poloprůhledné pozadí přes celou obrazovku (z-index 100) |
| `.modal` | Bílý dialog vycentrovaný na obrazovce (z-index 101) |
| `.modal-header` | Tmavá hlavička modalu s názvem a tlačítkem zavřít |
| `.modal-row` | Klikatelný řádek tabulky v modalu |

---

## Verze

| Verze | Datum | Změna |
|---|---|---|
| 1.0.8 | 2026-06-04 | Design systém, komponenty add/delete/modify napojeny na `/api/db/*` |
| 1.0.9 | 2026-06-04 | UserModifyComponent — modální výběr uživatele před editací |
| 1.1.0 | 2026-06-04 | UserDelComponent — dvoustupňový modální tok: výběr + potvrzení se třemi akcemi |
