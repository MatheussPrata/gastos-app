# GastosApp - Controle de Gastos Pessoal

App Android nativo para controle de gastos pessoais, construído com **Kotlin + Jetpack Compose + Room + Hilt**.

## Funcionalidades

- 📝 **Registro de gastos e receitas** com descrição, valor, data, categoria
- 💳 **Suporte a múltiplos cartões de crédito** (limite, fechamento, vencimento, parcelas)
- 📂 **Categorias e subcategorias** personalizáveis (criar, editar, excluir)
- 📊 **Relatórios** por categoria, cartão e tendências mensais
- ⚠️ **Alertas de limite de gasto** por categoria (WorkManager + notificações)
- ☁️ **Backup no Google Drive** (exportar/importar JSON)
- 🎨 **Tema dinâmico Material You** - usuário escolhe a cor de destaque
- 💾 **Armazenamento local** com Room (SQLite)

## Tech Stack

| Camada | Tecnologia |
|--------|------------|
| Linguagem | Kotlin 1.9.24 |
| UI | Jetpack Compose + Material 3 |
| Arquitetura | MVVM + Repository + Hilt (DI) |
| Banco de Dados | Room (SQLite) |
| Navegação | Navigation Compose |
| Gráficos | compose-charts |
| Background | WorkManager |
| Backup | Google Drive API |
| Serialização | kotlinx-serialization (JSON) |

## Estrutura do Projeto

```
gastos-app/
├── app/
│   ├── src/main/java/com/matheus/gastos/
│   │   ├── data/
│   │   │   ├── entity/       # Entidades Room (Category, Subcategory, CreditCard, Transaction, BudgetLimit, AppSettings)
│   │   │   ├── dao/          # DAOs Room
│   │   │   ├── local/        # AppDatabase + Converters
│   │   │   ├── repository/   # Repositories (Category, CreditCard, Transaction, BudgetLimit, Settings)
│   │   │   └── backup/       # DriveBackupManager
│   │   ├── di/               # Hilt Modules
│   │   ├── ui/
│   │   │   ├── theme/        # GastosTheme, Typography, Colors
│   │   │   ├── navigation/   # AppNavHost, Screen routes
│   │   │   ├── screen/
│   │   │   │   ├── main/         # MainScreen (Bottom Nav host)
│   │   │   │   ├── expenses/     # ExpensesScreen
│   │   │   │   ├── cards/        # CardsScreen
│   │   │   │   ├── reports/      # ReportsScreen
│   │   │   │   ├── settings/     # SettingsScreen
│   │   │   │   ├── add_expense/  # AddExpenseScreen
│   │   │   │   ├── add_card/     # AddCardScreen
│   │   │   │   └── categories/   # CategoriesScreen
│   │   │   └── components/   # Componentes reutilizáveis
│   │   ├── viewmodel/        # ViewModels (Category, CreditCard, Transaction, BudgetLimit, Settings)
│   │   ├── worker/           # BudgetAlertWorker
│   │   ├── GastosApplication.kt
│   │   └── MainActivity.kt
│   └── src/main/res/         # Resources (strings, colors, themes, xml)
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
└── gradle/wrapper/
```

## Como Executar

### Pré-requisitos
- Android Studio Ladybug (2024.2.1) ou superior
- JDK 17
- Android SDK 34
- Dispositivo/Emulador Android 7.0 (API 24) ou superior

### Passos

1. **Clone/abra o projeto no Android Studio**
   ```
   File > Open > Selecione a pasta gastos-app
   ```

2. **Sincronize o Gradle**
   - Clique em "Sync Now" ou `File > Sync Project with Gradle Files`

3. **Configure Google Drive API (opcional, para backup)**
   - Acesse [Google Cloud Console](https://console.cloud.google.com/)
   - Crie um projeto ou use existente
   - Ative a **Google Drive API**
   - Crie credenciais OAuth 2.0 (Android)
   - Adicione o `client_id` no `google-services.json` (colocar em `app/`)

4. **Execute no dispositivo/emulador**
   - `Run > Run 'app'` ou `Shift+F10`

## Configuração do Google Drive Backup

O backup usa a API Google Drive com escopo `drive.file` (acesso apenas aos arquivos criados pelo app).

1. No Google Cloud Console, configure a **OAuth Consent Screen**
2. Adicione o escopo `https://www.googleapis.com/auth/drive.file`
3. Crie credenciais **OAuth 2.0 Client ID** para Android
   - Package name: `com.matheus.gastos`
   - SHA-1: Obtenha com `./gradlew signingReport`
4. Baixe o `google-services.json` e coloque em `app/`

## Build

```bash
# Debug APK
./gradlew assembleDebug

# Release APK (sem assinatura)
./gradlew assembleRelease

# Testes
./gradlew test
./gradlew connectedAndroidTest
```

## Próximos Passos (Roadmap)

- [ ] Implementar gráficos reais com `compose-charts`
- [ ] Adicionar categorias padrão pré-carregadas na primeira execução
- [ ] Melhorar seletor de data (Material Date Picker)
- [ ] Implementar exportação/importação CSV
- [ ] Adicionar autenticação biométrica/PIN
- [ ] Compartilhamento familiar (cloud sync)
- [ ] Widgets de homescreen
- [ ] Modo offline-first com sync posterior
- [ ] Testes unitários e de UI

## Licença

MIT License - sinta-se livre para usar e modificar.