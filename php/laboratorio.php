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
            SELECT m.codigo, m.hostname, ms.cpuused, ms.memused, ms.online,
                m.ignorar, ms.iniciarjob, (mi.valor > coalesce(ms.cpuused,mi.valor)) as ociosa,
                TO_CHAR(ms.datahora,'DD/MM/YY HH24:MI:SS') AS ultimoacesso
            FROM maquina m
                INNER JOIN (SELECT maquina_codigo, CAST(valor AS INTEGER) as valor 
								FROM maquina_infomaquina WHERE infomaquina_codigo=".INFOMAQUINA_CPU_OCIOSA.") as mi
					ON mi.maquina_codigo=m.codigo		
                LEFT JOIN 
                    (SELECT MAX(codigo) as codigo, maquina_codigo 
                        FROM maquinastatus 
                        GROUP BY maquina_codigo) maxms ON m.codigo=maxms.maquina_codigo
                LEFT JOIN maquinastatus ms ON maxms.codigo=ms.codigo		
            GROUP BY m.codigo, m.hostname, ms.cpuused, ms.memused, ms.online, 
                m.ignorar, ms.iniciarjob, ms.datahora, mi.valor
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
                    <div class="card text-bg-light" >   
                    <div class="card-body">
                        <h5 class="card-title"><?php echo $obj["hostname"]; ?></h5>
                        <h6 class="card-subtitle mb-2 text-muted"><?php echo $obj["ultimoacesso"]; ?></h6>
                        <?php
                            echo "<h6 class='card-title text-".($obj["online"]?(!$obj["ignorar"] && ($obj["ociosa"])?"danger":"primary"):"secondary")."'>".$obj["cpuused"]."% cpu</h6>";
                            echo "<p class='card-text text-success'>".$obj["memused"]."% Mem</p>";
                            
                        ?>
                        
                        <?php if(false && !$obj["ignorar"]){ ?>
                            <img src="graph/graph_maquina_mini.php?mid=<?php echo $obj["codigo"]; ?>&o=<?php echo $obj["ociosa"]?"1":"0"; ?>" class="img-responsive" style="width:100%" />
                        <?php } ?></div>
                    <div class="card-footer text-<?php echo $obj["online"]?"primary":"danger";?>"><?php echo ($obj["online"]?($obj["iniciarjob"]?"Dispon&iacute;vel":"Online"):"<span class='bi-alert'></span>  Offline"); ?></div>
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
            <div class="card text-bg-light">
               <div class="card-body">
                    <h5 class="card-title">Database</h5>
                    <?php
                        $query = "SELECT hostname FROM maquina WHERE head";
                        $stBusca = $con->prepare($query);	
                        $stBusca->execute();
                        $rsBusca = $stBusca->fetchAll(PDO::FETCH_ASSOC);
                        if(sizeof($rsBusca)>0){
                            ?><h6 class="card-subtitle mb-2 text-muted"><?php echo $rsBusca[0]["hostname"]; ?></h6><?php
                        }
                        unset($rsBusca);
                        $rsBusca = null;
                        $stBusca->closeCursor();
                    ?>
                    
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
                            echo "<div class='row'><div class='col text-primary'>Total</div><div class='col text-primary text-end'>".$dbsize["tam"]."</div></div>";
                            echo "";
                            foreach ($rsBusca as $obj){	
                                echo "<div class='row'><div class='col text-secondary'><small>".$obj["table_name"]."</small></div><div class='col text-secondary text-end'><small>".$obj["tamtb"]."</small></div></div>";
                            }
                            echo "";
                        }
                        unset($rsBusca);
                        $rsBusca = null;
                        $stBusca->closeCursor();
                    ?>
                </div>
            </div>
        </div>
        
    </div>
    
<?php include_once 'include/bottom.php'; ?>
</body>
</html>



