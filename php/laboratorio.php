<?php 
require_once("config/configuracao.php"); 

?>
<!doctype html>
<html lang="pt-BR">
<head><?php include_once 'include/head.php'; ?></head>
<body>
<?php include_once 'include/menu.php'; ?>

<div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3 border-bottom">
    <h1 class="h3">M&aacute;quinas dispon&iacute;veis</h1>
    
</div>
    
<div class="row row-cols-1 row-cols-md-4 g-4">
        <?php
        $query = "
            SELECT m.codigo, m.hostname, ms.cpuused, ms.memused, ms.online, m.ignorar, ms.iniciarjob,
                TO_CHAR(ms.datahora,'DD/MM/YY HH24:MI:SS') AS ultimoacesso
            FROM maquina m
                LEFT JOIN maquinastatus ms ON m.codigo=ms.maquina_codigo
            GROUP BY m.codigo, m.hostname, ms.cpuused, ms.memused, ms.online, m.ignorar, ms.iniciarjob, ms.datahora
            ORDER BY ms.online, m.hostname
            ";

        $stBusca = $con->prepare($query);	
        $stBusca->execute();
        $rsBusca = $stBusca->fetchAll(PDO::FETCH_ASSOC);
        if(sizeof($rsBusca)>0){
            $i = 0;
            foreach ($rsBusca as $obj){	
                
                ?>
                <div class="col">
                    <div class="card border-<?php echo $obj["online"]?"success":"danger";?>" >
                    <div class="card-header"><?php echo $obj["hostname"]; ?></div>
                    <div class="card-body text-<?php echo $obj["online"]?"success":"danger";?>">
                        <p class="card-text">
                        <?php
                            echo "<span style='color:".($obj["online"]?(!$obj["ignorar"] && ($obj["cpuused"] <50 || $obj["ociosa"])?"red":"blue"):"#666666").";font-size:14px;font-weight:bold;'>".$obj["cpuused"]."% cpu</span><br>";
                            echo "<span style='color:".($obj["online"]?"green":"#666666").";font-size:13px;'>".$obj["memused"]."% Mem</span><br>";
                            echo "<span style='font-size:12px;'>".$obj["ultimoacesso"]."</span><br>";
                        ?>
                        </p>
                        <?php if(false && !$obj["ignorar"]){ ?>
                            <img src="graph/graph_maquina_mini.php?mid=<?php echo $obj["codigo"]; ?>&o=<?php echo $obj["ociosa"]?"1":"0"; ?>" class="img-responsive" style="width:100%" />
                        <?php } ?></div>
                    <div class="card-footer text-muted"><?php echo ($obj["online"]?($obj["iniciarjob"]?"Autorizada a iniciar processo...":"Online"):"<span class='bi-alert'></span>  Offline"); ?></div>
                </div>
                </div>
                <?php			
            }
        }
        unset($rsBusca);
        $rsBusca = null;
        $stBusca->closeCursor();

        
        ?>
    
        <div class="col"> 
            <div class="card border-primary">
                <div class="card-header">Database</div>
                <div class="card-body text-primary">
                    <p class="card-text">
                    <?php
                        $query = "SELECT 
                            pg_size_pretty(pg_database_size('".Ambiente::$dbase."')) as tam,
                            table_name, 
                            pg_size_pretty( pg_total_relation_size(quote_ident(table_name))) as tamtb
                        FROM 
                            information_schema.tables
                        WHERE 
                            table_schema = 'public'
                        ORDER BY 
                            pg_total_relation_size(quote_ident(table_name)) DESC limit 3";
                        $stBusca = $con->prepare($query);	
                        $stBusca->execute();
                        $rsBusca = $stBusca->fetchAll(PDO::FETCH_ASSOC);
                        if(sizeof($rsBusca)>0){
                            $dbsize = $rsBusca[0];
                            echo "<span style='font-size:13px;'>Total: ".$dbsize["tam"]."</span><br>";
                            foreach ($rsBusca as $obj){	
                                echo "<span style='color:#666666;font-size:12px;'>&nbsp; ".$obj["table_name"].": ".$obj["tamtb"]."</span><br>";
                            }
                        }
                        unset($rsBusca);
                        $rsBusca = null;
                        $stBusca->closeCursor();
                    ?></p>
                </div>
                <div class="card-footer text-muted">Online</div>
            </div>
        </div>
        
    </div>
    
<?php include_once 'include/bottom.php'; ?>
</body>
</html>



