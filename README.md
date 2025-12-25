# Sistema de Gestão Agrícola

Este projeto é uma refatoração completa de um sistema legado, migrado para uma arquitetura moderna utilizando **Java** com **Spring Boot** no backend e **JavaFX** para a interface gráfica desktop. O sistema utiliza **SQLite** como banco de dados local.

## 🚀 Tecnologias Utilizadas

*   **Linguagem**: Java 21+
*   **Backend**: Spring Boot 3 (Data JPA, Validation)
*   **Frontend**: JavaFX (FXML)
*   **Banco de Dados**: SQLite
*   **Gerenciamento de Dependências**: Maven
*   **Testes**: JUnit 5, Mockito, TestFX (para testes de UI)

## 📦 Módulos e Funcionalidades

O sistema é dividido em módulos principais para atender ao fluxo de trabalho agrícola:

*   **Gestão de Produtores**: Cadastro, listagem e edição de dados dos produtores rurais.
*   **Serviços**: Catálogo de serviços oferecidos (ex: Gradagem, Plantio) com definição de valores/hora.
*   **Execuções**: Registro das atividades realizadas para cada produtor, calculando custos automaticamente.
*   **Pagamentos**: Controle financeiro dos pagamentos recebidos pelas execuções.
*   **Relatórios**: Geração de relatórios gerenciais e gráficos para análise de dados.
*   **Backup**: Sistema automático de backup do banco de dados ao iniciar a aplicação, mantendo as 10 versões mais recentes.

## 🛠️ Como Executar

### Pré-requisitos
*   Java JDK 21 ou superior instalado.
*   Maven instalado.

### Passos
1.  Clone o repositório.
2.  Navegue até a pasta raiz do projeto.
3.  Execute o comando abaixo para limpar, compilar e iniciar a aplicação:

```bash
mvn clean javafx:run
```

Ou, se preferir usar o plugin do Spring Boot (dependendo da configuração do `pom.xml`):

```bash
mvn spring-boot:run
```

A aplicação iniciará e abrirá a janela principal do sistema.

## 📂 Estrutura do Projeto

*   `src/main/java/com/agricultura/sistema`: Código fonte Java.
    *   `model`: Entidades JPA (Produtor, Servico, Execucao, Pagamento).
    *   `repository`: Interfaces de acesso a dados (Repositories).
    *   `service`: Regras de negócio.
    *   `controller`: Controladores JavaFX.
    *   `view`: Arquivos FXML da interface gráfica (localizados no resources).
*   `src/main/resources`: Arquivos de configuração (`application.properties`) e views (`.fxml`).

## 🛡️ Segurança e Logs

*   Logs de execução são salvos periodicamente na pasta `logs/`.
*   Backups do banco de dados são gerados automaticamente na pasta `backups/`.