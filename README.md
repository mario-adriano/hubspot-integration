
---

# 🚀 Integração com HubSpot usando Spring Boot e PostgreSQL

Projeto desenvolvido em **Java 21** com **Spring Boot**, integrando autenticação via **OAuth 2.0** e gerenciamento de contatos com o **HubSpot**, utilizando **PostgreSQL** como banco de dados.

A aplicação segue padrões de arquitetura bem definidos, separando claramente as responsabilidades em **Model**, **Controller**, **Service** e **Repository**, garantindo maior facilidade de manutenção e escalabilidade.

Para maior segurança, a aplicação não expõe diretamente os tokens do HubSpot. Em vez disso, gera seus próprios tokens internos para gerenciar os tokens do HubSpot, incluindo o uso de **refresh_token**.

---

## ✨ Tecnologias Utilizadas

- **Java 21**  
- **Spring Boot**  
- **PostgreSQL** (Banco robusto com suporte nativo a JSON através do tipo `jsonb`)  
- **Liquibase** (Gerenciamento ágil de migrações do banco de dados)  
- **Spring Boot DevTools** (Experiência fluída em desenvolvimento com hot reload)  
- **Lombok** (Redução de código boilerplate através de anotações)  
- **Spring Data JPA** (Produtividade no acesso a dados e consultas SQL)  

---

## 📦 Requisitos

### ✅ Com DevContainer (Recomendado)

Este projeto possui um **DevContainer** configurado que já inclui o ambiente Java, PostgreSQL e todas as dependências.

1. Renomeie o arquivo `.env.example` para `.env` e configure as variáveis necessárias:

   ```env
   HUBSPOT_CLIENT_ID=
   HUBSPOT_CLIENT_SECRET=
   ```

   🔹 **Atenção:** Obtenha esses valores no **HubSpot Developer** ao configurar sua aplicação.

2. **Escopos necessários:**

   - `crm.objects.contacts.read`
   - `crm.objects.contacts.write`

3. **URL de redirecionamento:**

   ```plaintext
   http://localhost:8080/hubspot/callback
   ```

4. Abra o projeto com o **DevContainer** no **VS Code**.

---

### ❌ Sem DevContainer

Se preferir rodar o projeto sem DevContainer:

1. Tenha instalado:
   - **Java 21**
   - **PostgreSQL**

2. Altere o arquivo `application-dev.properties` com as configurações do seu banco:

   ```properties
   spring.datasource.url=jdbc:postgresql://localhost:5432/hubspot-integration
   ```

---

## 🚀 Execução do Projeto

Dentro da pasta raiz do projeto, execute os seguintes comandos:

```sh
./mvnw clean install      # Compila o projeto e instala as dependências
./mvnw liquibase:update   # Executa as migrações do banco
./mvnw spring-boot:run    # Inicia a aplicação
```

### Explicação dos Comandos:

- `./mvnw clean install`: Remove versões anteriores compiladas, recompila o projeto e instala as dependências necessárias.
- `./mvnw liquibase:update`: Executa scripts de migração do banco, atualizando o esquema.

---

## 🌐 Documentação da API

Acesse a documentação via **Swagger**:

🔗 [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

### 🔑 Fluxo OAuth com HubSpot

1. Acesse o endpoint `/hubspot/authorize` para obter o **link de autorização** do HubSpot.
2. Use esse link para fazer login com uma conta HubSpot (não desenvolvedora).
3. Após o login, você será redirecionado para `/hubspot/callback` com o **token Bearer**.
4. Utilize esse **token Bearer** para autenticação no **Swagger** ou em ferramentas como **Postman**.

📌 **Exemplo de link de autorização:**

```plaintext
https://app.hubspot.com/oauth/authorize?client_id=SEU_CLIENT_ID&redirect_uri=http://localhost:8080/hubspot/callback&scope=crm.objects.contacts.read&response_type=code
```

---

## 📬 Testando Webhooks com ngrok

Para expor o ambiente local e testar webhooks:

1. **Instale e execute o ngrok:**

   ```sh
   ngrok http 8080
   ```

2. **Configure o webhook no HubSpot:**

   - **URL:** `{URL_DO_NGROK}/hubspot/webhook`
   - **Objeto:** Contato
   - **Evento:** Criado

3. **Ative a assinatura** para que os eventos sejam enviados ao seu servidor.

---

## 📖 Decisões Técnicas e Possíveis Melhorias

### 💡 Decisões Técnicas

- **PostgreSQL**: Selecionado devido ao suporte robusto a JSON (`jsonb`), ideal para armazenar e consultar respostas de webhooks.
- **Liquibase**: Escolhido pela eficiência no gerenciamento de migrações, facilitando o controle das mudanças no esquema do banco de dados.
- **Spring Boot DevTools**: Melhorar significativamente a experiência de desenvolvimento através do **hot reload**, dispensando reinicializações manuais.
- **Lombok**: Reduz o código boilerplate gerando automaticamente construtores, getters e setters.
- **JPA**: Escolhido pela produtividade e facilidade de desenvolvimento com abstrações eficientes sobre consultas SQL.

### 🔧 Melhorias Futuras

- **Limpeza automática de tokens**: Atualmente, tokens internos ficam indefinidamente no banco de dados. Uma futura melhoria pode ser a implementação de um serviço para limpeza automática desses tokens após certo período. Recomenda-se a utilização do **Spring Scheduler** ou **Quartz** para gerenciar tarefas de limpeza periódica.

---

## 🎯 Contribuições

Contribuições são sempre bem-vindas!  

📌 Para colaborar:

1. **Fork** este repositório
2. Crie uma **branch** para sua feature (`git checkout -b minha-feature`)
3. Faça o **commit** das suas alterações (`git commit -m 'Minha nova feature'`)
4. **Push** para a branch (`git push origin minha-feature`)
5. Abra um **Pull Request** 🚀


---

🔥 **Feito com ❤️ e Java!**

--- 
