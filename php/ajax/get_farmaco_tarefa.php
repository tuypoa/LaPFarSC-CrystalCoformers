<?php
require_once("../config/configuracao.php"); 

$p_farmaco = preg_match("/^[0-9]+$/", $_GET['fid']) ? $_GET['fid'] : 0;
$p_protocolo = preg_match("/^[0-9]+$/", $_GET['pid']) ? $_GET['pid'] : 0;
$p_tarefa = preg_match("/^[0-9]+$/", $_GET['tid']) ? $_GET['tid'] : 0;

$processo = null;
$historico = null;

$query = "SELECT
        m.hostname, p1t.software,
        TO_CHAR(p1.disponivel,'DD/MM/YY HH24:MI:SS')  as p1_inicio,
        TO_CHAR(p2.datahora,'DD/MM/YY HH24:MI:SS')  as p1_fim,
        p1.tipomsg_codigo as p1_tipomsg, 
        p1.msg as p1_msg,

        TO_CHAR(p2.datahora,'DD/MM/YY HH24:MI:SS')  as p2_inicio,
        p2.executando as p2_executando,
        p2.pid as p2_pid,
        TO_CHAR(p2jv.datahora,'DD/MM/YY HH24:MI:SS')  as p2_verificado,
        TO_CHAR(p2.interrompido,'DD/MM/YY HH24:MI:SS')  as p2_interrompido,
        TO_CHAR(p2.concluido,'DD/MM/YY HH24:MI:SS')  as p2_concluido,
        p2.tipomsg_codigo as p2_tipomsg, 
        p2.msg as p2_msg, p2.comando_log,

        TO_CHAR(p3j.datahora,'DD/MM/YY HH24:MI:SS')  as p3_inicio,
        TO_CHAR(p3.digerido,'DD/MM/YY HH24:MI:SS')  as p3_fim,
        p3.tipomsg_codigo as p3_tipomsg, 
        p3.msg as p3_msg

        FROM farmaco_protocolo p1
            INNER JOIN tarefa p1t ON p1.tarefa_codigo=p1t.codigo
            LEFT JOIN jarleitura p1j ON p1j.codigo=p1.jarleitura_codigo
            LEFT JOIN labjob p2 ON p1j.codigo=p2.jarleitura_codigo AND p2.tarefa_codigo=p1t.codigo
            LEFT JOIN jarleitura p2jv ON p2jv.codigo=p2.jarleitura_verificado
            LEFT JOIN maquinastatus ms ON ms.codigo=p1j.maquinastatus_codigo
            LEFT JOIN maquina m ON m.codigo=ms.maquina_codigo
            LEFT JOIN farmaco_resultado p3 ON p1.farmaco_codigo=p3.farmaco_codigo AND p1.protocolo_codigo=p3.protocolo_codigo AND p1.tarefa_codigo=p3.tarefa_codigo
            LEFT JOIN jarleitura p3j ON p3j.codigo=p3.jarleitura_codigo
        WHERE p1.farmaco_codigo = :fid AND 
            p1.protocolo_codigo = :pid AND 
            p1t.codigo = :tid ";
$stPro = $con->prepare($query);	
$stPro->bindParam(':fid', $p_farmaco, PDO::PARAM_INT);
$stPro->bindParam(':pid', $p_protocolo, PDO::PARAM_INT);
$stPro->bindParam(':tid', $p_tarefa, PDO::PARAM_INT);
$stPro->execute();
$rsProc = $stPro->fetchAll(PDO::FETCH_ASSOC);
if(sizeof($rsProc)>0){
    $processo = $rsProc[0];
}
unset($rsProc);
$rsProc = null;
$stPro->closeCursor();

