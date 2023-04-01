 
<header class="navbar navbar-dark sticky-top bg-dark flex-md-nowrap p-0 shadow">
  <a class="navbar-brand col-md-3 col-lg-2 me-0 px-3 fs-6" href="index.php"><img src="images/logo-lab-top.png" alt="Laborat&oacute;rio de Planejamento Farmac&ecirc;utico e Simula&ccedil;&atilde;o Computacional" /></a>
  <button class="navbar-toggler position-absolute d-md-none collapsed" type="button" data-bs-toggle="collapse" data-bs-target="#sidebarMenu" aria-controls="sidebarMenu" aria-expanded="false" aria-label="Toggle navigation">
    <span class="navbar-toggler-icon"></span>
  </button>
  <!--
  <div class="navbar-nav  w-100">
    <div class="nav-item text-nowrap">
      <a class="nav-link px-3" href="#"><span class="bi-columns-gap" class="align-text-bottom"></span> Login</a>
    </div>
  </div>
-->
</header>


<div class="container-fluid">
  <div class="row">
    <nav id="sidebarMenu" class="col-md-3 col-lg-2 d-md-block bg-light sidebar collapse">
      <div class="position-sticky pt-3 sidebar-sticky">
        <ul class="nav flex-column">
          <li class="nav-item">
              <a class="nav-link <?php echo ('laboratorio.php'==$currentPageNamePhp?"active":""); ?>" aria-current="page" href="laboratorio.php">
                <span class="bi-diagram-3" class="align-text-bottom"></span>
                Laborat&oacute;rio
              </a>
          </li>
          
          <?php
            $query = "SELECT s.codigo, s.nome, s.link, s.icon FROM secao s ORDER BY s.ordem asc";
            $stBusca = $con->prepare($query);	
            $stBusca->execute();
            $rsBusca = $stBusca->fetchAll(PDO::FETCH_ASSOC);
            if(sizeof($rsBusca)>0){
              foreach ($rsBusca as $obj){
                $selecionaMenu = ($obj["link"]==$currentPageNamePhp || 
                    substr($obj["link"],0,strrpos($obj["link"],".")) == substr($currentPageNamePhp,0,strrpos($currentPageNamePhp,"_")) );
                  ?>					
                  <li class="nav-item">
                      <a class="nav-link <?php echo ($selecionaMenu?"active":""); ?>" href="<?php echo $obj["link"]; ?>">
                        <span class="bi-<?php echo $obj["icon"]; ?>" class="align-text-bottom"></span>
                        <?php echo $obj["nome"]; ?>
                      </a>
                  </li>
                  <?php
              }
            }
            unset($rsBusca);
            $rsBusca = null;
            $stBusca->closeCursor();
            ?>
       
       <?php
         // if($secao!=null) {
         //   switch ($secao["codigo"]) {
          //    case SECAO_COCRYSTAL:
                  ?>
                <li class="nav-item px-2">
                  <a class="nav-link <?php echo ($currentPageNamePhp=="cocrystal_addfarmaco.php"?"active":""); ?>" href="cocrystal_addfarmaco.php">
                    <span class="bi-file-earmark-plus" class="align-text-bottom"></span>
                    Adicionar F&aacute;rmaco
                  </a>
                </li> 

                  <?php
                  $query = "SELECT f.codigo, f.nome, p.codigo as protocolo_codigo
                            FROM farmaco f 
                              LEFT JOIN farmaco_protocolo fp ON f.codigo=fp.farmaco_codigo
                              LEFT JOIN protocolo p ON p.codigo=fp.protocolo_codigo
                            WHERE (fp.farmaco_codigo IS NULL OR p.desativado) 
                                AND NOT f.desativado
                            ORDER BY f.datahora desc, f.nome asc";
                  $stBusca = $con->prepare($query);	
                  $stBusca->execute();
                  $rsBusca = $stBusca->fetchAll(PDO::FETCH_ASSOC);
                  if(sizeof($rsBusca)>0){
                    $i = 0;
                    foreach ($rsBusca as $obj){
                      $selecionaFarmaco = ($farmaco!=null && $obj["codigo"]==$farmaco["codigo"]) ;
                      ?>					
                      <li class="nav-item  px-2">
                        <a class="nav-link <?php echo ($selecionaFarmaco?"active":""); ?>" href="cocrystal_farmaco.php?farid=<?php echo $obj["codigo"]; ?>" 
                              style="<?php echo ($obj["desativado"]?"text-decoration: line-through;":""); ?> <?php echo ($selecionaFarmaco?"font-weight:bold;":""); ?>">
                            <span class="bi-file-earmark" class="align-text-bottom"></span>
                            <?php echo htmlspecialchars($obj["nome"]); ?>
                            <?php if($i++ == 0 && $obj["protocolo_codigo"]==null) { ?><span class="badge text-bg-primary">Novo</span><?php } ?>
                        </a>
                      </li>
                      <?php
                    }
                  }
                  unset($rsBusca);
                  $rsBusca = null;
                  $stBusca->closeCursor();
                  ?>

                  <?php 
          //        break;
          //    }
          //}
          ?>

        </ul>


        <?php
          //  switch ($secao["codigo"]) {
          //    case SECAO_COCRYSTAL:

            $query = "SELECT e.codigo, e.nome, e.icon, COUNT(f.codigo) as qtde_farmaco FROM etapa e
                          LEFT JOIN farmaco_protocolo fp 
                              ON e.protocolo_codigo=fp.protocolo_codigo
                              AND e.codigo=fp.etapa_codigo
                          LEFT JOIN farmaco f ON f.codigo=fp.farmaco_codigo AND NOT f.desativado 
                       WHERE e.protocolo_codigo = :protocoloid
                       GROUP BY e.codigo, e.nome, e.icon, e.ordem
                       ORDER BY ordem";
            $stBusca = $con->prepare($query);	
            $stBusca->bindParam(':protocoloid', $protocolo["codigo"], PDO::PARAM_INT);
            $stBusca->execute();
            $rsBusca = $stBusca->fetchAll(PDO::FETCH_ASSOC);
            if(sizeof($rsBusca)>0){
              $i = 1;
              foreach ($rsBusca as $obj){
              ?>
              <h6 class="sidebar-heading d-flex justify-content-between align-items-center px-4 mt-3 mb-1 text-muted text-uppercase">
                <span><?php echo ($i++).". ".$obj["nome"]; ?></span>
                <a class="link-secondary" href="#" aria-label="Arquivos">
                  <span class="badge rounded-pill text-bg-<?php echo $obj["qtde_farmaco"]>0?"primary":"secondary"; ?>"><?php echo $obj["qtde_farmaco"]; ?></span>
                </a>
              </h6>
              <ul class="nav flex-column mb-2 px-3">
                  <?php
                  $query = "SELECT f.codigo, f.nome
                            FROM farmaco f 
                              INNER JOIN farmaco_protocolo fp 
                                  ON f.codigo=fp.farmaco_codigo
                            WHERE fp.etapa_codigo = :eid AND NOT f.desativado
                            ORDER BY f.nome asc, f.datahora desc";
                  $stBusca2 = $con->prepare($query);
                  $stBusca2->bindParam(':eid', $obj["codigo"], PDO::PARAM_INT);
                  $stBusca2->execute();
                  $rsBusca2 = $stBusca2->fetchAll(PDO::FETCH_ASSOC);
                  if(sizeof($rsBusca2)>0){
                    foreach ($rsBusca2 as $objFarmaco){
                      $selecionaFarmaco = ($farmaco!=null && $objFarmaco["codigo"]==$farmaco["codigo"]) ;
                      ?>					
                      <li class="nav-item">
                        <a class="nav-link <?php echo ($selecionaFarmaco?"active":""); ?>" href="cocrystal_farmaco.php?farid=<?php echo $objFarmaco["codigo"]; ?>" 
                              style="<?php echo ($selecionaFarmaco?"font-weight:bold;":""); ?>">
                            <span class="bi-<?php echo $obj["icon"]; ?>" class="align-text-bottom"></span>
                            <?php echo htmlspecialchars($objFarmaco["nome"]); ?>
                        </a>
                      </li>
                      <?php
                    }
                  }
                  unset($rsBusca);
                  $rsBusca = null;
                  $stBusca->closeCursor();
              ?>
            </ul>
            <?php
            }
          }
          unset($rsBusca);
          $rsBusca = null;
          $stBusca->closeCursor();

         //   break;
        //}
      //}
      ?>

  
        <?php
        $query = "SELECT f.codigo, f.nome
                  FROM farmaco f 
                  WHERE f.desativado
                  ORDER BY f.nome asc, f.datahora desc";
        $stBusca2 = $con->prepare($query);
        $stBusca2->execute();
        $rsBusca2 = $stBusca2->fetchAll(PDO::FETCH_ASSOC);
        if(sizeof($rsBusca2)>0){
          ?>
          <hr>
          <ul class="nav flex-column mb-2 px-2">
          <?php
          foreach ($rsBusca2 as $objFarmaco){
            $selecionaFarmaco = ($farmaco!=null && $objFarmaco["codigo"]==$farmaco["codigo"]) ;
            ?>					
            <li class="nav-item">
              <a class="nav-link <?php echo ($selecionaFarmaco?"active":""); ?>" href="cocrystal_farmaco.php?farid=<?php echo $objFarmaco["codigo"]; ?>" 
                    style="text-decoration: line-through;">
                  <span class="bi-exclamation-diamond" class="align-text-bottom"></span>
                  <?php echo htmlspecialchars($objFarmaco["nome"]); ?>
              </a>
            </li>
            <?php
          }
          ?>
          </ul>
          <?php
        }
        unset($rsBusca);
        $rsBusca = null;
        $stBusca->closeCursor();
        ?>
        

      </div>
    </nav>

    <main class="col-md-9 ms-sm-auto col-lg-10 px-md-4">

