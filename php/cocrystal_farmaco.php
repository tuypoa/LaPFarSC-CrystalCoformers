<?php 
require_once("config/configuracao.php"); 

$msg = $_GET["msg"]=="1"?1:0;

$id_farmaco = is_numeric($_GET["farid"])? $_GET["farid"] : NULL ;
$desativar = is_numeric($_GET["dd"])? $_GET["dd"] : NULL ;

$farmaco = null;

$query = "SELECT f.codigo, f.nome, f.filename
		FROM farmaco f
		WHERE f.codigo = :farid ";
$stBusca = $con->prepare($query);	
$stBusca->bindParam(':farid', $id_farmaco, PDO::PARAM_INT);
$stBusca->execute();
$rsBusca = $stBusca->fetchAll(PDO::FETCH_ASSOC);
if(sizeof($rsBusca)>0){
	$farmaco = $rsBusca[0];
}
unset($rsBusca);
$rsBusca = null;
$stBusca->closeCursor();

$farmaco_protocolo = null;

$query = "SELECT p.codigo, p.nome, p.versao, fp.etapa_codigo, s.icon
		FROM protocolo p
			INNER JOIN secao s ON s.codigo=p.secao_codigo
			INNER JOIN farmaco_protocolo fp ON p.codigo=fp.protocolo_codigo
		WHERE fp.farmaco_codigo = :farmacoid AND NOT p.desativado";
$stBusca = $con->prepare($query);	
$stBusca->bindParam(':farmacoid', $farmaco["codigo"], PDO::PARAM_INT);
$stBusca->execute();
$rsBusca = $stBusca->fetchAll(PDO::FETCH_ASSOC);
if(sizeof($rsBusca)>0){
	$farmaco_protocolo = $rsBusca[0];
}
unset($rsBusca);
$rsBusca = null;
$stBusca->closeCursor();


