/*
DROP TABLE farmaco_biblioteca;
DROP TABLE biblioteca_coformador;
DROP TABLE coformador;
DROP TABLE tipocoformador;
DROP TABLE farmaco_historico;
DROP TABLE farmaco_protocolo;
DROP TABLE farmaco_infofarmaco;
DROP TABLE farmaco_arquivo;
DROP TABLE tipoarquivo;
DROP TABLE arquivo;
DROP TABLE resultado;
DROP TABLE farmaco;
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
DROP TABLE tipomsg;
DROP TABLE biblioteca;
*/

CREATE TABLE secao (
  codigo int NOT NULL PRIMARY KEY ,
  datahora timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  nome varchar(50) NOT NULL,
  rootpath varchar(50) NOT NULL,
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
  javaclass varchar(100),
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
  tipoarquivo_codigo int NOT NULL REFERENCES tipoarquivo (codigo),
  datahora timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (farmaco_codigo, arquivo_codigo, tipoarquivo_codigo)
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
  maquinastatus_codigo int not null REFERENCES maquinastatus (codigo),
  datahora timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE comando (
  codigo int NOT NULL PRIMARY KEY,
  datahora timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  cmdtemplate varchar(100) NOT NULL,
  cmdprefixo varchar(30) NOT NULL
);

CREATE TABLE tipomsg (
  codigo smallint NOT NULL PRIMARY KEY,
  nome varchar(100) NOT NULL
);

CREATE TABLE labjob ( /* java cria os jobs diferentes bibliotecas: 3 jobs */
  codigo serial NOT NULL PRIMARY KEY,
  jarleitura_codigo integer NOT NULL REFERENCES jarleitura (codigo),
  tarefa_codigo integer NOT NULL REFERENCES tarefa (codigo),
  comando_codigo integer NOT NULL REFERENCES comando (codigo),
  datahora timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  comando varchar(150) NOT NULL,
  comando_log text NOT NULL,
  workpath varchar(150) NOT NULL,
  jarleitura_verificado integer REFERENCES jarleitura (codigo), /** ps aux */
  pid bigint,
  interrompido timestamp,
  executando boolean NOT NULL DEFAULT true,
  concluido timestamp,
  tipomsg_codigo smallint REFERENCES tipomsg (codigo),
  msg varchar(150)
);

/* java verifica se os 3 jobs foram concluidos para tarefa ser dada como completada */
CREATE TABLE resultado ( 
  codigo serial NOT NULL PRIMARY KEY ,
  farmaco_codigo int NOT NULL REFERENCES farmaco (codigo),
  protocolo_codigo int NOT NULL REFERENCES protocolo (codigo),
  tarefa_codigo integer NOT NULL REFERENCES tarefa (codigo),
  jarleitura_codigo int NOT NULL REFERENCES jarleitura (codigo),
  datahora timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  resultpath varchar(150) NOT NULL,
  digerido timestamp,
  tipomsg_codigo smallint REFERENCES tipomsg (codigo),
  msg varchar(150)
);


CREATE TABLE farmaco_protocolo (
  farmaco_codigo int NOT NULL REFERENCES farmaco (codigo),
  protocolo_codigo int NOT NULL REFERENCES protocolo (codigo),
  datahora timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  etapa_codigo int NOT NULL REFERENCES etapa (codigo),
  tarefa_codigo int NOT NULL REFERENCES tarefa (codigo),
  disponivel timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  jarleitura_codigo int REFERENCES jarleitura (codigo), /** ticket */
  tipomsg_codigo smallint REFERENCES tipomsg (codigo),
  msg varchar(150),
  PRIMARY KEY (farmaco_codigo, protocolo_codigo)
);


CREATE TABLE farmaco_historico (
  codigo serial NOT NULL PRIMARY KEY,
  datahora timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP, 
  farmaco_codigo int NOT NULL REFERENCES farmaco (codigo),
  protocolo_codigo int NOT NULL REFERENCES protocolo (codigo),
  etapa_codigo int NOT NULL REFERENCES etapa (codigo),
  tarefa_codigo int NOT NULL REFERENCES tarefa (codigo),
  jarleitura_codigo int NOT NULL REFERENCES jarleitura (codigo),
  resultado_codigo int REFERENCES resultado (codigo)
);


CREATE TABLE biblioteca (
  codigo int NOT NULL PRIMARY KEY,
  datahora timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  tipo varchar(50) NOT NULL,
  mopacinfo text,
  rootpath varchar(50) NOT NULL,
  desativado boolean NOT NULL DEFAULT FALSE 
);

CREATE TABLE farmaco_biblioteca (
  farmaco_codigo int NOT NULL REFERENCES farmaco (codigo),
  biblioteca_codigo int NOT NULL REFERENCES biblioteca (codigo),
  datahora timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  arquivo_codigo int NOT NULL REFERENCES arquivo (codigo),
  PRIMARY KEY (farmaco_codigo, biblioteca_codigo)
);

CREATE TABLE tipocoformador (
  codigo int NOT NULL PRIMARY KEY,
  nome varchar(50) NOT NULL
);

CREATE TABLE coformador (
  codigo serial NOT NULL PRIMARY KEY,
  datahora timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  nome varchar(100) NOT NULL,
  tipocoformador_codigo int NOT NULL REFERENCES tipocoformador (codigo),
  arquivo_codigo int NOT NULL REFERENCES arquivo (codigo)  
);

CREATE TABLE biblioteca_coformador (
  biblioteca_codigo int NOT NULL REFERENCES biblioteca (codigo),
  coformador_codigo int NOT NULL REFERENCES coformador (codigo),
  datahora timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  arquivo_codigo int NOT NULL REFERENCES arquivo (codigo),
  PRIMARY KEY (biblioteca_codigo, coformador_codigo)
);


