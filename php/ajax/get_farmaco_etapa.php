<?php
require_once("../config/configuracao.php"); 



$farmaco_protocolo = null;                

$p_farmaco = preg_match("/^[0-9]+$/", $_GET['farid']) ? $_GET['farid'] : 0;
//$p_etapa = preg_match("/^[0-9]+$/", $_GET['eid']) ? $_GET['eid'] : null;

//if($p_etapa==null){
    $query = "SELECT e.codigo, e.nome, fp.protocolo_codigo, fp.tarefa_codigo
                FROM protocolo p
                    INNER JOIN farmaco_protocolo fp ON p.codigo=fp.protocolo_codigo
                    INNER JOIN etapa e ON e.codigo=fp.etapa_codigo
                WHERE fp.farmaco_codigo = :farmacoid AND NOT p.desativado";
    $stBusca = $con->prepare($query);	
    $stBusca->bindParam(':farmacoid', $p_farmaco, PDO::PARAM_INT);
    $stBusca->execute();
    $rsBusca = $stBusca->fetchAll(PDO::FETCH_ASSOC);
    if(sizeof($rsBusca)>0){
        $etapa = $rsBusca[0];
    }
    unset($rsBusca);
    $rsBusca = null;
    $stBusca->closeCursor();
//}
?>
<h5><?php echo $etapa["nome"]; ?></h5>

<div class="accordion p-3" id="accordionPanels">
    <?php
    $query = "SELECT t.codigo, t.nome, t.descricao, t.manual, t.tarefa_codigo 
                FROM tarefa t
                    INNER JOIN etapa e ON t.etapa_codigo=e.codigo
                WHERE e.protocolo_codigo = :proid AND 
                        e.codigo = :eid
                ORDER BY t.ordem";
    $stBusca = $con->prepare($query);	
    $stBusca->bindParam(':proid', $etapa["protocolo_codigo"], PDO::PARAM_INT);
    $stBusca->bindParam(':eid', $etapa["codigo"], PDO::PARAM_INT);
    $stBusca->execute();
    $rsBusca = $stBusca->fetchAll(PDO::FETCH_ASSOC);
    if(sizeof($rsBusca)>0){
        foreach ($rsBusca as $tarefa){
        ?>
        <div class="accordion-item">
            <h2 class="accordion-header" id="panelsStayOpen-heading<?php echo $tarefa["codigo"]; ?>">
            <button class="accordion-button <?php echo $etapa["tarefa_codigo"]==$tarefa["codigo"]?"":"collapsed"; ?>" type="button" data-bs-toggle="collapse" data-bs-target="#panelsStayOpen-collapse<?php echo $tarefa["codigo"]; ?>" aria-expanded="true" aria-controls="panelsStayOpen-collapse<?php echo $tarefa["codigo"]; ?>">
             <?php echo $tarefa["nome"]; ?> <!-- &nbsp;<i class="bi-clock" ></i> Tempo estimado 1h30min -->
            </button>
            </h2>
            <div id="panelsStayOpen-collapse<?php echo $tarefa["codigo"]; ?>" class="accordion-collapse collapse <?php echo $etapa["tarefa_codigo"]==$tarefa["codigo"]?"show":""; ?>" aria-labelledby="panelsStayOpen-heading<?php echo $tarefa["codigo"]; ?>">
            <div class="accordion-body">
                <h6><?php echo $tarefa["descricao"]; ?></h6>
            
                <div id="tarefa_status_<?php echo $tarefa["codigo"]; ?>">
                <?php
                if(!$tarefa["manual"]){
                    ?>
                    <div class="card-group">
                    <div class="card">
                        <div class="card-header text-bg-light text-secondary placeholder-glow"><?php echo ICON_TAREFA_P1; ?> <span class="placeholder col-6"></span></div>
                    </div>
                    <div class="card">
                        <div class="card-header text-bg-light text-secondary placeholder-glow"><?php echo ICON_TAREFA_P2; ?> <span class="placeholder col-9"></span></div>
                    </div>
                    <div class="card">
                        <div class="card-header text-bg-light text-secondary placeholder-glow"><?php echo ICON_TAREFA_P3; ?> <span class="placeholder col-3"></span></div>
                    </div>
                    </div>
                    <script>
                    $(function() {
                        $("#tarefa_status_<?php echo $tarefa["codigo"]; ?>").load('ajax/get_farmaco_tarefa.php?fid=<?php echo $p_farmaco; ?>&pid=<?php echo $etapa["protocolo_codigo"]; ?>&tid=<?php echo $tarefa["codigo"]; ?>');
                    });
                    </script>
                    <?php
                 }   
                ?>
                </div>
            </div>
            </div>
        </div>
        <?php
        }   
    }
    unset($rsBusca);
    $rsBusca = null;
    $stBusca->closeCursor();

    ?>
</div>