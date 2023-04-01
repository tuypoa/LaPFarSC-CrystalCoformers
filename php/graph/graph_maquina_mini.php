<?php 
require_once("../config/configuracao.php"); 
require_once('phplot.php');

$id_maquina = is_numeric($_GET["mid"])? $_GET["mid"] : NULL ;
$ociosa = is_numeric($_GET["o"])? ($_GET["o"]=="1") : NULL ;

$query = "
        SELECT datahora,cpuused,memused
        FROM jarleitura
        WHERE maquina_codigo = :mid
        ORDER BY codigo DESC LIMIT 25
    	";

$stBusca = $con->prepare($query);	
$stBusca->bindParam(':mid', $id_maquina, PDO::PARAM_INT);
$stBusca->execute();
$rsBusca = $stBusca->fetchAll(PDO::FETCH_ASSOC);
if(sizeof($rsBusca)>0){
	$data = array();
	foreach ($rsBusca as $obj){
	//	$registro = array();
		array_push( $data, array($obj["datahora"],$obj["cpuused"],$obj["memused"]) );
	}
//print_r($dados);
}else{
	$data = array(array('', 0, 0), array('', 0, 0));
}
unset($rsBusca);
$rsBusca = null;
$stBusca->closeCursor();


$plot = new PHPlot(145,50);
/*
$data = array(array('', 0, 0), array('', 1, 9));
$plot->SetDataValues($data);
$plot->SetDataType('data-data');
$plot->DrawGraph();

*/
//SetFileFormat("png");
 
#Indicamos o título do gráfico e o título dos dados no eixo X e Y do mesmo
//$plot->SetTitle("Grafico de exemplo");
//$plot->SetXTitle("Eixo X");
//$plot->SetYTitle("Eixo Y");

//$plot->SetYTickIncrement(0.2);
$plot->SetPlotAreaWorld(0, 0, 25, 100);
$plot->SetImageBorderType('plain');
$plot->SetLineWidths(2);
$plot->SetPlotType('lines');
# Turn on Y data labels:
//$plot->SetYDataLabelPos('plotin');
$plot->SetXDataLabelPos('plotin');

# Turn on X data label lines (drawn from X axis up to data point):
//$plot->SetDrawXDataLabelLines(True);

# With Y data labels, we don't need Y ticks, Y tick labels, or grid lines.
$plot->SetYTickLabelPos('none');
$plot->SetYTickPos('none');
$plot->SetDrawYGrid(False);
# X tick marks are meaningless with this data:
$plot->SetXTickPos('none');
//$p->SetDefaultTTFont('./arial.ttf');
$plot->SetXTickLabelPos('none');

//$plot->SetPlotBorderType('full');
$plot->SetPlotBorderType('none');

$plot->SetBackgroundColor('#ffffff');
$plot->SetDrawPlotAreaBackground(true);
$plot->SetPlotBgColor('#ffffff');

if($ociosa){
	$plot->SetDataColors(array('red','#3bb54a'));
}else{
	$plot->SetDataColors(array('#4895ff','#3bb54a'));
}
$plot->SetDataValues($data);
 

#Exibimos o gráfico
//$plot->SetPlotType('thinbarline');
$plot->DrawGraph();


?>
