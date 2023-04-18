<?php

//IDS DO BANCO DE DADOS

define("SECAO_COCRYSTAL",  1);

define("TIPOARQUIVO_ENVIADO",  1);

define("INFOMAQUINA_CPU_OCIOSA",  7);


//  (1, 'OK'), (2, 'Aviso'), (3, 'Erro'), (4,'Exception');
define("TIPOMSG_OK",  1);
define("TIPOMSG_AVISO", 2);
define("TIPOMSG_ERRO", 3); //logico
define("TIPOMSG_EXCEPTION", 4); //sistema


define("ICON_TAREFA_P1",  "<i class='bi-robot'></i>");
define("ICON_TAREFA_P2",  "<i class='bi-gear'></i>");
define("ICON_TAREFA_P3",  "<i class='bi-check2-circle'></i>");


function getStyleCardHeader($tipomsg){    
    return $tipomsg==TIPOMSG_OK?"text-bg-primary":($tipomsg==TIPOMSG_AVISO?"text-bg-warning":($tipomsg==TIPOMSG_ERRO || $tipomsg==TIPOMSG_EXCEPTION?"text-bg-danger":"text-bg-light text-secondary"));
}
function getStyleCardText($tipomsg){    
    return $tipomsg==TIPOMSG_OK?"text-primary":($tipomsg==TIPOMSG_AVISO?"":($tipomsg==TIPOMSG_ERRO || $tipomsg==TIPOMSG_EXCEPTION?"text-danger":"text-secondary"));
}

?>