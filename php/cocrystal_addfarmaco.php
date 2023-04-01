<?php 
require_once("config/configuracao.php"); 


if(isset($_POST["submit"])) {

    $p_nome		= $_POST['farmaco_nome'];

    $uploadOk = 1;
    $msgUpload = "";
    $filename = basename($_FILES["fileToUpload"]["name"]);
    $fileType = strtolower(pathinfo($filename,PATHINFO_EXTENSION));

    // Check file size
    if (($_FILES["fileToUpload"]["size"] / 1024) > 500) {
        $msgUpload =  "Tamanho acima de 500Kb, verifique se o arquivo cont&eacute;m somente 1 mol&eacute;cula.";
        $uploadOk = 0;
    }

    // Allow certain file formats
    if($fileType != "cif" && $fileType != "mol2" && $fileType != "xyz" && $fileType != "pdb" ) {
        $msgUpload =  "Apenas aquivos CIF, MOL2, XYZ e PDB s&atilde;o permitidos.";
        $uploadOk = 0;
    }

    if($filename==""){
        $msgUpload =  "Por favor, selecione um arquivo antes de enviar.";
        $uploadOk = 0;
    }

    // Check if $uploadOk is set to 0 by an error
    if ($uploadOk == 0) {
        if($msgUpload == ""){
            $msgUpload =  "Ocorreu um erro desconhecido.";
        }
    // if everything is ok, try to upload file
    } else {
        $file = fopen($_FILES["fileToUpload"]["tmp_name"], "r");
        while(!feof($file)){ 
            $fileConteudo = $fileConteudo.fgets($file);
        }
        fclose($file);
        
        if(strlen($fileConteudo) < 10){
            $msgUpload =  "Verifique se o arquivo cont&eacute;m algum conte&uacute;do.";
            $uploadOk = 0;
        }else{
            //inserir no banco de dados
            $hash_md5 = hash('md5', $fileConteudo);
            
            $query = "SELECT f.codigo, f.nome FROM farmaco f
                        WHERE f.hash LIKE :hash AND NOT f.desativado LIMIT 1 ";
            $stBusca = $con->prepare($query);	
            $stBusca->bindParam(':hash', $hash_md5, PDO::PARAM_STR);
            $stBusca->execute();
            $rsBusca = $stBusca->fetchAll(PDO::FETCH_ASSOC);
            if(sizeof($rsBusca)>0){
                $msgUpload =  "O f&aacute;rmaco j&aacute; foi cadastrado anteriormente com o nome de \"".htmlspecialchars($rsBusca[0]["nome"])."\".";
                $uploadOk = 0;
            }
            unset($rsBusca);
            $rsBusca = null;
            $stBusca->closeCursor();
            
            if($uploadOk == 1){
                //
                if($p_nome==""){
                    $p_nome = $filename;
                }
                $arquivo_id = null;
                $arquivo_id = null;

                $stInsert = $con->prepare("INSERT INTO farmaco(nome,filename,hash) values (:nome,:filename,:hash)");
                $stInsert->bindParam(":nome", $p_nome, PDO::PARAM_STR);
                $stInsert->bindParam(":filename", $filename, PDO::PARAM_STR);
                $stInsert->bindParam(":hash", $hash_md5 , PDO::PARAM_STR);
                $exec = $stInsert->execute();
                if($exec){
                    $farmaco_id = $con->lastInsertId();
                }
                $stInsert->closeCursor();

                $query = "SELECT codigo FROM arquivo WHERE hash LIKE :hash ORDER BY codigo LIMIT 1 ";
                $stBusca = $con->prepare($query);	
                $stBusca->bindParam(':hash', $hash_md5, PDO::PARAM_STR);
                $stBusca->execute();
                $rsBusca = $stBusca->fetchAll(PDO::FETCH_ASSOC);
                if(sizeof($rsBusca)>0){
                    $arquivo_id = $rsBusca[0]["codigo"];
                }
                unset($rsBusca);
                $rsBusca = null;
                $stBusca->closeCursor();

                if($arquivo_id==null){
                    $stInsert = $con->prepare("INSERT INTO arquivo(filename,hash,conteudo) values (:filename,:hash,:conteudo)");
                    $stInsert->bindParam(":filename", $filename, PDO::PARAM_STR);
                    $stInsert->bindParam(":hash", $hash_md5 , PDO::PARAM_STR);
                    $stInsert->bindParam(":conteudo", $fileConteudo, PDO::PARAM_STR);
                    $exec = $stInsert->execute();
                    if($exec){
                        $arquivo_id = $con->lastInsertId();
                    }
                    $stInsert->closeCursor();
                }
                if($farmaco_id!=null && $arquivo_id!=null){
                    $stInsert = $con->prepare("INSERT INTO farmaco_arquivo(farmaco_codigo,arquivo_codigo,tipoarquivo_codigo) values (:farmaco,:arquivo,:tipo)");
                    $stInsert->bindParam(":farmaco", $farmaco_id , PDO::PARAM_INT);
                    $stInsert->bindParam(":arquivo", $arquivo_id , PDO::PARAM_INT);
                    $tempcodigo = TIPOARQUIVO_ENVIADO;
                    $stInsert->bindParam(":tipo", $tempcodigo, PDO::PARAM_INT);
                    $exec = $stInsert->execute();
                    $stInsert->closeCursor();
                
                    //$msgUpload =  "Arquivo enviado com sucesso.";
                    header("Location: cocrystal_farmaco.php?msg=1&farid=".$farmaco_id);
                    header("Content-Length: 0");
                    header("Connection: close");
                    flush();
                    //Do work here

                    unlink($_FILES["fileToUpload"]["tmp_name"]);
                }else{
                    $msgUpload =  "Ocorreu um erro ao vincular o arquivo ao sistema.";
                    $uploadOk = 0;
                }
            }
        }
    }
}

