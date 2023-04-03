/*
DROP TABLE farmaco_historico;
DROP TABLE farmaco_protocolo;
DROP TABLE farmaco_infofarmaco;
DROP TABLE farmaco_arquivo;
DROP TABLE tipoarquivo;
DROP TABLE arquivo;
DROP TABLE resultado;
DROP TABLE farmaco;
DROP TABLE tarefa_comando;
DROP TABLE labjob;
DROP TABLE tarefa;
DROP TABLE etapa;
DROP TABLE protocolo;
DROP TABLE jarleitura;
DROP TABLE javadeploy_maquina;
DROP TABLE javadeploy;
DROP TABLE comando;
DROP TABLE infofarmaco;
DROP TABLE maquina_infomaquina;
DROP TABLE maquinastatus;
DROP TABLE maquina;
DROP TABLE infomaquina;
DROP TABLE secao;
DROP TABLE tipoinfo;
*/

CREATE TABLE secao (
  codigo int NOT NULL PRIMARY KEY ,
  datahora timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  nome varchar(50) NOT NULL,
  link varchar(50) NOT NULL,
  ordem integer NOT NULL,
  icon varchar(20) NOT NULL
);

CREATE TABLE protocolo (
  codigo serial NOT NULL PRIMARY KEY ,
  datahora timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  nome varchar(50) NOT NULL,
  versao varchar(10) NOT NULL,
  desativado boolean NOT NULL DEFAULT FALSE,
  secao_codigo integer NOT NULL REFERENCES secao (codigo)
);

CREATE TABLE etapa (
  codigo serial NOT NULL PRIMARY KEY ,
  datahora timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  nome varchar(100) NOT NULL,
  ordem integer NOT NULL,
  icon varchar(50) NOT NULL,
  protocolo_codigo integer NOT NULL REFERENCES protocolo (codigo)
);

CREATE TABLE tarefa (
  codigo serial NOT NULL PRIMARY KEY ,
  datahora timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  nome varchar(150) NOT NULL,
  descricao text,  
  ordem integer NOT NULL,
  manual boolean NOT NULL DEFAULT FALSE,
  etapa_codigo integer NOT NULL REFERENCES etapa (codigo),
  tarefa_codigo integer REFERENCES tarefa (codigo)
);

CREATE TABLE farmaco (
  codigo serial NOT NULL PRIMARY KEY ,
  datahora timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  nome varchar(100) NOT NULL,
  filename varchar(255) NOT NULL,
  hash character(32) NOT NULL,
  desativado boolean NOT NULL DEFAULT FALSE
);

CREATE TABLE tipoinfo (
  codigo int NOT NULL PRIMARY KEY,
  nome varchar(50) NOT NULL
);

CREATE TABLE infofarmaco (
  codigo int NOT NULL PRIMARY KEY,
  nome varchar(50) NOT NULL,
  tipoinfo_codigo integer NOT NULL REFERENCES tipoinfo (codigo)
);

CREATE TABLE farmaco_infofarmaco (
  farmaco_codigo int NOT NULL REFERENCES farmaco (codigo),
  infofarmaco_codigo int NOT NULL REFERENCES infofarmaco (codigo),
  datahora timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  valor text NOT NULL,
  PRIMARY KEY (farmaco_codigo, infofarmaco_codigo)
);



CREATE TABLE arquivo (
  codigo serial NOT NULL PRIMARY KEY,
  datahora timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  filename varchar(255) NOT NULL,
  hash character(32) NOT NULL,
  conteudo text NOT NULL
);

CREATE TABLE tipoarquivo (
  codigo int NOT NULL PRIMARY KEY,
  nome varchar(50) NOT NULL
);

CREATE TABLE farmaco_arquivo (
  farmaco_codigo int NOT NULL REFERENCES farmaco (codigo),
  arquivo_codigo int NOT NULL REFERENCES arquivo (codigo),
  datahora timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  tipoarquivo_codigo int NOT NULL REFERENCES tipoarquivo (codigo),
  PRIMARY KEY (farmaco_codigo, arquivo_codigo)
);

CREATE TABLE infomaquina (
  codigo int NOT NULL PRIMARY KEY,
  nome varchar(50) NOT NULL
);

