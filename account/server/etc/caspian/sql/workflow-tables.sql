--
-- Database: `workflow`
--

USE `workflow`;

--
-- Table structure for table `job`
--

CREATE TABLE `job` (
  `id` varchar(64) NOT NULL,
  `target_type` varchar(16) NOT NULL,
  `target_frame_id` varchar(64),
  `target_name` varchar(64) NOT NULL,
  `priority` tinyint,
  `status` varchar(32) NOT NULL,
  `creation_time` bigint,
  `completion_time` bigint,
  `parameters` text,
  `environment` text,
  `execution_state` text,
  `output` text,
  `err_stream` text,
  `out_stream` text,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table `task`
--

CREATE TABLE `task` (
  `id` varchar(64) NOT NULL,
  `name` varchar(64) NOT NULL,
  `return_type` varchar(64) NOT NULL,
  `jar_id` varchar(64),
  `parameters` text,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table `task_frame`
--

CREATE TABLE `task_frame` (
  `id` varchar(64) NOT NULL,
  `job_id` varchar(64) NOT NULL,
  `workflow_id` varchar(64),
  `task_id` varchar(64) NOT NULL,
  `priority` tinyint,
  `status` varchar(32) NOT NULL,
  `creation_time` bigint,
  `start_time` bigint,
  `end_time` bigint,
  `attempt_counter` tinyint,
  `previous_attempt_id` varchar(64),
  `parameters` text,
  `environment` text,
  `output` text,
  `err_stream` text,
  `out_stream` text,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table `task_queue`
--

CREATE TABLE `task_queue` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `message` text NOT NULL,
  `creation_time` bigint,
  `lease_time` bigint,
  `lease_period` bigint,
  `lease_count` bigint,
  `handle` varchar(64),
  `retry_interval` bigint,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

--
-- Table structure for table `task_completion_queue`
--

CREATE TABLE `task_completion_queue` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `message` text NOT NULL,
  `creation_time` bigint,
  `lease_time` bigint,
  `lease_period` bigint,
  `lease_count` bigint,
  `handle` varchar(64),
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


--
-- stored procedures for test-and-set lock and retrieved locked item
--

DELIMITER $$

CREATE PROCEDURE `get_from_task_queue` (
    in_lease_time bigint,
    in_lease_period bigint,
    in_handle varchar(64),
    OUT queue_id varchar(64),
    OUT queue_message text,
    OUT queue_creation_time bigint
    )
BEGIN
    START TRANSACTION;
      SELECT id, message, creation_time
      INTO queue_id, queue_message, queue_creation_time
      FROM task_queue
      where handle IS NULL OR ((lease_time IS NULL OR (lease_time + lease_period < in_lease_time)) AND (retry_interval IS NULL OR (creation_time + retry_interval < in_lease_time)))
      LIMIT 1
      FOR UPDATE;

      UPDATE task_queue
      SET handle = in_handle, lease_time = in_lease_time, lease_period = in_lease_period, lease_count = lease_count+1
      WHERE id = queue_id;
    COMMIT;
END
$$

CREATE PROCEDURE `get_from_task_completion_queue` (
    in_lease_time bigint,
    in_lease_period bigint,
    in_handle varchar(64),
    OUT queue_id varchar(64),
    OUT queue_message text,
    OUT queue_creation_time bigint
    )
BEGIN
    START TRANSACTION;
      SELECT id, message, creation_time
      INTO queue_id, queue_message, queue_creation_time
      FROM task_completion_queue
      where handle IS NULL OR (lease_time IS NOT NULL AND (lease_time + lease_period < in_lease_time))
      LIMIT 1
      FOR UPDATE;

      UPDATE task_completion_queue
      SET handle = in_handle, lease_time = in_lease_time, lease_period = in_lease_period, lease_count = lease_count+1
      WHERE id = queue_id;
    COMMIT;
END
$$

DELIMITER ;