?>
<!doctype html>
<html lang="pt-BR">
<head><?php include_once 'include/head.php'; ?>
<script>
    function enviar(btn){
        btn.style.display = 'none';
        document.getElementById('enviando').style.display = '';
    }
</script>
</head>
<body>
<?php include_once 'include/menu.php'; ?>


<div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3 border-bottom">
    <h1 class="h3">Adicionar F&aacute;rmaco</h1>    

</div>


<div class="col-12"> 

    <form action="cocrystal_addfarmaco.php" method="post" enctype="multipart/form-data">  

    <?php 
        if($msgUpload!=""){
            ?><div class="alert alert-danger" role="alert"><?php echo ($filename==""?"":"Arquivo n&atilde;o foi enviado: <strong>".htmlspecialchars($filename)."</strong><br>").$msgUpload; ?></div><?php 
        }
    ?>

    <div class="input-group mb-3">
        <span class="input-group-text" id="inputGroup-nome">Nome</span>
        <input type="text" class="form-control" aria-label="F&aacute;rmaco" aria-describedby="inputGroup-nome" placeholder="F&aacute;rmaco" name="farmaco_nome" id="farmaco_nome" value="<?php echo htmlspecialchars($p_nome); ?>">
    </div>
    <div class="input-group mb-3">
        <input type="file" class="form-control" name="fileToUpload" id="fileToUpload">
        <label class="input-group-text" for="fileToUpload">.cif .mol2 .xyz .pdb</label>
    </div>
    <p>Cetifique-se de que h&aacute; somente UMA mol&eacute;cula do f&aacute;rmaco no arquivo.</p>
    

    <input type="submit" value="Enviar" name="submit" class="btn btn-primary" onclick="enviar(this);">
    <button id="enviando" class="btn btn-primary" type="button" disabled style="display: none;">
        <span class="spinner-grow spinner-grow-sm" role="status" aria-hidden="true"></span>
        Enviando...
    </button>
    </form>
</div>

<?php include_once 'include/bottom.php'; ?>
</body>
</html>



