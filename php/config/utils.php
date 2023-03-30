<?php


function calcularSistemaCristal($cellparams){    
    $linhas = explode("\n",$cellparams);
    if(sizeof($linhas)==4){    
        $cell_a = $linhas[1];
        $cell_b = $linhas[2];
        $cell_c = $linhas[3];
        do {
            $cell_a = str_replace("  ", " ", trim($cell_a), $count);
        } while ($count > 0);
        do {
            $cell_b = str_replace("  ", " ", trim($cell_b), $count);
        } while ($count > 0);
        do {
            $cell_c = str_replace("  ", " ", trim($cell_c), $count);
        } while ($count > 0);
        $cell_a = explode(" ",$cell_a);
        $cell_b = explode(" ",$cell_b);
        $cell_c = explode(" ",$cell_c);

        $cell_a = array( floatval($cell_a[0]), floatval($cell_a[1]), floatval($cell_a[2]) );
        $cell_b = array( floatval($cell_b[0]), floatval($cell_b[1]), floatval($cell_b[2]) );
        $cell_c = array( floatval($cell_c[0]), floatval($cell_c[1]), floatval($cell_c[2]) );

        //print_r($cell_a);

        $lado_A = sqrt( pow($cell_a[0], 2) + pow($cell_a[1], 2) + pow($cell_a[2], 2) );
        $lado_B = sqrt( pow($cell_b[0], 2) + pow($cell_b[1], 2) + pow($cell_b[2], 2) );
        $lado_C = sqrt( pow($cell_c[0], 2) + pow($cell_c[1], 2) + pow($cell_c[2], 2) );

        //print_r($lado_A);

        $alpha = rad2deg( acos( ( $cell_b[0]*$cell_c[0] + $cell_b[1]*$cell_c[1] + $cell_b[2]*$cell_c[2] ) / 
                            ( abs( sqrt(pow($cell_b[0], 2)+pow($cell_b[1], 2)+pow($cell_b[2], 2))) * 
                              abs( sqrt(pow($cell_c[0], 2)+pow($cell_c[1], 2)+pow($cell_c[2], 2)))  ) ) );

        $beta = rad2deg( acos( ( $cell_a[0]*$cell_c[0] + $cell_a[1]*$cell_c[1] + $cell_a[2]*$cell_c[2] ) / 
                            ( abs( sqrt(pow($cell_a[0], 2)+pow($cell_a[1], 2)+pow($cell_a[2], 2))) * 
                              abs( sqrt(pow($cell_c[0], 2)+pow($cell_c[1], 2)+pow($cell_c[2], 2)))  ) ) );

        $gamma = rad2deg( acos( ( $cell_a[0]*$cell_b[0] + $cell_a[1]*$cell_b[1] + $cell_a[2]*$cell_b[2] ) / 
                              ( abs( sqrt(pow($cell_a[0], 2)+pow($cell_a[1], 2)+pow($cell_a[2], 2))) * 
                                abs( sqrt(pow($cell_b[0], 2)+pow($cell_b[1], 2)+pow($cell_b[2], 2)))  ) ) );

        //print_r($alpha);

        $lados = array($lado_A, $lado_B, $lado_C);
        $angulos = array($alpha, $beta, $gamma);

        return array($lados, $angulos);
    }
    return null;    
}


function definirNomeSistemaCristal($cellparams){
        echo "<!-- \n ".$cellparams." -->";

        $celula = calcularSistemaCristal($cellparams);
        $lad = $celula[0];
        $ang = $celula[1];
        echo "<!-- \n "; print_r( $celula ); echo " -->";


        if($lad[0]!=$lad[1] && $lad[0]!=$lad[2] && $lad[1]!=$lad[2] &&
            $ang[0]==90 && $ang[1]==90 && $ang[2]==90 ){
            echo "Ortorr&ocirc;mbico";

        }else if($lad[0]==$lad[1] && $lad[0]!=$lad[2] && 
            $ang[0]==90 && $ang[1]==90 && $ang[2]==90 ){
            echo "Tetragonal";

        }else if($lad[0]==$lad[1] && $lad[0]!=$lad[2] && 
            $ang[0]==90 && $ang[1]==90 && $ang[2]==120 ){
            echo "Hexagonal";

        }else if($lad[0]==$lad[1] && $lad[0]==$lad[2] &&
            $ang[1]==$ang[2] && $ang[0]==$ang[2] ){
            echo "Rombo&eacute;drico";

        }else if($lad[0]==$lad[1] && $lad[0]==$lad[2] &&
            $ang[0]==90 && $ang[1]==90 && $ang[2]==90 ){
            echo "C&uacute;bico";

        }else if($lad[0]!=$lad[1] && $lad[0]!=$lad[2] && $lad[1]!=$lad[2] &&
            $ang[1]!=$ang[2] && $ang[0]!=$ang[2] && $ang[0]!=$ang[1]  ){

            echo "Tricl&iacute;nico";
        }
    
    
    }


?>