# FrotaMind

Aplicativo Android nativo para operacao logistica, controle de abastecimento de ARLA/DIESEL, evidencias, afericao de odometro, indicadores operacionais e modulo de seguranca com eventos importados da Maxtrack.

## Principais modulos

- Abastecimento de ARLA e DIESEL com evidencias.
- Controle financeiro por litro, valor total e custo por km.
- Controle de odometro inicial/final e afericao periodica.
- Seguranca operacional com importacao de eventos.
- Dashboards e indicadores para operacao e gestao.
- Funcionamento local/offline com base Room.

## Tecnologias

- Android nativo
- Java
- XML layouts
- Room
- Retrofit
- WorkManager
- Material Components

## Como compilar

```powershell
.\gradlew.bat :app:assembleDebug
```

## Como testar

```powershell
.\gradlew.bat :app:testDebugUnitTest
```

## Observacoes

O arquivo `local.properties`, caches do Gradle, builds gerados e dados locais do Android Studio nao devem ser versionados.
