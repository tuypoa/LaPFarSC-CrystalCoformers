<?php 
require_once("../config/configuracao.php"); 
require_once('phplot.php');

$ppe = $_GET["pe"]=="1";
$ppv = $_GET["pv"]=="1";
$ppd = $_GET["pd"]=="1";
$pps = $_GET["ps"]=="1";

$id_resumo = is_numeric($_GET["rid"])? $_GET["rid"] : NULL ;

$limit_query = "";//"LIMIT 100";
$cor_param = "#4895ff";
if($ppe){
	$cor_param = "#ff7575";
	$query = "SELECT qi.scfcycles,qi.enthalpy valor
			FROM qeresumo r
				INNER JOIN qeinfoscf qi ON qi.qeresumo_codigo=r.codigo
			WHERE r.codigo = :rid
			ORDER BY scfcycles DESC ".$limit_query;
}else if($ppv){
	$cor_param = "#63d3ff";
	$query = "SELECT qi.scfcycles,qi.volume valor
			FROM qeresumo r
				INNER JOIN qeinfoscf qi ON qi.qeresumo_codigo=r.codigo
			WHERE r.codigo = :rid
			ORDER BY scfcycles DESC ".$limit_query;
}else if($ppd){
	$cor_param = "#76b5a0";
	$query = "SELECT qi.scfcycles,qi.density valor
			FROM qeresumo r
				INNER JOIN qeinfoscf qi ON qi.qeresumo_codigo=r.codigo
			WHERE r.codigo = :rid
			ORDER BY scfcycles DESC ".$limit_query;
}else if($pps){
	$cor_param = "#820081";
	$query = "SELECT qi.scfcycles, (max(cputime)-min(cputime)) valor
			FROM qeresumo r
				INNER JOIN qeinfoscf qi ON qi.qeresumo_codigo=r.codigo
				INNER JOIN qeinfoiteration qie ON r.codigo=qie.qeresumo_codigo AND qi.scfcycles=qie.scfcycles
			WHERE r.codigo = :rid
			GROUP BY qi.scfcycles
			ORDER BY scfcycles DESC ".$limit_query;
}


$query = "SELECT * FROM (".$query.") AS tb ORDER BY scfcycles ASC";

$stBusca = $con->prepare($query);	
$stBusca->bindParam(':rid', $id_resumo, PDO::PARAM_INT);
$stBusca->execute();
$rsBusca = $stBusca->fetchAll(PDO::FETCH_ASSOC);
if(sizeof($rsBusca)>0){
	$data = array();
	foreach ($rsBusca as $obj){
	//	$registro = array();
		array_push( $data, array($obj["scfcycles"],$obj["valor"]) );
	}
//print_r($dados);
}else{
	$data = array(array('', 0, 0), array('', 0, 0));
}
unset($rsBusca);
$rsBusca = null;
$stBusca->closeCursor();


$plot = new PHPlot(120,60);
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
if($pps){	
//	$plot->SetYTitle("horas");
}

//$plot->SetYTickIncrement(0.2);
//$plot->SetPlotAreaWorld(0, 0, 20, 100);
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

//$plot->SetBackgroundColor('#ffffff');
$plot->SetDrawPlotAreaBackground(true);
$plot->SetPlotBgColor('#ffffff');


$plot->SetDataColors(array($cor_param));
$plot->SetDataValues($data);
 
#Exibimos o gráfico
//$plot->SetPlotType('thinbarline');
$plot->DrawGraph();

?>
