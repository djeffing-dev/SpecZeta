# Comment obtenir le Access Token et le Refresh Token Dropbox

---

## Prérequis

Avant de commencer, récupère ces deux valeurs dans la [console développeur Dropbox](https://www.dropbox.com/developers/apps) → sélectionne ton app → onglet **Settings** :

| Valeur | Où la trouver |
|---|---|
| `APP_KEY` | Visible directement dans Settings |
| `APP_SECRET` | Clique sur **"Show"** pour la révéler |

---

## Étape 1 — Obtenir le code d'autorisation

Ouvre ce lien dans ton navigateur **(remplace `APP_KEY` par ta vraie valeur)** :

```
https://www.dropbox.com/oauth2/authorize?client_id=APP_KEY&response_type=code&token_access_type=offline
```

> Le paramètre `token_access_type=offline` est **obligatoire** — sans lui, Dropbox ne génère pas de refresh token.

Dropbox affiche une page de confirmation → clique **"Autoriser"**.

Tu es redirigé vers une page qui affiche un code du type :

```
sl.xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
```

**Copie ce code immédiatement** — il expire en quelques minutes.

---

## Étape 2 — Échanger le code contre les tokens

Ouvre **PowerShell** et exécute la commande suivante en remplaçant les trois valeurs :

```powershell
Invoke-RestMethod -Method Post `
  -Uri "https://api.dropbox.com/oauth2/token" `
  -Headers @{ Authorization = "Basic " + [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("APP_KEY:APP_SECRET")) } `
  -Body @{
    code       = "TON_CODE_AUTORISATION"
    grant_type = "authorization_code"
  }
```

| Placeholder | Remplacer par |
|---|---|
| `APP_KEY` | Ta clé d'application (ex: `dea3oj82nvrk9cj`) |
| `APP_SECRET` | Ton secret d'application |
| `TON_CODE_AUTORISATION` | Le code copié à l'étape 1 |

---

## Résultat attendu

PowerShell affiche une réponse JSON de ce type :

```json
{
  "access_token"  : "sl.u.xxxxxxxxxxxxxxxxxxxxxxxx",
  "token_type"    : "bearer",
  "expires_in"    : 14400,
  "refresh_token" : "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx",
  "scope"         : "...",
  "account_id"    : "dbid:..."
}
```

| Champ | Durée de vie | Usage |
|---|---|---|
| `access_token` | ~4 heures | Authentification des appels API immédiats |
| `refresh_token` | Permanent (jusqu'à révocation) | Renouvellement automatique de l'access token |

---

## Étape 3 — Stocker les tokens

Ajoute les valeurs obtenues dans tes variables d'environnement (fichier `.env` ou variables système) :

```env
DROPBOX_APP_KEY=ta_app_key
DROPBOX_APP_SECRET=ton_app_secret
DROPBOX_ACCESS_TOKEN=sl.u.xxxxxxxxxxxxxxxxxxxxxxxx
DROPBOX_REFRESH_TOKEN=xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
```

> ⚠️ **Ne jamais** committer ces valeurs dans Git. Assure-toi que ton fichier `.env` est bien listé dans `.gitignore`.

---

## ⚠️ Points d'attention

**Le code d'autorisation (étape 1) est à usage unique** — si la commande PowerShell échoue, tu dois retourner à l'étape 1 et en générer un nouveau.

**Le token doit être sur une seule ligne** — lors du copier-coller, vérifie qu'aucun saut de ligne ou espace ne s'est glissé dans la valeur, sinon Dropbox retournera une erreur `400 Bad Request: Invalid authorization value`.

**Le refresh token ne expire pas** mais il est révoqué si tu déconnectes l'app depuis les paramètres Dropbox ou si tu génères un nouveau jeu de tokens — dans ce cas, reprends depuis l'étape 1.