//verificar historico
if($processo==null){
    $query = "SELECT
        m.hostname, p1t.software,
        TO_CHAR(p1.datainicio,'DD/MM/YY HH24:MI:SS')  as p1_inicio,
        TO_CHAR(p2.datahora,'DD/MM/YY HH24:MI:SS')  as p1_fim,

        TO_CHAR(p2.datahora,'DD/MM/YY HH24:MI:SS')  as p2_inicio,
        p2.executando as p2_executando,
        p2.pid as p2_pid,
        TO_CHAR(p2jv.datahora,'DD/MM/YY HH24:MI:SS')  as p2_verificado,
        TO_CHAR(p2.interrompido,'DD/MM/YY HH24:MI:SS')  as p2_interrompido,
        TO_CHAR(p2.concluido,'DD/MM/YY HH24:MI:SS')  as p2_concluido,
        p2.tipomsg_codigo as p2_tipomsg, 
        p2.msg as p2_msg, p2.comando_log,

        TO_CHAR(p3j.datahora,'DD/MM/YY HH24:MI:SS')  as p3_inicio,
        TO_CHAR(p3.digerido,'DD/MM/YY HH24:MI:SS')  as p3_fim,
        p3.tipomsg_codigo as p3_tipomsg, 
        p3.msg as p3_msg

        FROM farmaco_historico p1
            INNER JOIN tarefa p1t ON p1.tarefa_codigo=p1t.codigo
            INNER JOIN jarleitura p1j ON p1j.codigo=p1.jarleitura_codigo
            INNER JOIN farmaco_resultado p3 ON p1.farmaco_codigo=p3.farmaco_codigo AND p1.protocolo_codigo=p3.protocolo_codigo AND p1.tarefa_codigo=p3.tarefa_codigo
			INNER JOIN labjob p2 ON p2.codigo=p3.labjob_codigo
            INNER JOIN jarleitura p2jv ON p2jv.codigo=p2.jarleitura_verificado
            INNER JOIN maquinastatus ms ON ms.codigo=p1j.maquinastatus_codigo
            INNER JOIN maquina m ON m.codigo=ms.maquina_codigo
            INNER JOIN jarleitura p3j ON p3j.codigo=p3.jarleitura_codigo
        WHERE p1.farmaco_codigo = :fid AND 
            p1.protocolo_codigo = :pid AND 
            p1t.codigo = :tid 
        ORDER BY p3.codigo DESC, p2.codigo DESC LIMIT 1";
    $stPro = $con->prepare($query);	
    $stPro->bindParam(':fid', $p_farmaco, PDO::PARAM_INT);
    $stPro->bindParam(':pid', $p_protocolo, PDO::PARAM_INT);
    $stPro->bindParam(':tid', $p_tarefa, PDO::PARAM_INT);
    $stPro->execute();
    $rsProc = $stPro->fetchAll(PDO::FETCH_ASSOC);
    if(sizeof($rsProc)>0){
        $historico = $rsProc[0];
    }
    unset($rsProc);
    $rsProc = null;
    $stPro->closeCursor();
}


