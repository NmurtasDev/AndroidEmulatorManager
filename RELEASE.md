# Release Pipeline Documentation

## 📦 Automated Release System

Questo progetto utilizza GitHub Actions per creare automaticamente release e pre-release con build multi-piattaforma.

## 🚀 Come Funziona

### 1. Sistema di Versioning

Il sistema di release si basa sui **Git Tags** seguendo il pattern Semantic Versioning:

```
v3.0.0       -> Release stabile
v3.0.0-rc1   -> Pre-release (Release Candidate)
v3.1.0-beta  -> Pre-release (Beta)
v3.1.0-alpha -> Pre-release (Alpha)
```

### 2. Workflow Automatico

Quando crei un tag che inizia con `v`, GitHub Actions:

1. **Estrae la versione** dal tag (es. `v3.0.0` → `3.0.0`)
2. **Determina il tipo di release**:
   - Contiene `-` (es. `-rc1`, `-beta`) → **Pre-release** 🟡
   - Non contiene `-` (es. `3.0.0`) → **Release stabile** 🟢
3. **Builda per tutte le piattaforme** in parallelo:
   - Universal JAR (tutti i sistemi con Java 21+)
   - Windows EXE (richiede Java 21+ installato)
   - Linux DEB (include JRE)
   - Linux AppImage (include JRE)
4. **Crea la release su GitHub** con tutti i file binari allegati
5. **Genera note di rilascio automatiche** dai commit

## 📋 Come Creare una Release

### Metodo 1: Da Linea di Comando

#### Release Stabile

```bash
# 1. Assicurati di essere su main e aggiornato
git checkout main
git pull origin main

# 2. Crea e pusha il tag
git tag v3.0.0
git push origin v3.0.0

# GitHub Actions creerà automaticamente la release!
```

#### Pre-Release (Beta/RC)

```bash
# Per una release candidate
git tag v3.0.0-rc1
git push origin v3.0.0-rc1

# Per una beta
git tag v3.1.0-beta
git push origin v3.1.0-beta
```

### Metodo 2: Da GitHub Web UI

1. Vai su **Releases** → **Draft a new release**
2. Clicca su **Choose a tag**
3. Digita il tag (es. `v3.0.0`) e clicca **Create new tag**
4. Compila titolo e descrizione (opzionale, verrà auto-generata)
5. Spunta **Set as a pre-release** se è una pre-release
6. Clicca **Publish release**

GitHub Actions verrà triggerato automaticamente e aggiungerà i binari.

## 🏗️ Build Matrix

| Platform | File Output | Include JRE | Dimensione ~|
|----------|-------------|-------------|-------------|
| **Universal JAR** | `android-emulator-manager-X.X.X.jar` | ❌ | ~5 MB |
| **Windows EXE** | `android-emulator-manager-X.X.X.exe` | ❌ | ~5 MB |
| **Linux DEB** | `android-emulator-manager_X.X.X_amd64.deb` | ✅ | ~50 MB |
| **Linux AppImage** | `android-emulator-manager-X.X.X-linux.tar.gz` | ✅ | ~50 MB |

## 🔄 Processo Completo di Release

### 1. Preparazione

```bash
# Assicurati che il codice sia stabile
mvn clean test
mvn clean package

# Testa l'applicazione
java -jar target/android-emulator-manager-3.0.0-SNAPSHOT-jar-with-dependencies.jar
```

### 2. Aggiorna la Versione nel POM

```bash
# Modifica pom.xml
<version>3.0.0</version>  # Rimuovi -SNAPSHOT

# Commit
git add pom.xml
git commit -m "Release version 3.0.0"
git push origin main
```

### 3. Crea il Tag

```bash
# Release stabile
git tag -a v3.0.0 -m "Release 3.0.0 - Major UI improvements"
git push origin v3.0.0
```

### 4. Monitora la Build

1. Vai su **Actions** → **Release Build**
2. Verifica che tutte le build completino con successo (✅)
3. La release apparirà automaticamente in **Releases**

### 5. Post-Release

