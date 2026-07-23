# SamsungControll

SamsungControll é um controle remoto Android local-first para TVs Samsung. O app descobre TVs na rede local, conecta via WebSocket e envia comandos de controle remoto sem depender de Samsung Account ou integração com SmartThings.

## Objetivo

O projeto busca ser um controle simples, direto e privado para uso na rede local:

- Sem login Samsung.
- Sem cloud própria.
- Sem analytics.
- Sem automações frágeis dependentes do estado interno de apps de streaming.
- Com persistência segura de tokens e pareamento local com a TV.
- Respeitando o padrão **SOLID** de arquitetura de software.

## Funcionalidades

- Busca de TVs Samsung na rede local via SSDP/UPnP com filtragem estrita de dispositivos Samsung/Tizen.
- Entrada manual de IP com botão dedicado quando a TV não for descoberta automaticamente.
- Apelidos personalizados para TVs salvas (*TV da Sala*, *TV do Quarto*), armazenados com criptografia no dispositivo.
- Barra superior redesenhada com emblema glassmorphism da marca e **Menu de Opções de 3 pontos (`MoreVert`)**:
  - Apelidar TV;
  - Reconectar TV;
  - Exibir IP do dispositivo conectado.
- Resolução e persistência do endereço MAC da TV para Wake-on-LAN.
- Wake-on-LAN ao ligar a TV pelo app quando ela está desconectada.
- Wake-on-LAN antes da reconexão automática com a última TV usada.
- Feedback tátil (vibração hática) configurável via `HapticsManager`.
- Animações sutis de compressão com mola física (`pressScale`) ao pressionar botões do controle.
- Emissor LED infravermelho/Wi-Fi virtual com brilho dinâmico ao transmitir comandos.
- Controle de navegação:
  - D-Pad neomórfico (Cima, Baixo, Esquerda, Direita);
  - OK esculpido centralizado;
  - voltar (BACK);
  - home (HOME);
  - exit (EXIT).
- Controle de mídia e volume:
  - power;
  - mute;
  - controle vertical de volume (+ / -);
  - controle vertical de canal (+ / -).
- Atalhos para apps:
  - Netflix;
  - YouTube;
  - Prime Video.
- Splash screen com logo do projeto.
- Ícone adaptativo do app redimensionado para se ajustar às bordas do Android.

## Stack

- Kotlin.
- Android SDK 37.
- Jetpack Compose com Material 3.
- AndroidX Lifecycle ViewModel.
- OkHttp para HTTP/WebSocket.
- Gradle Kotlin DSL.

## Arquitetura & SOLID

Principais classes e abstrações:

- `MainActivity`: ponto de entrada e host do Compose.
- `RemoteControlScreen`: tela principal do controle remoto com header simétrico e opções de 3 pontos.
- `RemoteViewModel`: estado de UI, descoberta, conexão, apelidos e ações do controle.
- `HapticsManager`: interface desacoplada para feedback tátil (DIP/ISP), fornecendo `AndroidHapticsManager` e `NoOpHapticsManager`.
- `PressAnimation`: modificador reutilizável `Modifier.pressScale` baseado em animações de mola do Compose.
- `TvController`: contrato de controle remoto.
- `SamsungTvController`: implementação para TVs Samsung via WebSocket/API local.
- `DiscoveryService`: contrato de descoberta.
- `TvDiscovery`: descoberta SSDP/UPnP filtrada estritamente para Smart TVs Samsung.
- `WakeOnLanSender`: envio de magic packets para acordar a TV.
- `MacAddressResolver`: leitura do cache ARP local para associar IP e MAC.
- `SecureTvPreferences`: persistência segura de IP, apelidos, identidade, MAC, token e fingerprint de certificado.

## Segurança

O app foi estruturado para reduzir riscos comuns em apps de controle remoto local:

- Só permite conexão com IPs/hosts de rede local.
- Armazena tokens com Android Keystore.
- Associa tokens, apelidos e MACs por identidade SSDP e por IP, reduzindo perda de pareamento quando o IP muda.
- Exclui preferências sensíveis de backup/cloud transfer.
- Usa pinagem TOFU do certificado da TV.
- Ignora Wake-on-LAN e resolução de MAC para hosts fora da rede local.

## Requisitos

- Android Studio recente.
- JDK compatível com o Gradle configurado.
- Android SDK 37.1 instalado.
- Dispositivo Android conectado na mesma rede local da TV.
- TV Samsung com controle remoto por rede habilitado.
- TV configurada para aceitar Wake-on-LAN/ligar por rede, quando esse recurso for usado.

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

## Licença

Distribuído sob a licença MIT. Veja [LICENSE](LICENSE).
