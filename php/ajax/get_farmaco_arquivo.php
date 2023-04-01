<?php
require_once("../config/configuracao.php"); 

$p_farmaco = preg_match("/^[0-9]+$/", $_GET['farid']) ? $_GET['farid'] : 0;
$p_tipoarquivo = preg_match("/^[0-9]+$/", $_GET['taid']) ? $_GET['taid'] : 0;

$query = "SELECT conteudo
    FROM arquivo a 
        INNER JOIN farmaco_arquivo fa ON a.codigo=fa.arquivo_codigo 
            AND fa.farmaco_codigo = :farid
    WHERE fa.tipoarquivo_codigo = :tipoid ";
$stBusca = $con->prepare($query);	
$stBusca->bindParam(':farid', $p_farmaco, PDO::PARAM_INT);
$stBusca->bindParam(':tipoid', $p_tipoarquivo, PDO::PARAM_INT);
$stBusca->execute();
$rsBusca = $stBusca->fetchAll(PDO::FETCH_ASSOC);
if(sizeof($rsBusca)>0){
    header("Content-Type: text/plain");
    echo $rsBusca[0]["conteudo"];
}
unset($rsBusca);
$rsBusca = null;
$stBusca->closeCursor();
?>