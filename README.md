# SamsungControll

SamsungControll é um controle remoto Android local-first para TVs Samsung. O app descobre TVs na rede local, conecta via WebSocket e envia comandos de controle remoto sem depender de Samsung Account ou integração com SmartThings.

## Objetivo

O projeto busca ser um controle simples, direto e privado para uso na rede local:

- Sem login Samsung.
- Sem cloud própria.
- Sem analytics.
- Sem automações frágeis dependentes do estado interno de apps de streaming.
- Com persistência segura de tokens e pareamento local com a TV.

## Funcionalidades

- Busca de TVs Samsung na rede local via SSDP/UPnP.
- Reconexão automática com a última TV usada.
- Controle de navegação:
  - direcional;
  - OK;
  - voltar;
  - home;
  - exit.
- Controle de mídia básico:
  - power;
  - mute;
  - volume;
  - canal.
- Atalhos para apps:
  - Netflix;
  - YouTube;
  - Prime Video.
- Splash screen com logo do projeto.
- Ícone adaptativo do app baseado na marca do projeto.

## Stack

- Kotlin.
- Android SDK 37.
- Jetpack Compose.
- Material 3.
- AndroidX Lifecycle ViewModel.
- OkHttp para HTTP/WebSocket.
- Gradle Kotlin DSL.

## Arquitetura

Principais classes:

- `MainActivity`: ponto de entrada e host do Compose.
- `RemoteControlScreen`: tela principal do controle remoto.
- `RemoteViewModel`: estado de UI, descoberta, conexão e ações do controle.
- `TvController`: contrato de controle remoto.
- `SamsungTvController`: implementação para TVs Samsung via WebSocket/API local.
- `DiscoveryService`: contrato de descoberta.
- `TvDiscovery`: descoberta SSDP/UPnP na rede local.
- `SecureTvPreferences`: persistência segura de IP, token e fingerprint de certificado.
- `LocalNetworkValidator`: validação de host/IP local antes de conectar.

## Segurança

O app foi estruturado para reduzir riscos comuns em apps de controle remoto local:

- Só permite conexão com IPs/hosts de rede local.
- Armazena tokens com Android Keystore.
- Exclui preferências sensíveis de backup/cloud transfer.
- Usa pinagem TOFU do certificado da TV.
- Evita aceitar hosts arbitrários.

### Sobre TLS e TVs Samsung

Algumas TVs Samsung usam certificados próprios no endpoint local `wss://IP:8002`. Por isso, o app usa pinagem TOFU:

1. Na primeira conexão, salva o fingerprint do certificado da TV.
2. Nas próximas conexões, rejeita certificados diferentes para o mesmo IP.

Isso é mais seguro do que aceitar qualquer certificado silenciosamente, mas ainda pressupõe que a primeira conexão foi feita em uma rede confiável.

### Compatibilidade por modelo de TV

TVs Samsung de anos/modelos diferentes podem variar em:

- portas disponíveis;
- comportamento da autorização;
- suporte a token;
- IDs de aplicativos;
- resposta da API REST de apps.

O app tenta primeiro `wss://IP:8002` e faz fallback para `ws://IP:8001` quando necessário.

## Requisitos

- Android Studio recente.
- JDK compatível com o Gradle configurado.
- Android SDK 37.1 instalado.
- Dispositivo Android conectado na mesma rede local da TV.
- TV Samsung com controle remoto por rede habilitado.

## Como Rodar

Clone ou abra o projeto no Android Studio e execute o módulo `app`.

Via terminal:

```bash
./gradlew test
./gradlew lint
./gradlew assembleDebug
```

Para gerar release local:

```bash
./gradlew assembleRelease
```

## Validação Recomendada

Antes de entregar mudanças, rode:

```bash
./gradlew test lint assembleRelease
```

Estado atual esperado:

```text
BUILD SUCCESSFUL
No issues found.
```

## Uso

1. Conecte o celular e a TV na mesma rede local.
2. Abra o app.
3. Toque no botão de busca.
4. Selecione a TV encontrada.
5. Aceite a autorização na TV, se solicitado.
6. Use o controle remoto no app.

Depois da primeira conexão, o app salva a última TV e tenta reconectar automaticamente.

## Troubleshooting

### A TV não aparece na busca

Verifique:

- celular e TV estão na mesma rede;
- VPN está desligada;
- isolamento de clientes no roteador está desativado;
- permissões de rede/Wi-Fi estão disponíveis;
- a TV está ligada ou em estado que aceite controle remoto de rede.

### Conexão falha

Possíveis causas:

- TV recusou autorização;
- IP salvo mudou;
- certificado da TV mudou;
- TV está em rede diferente;
- portas `8001` ou `8002` bloqueadas.

Tente:

1. Buscar a TV novamente.
2. Selecionar o IP correto.
3. Confirmar a permissão exibida na TV.
4. Reiniciar a TV se ela não responder à API local.

### Apps não abrem

Os IDs de apps podem variar por região/modelo/firmware. Atualmente:

- Netflix: `3201907018807`
- YouTube: `111299001912`
- Prime Video: `3201910019365`

Se algum app não abrir, o fallback tenta enviar o evento via WebSocket.

## Build e Release

Configuração principal:

- `applicationId`: `com.example.samsungcontroll`
- `minSdk`: 24
- `targetSdk`: 37
- `compileSdk`: 37.1
- R8/minify habilitado em release.
- Shrink resources habilitado em release.

## Estrutura de Pastas

```text
app/src/main/java/com/example/samsungcontroll
app/src/main/java/com/example/samsungcontroll/ui/components
app/src/main/java/com/example/samsungcontroll/ui/screens
app/src/main/java/com/example/samsungcontroll/ui/theme
app/src/main/res
```

## Licença

Distribuído sob a licença MIT. Veja [LICENSE](LICENSE).
