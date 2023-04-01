<?php 
require_once("Ambiente.class.php"); 

try{
	$con = new PDO("pgsql:host=".Ambiente::$host.";port=5432;dbname=".Ambiente::$dbase.";", Ambiente::$user, Ambiente::$pswd);
}catch (PDOException $e) {
	echo 'Connection failed: ' . $e->getMessage();
	//echo json_encode($return);
	die();
}

//setlocale(LC_ALL, "pt_BR.UTF-8");;

//ini_set('display_startup_errors', 1);
//error_reporting(E_ALL);

require_once("constantes.php"); 
require_once("utils.php"); 

?>