if($processo!=null){
    ?>
    <div class="card-group">
        <div class="card">
            <div class="card-header text-nowrap <?php echo getStyleCardHeader($processo["hostname"]==null?TIPOMSG_OK:$processo["p1_tipomsg"]); ?>"><?php echo ICON_TAREFA_P1; ?> Processo autom&aacute;tico</div>
            <div class="card-body <?php echo getStyleCardText($processo["p1_tipomsg"]); ?>">
                <h5 class="card-title"><?php echo $processo["hostname"]; ?></h5>
                <p class="card-text"><?php echo $processo["p1_inicio"]; ?> <br><?php echo $processo["p1_msg"]; ?></p>
                <?php if($processo["p1_tipomsg"]!=TIPOMSG_OK){ ?>
                    <button type="button" class="btn btn-outline-danger" onclick="alert('OK');">Refazer</button>
                <?php } ?>
            </div> 
            <?php if($processo["hostname"]==null){ ?>
                <div class="card-footer text-nowrap text-primary"><div class='spinner-grow spinner-grow-sm text-primary' role='status'></div> Aguardando m&aacute;quina...</div>
            <?php }elseif($processo["p1_fim"]==null){ ?>
                <div class="card-footer text-nowrap "><small class="text-muted">N&atilde;o iniciado.</small></div>
            <?php }else{ ?>
                <div class="card-footer text-nowrap <?php echo getStyleCardText($processo["p1_tipomsg"]); ?>"><i class='bi-<?php echo $processo["p1_tipomsg"]==TIPOMSG_OK?"check-lg":"x-circle-fill"; ?>'></i> <?php echo $processo["p1_fim"]; ?></div>
            <?php } ?>
        </div>
        <div class="card">
            <div class="card-header text-nowrap <?php echo getStyleCardHeader($processo["p2_executando"] && $processo["p2_verificado"]==null?TIPOMSG_OK:$processo["p2_tipomsg"]); ?>"><?php echo ICON_TAREFA_P2; ?> <?php echo $processo["software"]; ?></div>
            <div class="card-body <?php echo getStyleCardText($processo["p2_tipomsg"]); ?>">
                <h5 class="card-title"><?php echo $processo["p2_inicio"]; ?></h5>
                <p class="card-text"><?php echo $processo["p2_msg"]; ?></p>
                
            </div>
            <?php if($processo["p2_executando"]){ ?>
                <div class="card-footer text-primary text-nowrap"><div class='spinner-border spinner-border-sm' role='status'></div> &nbsp; <?php echo $processo["p2_verificado"]==null?"Executando...":$processo["p2_verificado"]; ?> <span class="badge rounded-pill text-bg-secondary bg-opacity-50"><?php echo $processo["p2_pid"]; ?></span></div>
            <?php }elseif($processo["p2_concluido"]!=null){ ?>
                <div class="card-footer text-nowrap <?php echo getStyleCardText($processo["p2_tipomsg"]); ?>"><i class='bi-check-lg'></i> <?php echo $processo["p2_concluido"]; ?></div>
            <?php }elseif($processo["p2_interrompido"]!=null){ ?>
                <div class="card-footer text-nowrap <?php echo getStyleCardText($processo["p2_tipomsg"]); ?>"><i class='bi-x-circle-fill'></i> <?php echo $processo["p2_interrompido"]; ?></div>
            <?php }else{ ?>
                <div class="card-footer text-nowrap"><small class="text-muted">N&atilde;o iniciado.</small></div>
            <?php } ?>
        </div>
        <div class="card">
            <div class="card-header text-nowrap <?php echo getStyleCardHeader($processo["p3_tipomsg"]); ?>"><?php echo ICON_TAREFA_P3; ?> Resultado</div>
            <div class="card-body <?php echo getStyleCardText($processo["p3_tipomsg"]); ?>">
              
                <p class="card-text"><?php echo $processo["p3_inicio"]; ?> <br><?php echo $processo["p3_msg"]; ?></p>
            </div>
            <?php if($processo["p3_tipomsg"]==null){ ?>
                <div class="card-footer text-nowrap "><small class="text-muted">N&atilde;o iniciado.</small></div>
            <?php }else{ ?>
                <div class="card-footer text-nowrap <?php echo getStyleCardText($processo["p3_tipomsg"]); ?>"><i class='bi-<?php echo $processo["p3_tipomsg"]==TIPOMSG_OK?"check-lg":"x-circle-fill"; ?>'></i> <?php echo $processo["p3_fim"]; ?></div>
            <?php } ?>
        </div>
    </div>
    <?php if($processo["hostname"]==null || $processo["p2_executando"]){ ?>
        <script>
        $(function() {
            setTimeout(function () {
                $("#tarefa_status_<?php echo $p_tarefa; ?>").load('ajax/get_farmaco_tarefa.php?fid=<?php echo $p_farmaco; ?>&pid=<?php echo $p_protocolo; ?>&tid=<?php echo $p_tarefa; ?>');
            }, 5000);
        });
        </script>
    <?php }elseif($processo["p3_tipomsg"]==TIPOMSG_OK){ ?>
        <script>
        $(function() {
            setTimeout(function () {
                loadEtapaFarmaco('<?php echo $p_farmaco; ?>');
            }, 10000);
        });
        </script>
    <?php } ?>
    <?php
}elseif($historico!=null){
    ?>
    <div class="card-group">
        <div class="card">
            <div class="card-header text-nowrap text-bg-success"><?php echo ICON_TAREFA_P1; ?> Processo autom&aacute;tico</div>
            <div class="card-body text-success">
                <h5 class="card-title"><?php echo $historico["hostname"]; ?></h5>
                <p class="card-text"><?php echo $historico["p1_inicio"]; ?> <br><?php echo $historico["p1_msg"]; ?></p>
                
            </div> 
            <div class="card-footer text-nowrap <?php echo getStyleCardText($historico["p2_tipomsg"]); ?>"><i class='bi-check-lg'></i> <?php echo $historico["p1_fim"]; ?></div>
        </div>
        <div class="card">
            <div class="card-header text-nowrap text-bg-success"><?php echo ICON_TAREFA_P2; ?> <?php echo $historico["software"]; ?></div>
            <div class="card-body text-success">
                <h5 class="card-title"><?php echo $historico["p2_inicio"]; ?></h5>
                <p class="card-text"><?php echo $historico["p2_msg"]; ?></p>
                
            </div>
            <?php if($historico["p2_concluido"]!=null){ ?>
                <div class="card-footer text-nowrap <?php echo getStyleCardText($historico["p2_tipomsg"]); ?>"><i class='bi-check-lg'></i> <?php echo $historico["p2_concluido"]; ?></div>
            <?php }elseif($historico["p2_interrompido"]!=null){ ?>
                <div class="card-footer text-nowrap <?php echo getStyleCardText($historico["p2_tipomsg"]); ?>"><i class='bi-x-circle-fill'></i> <?php echo $historico["p2_interrompido"]; ?></div>
            <?php } ?>
        </div>
        <div class="card">
            <div class="card-header text-nowrap text-bg-success"><?php echo ICON_TAREFA_P3; ?> Resultado</div>
            <div class="card-body text-success">
                <p class="card-text"><?php echo $historico["p3_inicio"]; ?> <br><?php echo $historico["p3_msg"]; ?></p>
                <button type="button" class="btn btn-outline-success" onclick="">Detalhes</button>
            </div>
            <div class="card-footer text-nowrap <?php echo getStyleCardText($historico["p3_tipomsg"]); ?>"><i class='bi-<?php echo $historico["p3_tipomsg"]==TIPOMSG_OK?"check-lg":"x-circle-fill"; ?>'></i> <?php echo $historico["p3_fim"]; ?></div>
        </div>
    </div>
    <?php
}else{
    $query = "SELECT t.codigo, t.software FROM tarefa t WHERE t.codigo = :tid";
        $stBusca = $con->prepare($query);	
        $stBusca->bindParam(':tid', $p_tarefa, PDO::PARAM_INT);
        $stBusca->execute();
        $rsBusca = $stBusca->fetchAll(PDO::FETCH_ASSOC);
        if(sizeof($rsBusca)>0){
            $tarefa = $rsBusca[0];
        }
        unset($rsBusca);
        $rsBusca = null;
        $stBusca->closeCursor();
    ?>
    <div class="card-group">
        <div class="card">
            <div class="card-header text-nowrap text-bg-light text-secondary"><?php echo ICON_TAREFA_P1; ?> Processo autom&aacute;tico</div>
            <div class="card-body text-secondary">
                <p class="card-text">Prepara&ccedil;&atilde;o</p>
            </div> 
        </div>
        <div class="card">
            <div class="card-header text-nowrap text-bg-light text-secondary"><?php echo ICON_TAREFA_P2; ?> <?php echo $tarefa["software"]; ?></div>
            <div class="card-body text-secondary">
              <p class="card-text">Execu&ccedil;&atilde;o</p>
            </div>
        </div>
        <div class="card">
            <div class="card-header text-bg-light text-secondary"><?php echo ICON_TAREFA_P3; ?> Resultado</div>
            <div class="card-body text-secondary">
                <p class="card-text">Verifica&ccedil;&atilde;o</p>
            </div>
        </div>
    </div>
    <?php
}


?>