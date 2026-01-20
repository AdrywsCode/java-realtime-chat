# Java Realtime Chat

Aplicação de chat em tempo real desenvolvida em **Java puro**, utilizando **Sockets TCP** e **Multithreading**, permitindo múltiplos clientes simultâneos via terminal.

Agora com **salas (rooms)**, **mensagens privadas**, **lista de usuários por sala** e arquitetura preparada para GUI (Swing).

---

## 🚀 Funcionalidades

* Múltiplos clientes simultâneos
* Comunicação em tempo real
* Servidor centralizado
* Sistema de nick
* Mensagens públicas por sala
* Mensagens privadas (PM)
* Sistema de salas (rooms)
* Lista de usuários na sala (`/who`)
* Troca de sala (`/join nomeDaSala`)
* Execução via terminal (console)

---

## 🛠️ Tecnologias utilizadas

* Java (puro, sem frameworks)
* Sockets TCP
* Threads
* Programação concorrente
* Arquitetura cliente-servidor

---

## 📁 Estrutura do projeto

```
java-realtime-chat/
  src/
    common/
      Protocol.java
    client/
      ChatConnection.java
      ChatClientMain.java
    server/
      ChatServer.java
      ClientHandler.java
```

---

## ▶️ Como compilar

No CMD (Windows), dentro da pasta do projeto:

```bat
rmdir /s /q out
mkdir out
javac -d out src\common\Protocol.java src\client\ChatConnection.java src\client\ChatClientMain.java src\server\ChatServer.java src\server\ClientHandler.java
```

---

## ▶️ Como executar

### 1. Inicie o servidor

Abra um terminal:

```bat
java -cp out server.ChatServer
```

---

### 2. Inicie os clientes

Abra um ou mais terminais:

```bat
java -cp out client.ChatClientMain
```

Cada terminal representa um usuário diferente.

---

## 💬 Comandos disponíveis

| Comando             | Função                           |
| ------------------- | -------------------------------- |
| Texto normal        | Envia mensagem para a sala atual |
| `/pm nick mensagem` | Envia mensagem privada           |
| `/join sala`        | Entra/cria uma sala              |
| `/who`              | Lista usuários da sala           |
| `/quit`             | Sai do chat                      |

---

## 🏠 Sistema de Salas (Rooms)

* Todo usuário começa na sala `lobby`
* Ao entrar em outra sala, você sai da atual
* Mensagens públicas só aparecem para usuários da mesma sala
* Exemplo:

```text
/join games
/join java
```

---

## 📸 Screenshots

> Adicione prints do servidor e de múltiplos clientes rodando.

---

## 📌 Próximos upgrades planejados

* Interface gráfica (Swing)
* Autenticação (login e registro)
* Histórico persistente
* Notificações
* Lista visual de usuários

---

## 👨‍💻 Autor

Desenvolvido por **Jhonatan**

---

## 📄 Licença

Este projeto é livre para fins educacionais e de aprendizado.


