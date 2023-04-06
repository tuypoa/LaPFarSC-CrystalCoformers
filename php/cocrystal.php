<?php 
require_once("config/configuracao.php"); 

?>
<!doctype html>
<html lang="pt-BR">
<head><?php include_once 'include/head.php'; ?></head>
<body>
<?php include_once 'include/menu.php'; ?>

<?php 
$query = "SELECT p.codigo, p.nome, p.versao
		FROM protocolo p
		WHERE p.secao_codigo = :secaoid AND NOT desativado
        ORDER BY datahora DESC LIMIT 1 ";
$stBusca = $con->prepare($query);	
$stBusca->bindParam(':secaoid', $secao["codigo"], PDO::PARAM_INT);
$stBusca->execute();
$rsBusca = $stBusca->fetchAll(PDO::FETCH_ASSOC);
if(sizeof($rsBusca)>0){
    $protocolo = $rsBusca[0];
}
unset($rsBusca);
$rsBusca = null;
$stBusca->closeCursor();


?>

<div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3 border-bottom">
    <h1 class="h3"><?php echo $protocolo["nome"]; ?></h1>    
    <div class="btn-toolbar mb-2 mb-md-0">
        <span style="color:#999999;">Protocolo <?php echo $protocolo["versao"]; ?></span>
    </div>
</div>


<ol>
    <?php
    $query = "SELECT e.codigo as e_codigo, e.nome as e_nome, t.codigo as t_codigo, t.nome as t_nome, t.descricao, t.manual, t.tarefa_codigo 
            FROM tarefa t
                INNER JOIN etapa e ON t.etapa_codigo=e.codigo
            WHERE e.protocolo_codigo = :proid
            ORDER BY e.ordem, t.ordem";
    $stBusca = $con->prepare($query);
    $stBusca->bindParam(':proid', $protocolo["codigo"], PDO::PARAM_INT);
    $stBusca->execute();
    $rsBusca = $stBusca->fetchAll(PDO::FETCH_ASSOC);
    $etapaCodigo = null;
    if(sizeof($rsBusca)>0){
        foreach ($rsBusca as $obj){
            if($etapaCodigo != $obj["e_codigo"]){
                if($etapaCodigo!=null) echo "\n</ul>";
                echo "<li>".$obj["e_nome"]."</li>\n";
                echo "\n<ul>";
                $etapaCodigo = $obj["e_codigo"];
            }
            ?>				
            <li><?php echo $obj["t_nome"]; ?> <?php 
                    if(!$obj["manual"]){ 
                        ?><i class="bi-robot" style="color: cornflowerblue;"></i><?php 
                    } ?><br><span style="color:#999999;"><?php echo $obj["descricao"]; ?></span></li>
            <?php
        }
        if($etapaCodigo!=null) echo "</ul>";
    }
    unset($rsBusca);
    $rsBusca = null;
    $stBusca->closeCursor();
    ?> 
</ol>
<br>
<?php include_once 'include/bottom.php'; ?>
</body>
</html>



