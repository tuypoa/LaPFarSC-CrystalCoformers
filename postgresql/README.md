
1. Install postgresql and change the password
<pre>
root# apt-get install postgresql
root$ su postgres
postgres$ psql
	> ALTER ROLE postgres WITH PASSWORD 'postgres';
	\q
</pre>
2. Execute 01-TABLES.sql

3. Change access config 
<pre>
root# gedit /etc/postgresql/13/main/pg_hba.conf
	# IPv4 local connections:
	### ADD LINE: ###
	host    all             all             all		        md5
	
root# gedit /etc/postgresql/13/main/postgresql.conf
	# - Connection Settings -
	### CHANGE THIS LINE: ###
	#FROM:
	 #listen_addresses = 'localhost'
	#TO: 
	 listen_addresses = '*'
	 
root# service postgresql restart
</pre>
