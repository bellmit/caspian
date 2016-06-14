--
-- Database: `accounts`
--

USE `accounts`;

--
-- Table structure for table `account`
--

CREATE TABLE `account` (
  `account_id` varchar(64), 
  `id` varchar(64) NOT NULL,
  `state` varchar(16) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Default Account Creation
--

INSERT INTO `account`
(`account_id`,`id`,`state`) values
("default","default","ACTIVE");

--
-- Table structure for table `IdpPwd`
--

CREATE TABLE  `IdpPwd` (
  `idp_id` varchar(64) NOT NULL,
  `idp_user` text,
  `idp_pwd` text NOT NULL,
  PRIMARY KEY (`idp_id`)
  ) ENGINE=InnoDB DEFAULT CHARSET=utf8;
