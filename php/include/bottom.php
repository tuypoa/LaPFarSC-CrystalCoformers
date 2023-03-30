
		<br>
	</main>
  </div>
</div>

<!--
<footer class="container-fluid text-center">
  <p>
	LaPFarSC - Laborat&oacute;rio de Planejamento Farmac&ecirc;utico e Simula&ccedil;&atilde;o Computacional,
	UFRJ, Centro de Ci&ecirc;ncias da Sa&uacute;de.
	</p>
</footer>
-->

<!--
<img style='position: absolute;' src='images/load.gif' width='60'/>

<div width="100%" style="border-bottom: solid 1px #cccccc;">
<table cellspacing="0" cellpadding="18" border="0" >
<tr>
	<td style="padding-top: 22px;">
	<span style="padding-left:30px;font-size:16px;font-weight:bold;color:#9f9f9f;">Executando</span>
	<pre><?php
	$query = "
	SELECT m.codigo, m.nome, p.conteudo,
		TO_CHAR(p.datahora,'DD/MM/YYYY HH24:MI:SS') AS datahora,
		MAX(qi.scfcycles) as ciclos,
		(MAX(qie.cputime)/3600) as horas
	FROM psaux p 
		INNER JOIN maquina m ON p.maquina_codigo=m.codigo
		LEFT JOIN qeresumo r ON p.qeresumo_codigo=r.codigo
		LEFT JOIN qeinfoscf qi ON qi.qeresumo_codigo=r.codigo
		LEFT JOIN qeinfoiteration qie ON qie.qeresumo_codigo=r.codigo AND qi.scfcycles=qie.scfcycles
	WHERE comando_codigo = 2
	GROUP BY m.codigo, m.nome, p.conteudo, p.datahora
	ORDER BY m.online, m.nome
	";

$stBusca = $con->prepare($query);	
$stBusca->execute();
$rsBusca = $stBusca->fetchAll(PDO::FETCH_ASSOC);
if(sizeof($rsBusca)>0){
	foreach ($rsBusca as $obj){
		
		echo " ".$obj["datahora"]." <a href='maquina.php?mid=".$obj["codigo"]."' style='font-weight:bold;text-decoration:none;' ".($maquina!=null && $obj["codigo"]==$maquina["codigo"]?" class='fblue'":"").">".$obj["nome"]."</a>$ ".$obj["conteudo"];
		if($obj["ciclos"]!=null) { 
			echo " <span class='fblue'>(".$obj["ciclos"]." steps, ".($obj["horas"]>72?number_format($obj["horas"]/24,0)."d".number_format($obj["horas"]%24,0)."h":$obj["horas"]."h").")</span>"; 
		}
		echo "\n";

	}
}
unset($rsBusca);
$rsBusca = null;
$stBusca->closeCursor();
?></pre></td>
</tr>
<?php
$query = "
SELECT m.codigo, m.nome, a.nome as conteudo, r.concluido, r.erro,
TO_CHAR(r.ultimalida,'DD/MM/YYYY HH24:MI:SS') AS datahora,
MAX(qi.scfcycles) as ciclos,
(MAX(qie.cputime)/3600) as horas
FROM qeresumo r 		
	INNER JOIN qearquivoin a ON a.codigo=r.qearquivoin_codigo
	INNER JOIN maquina_qearquivoin qa ON a.codigo=qa.qearquivoin_codigo
	INNER JOIN maquina m ON m.codigo=qa.maquina_codigo
	INNER JOIN qeinfoscf qi ON qi.qeresumo_codigo=r.codigo
	INNER JOIN qeinfoiteration qie ON qie.qeresumo_codigo=r.codigo AND qi.scfcycles=qie.scfcycles
WHERE NOT executando
GROUP BY m.codigo, m.nome, a.nome, r.ultimalida, r.concluido, r.erro
ORDER BY r.ultimalida DESC
LIMIT 10
";

$stBusca = $con->prepare($query);	
$stBusca->execute();
$rsBusca = $stBusca->fetchAll(PDO::FETCH_ASSOC);
if(sizeof($rsBusca)>0){
?>
<tr>
	<td style="padding-top: 0px;">
	<span style="font-size:16px;font-weight:bold;color:#9f9f9f;">Hist&oacute;rico</span>
	<pre><?php
		foreach ($rsBusca as $obj){
			
			echo " ".$obj["datahora"]." <a href='maquina.php?mid=".$obj["codigo"]."' style='font-weight:bold;text-decoration:none;' ".($maquina!=null && $obj["codigo"]==$maquina["codigo"]?" class='fblue'":"").">".$obj["nome"]."</a>$ ".$obj["conteudo"];
			if($obj["ciclos"]!=null) { 
				echo " <span class='".($obj["concluido"]?"fgreen":($obj["erro"]!=null?"fred":"fgray"))."'>".($obj["erro"]!=null?"Error ":(!$obj["concluido"]?"Caiu ":""))."(".$obj["ciclos"]." steps, ".($obj["horas"]>72?number_format($obj["horas"]/24,0)."d".number_format($obj["horas"]%24,0)."h":$obj["horas"]."h").")</span>"; 
			}
			echo "\n";

		}	
	?></pre>
</td>
</tr>
<?php 
}
unset($rsBusca);
$rsBusca = null;
$stBusca->closeCursor();
?>
</table>
</div>

-->