//PASSO 1: SALVAR PROTOCOLO
if($farmaco_protocolo == null){
	$p_protocolo = $_POST["farmaco_protocolo"];
	if (isset($_POST["submit"]) && is_numeric($p_protocolo)) {

		$query = "SELECT t.codigo, t.etapa_codigo 
					FROM tarefa t 
						INNER JOIN etapa e ON e.codigo=t.etapa_codigo
					WHERE e.protocolo_codigo=:protocolo ORDER BY e.ordem, t.ordem LIMIT 1";
		$stBusca = $con->prepare($query);	
		$stBusca->bindParam(':protocolo', $p_protocolo, PDO::PARAM_INT);
		$stBusca->execute();
		$rsBusca = $stBusca->fetchAll(PDO::FETCH_ASSOC);
		if(sizeof($rsBusca)>0){
			$tarefa = $rsBusca[0];
		}
		unset($rsBusca);
		$rsBusca = null;
		$stBusca->closeCursor();
		

		$stInsert = $con->prepare("INSERT INTO farmaco_protocolo(farmaco_codigo,protocolo_codigo,etapa_codigo,tarefa_codigo) 
										VALUES (:farmaco,:protocolo,:etapa,:tarefa)");
		$stInsert->bindParam(":farmaco", $farmaco["codigo"], PDO::PARAM_INT);
		$stInsert->bindParam(":protocolo", $p_protocolo, PDO::PARAM_INT);
		$stInsert->bindParam(":etapa", $tarefa["etapa_codigo"], PDO::PARAM_INT);
		$stInsert->bindParam(":tarefa", $tarefa["codigo"], PDO::PARAM_INT);
		$exec = $stInsert->execute();
		$stInsert->closeCursor();

		if($exec){
			$query = "SELECT inf.codigo as infocodigo, inf.nome as infonome, ti.codigo as tipocodigo, ti.nome as tiponome
						FROM infofarmaco inf
							INNER JOIN tipoinfo ti ON inf.tipoinfo_codigo=ti.codigo
						ORDER BY ti.codigo";
			$stBusca = $con->prepare($query);
			$stBusca->execute();
			$rsBusca = $stBusca->fetchAll(PDO::FETCH_ASSOC);
			if(sizeof($rsBusca)>0){
				foreach ($rsBusca as $obj){
					$p_info = $_POST["input-".$obj["infocodigo"]];
					$params["input-".$obj["infocodigo"]] = $p_info;
					if($p_info!=null && strlen($p_info) > 0){		
						$stInsert = $con->prepare("INSERT INTO farmaco_infofarmaco(farmaco_codigo,infofarmaco_codigo,valor) values (:farmaco,:info,:valor)");
						$stInsert->bindParam(":farmaco", $farmaco["codigo"] , PDO::PARAM_INT);
						$stInsert->bindParam(":info", $obj["infocodigo"] , PDO::PARAM_INT);
						$stInsert->bindParam(":valor", $p_info, PDO::PARAM_STR);
						$exec = $stInsert->execute();
						$stInsert->closeCursor();
					}
				}
			}
			unset($rsBusca);
			$rsBusca = null;
			$stBusca->closeCursor();

			header("Location: cocrystal_farmaco.php?farid=".$farmaco["codigo"]);
			header("Content-Length: 0");
			header("Connection: close");
			flush();
		}
	}else{
		$query = "SELECT infofarmaco_codigo as infocodigo, valor
					FROM farmaco_infofarmaco
					WHERE farmaco_codigo=:farmaco";
		$stBusca = $con->prepare($query);
		$stBusca->bindParam(":farmaco", $farmaco["codigo"] , PDO::PARAM_INT);
		$stBusca->execute();
		$rsBusca = $stBusca->fetchAll(PDO::FETCH_ASSOC);
		if(sizeof($rsBusca)>0){
			foreach ($rsBusca as $obj){
				$params["input-".$obj["infocodigo"]] = $obj["valor"];
			}
		}
		unset($rsBusca);
		$rsBusca = null;
		$stBusca->closeCursor();
	}
}

//DESATIVAR FARMACO
if($desativar!=null){
	$stUpdate = $con->prepare("UPDATE farmaco SET desativado = :desativado WHERE codigo=:farmaco");
	$stUpdate->bindParam(":farmaco", $farmaco["codigo"] , PDO::PARAM_INT);
	$stUpdate->bindParam(":desativado", $desativar, PDO::PARAM_INT);
	$exec = $stUpdate->execute();
	$stUpdate->closeCursor();
	if($exec){
		if($desativar) {
			header("Location: cocrystal_addfarmaco.php");
		}else{
			header("Location: cocrystal_farmaco.php?farid=".$farmaco["codigo"]);
		}
		header("Content-Length: 0");
		header("Connection: close");
		flush();
	}
}


?>
<!doctype html>
<html lang="pt-BR">
<head><?php include_once 'include/head.php'; ?></head>
<body>
<?php include_once 'include/menu.php'; ?>

<div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3 border-bottom">
    <h1 class="h3"><?php echo htmlspecialchars($farmaco["nome"]); ?></h1>    
	<?php
	if($farmaco_protocolo!=null){
		?><div class="btn-toolbar mb-2 mb-md-0">
			<div class="btn-group me-2">
				<button type="button" class="btn btn-sm btn-outline-secondary">Arquivo </button>
				<button type="button" class="btn btn-sm btn-outline-secondary">Ver em 3D</button>
			</div>
			<button type="button" class="btn btn-sm btn-outline-secondary" disabled>
				<span class="bi-<?php echo $farmaco_protocolo["icon"]; ?>" class="align-text-bottom"></span>
				<?php echo $farmaco_protocolo["versao"]; ?>
			</button>
		</div>
		<?php
	}
	?>
</div>

<?php 
if($farmaco_protocolo == null){
	?>
	<div class="row">
	<div class="col-sm-8"> 
		<?php if($msg){ ?>
			<div class="alert alert-success" role="alert">Arquivo enviado com sucesso.</div>	
		<?php } ?>
		<div class="alert alert-warning" role="alert">Complete as informa&ccedil;&otilde;es abaixo e inicie o protocolo.</div>
		<form action="" method="post">
			<div class="input-group mb-3">
				<label class="input-group-text" for="farmaco_protocolo">Protocolo</label>
				<select class="form-select" name="farmaco_protocolo" id="farmaco_protocolo">
					<?php
					$query = "SELECT codigo, nome, versao
								FROM protocolo
								WHERE secao_codigo=:secaoid AND NOT desativado
								ORDER BY datahora desc";
					$stBusca = $con->prepare($query);	
					$stBusca->bindParam(':secaoid', $secao["codigo"], PDO::PARAM_INT);
					$stBusca->execute();
					$rsBusca = $stBusca->fetchAll(PDO::FETCH_ASSOC);
					if(sizeof($rsBusca)>0){
						foreach ($rsBusca as $obj){
							?>			
							<option value="<?php echo $obj["codigo"]; ?>"><?php echo htmlspecialchars($obj["nome"])." ".$obj["versao"].""; ?></option>		
							<?php
						}
					}
					unset($rsBusca);
					$rsBusca = null;
					$stBusca->closeCursor();
					?>
				</select>
			</div>
			
			<?php
			$query = "SELECT inf.codigo as infocodigo, inf.nome as infonome, ti.codigo as tipocodigo, ti.nome as tiponome
						FROM infofarmaco inf
							INNER JOIN tipoinfo ti ON inf.tipoinfo_codigo=ti.codigo
						ORDER BY ti.codigo";
			$stBusca = $con->prepare($query);
			$stBusca->execute();
			$rsBusca = $stBusca->fetchAll(PDO::FETCH_ASSOC);
			if(sizeof($rsBusca)>0){
				$tipocodigo = 0;
				foreach ($rsBusca as $obj){
					if($tipocodigo != $obj["tipocodigo"]){
						if($tipocodigo!=0){
							?>
							</fieldset>
							<?php
						}
						?>
						<fieldset>
							<legend class="fs-5 fw-light"><?php echo $obj["tiponome"]; ?></legend>
							<?php
						$tipocodigo = $obj["tipocodigo"];
					}
					?>			
					<div class="input-group mb-3">
						<span class="input-group-text" id="input-<?php echo $obj["infocodigo"]; ?>"><?php echo $obj["infonome"]; ?></span>
						<input type="text" class="form-control" aria-label="" aria-describedby="input-<?php echo $obj["infocodigo"]; ?>" 
							placeholder="Preencha aqui" name="input-<?php echo $obj["infocodigo"]; ?>" id="input-<?php echo $obj["infocodigo"]; ?>" 
							value="<?php echo htmlspecialchars($params["input-".$obj["infocodigo"]]); ?>">
					</div>
					<?php
				}
				if($tipocodigo!=0){
					?>
					</fieldset>
					<?php
				}
			}
			unset($rsBusca);
			$rsBusca = null;
			$stBusca->closeCursor();
			?>
			<button type="button" name="cancelar" class="btn btn-outline-primary" onclick="fdd(1,'<?php echo $currentPageNamePhp; ?>?farid=<?php echo $farmaco["codigo"]; ?>');">Cancelar</button>
			&nbsp;&nbsp;
			<button type="submit" name="submit" class="btn btn-primary">Iniciar protocolo <i class="bi-chevron-compact-right"></i></button>
		</form>
		<br>
		</div>
		<div class="col-sm-4 text-center"> 
			<div class="card">
				<div class="card-header">Arquivo enviado</div>
				<div class="card-body" style="padding:0px;"><div id="container-01" class="mol-container"></div>
				</div>
				<div class="card-footer text-body-secondary" style="padding:0px;">
					<!-- Button trigger modal -->
					<button type="button" class="btn btn-link" data-bs-toggle="modal" data-bs-target="#modalArquivo">
						<i class="bi-file-earmark-text"></i> <?php echo htmlspecialchars($farmaco["filename"]); ?> 
					</button>
				</div>
			</div>
			
		</div>
	</div>

	<!-- Modal -->
	<div class="modal fade" id="modalArquivo" data-bs-backdrop="static" data-bs-keyboard="false" tabindex="-1" aria-labelledby="staticBackdropLabel" aria-hidden="true">
	<div class="modal-dialog modal-xl modal-dialog-scrollable">
		<div class="modal-content">
		<div class="modal-header">
			<h1 class="modal-title fs-5" id="staticBackdropLabel">Arquivo: <?php echo htmlspecialchars($farmaco["filename"]); ?></h1>
			<button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
		</div>
		<div class="modal-body">
			<pre  id="farmaco_arquivo"></pre>
		</div>
		<div class="modal-footer">
			<button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Fechar</button>
		</div>
		</div>
	</div>
	</div>
	<script>
		$(function() {
			let element = $('#container-01');
			let config = { backgroundColor: 'white'};
			let viewer = $3Dmol.createViewer( element, config );
			let pdbUri = 'ajax/get_farmaco_arquivo.php?taid=<?php echo TIPOARQUIVO_ENVIADO; ?>&farid=<?php echo $farmaco["codigo"]; ?>';
			jQuery.ajax( pdbUri, { 
				success: function(data) {
					$('#farmaco_arquivo').html(data);
					let v = viewer;
					v.addModel( data, "<?php echo substr($farmaco["filename"],strrpos($farmaco["filename"],".")+1); ?>" );    /* load data */
					v.zoomTo();                                      /* set camera */
					v.setStyle({},{stick:{}}); 
					v.render();                                      /* render scene */
					v.zoom(1.1, 1000);                               /* slight zoom */
				},
				error: function(hdr, status, err) {
				console.error( "Failed to load: " + err );
				},
			});
		});
	</script>
<?php 
}else{
	?>
	<div class="row">
		<div class="col-sm-12">
			

			<div class="spinner-border spinner-border-sm text-primary" role="status">
				<span class="visually-hidden">Loading...</span>
			</div>
		</div>
	</div>
	<?php 
}
?>
<?php include_once 'include/bottom.php'; ?>
</body>
</html>



