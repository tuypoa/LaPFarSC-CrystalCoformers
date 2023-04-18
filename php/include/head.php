<?php 

$currentPageNamePhp = substr($_SERVER["SCRIPT_NAME"],strrpos($_SERVER["SCRIPT_NAME"],"/")+1); 
$currentPageName = substr($currentPageNamePhp,0,strrpos($currentPageNamePhp,"."));
$pos = strrpos($currentPageNamePhp,"_");
if ($pos === false) {
    $currentPageNamePrefixo = $currentPageName;
}else{
    $currentPageNamePrefixo = substr($currentPageNamePhp,0,$pos);;
}

$secao = null;
$protocolo = null;

$query = "SELECT s.codigo, s.nome, s.link FROM secao s WHERE s.link LIKE :link LIMIT 1";
$stBusca = $con->prepare($query);
$paramLink = $currentPageNamePrefixo.'%';
$stBusca->bindParam(':link', $paramLink, PDO::PARAM_STR);
$stBusca->execute();
$rsBusca = $stBusca->fetchAll(PDO::FETCH_ASSOC);
if(sizeof($rsBusca)>0){
    $secao = $rsBusca[0];
}
unset($rsBusca);
$rsBusca = null;
$stBusca->closeCursor();

/* SOMENTE 1 POR ENQUANTO */
if($secao==null){
    $query = "SELECT s.codigo, s.nome, s.link FROM secao s WHERE s.codigo=:eid ";
    $stBusca = $con->prepare($query);
    $paramLink = $currentPageNamePrefixo.'%';
    $tmpcodigo = SECAO_COCRYSTAL;
    $stBusca->bindParam(':eid', $tmpcodigo, PDO::PARAM_INT);
    $stBusca->execute();
    $rsBusca = $stBusca->fetchAll(PDO::FETCH_ASSOC);
    if(sizeof($rsBusca)>0){
        $secao = $rsBusca[0];
    }
    unset($rsBusca);
    $rsBusca = null;
    $stBusca->closeCursor();    
}


if($secao!=null){
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
}



?><title>LaPFarSC <?php 
if($farmaco == null && $secao!=null) {
    echo " / ".$secao["nome"];
}
if($farmaco!=null) {
    echo " / ".$farmaco["nome"];
}
?></title>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
  
    <link rel="stylesheet" href="bootstrap/css/bootstrap.min.css">
    <link rel="stylesheet" href="bootstrap/bootstrap-icons.css">
    <link rel="stylesheet" href="include/style.css"/>

    <script src="bootstrap/js/bootstrap.bundle.min.js"></script>
    <script src="bootstrap/Chart.min.js"></script>

    <script src="include/jquery.min.js"></script>
    <script src="include/3Dmol-min.js""></script>

    <script>
		function loadEtapaFarmaco(id){
			$("#etapa_load").load('ajax/get_farmaco_etapa.php?farid='+id);
		}

        function fdd(tipo,url){
            window.location.href = url+'&dd='+tipo;
        }
    </script>
