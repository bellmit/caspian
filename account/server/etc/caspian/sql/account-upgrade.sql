--
-- Database: `accounts`
--

USE `accounts`;

--
-- Delete table temp_account if it already exists in database
--

DROP TABLE IF EXISTS temp_account;

--
-- Insert temperory account table in accounts database
--

CREATE TABLE `temp_account` (
  `account_id` varchar(64),
  `id` varchar(64) NOT NULL,
  `state` varchar(16) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Insert existing accounts into account_domain table
--

INSERT INTO temp_account
        (`account_id`, `id`, `state`)
        SELECT account_id, domain_id, "ACTIVE"
        FROM account_domain_membership;

--
-- set foreign key check to 0
--

SET foreign_key_checks = 0;

--
-- Delete account_domain_membership table
--

DROP TABLE account_domain_membership;

--
-- Delete account table
--

DROP TABLE account;

--
-- Delete primary_role_assignment table
--

DROP TABLE primary_role_assignment;

--
-- change temperory account table to account table
--

Alter table `temp_account` rename to `account`;