```bash
# Aggiorna la versione per il prossimo sviluppo
# In pom.xml: <version>3.1.0-SNAPSHOT</version>

git add pom.xml
git commit -m "Prepare for next development iteration"
git push origin main
```

## 🛠️ Troubleshooting

### La pipeline fallisce

**Controlla i log su GitHub Actions:**
```
Actions → Release Build → Click sul workflow fallito
```

**Errori comuni:**
- **Maven build failed**: Verifica che `mvn clean package` funzioni localmente
- **Permission denied**: Verifica che `GITHUB_TOKEN` abbia permessi `contents: write`
- **Tag already exists**: Non puoi ricreare un tag esistente, eliminalo prima con `git tag -d vX.X.X && git push origin :refs/tags/vX.X.X`

### Come cancellare una release

```bash
# 1. Cancella la release da GitHub UI (Releases → Click release → Delete)

# 2. Cancella il tag localmente e remotamente
git tag -d v3.0.0
git push origin :refs/tags/v3.0.0
```

### Come modificare una release esistente

Non puoi modificare i binari di una release già pubblicata. Opzioni:
1. **Cancella e ricrea** (⚠️ evita se possibile, rompe i link)
2. **Crea una patch release** (consigliato): `v3.0.1`

## 📊 Workflow File

Il workflow è definito in `.github/workflows/release.yml`:

```yaml
# Trigger: push di tag che iniziano con 'v'
on:
  push:
    tags:
      - 'v*'

# Job principale:
# 1. Checkout del codice
# 2. Setup Java 21
# 3. Build di tutte le piattaforme
# 4. Creazione release con artifacts allegati
```

## 🔐 Sicurezza

- Il workflow usa `GITHUB_TOKEN` fornito automaticamente da GitHub
- Permessi limitati a `contents: write` (solo per creare release)
- Nessun secret personalizzato richiesto
- Build eseguita in ambiente isolato Ubuntu

## 📝 Best Practices

### Versioning
- **Major** (3.0.0): Breaking changes
- **Minor** (3.1.0): Nuove features (backward compatible)
- **Patch** (3.0.1): Bug fixes

### Pre-Release Stages
1. **Alpha** (`v3.1.0-alpha`): Features incomplete, testing interno
2. **Beta** (`v3.1.0-beta`): Features complete, testing pubblico
3. **RC** (`v3.1.0-rc1`): Release candidate, pronti per produzione

### Release Notes
GitHub genera automaticamente note basate sui commit. Per note migliori:

```bash
# Usa commit messages descrittivi
git commit -m "Add dark theme support for accordions"
git commit -m "Fix device info not displaying on card UI"
```

Oppure modifica le note manualmente dopo la pubblicazione.

## 🎯 Esempio Completo: Release 3.0.0

```bash
# Step 1: Finisci lo sviluppo su feature branch
git checkout feature/new-features
git push origin feature/new-features

# Step 2: Merge su main
git checkout main
git pull origin main
git merge feature/new-features
git push origin main

# Step 3: Aggiorna versione
# Modifica pom.xml: <version>3.0.0</version>
git add pom.xml
git commit -m "Release version 3.0.0"
git push origin main

# Step 4: Crea tag e release
git tag -a v3.0.0 -m "Release 3.0.0 - Major UI overhaul with card-based design"
git push origin v3.0.0

# Step 5: Attendi ~5-10 minuti per la build automatica
# Step 6: Verifica su GitHub Releases

# Step 7: Prepara prossima versione
# Modifica pom.xml: <version>3.1.0-SNAPSHOT</version>
git add pom.xml
git commit -m "Prepare for next development iteration"
git push origin main
```

## 📞 Support

Per problemi con la pipeline:
1. Controlla i **GitHub Actions logs**
2. Verifica che `mvn clean package` funzioni localmente
3. Apri un issue su GitHub con i log della pipeline

---

**Ultimo aggiornamento**: 2025-10-30
**Pipeline version**: 1.0
**Maintainer**: Nicola Murtas
