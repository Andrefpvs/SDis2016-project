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

## Instruções para instalação 


### Ambiente

[0] Iniciar sistema operativo Windows


[1] Iniciar servidores de apoio

JUDDI:
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
git clone -b SD_R1 https://github.com/tecnico-distsys/A_65-project
cd A_65-project
```
*A partir daqui, todos os passos começam na pasta "A_65-project"*

[4] Instalar módulos de bibliotecas auxiliares

```
cd uddi-naming
mvn clean install
```



-------------------------------------------------------------------------------

### Serviço TRANSPORTER (Os 4 passos seguintes devem ser em terminais separados)

[1] Construir e executar **servidor** *UpaTransporter1*

```
cd transporter-ws
mvn clean generate-sources install exec:java
```

[2] Construir **cliente** de *UpaTransporter1* e executar testes ("install" inclui o passo "verify")

```
cd transporter-ws-cli
mvn clean generate-sources install
```

[3] Executar **servidor** *UpaTransporter2*

```
cd transporter-ws
mvn -Dws.i=2 exec:java
```

[4] Construir **cliente** de *UpaTransporter2* e executar testes

```
cd transporter-ws-cli
mvn -Dws.i=2 verify
```


-------------------------------------------------------------------------------

### Serviço BROKER (Os  passos seguintes devem ser em terminais separados)

[0] Instalar cliente
```
cd broker-ws-cli
mvn install
```

[1] Construir e executar **servidor**

```
cd broker-ws
mvn clean generate-sources install exec:java
```

[2] Executar **servidor secundário**

```
cd broker-ws
mvn -Dws.sub=Sub -Dws.i=1 exec:java
```

[3] Construir **cliente** e executar testes ("install" inclui o passo "verify")

```
cd broker-ws-cli
mvn clean generate-sources install
```
-------------------------------------------------------------------------------
**FIM**
