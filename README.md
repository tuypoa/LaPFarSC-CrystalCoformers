# CrystalCoformers
Project to collect and analyse data of drugs and their cocrystals using scientific softwares on multiple machines.

<b>INSTALL</b>
1. Install postgresql server following instructions: postgresql/README.md
2. Install php server (apache2) and modules "php-pgsql" "php-gd"
<pre>
$ sudo apt-get install php-pgsql php-gd
$ sudo service apache2 restart
</pre>
3. Deploy JARs and check directories on db table "maquina_infomaquina" and "javadeploy"
4. Configure cron execution
<pre>
$ crontab -e
# m h  dom mon dow   command
*/15 * * * * java -jar /home/farmacia/Documentos/Guilherme/bin/java-CrystalCoformers_v0.1.0.jar 127.0.0.1
</pre>
