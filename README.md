# Claris FileMaker Hibernate Dialect

Projeto: Claris FileMaker Hibernate Dialect

Resumo
------
Este projeto fornece um Dialect leve para integrar o banco Claris/FileMaker ao Hibernate 7.x (ex.: 7.1.x). Ele registra os tipos DDL e um conjunto de funções SQL compatíveis com a sintaxe usada pelo driver FileMaker (fmjdbc). O artefato é empacotado como um JAR reutilizável que você pode publicar ou instalar no seu repositório Maven local.

Artefato gerado
----------------
- groupId: io.github.tiagoiwamoto
- artifactId: claris-filemaker
- version: 1.0.0-SNAPSHOT
- Dialect principal: `io.github.tiagoiwamoto.ClarisFilemakerDialect`

Requisitos
---------
- JDK 25 (o projeto está configurado para compatibilidade com Java 25)
- Maven 3.x
- Hibernate Core 7.1.x (dependência usada no pom)
- Um driver `fmjdbc.jar` do FileMaker — presente em `src/main/resources/fmjdbc.jar` neste repositório.

Observação sobre o `fmjdbc.jar`
-------------------------------
O driver `fmjdbc.jar` é um artefato proprietário/externo que não está no repositório Maven central. Neste projeto ele fica em `src/main/resources/fmjdbc.jar` e o `pom.xml` inclui um step para instalar automaticamente esse JAR no repositório local durante a build.

Importante: o `pom.xml` atualmente declara a dependência `fmjdbc` com `scope=system` e `systemPath` apontando para `src/main/resources/fmjdbc.jar` para evitar warnings do IDE. Isso funciona localmente, mas não é portátil para outros desenvolvedores/CI. Recomenda-se publicar `fmjdbc.jar` em um repositório Maven (privado ou público) ou instruir os consumidores a instalar manualmente o JAR no repositório local.

Como construir e instalar localmente
----------------------------------
O processo padrão para construir e instalar o artefato (e também instalar o `fmjdbc.jar` no repositório local) é:

```bash
mvn -DskipTests install
```

Esse comando executará um `install-file` (configurado no `pom.xml`) para instalar `fmjdbc.jar` em `~/.m2/repository/io/github/tiagoiwamoto/fmjdbc/1.0.0/` e, em seguida, instalará o JAR deste projeto no repositório local.

Instalando manualmente o `fmjdbc.jar` (opção alternativa)
-------------------------------------------------------
Se você preferir ou precisar instalar o driver manualmente em sua máquina (por exemplo, para CI ou outros desenvolvedores), execute:

```bash
mvn install:install-file \
  -Dfile=/caminho/para/fmjdbc.jar \
  -DgroupId=io.github.tiagoiwamoto \
  -DartifactId=fmjdbc \
  -Dversion=1.0.0 \
  -Dpackaging=jar
```

Depois disso, projetos que dependem de `io.github.tiagoiwamoto:fmjdbc:1.0.0` encontrarão o JAR no repositório local.

Como usar este Dialect em outro projeto (exemplo Maven)
------------------------------------------------------
Depois de executar `mvn install` neste projeto (ou de publicar o artefato em um repositório), adicione a dependência no `pom.xml` do projeto consumidor:

```xml
<dependency>
  <groupId>io.github.tiagoiwamoto</groupId>
  <artifactId>claris-filemaker</artifactId>
  <version>1.0.0-SNAPSHOT</version>
</dependency>
```

Configuração do Dialect (Hibernate / Spring Boot)
-------------------------------------------------
- Propriedade Hibernate direta:

```properties
hibernate.dialect=io.github.tiagoiwamoto.ClarisFilemakerDialect
```

- Exemplo Spring Boot (`application.properties`):

```properties
spring.jpa.properties.hibernate.dialect=io.github.tiagoiwamoto.ClarisFilemakerDialect
```

Notas de implementação e limitações
----------------------------------
- A implementação foi adaptada para Hibernate 7.x usando as APIs de `DdlTypeRegistry` e `FunctionContributions` em vez de `registerColumnType`/`registerFunction` antigas.
- Esta versão mantém comportamento conservador (por exemplo, sem suporte a ALTER TABLE, sem FOR UPDATE, etc.) para refletir o comportamento do antigo `FileMakerDialect` legado.
- Se você precisar de suporte avançado (custom `JdbcType`, SQL AST translators, suporte a sequences, etc.), recomendo implementar contributors adicionais ou estender o dialect conforme necessário.

Contribuição
------------
Sinta-se à vontade para abrir issues ou pull requests com correções e melhorias. Se quiser que eu ajude a transformar `fmjdbc.jar` em um módulo separado ou configurar publicação para um repositório (Nexus/Artifactory/OSSRH), posso ajudar.

Licença
-------
(Adicione aqui a licença do projeto, por exemplo MIT/Apache-2.0, ou deixe como pacote interno se for código proprietário.)

Contato
-------
Para dúvidas, relate um issue neste repositório ou entre em contato com o mantenedor do projeto.

