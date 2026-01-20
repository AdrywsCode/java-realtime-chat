# Java Realtime Chat

Aplicação de chat em tempo real desenvolvida em **Java puro**, utilizando **Sockets TCP** e **Multithreading**, permitindo múltiplos clientes se conectarem simultaneamente via console.

---

## 🚀 Funcionalidades

* Múltiplos clientes simultâneos
* Comunicação em tempo real
* Servidor centralizado
* Sistema de nick
* Mensagens públicas
* Mensagens privadas
* Execução via terminal (console)

---

## 🛠️ Tecnologias utilizadas

* Java (puro, sem frameworks)
* Sockets TCP
* Threads
* Programação concorrente

---

## 📁 Estrutura do projeto

```
java-realtime-chat/
  src/
    client/
      ChatClient.java
    server/
      ChatServer.java
      ClientHandler.java
```

---

## ▶️ Como executar

### 1. Compile o projeto

No CMD (Windows), dentro da pasta do projeto:

```bat
mkdir out
javac -d out src\client\ChatClient.java src\server\ChatServer.java src\server\ClientHandler.java
```

---

### 2. Execute o servidor

Abra um terminal:

```bat
java -cp out server.ChatServer
```

---

### 3. Execute os clientes

Abra outro(s) terminal(is):

```bat
java -cp out client.ChatClient
```

Cada terminal representa um usuário diferente.

---

## 💬 Comandos disponíveis

| Comando             | Função           |
| ------------------- | ---------------- |
| Mensagem normal     | Envia para todos |
| `/pm nick mensagem` | Mensagem privada |
| `/quit`             | Sai do chat      |

---

## 📸 Screenshots

> Adicione prints do servidor e dos clientes rodando aqui.

---

## 📌 Próximos upgrades (em desenvolvimento)

* Interface gráfica (Swing)
* Lista de usuários online
* Histórico de mensagens
* Sistema de login
* Criptografia

---

## 👨‍💻 Autor

Desenvolvido por **Jhonatan**

---

## 📄 Licença

Este projeto é de uso livre para fins educacionais.


