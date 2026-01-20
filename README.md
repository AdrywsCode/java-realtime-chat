# Java Realtime Chat

Chat em tempo real feito em Java puro com sockets TCP e multithreading. Suporta varios clientes simultaneos, salas, mensagens privadas e GUI em Swing.

## Funcionalidades
- Multiplos clientes simultaneos
- Mensagens em tempo real por sala
- Mensagens privadas (PM)
- Sistema de salas (`/join`)
- Lista de usuarios na sala (`/who`)
- Admin (primeiro nick conectado) com `/kick` e `/ban`
- GUI em Swing com historico por sala, limpar e lista de usuarios
- Logs do servidor com rotacao diaria

## Tecnologias
- Java (puro)
- Sockets TCP
- Threads
- Arquitetura cliente-servidor

## Estrutura
```
java-realtime-chat/
  src/
    common/
      Protocol.java
    client/
      ChatConnection.java
      ChatClientMain.java
      gui/
        ChatWindow.java
    server/
      ChatServer.java
      ClientHandler.java
  out/
```

## Como compilar (Windows)
No PowerShell dentro da pasta do projeto:
```powershell
rg --files -g "*.java" > .sources.tmp
javac -encoding UTF-8 -d out @.sources.tmp
del .sources.tmp
```

Ou use o script:
```powershell
.\build-and-run.bat
```

## Como executar
Servidor:
```powershell
java -cp out server.ChatServer
```

Cliente GUI:
```powershell
java -cp out client.gui.ChatWindow
```

Cliente terminal:
```powershell
java -cp out client.ChatClientMain
```

Abrir varios clientes:
```powershell
.\open-clients.bat 3
.\open-cli-clients.bat 3
```

## Comandos
| Comando | Funcao |
| --- | --- |
| Texto normal | Envia mensagem para a sala atual |
| `/pm nick mensagem` | Mensagem privada |
| `/join sala` | Entra/cria uma sala |
| `/who` | Lista usuarios da sala |
| `/kick nick` | Desconecta (admin) |
| `/ban nick` | Bane (admin) |
| `/quit` | Sai do chat |

## Regras importantes
- Todo usuario entra na sala `lobby`.
- O primeiro nick conectado vira admin.
- O `/kick` desconecta e permite reconectar.
- O `/ban` bloqueia o nick para novas conexoes.

## Logs
O servidor grava logs em arquivos diaros:
```
server-YYYY-MM-DD.log
```

## Autor
Desenvolvido por Jhonatan.
