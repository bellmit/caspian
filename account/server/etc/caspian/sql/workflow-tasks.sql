USE `workflow`;

--
-- Bootstraping task table with supported task methods
--

-- deleteAccount
INSERT INTO `task` VALUE ('aeae33fa-7afd-4bac-9f0d-63a803e3a5d1','deleteAccount','Boolean',NULL,'{\"type_bindings\":[{\"name\":\"accountId\",\"type\":\"String\"},{\"name\":\"keystoneUri\",\"type\":\"String\"},{\"name\":\"dbUserName\",\"type\":\"String\"},{\"name\":\"dbPassword\",\"type\":\"String\"},{\"name\":\"dbHostName\",\"type\":\"String\"},{\"name\":\"dbPort\",\"type\":\"String\"},{\"name\":\"database\",\"type\":\"String\"},{\"name\":\"listOfControllerHosts\",\"type\":\"String\"}]}');