CREATE TABLE maquina (
  codigo serial NOT NULL PRIMARY KEY,
  datahora timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  hostname varchar(50) NOT NULL,
  head boolean NOT NULL DEFAULT FALSE,
  ignorar boolean NOT NULL DEFAULT FALSE
);

CREATE TABLE maquina_infomaquina (
  maquina_codigo int NOT NULL REFERENCES maquina (codigo),
  infomaquina_codigo int NOT NULL REFERENCES infomaquina (codigo),
  datahora timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  valor varchar(200) NOT NULL,
  PRIMARY KEY (maquina_codigo, infomaquina_codigo)
);

CREATE TABLE maquinastatus (
  codigo serial NOT NULL PRIMARY KEY,
  maquina_codigo int NOT NULL REFERENCES maquina (codigo),
  datahora timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,  
  iniciarjob boolean NOT NULL DEFAULT FALSE,
  online boolean NOT NULL DEFAULT FALSE,
  cpuused numeric(5,2),
  memused numeric(5,2)
);

CREATE TABLE comando (
  codigo int NOT NULL PRIMARY KEY,
  datahora timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  cmdtemplate varchar(100) NOT NULL,
  cmdprefixo varchar(30) NOT NULL
);

CREATE TABLE tarefa_comando (
  tarefa_codigo int NOT NULL REFERENCES tarefa (codigo),
  comando_codigo int NOT NULL REFERENCES comando (codigo),
  datahora timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (tarefa_codigo, comando_codigo)
);

CREATE TABLE labjob ( /* java cria os jobs diferentes cenarios: 3 jobs */
  codigo serial NOT NULL PRIMARY KEY,
  datahora timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  maquina_codigo integer NOT NULL REFERENCES maquina (codigo),
  tarefa_codigo integer NOT NULL REFERENCES tarefa (codigo),
  comando_codigo integer NOT NULL REFERENCES comando (codigo),
  comando text NOT NULL,  
  workpath varchar(150) NOT NULL,
  verificado timestamp,
  iniciado timestamp,
  concluido timestamp
);

/* java verifica se os 3 jobs foram concluidos para tarefa ser dada como completada */
CREATE TABLE resultado ( 
  codigo serial NOT NULL PRIMARY KEY ,
  datahora timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  farmaco_codigo int NOT NULL REFERENCES farmaco (codigo),
  protocolo_codigo int NOT NULL REFERENCES protocolo (codigo),
  labjob_codigo integer REFERENCES labjob (codigo),
  tarefa_codigo integer NOT NULL REFERENCES tarefa (codigo),
  resultpath varchar(150) NOT NULL,
  digerido timestamp
);


/* estrutura do java */

CREATE TABLE javadeploy (
  codigo serial NOT NULL PRIMARY KEY,
  maquina_codigo int not null REFERENCES maquina (codigo),
  datahora timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  versao varchar(10) NOT NULL,
  path varchar(200) NOT NULL
);

CREATE TABLE javadeploy_maquina (
  javadeploy_codigo int NOT NULL REFERENCES javadeploy (codigo),
  maquina_codigo int NOT NULL REFERENCES maquina (codigo),
  datahora timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (javadeploy_codigo, maquina_codigo)
);

CREATE TABLE jarleitura (
  codigo serial NOT NULL PRIMARY KEY,
  javadeploy_codigo int not null REFERENCES javadeploy (codigo),
  maquina_codigo int not null REFERENCES maquina (codigo),
  datahora timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
);
/*  */

CREATE TABLE farmaco_protocolo (
  farmaco_codigo int NOT NULL REFERENCES farmaco (codigo),
  protocolo_codigo int NOT NULL REFERENCES protocolo (codigo),
  datahora timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  etapa_codigo int NOT NULL REFERENCES etapa (codigo),
  jarleitura_codigo int REFERENCES jarleitura (codigo),
  PRIMARY KEY (farmaco_codigo, protocolo_codigo)
);


CREATE TABLE farmaco_historico (
  codigo serial NOT NULL PRIMARY KEY,
  datahora timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP, 
  farmaco_codigo int NOT NULL REFERENCES farmaco (codigo),
  protocolo_codigo int NOT NULL REFERENCES protocolo (codigo),
  etapa_codigo int NOT NULL REFERENCES etapa (codigo),
  jarleitura_codigo int NOT NULL REFERENCES jarleitura (codigo),
  tarefa_codigo int NOT NULL REFERENCES tarefa (codigo)
);



