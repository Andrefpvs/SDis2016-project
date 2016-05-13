# Projeto de Sistemas Distribuídos 2015-2016 #

## Grupo de SD 65 - Campus Alameda

| Aluno               | Número IST | Email                       |             
|---------------------|------------|-----------------------------|
| André Silva         |	68707      | andrefpvs@netcabo.pt        |
| Duarte Coelho       | 73339      | coelho.106@hotmail.com      |
| João Afonso Martins | 73438      | martins.joao.a@gmail.com    |

Repositório:
[tecnico-distsys/A_65-project](https://github.com/tecnico-distsys/A_65-project/)

-------------------------------------------------------------------------------

## Instruções de instalação da segunda entrega (para primeira entrega, ver [README da release SD_R1](https://github.com/tecnico-distsys/A_65-project/blob/66ccda97bae4877c0db212d7a9688bf565f92e23/README.md))


### Ambiente

[0] Iniciar sistema operativo Windows

**Antes de continuar com os passos seguintes, é importante apagar a base de dados local do JUDDI, caso exista:**
```
shutdown.bat (se necessário)
> apagar manualmente a pasta "target" e ficheiro "derby.log", gerados pelo JUDDI em execuções anteriores
```

[1] Iniciar servidores de apoio

**JUDDI**:
```
startup.bat
```


[2] Criar pasta temporária

```
cd Desktop
mkdir SD1
cd SD1
```


[3] Obter código fonte do projeto (versão entregue)

```
git clone -b SD_R2 https://github.com/tecnico-distsys/A_65-project
cd A_65-project
```
***A partir daqui, todos os passos começam na pasta "A_65-project"***

[4] Instalar módulos de **bibliotecas auxiliares**

```
cd uddi-naming
mvn clean install
cd ..
cd ws-handlers
mvn clean install
```



-------------------------------------------------------------------------------

### Serviço TRANSPORTER (Os passos seguintes devem ser em terminais separados)

[1] Construir e executar **servidor** *UpaTransporter1*

```
cd transporter-ws
mvn clean generate-sources install exec:java
```

[2] Construir **cliente** de *UpaTransporter1* e executar testes IT ("install" inclui o passo "verify")

```
cd transporter-ws-cli
mvn clean generate-sources install
```

[3] Executar **servidor** *UpaTransporter2*

```
cd transporter-ws
mvn -Dws.i=2 exec:java
```

-------------------------------------------------------------------------------

### Serviço BROKER (Os passos seguintes devem ser em terminais separados)

[1] Instalar **cliente** no repositório local da máquina (necessário para replicação)
```
cd broker-ws-cli
mvn clean generate-sources install -DskipTests
```

[2] Construir e executar **servidor principal** *UpaBroker*

```
cd broker-ws
mvn clean generate-sources install exec:java -DskipTests

```

[3] Executar **servidor secundário** *UpaBrokerSub* (quando instruído pelo servidor principal)

```
cd broker-ws
mvn -Dws.sub=Sub -Dws.port=8090 exec:java -DskipTests
```

[4] Executar testes IT do **cliente** (neste passo, ignorar o resultado dos testes de *ReplicationIT*)

```
cd broker-ws-cli
mvn verify
```

-------------------------------------------------------------------------------

### Demonstração
#### Replicação

Os testes estão na classe *ReplicationIT* do *broker-ws-cli*, e devem ser corridos individualmente de acordo com as seguintes instruções:

[1] Correr teste `testStateReplication()`.
    Este teste irá demonstrar o funcionamento normal da replicação, incluindo a impressão para a consola de cada vez que é feita a propagação de alterações e provas-de-vida. Para instigar a propagação de alterações, são usados os métodos `requestTransport`, `viewTransport`, e `clearTransports`.
```
> iniciar Broker Primário e Secundário
cd broker-ws-cli
mvn -Dit.test=ReplicationIT#testStateReplication verify
```

[2] Correr teste `testStateReplicationWithFault()`.
    Este teste irá demonstrar que após o Broker Primário cair, o Broker Secundário irá conter o mesmo estado
    que o Primário na altura em que caiu. É necessário terminar o Primário manualmente (`sigkill`) quando
    pedido pelo teste.
```
> iniciar Broker Primário e Secundário
cd broker-ws-cli
mvn -Dit.test=ReplicationIT#testStateReplicationWithFault verify
> quando aparecer a mensagem "You now have 15 SECONDS to terminate Primary Broker.", terminar o processo do Broker Primário (CTRL+C)
```

[3] Correr teste `testUDDISubstitution()`.
    Este teste irá demonstrar que após o Broker Primário cair, o FrontEnd do Cliente irá obter
    o *endpoint* do Broker Secundário, procurando no UDDI pelo nome do Primário (único nome conhecido
    pelo Cliente). É necessário terminar o Primário manualmente (`sigkill`) quando pedido pelo teste.
```
> iniciar Broker Primário e Secundário
cd broker-ws-cli
mvn -Dit.test=ReplicationIT#testUDDISubstitution verify
> quando aparecer a mensagem "You now have 20 SECONDS to terminate Primary Broker.", terminar o processo do Broker Primário (CTRL+C)
```

#### Segurança

Devido a dificuldades, testes de Segurança não foram implementados. Para avaliação, enviamos a estrutura e implementação isolados do resto do projeto. Mais detalhes no [relatório](https://github.com/tecnico-distsys/A_65-project/blob/master/doc/A65-report.pdf), na parte da Segurança.

-------------------------------------------------------------------------------
-------------------------------------------------------------------------------

Para detalhes sobre implementação, consultar o [relatório do projeto](https://github.com/tecnico-distsys/A_65-project/blob/master/doc/A65-report.pdf).

**FIM**
