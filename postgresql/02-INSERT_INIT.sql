INSERT INTO secao(codigo,nome,link,ordem,icon) values 
	(1, 'Co-Crystal Design','cocrystal.php',10,'columns-gap');

INSERT INTO protocolo(codigo,nome,versao,secao_codigo) values 
	(1,'Planejamento de Co-cristais','v0.1',1);

INSERT INTO etapa(codigo,nome,ordem,protocolo_codigo,icon) values 
  (1,'Preparar Fármaco',10,1,'diamond'),
  (2,'Análise Qualitativa',20,1,'x-diamond'),
  (3,'Análise Quantitativa',30,1,'diamond-half'),
  (4,'Ranking de triagem',40,1,'ui-checks-grid'),
  (5,'Cálculos de Simetria',50,1,'bounding-box'),
  (6,'Complexos cristalinos',60,1,'box');
  
INSERT INTO tarefa(codigo,nome,descricao,ordem,manual,etapa_codigo,tarefa_codigo) values 
  (2,'Otimizar geometria','Minimização de energia utilizando método de mecânica molecular com campo de força MMFF94.',20,true,1,null),
  (3,'Refinar geometria','Utilizando métodos de mecânica quântica semi-empíricos PM6 e PM7.',30,true,1,2),
  (5,'Definir co-formadores',null,40,true,1,2);

INSERT INTO tarefa(codigo,nome,descricao,ordem,manual,etapa_codigo,tarefa_codigo) values 
  (6,'Gerar confôrmeros do fármaco','Utilizar o programa Mercury na opção CSD-Materials.',10,false,2,5),
  (7,'Complementariedade molecular','Utilizar o programa Mercury na opção CSD-Materials > Co-Crystal Design.',20,false,2,6),
  (8,'Compilar dados',null,30,true,2,7);
  
INSERT INTO tarefa(codigo,nome,descricao,ordem,manual,etapa_codigo,tarefa_codigo) values 
  (9 ,'Docking molecular entre fármaco e co-formadores',null,10,true,3,5),
  (10,'Compilar dados',null,20,true,3,9);
  
INSERT INTO tarefa(codigo,nome,descricao,ordem,manual,etapa_codigo,tarefa_codigo) values 
  (11 ,'Fármaco e co-formadores',null,10,true,4,10);
  
SELECT setval('protocolo_codigo_seq', 1);
SELECT setval('etapa_codigo_seq', 5);
SELECT setval('tarefa_codigo_seq', 11);

INSERT INTO tipoinfo(codigo,nome) values 
  (10, 'Identificadores'),(20, 'Físico-química');

INSERT INTO infofarmaco(codigo,nome,tipoinfo_codigo) values 
  (10, 'CSD Refcode',10),
  (20, 'PubChem',10),
  (30, 'Fórmula molecular',20);

INSERT INTO tipoarquivo(codigo,nome) values 
  (1, 'Enviado');

INSERT INTO infomaquina(codigo,nome) values 
  (1,'IP'),(2,'Usuário'),(3,'Senha'),(4,'CPU Total'), 
  (5,'CPU Disponível'),(6,'RootPath'),(7,'JAR');
  
INSERT INTO maquina(codigo,nome,ignorar) values 
  (1,'Anguirel',false),(2,'Anglachel',false),(3,'Gurthang',false),(4,'Tiamat',true);

SELECT setval('maquina_codigo_seq', 4);

INSERT INTO maquina_infomaquina(maquina_codigo,infomaquina_codigo,valor) values
  (1,1,'192.168.0.102'),(1,2,'lapfarsc'),(1,3,'senha'),(1,4,'8'),(1,5,'4'),(1,6,'/home/lapfarsc/Documentos/Guilherme/CrystalCoformers/'),(1,7,'LaPFarSC_CrystalCoformers_v0.1.0.jar'),
  (2,1,'192.168.0.108'),(2,2,'farmacia'),(2,3,'labsala18'),(2,4,'8'),(2,5,'8'),(2,6,'/home/farmacia/Documentos/Guilherme/CrystalCoformers/'),(2,7,'LaPFarSC_CrystalCoformers_v0.1.0.jar'),
  (3,1,'192.168.0.104'),(3,2,'farmacia'),(3,3,'labsala18'),(3,4,'12'),(3,5,'12'),(3,6,'/home/farmacia/Documentos/Guilherme/CrystalCoformers/'),(3,7,'LaPFarSC_CrystalCoformers_v0.1.0.jar');
  
INSERT INTO comando(codigo,cmdtemplate,cmdprefixo) values  
  (1,'java -jar @JARPATH @ARG &','java'),
  (2,'mpirun -np @NCPU pw.x -in @QEARQIN > @QEARQOUT &','mpirun'), 
  (3,'pw.x -in @QEARQIN > @QEARQOUT &','pw.x');
  
  
/*  FIM */

