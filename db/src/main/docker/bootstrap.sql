---  Create root password
GRANT ALL ON *.* TO root@'%' IDENTIFIED BY 'dbPASS1_' WITH GRANT OPTION; FLUSH PRIVILEGES;

---  Create keystone database and user

CREATE DATABASE `keystone`;

--
-- admin user for keystone
-- username: keystoneadmin
-- password: keystone
--
GRANT ALL PRIVILEGES ON keystone.* TO 'keystoneadmin'@'localhost' IDENTIFIED BY 'keystone';
GRANT ALL PRIVILEGES ON keystone.* TO 'keystoneadmin'@'%' IDENTIFIED BY 'keystone';

---  Create account service database and user

CREATE DATABASE `accounts`;

--
-- admin user for accounts
-- username: accountadmin
-- password: accountadmin
--
GRANT ALL PRIVILEGES ON `accounts`.* TO 'accountadmin'@'localhost' IDENTIFIED BY 'accountadmin';
GRANT ALL PRIVILEGES ON `accounts`.* TO 'accountadmin'@'%' IDENTIFIED BY 'accountadmin';

---  Create workflow database and user

CREATE DATABASE `workflow`;

--
-- admin user for workflow
-- username: workflowadmin
-- password: workflowadmin
--
GRANT ALL PRIVILEGES ON `workflow`.* TO 'workflowadmin'@'localhost' IDENTIFIED BY 'workflowadmin';
GRANT ALL PRIVILEGES ON `workflow`.* TO 'workflowadmin'@'%' IDENTIFIED BY 'workflowadmin';
GRANT SELECT ON mysql.proc TO 'workflowadmin'@'localhost' IDENTIFIED BY 'workflowadmin';
GRANT SELECT ON mysql.proc TO 'workflowadmin'@'%' IDENTIFIED BY 'workflowadmin';

