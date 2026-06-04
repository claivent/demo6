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

- Formulář: ID uživatele
- Po úspěchu zobrazí zprávu ze serveru
- Validace: ID musí být > 0

### UserModifyComponent (`/user/modify`)

- Formulář: ID + volitelné nové Jméno + volitelný nový Email
- Posílá pouze vyplněná pole (prázdné pole = beze změny)
- Validace: musí být vyplněno alespoň jedno z polí jméno/email

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

---

## Verze

| Verze | Datum | Změna |
|---|---|---|
| 1.0.8 | 2026-06-04 | Design systém, komponenty add/delete/modify napojeny na `/api/db/*` |
