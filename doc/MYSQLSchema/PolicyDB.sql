-- --------------------------------------------------------
-- Host:                         127.0.0.1
-- Server version:               5.6.25-log - MySQL Community Server (GPL)
-- Server OS:                    Win64
-- HeidiSQL Version:             9.3.0.4984
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8mb4 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;

-- Dumping database structure for policydb
DROP DATABASE IF EXISTS policydb;
CREATE DATABASE IF NOT EXISTS `policydb` /*!40100 DEFAULT CHARACTER SET utf8 */;
USE `policydb`;


-- Dumping structure for function policydb.add_script
DELIMITER //
CREATE DEFINER=`root`@`localhost` FUNCTION `add_script`(script_name varchar(50)) RETURNS int(11)
BEGIN
                declare script_id_var int;
                insert into ob_script(script_name, count) values (script_name, 0);
                select MAX(script_id) into script_id_var from ob_script;
RETURN script_id_var;
END//
DELIMITER ;


-- Dumping structure for function policydb.allowed_operations
DELIMITER //
CREATE DEFINER=`root`@`localhost` FUNCTION `allowed_operations`(ua_id_in int(11), oa_id_in int(11)) RETURNS varchar(500) CHARSET utf8
BEGIN

DECLARE policy_id_in int;
DECLARE opset_id int;
DECLARE allowed_ops varchar(500);
DECLARE done boolean DEFAULT FALSE;
DECLARE names VARCHAR(8000);
DECLARE policies CURSOR FOR select a.start_node_id as policy_id from assignment a where get_node_type(a.start_node_id) = 2 and end_node_id = oa_id_in;
DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
  OPEN policies;
  SET allowed_ops='';
  ploicy_loop: LOOP
    FETCH policies INTO policy_id_in;
    IF done THEN
      LEAVE ploicy_loop;
    END IF;
    BEGIN
                                DECLARE opsets CURSOR FOR SELECT a.opset_id from association as a where is_member(ua_id_in, a.ua_id) and is_member(oa_id_in, a.oa_id) and is_member(ua_id_in, policy_id_in);
                                open opsets;
                                opset_loop: loop
                                                FETCH opsets INTO opset_id;
            IF done THEN
                                                                LEAVE opset_loop;
                                                END IF;
                                                set allowed_ops = CONCAT(allowed_ops,',',opset_id);
                                end loop opset_loop;
                                CLOSE opsets;
                END;
  END LOOP ploicy_loop;
  CLOSE policies;
  IF substring(allowed_ops FROM 1 FOR 1) = ',' THEN
                SET allowed_ops = substring(allowed_ops,2);
  END IF;

                SELECT group_concat(concat(name) separator ',') into names from operation as o, operation_set_details as osd
                where o.operation_id = osd.operation_id
                and osd.operation_set_details_node_id in (allowed_ops);

RETURN Names;
END//
DELIMITER ;


-- Dumping structure for table policydb.application
CREATE TABLE IF NOT EXISTS `application` (
  `application_id` int(11) NOT NULL AUTO_INCREMENT,
  `host_id` int(11) NOT NULL,
  `application_name` varchar(50) NOT NULL,
  `application_main_class` varchar(200) NOT NULL,
  `application_path` varchar(2000) NOT NULL,
  `application_prefix` varchar(50) NOT NULL,
  PRIMARY KEY (`application_id`),
  KEY `fk_application_host_id_idx` (`host_id`),
  KEY `idx_application_application_name` (`application_name`),
  CONSTRAINT `fk_application_host_id` FOREIGN KEY (`host_id`) REFERENCES `host` (`host_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='application';

-- Data exporting was unselected.


-- Dumping structure for table policydb.assignment
CREATE TABLE IF NOT EXISTS `assignment` (
  `assignment_id` int(11) NOT NULL AUTO_INCREMENT,
  `start_node_id` int(11) DEFAULT NULL,
  `end_node_id` int(11) DEFAULT NULL,
  `depth` int(11) DEFAULT NULL,
  `assignment_path_id` int(2) DEFAULT NULL,
  PRIMARY KEY (`assignment_id`),
  KEY `end_node_id_idx` (`end_node_id`),
  KEY `fk_start_node_id_idx` (`start_node_id`),
  KEY `idx_all_columns` (`start_node_id`,`depth`,`assignment_path_id`,`end_node_id`),
  CONSTRAINT `fk_end_node_id` FOREIGN KEY (`end_node_id`) REFERENCES `node` (`node_id`) ON DELETE CASCADE ON UPDATE NO ACTION,
  CONSTRAINT `fk_start_node_id` FOREIGN KEY (`start_node_id`) REFERENCES `node` (`node_id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='This table stores assignment relations';

-- Data exporting was unselected.


-- Dumping structure for table policydb.assignment_path
CREATE TABLE IF NOT EXISTS `assignment_path` (
  `assignment_path_id` int(11) NOT NULL AUTO_INCREMENT,
  `assignment_node_id` int(11) NOT NULL,
  PRIMARY KEY (`assignment_path_id`),
  KEY `fk_assignment_node_id` (`assignment_node_id`),
  CONSTRAINT `fk_assignment_node_id` FOREIGN KEY (`assignment_node_id`) REFERENCES `node` (`node_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table policydb.audit_information
CREATE TABLE IF NOT EXISTS `audit_information` (
  `SESS_ID` varchar(32) NOT NULL,
  `USER_ID` varchar(32) NOT NULL,
  `USER_NAME` varchar(80) NOT NULL,
  `HOST_NAME` varchar(80) NOT NULL,
  `TS` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `ACTION` varchar(80) DEFAULT NULL,
  `RESULT_SUCCESS` tinyint(1) DEFAULT NULL,
  `DESCRIPTION` varchar(300) DEFAULT NULL,
  `OBJ_ID` varchar(80) DEFAULT NULL,
  `OBJ_NAME` varchar(80) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for procedure policydb.create_assignment
DELIMITER //
CREATE DEFINER=`root`@`localhost` PROCEDURE `create_assignment`(start_node int, end_node int)
BEGIN
DECLARE node_type_id_in int;
DECLARE path_id int;
                IF exists ( SELECT NODE_ID FROM policydb.NODE WHERE NODE_ID = start_node) THEN
                                IF exists ( SELECT NODE_ID FROM policydb.NODE WHERE NODE_ID = END_NODE)  THEN
                                                IF start_node <> end_node THEN
                                                                IF get_node_type(start_node) <> 7 and get_node_type(end_node) <> 7 THEN
                                                                                -- create path_id
                                                                                INSERT INTO ASSIGNMENT_PATH (ASSIGNMENT_NODE_ID) VALUES (END_NODE);
                                                                                SELECT MAX(ASSIGNMENT_PATH_ID) INTO path_id FROM ASSIGNMENT_PATH;
                                                                                -- Insert in assignment table
                                                                                INSERT INTO policydb.ASSIGNMENT (start_node_id, end_node_id, depth, assignment_path_id)
                                                                                                (SELECT start_node, end_node, 1, path_id FROM DUAL)
                                                                                UNION
                                                                                                (SELECT DISTINCT start_node_id, end_node, depth+1, path_id
                                                                                FROM policydb.ASSIGNMENT
                                                                                WHERE end_node_id = start_node
                                                                                AND assignment_path_id > 0
                                                                                AND depth > 0);
                                                                ELSE
                                                                                INSERT INTO policydb.ASSIGNMENT (start_node_id, end_node_id, depth) values
                                                                                                (start_node, end_node, 1 );
                                                                END IF;
                                                END IF;
                                                IF get_node_type(end_node) <> 2 THEN -- other than policy class
                                                                -- delete connection to connector
                                                                DELETE FROM assignment WHERE START_NODE_ID = 1 AND END_NODE_ID = end_node AND depth = 1;
                                                END IF;
                                END IF;
                END IF;
END//
DELIMITER ;


-- Dumping structure for procedure policydb.create_association
DELIMITER //
CREATE DEFINER=`root`@`localhost` PROCEDURE `create_association`(oa_node int, ua_node int, opset_name varchar(45), operations varchar(1000))
BEGIN
DECLARE node_id int;
DECLARE opset_id int;
-- Insert in assignment table
                IF not exists ( SELECT NAME FROM policydb.NODE WHERE UPPER(NAME) = UPPER(opset_name) ) THEN
                                call create_opset(opset_name, operations);
                END IF;
                SELECT get_node_id(opset_name, 's') INTO opset_id;

                -- Create dual assignment. Assiciations will have path_id null
    if oa_node is not null then
                                INSERT INTO policydb.ASSIGNMENT (start_node_id, end_node_id, depth) values (oa_node, opset_id, 1);
                end if;
    if ua_node is not null then
                                INSERT INTO policydb.ASSIGNMENT (start_node_id, end_node_id, depth) values (opset_id, ua_node, 1);
                end if;
END//
DELIMITER ;


-- Dumping structure for procedure policydb.create_host
DELIMITER //
CREATE DEFINER=`root`@`localhost` PROCEDURE `create_host`(user_id int, hostname varchar(50), domain_controller_ind varchar(1), path varchar(200))
BEGIN
                IF NOT EXISTS (SELECT HOST_ID FROM HOST WHERE UPPER(HOST_NAME) = UPPER(HOSTNAME)) THEN
                                INSERT INTO HOST (HOST_NAME, IS_DOMAIN_CONTROLLER, WORKAREA_PATH)
                                                VALUES(hostname, domain_controller_ind, path);
                END IF;

END//
DELIMITER ;


-- Dumping structure for function policydb.create_node_fun
DELIMITER //
CREATE DEFINER=`root`@`localhost` FUNCTION `create_node_fun`(node_name varchar(200), node_type_name varchar(20), node_description varchar(200),base_node_id int(11)) RETURNS int(11)
BEGIN
DECLARE node_type_id_in int;
DECLARE inserted_node_id int;
-- Insert in Node table
                                SELECT NODE_TYPE_ID INTO node_type_id_in FROM NODE_TYPE WHERE UPPER(NAME) = UPPER(node_type_name);
    INSERT INTO NODE (NODE_TYPE_ID, NAME, description) VALUES (node_type_id_in,node_name,node_description);
    SELECT MAX(NODE_ID) INTO inserted_node_id FROM NODE;
    -- create self assignment
                 INSERT INTO ASSIGNMENT (start_node_id, end_node_id, depth,assignment_path_id) VALUES (inserted_node_id, inserted_node_id,0,0);
    -- add assignment to the given base node
    IF base_node_id is not NULL THEN
      CALL create_assignment(base_node_id,inserted_node_id);
    END IF;
  RETURN inserted_node_id;
END//
DELIMITER ;


-- Dumping structure for procedure policydb.create_object_class
DELIMITER //
CREATE DEFINER=`root`@`localhost` PROCEDURE `create_object_class`(object_class_name varchar(45), object_class_id_in int, class_object_description varchar(100))
BEGIN
-- Insert in object_class table
                IF not exists ( SELECT OBJECT_CLASS_ID FROM policydb.OBJECT_CLASS WHERE object_class_id = object_class_id_in ) THEN
                                INSERT INTO policydb.OBJECT_CLASS (object_class_id, name, description) values (object_class_id_in, object_class_name, class_object_description);
                END IF;
END//
DELIMITER ;


-- Dumping structure for procedure policydb.create_object_detail
DELIMITER //
CREATE DEFINER=`root`@`localhost` PROCEDURE `create_object_detail`(object_id int, original_obj_node_id int, object_class_id_in int, host_id_in int, obj_path varchar(200), include_ascedants_in int(1), template_id_in int(11))
BEGIN
DECLARE new_object_id int;
  IF object_id is not null THEN
                IF exists ( SELECT node_id FROM NODE WHERE node_id = object_id) THEN
                                INSERT INTO OBJECT_DETAIL (object_node_id, original_node_id, object_class_id, host_id, path, include_ascedants, template_id)
                                                    VALUES (object_id, original_obj_node_id, object_class_id_in, host_id_in, obj_path, include_ascedants_in, template_id_in);
      SELECT MAX(OBJECT_NODE_ID) INTO new_object_id from object_detail;
                END IF;
  END IF;
END//
DELIMITER ;


-- Dumping structure for function policydb.create_ob_cont_spec
DELIMITER //
CREATE DEFINER=`root`@`localhost` FUNCTION `create_ob_cont_spec`(event_pattern_id_in varchar(50), node_type_in varchar(50),
                cont_spec_value_in varchar(50)) RETURNS int(11)
BEGIN
                declare cont_spec_id_var int;
                insert into ob_cont_spec (event_pattern_id, cont_spec_type, cont_spec_value)
                                values (event_pattern_id_in, get_node_type_id(node_type_in), cont_spec_value_in);
                select MAX(cont_spec_id) into cont_spec_id_var from ob_cont_spec;
RETURN cont_spec_id_var;
END//
DELIMITER ;


-- Dumping structure for function policydb.create_ob_obj_spec
DELIMITER //
CREATE DEFINER=`root`@`localhost` FUNCTION `create_ob_obj_spec`(event_pattern_id_in varchar(50), node_type_in varchar(50),
                obj_spec_value_in varchar(50)) RETURNS int(11)
BEGIN
                declare obj_spec_id_var int;
                insert into ob_obj_spec (event_pattern_id, obj_spec_type, obj_spec_value) values (event_pattern_id_in, get_node_type_id(node_type_in), obj_spec_value_in);
                select MAX(obj_spec_id) into obj_spec_id_var from ob_obj_spec;
RETURN obj_spec_id_var;
END//
DELIMITER ;


-- Dumping structure for function policydb.create_ob_op_spec
DELIMITER //
CREATE DEFINER=`root`@`localhost` FUNCTION `create_ob_op_spec`(event_pattern_id_in varchar(50), node_type_in varchar(50),
                op_spec_value_in varchar(50)) RETURNS int(11)
BEGIN
                declare op_spec_id_var int;
                insert into ob_op_spec (event_pattern_id, op_spec_event_id)
                                values (event_pattern_id_in, get_op_spec_type_id(op_spec_value_in));
                select MAX(op_spec_id) into op_spec_id_var from ob_op_spec;
RETURN op_spec_id_var;
END//
DELIMITER ;


-- Dumping structure for function policydb.create_ob_pc_spec
DELIMITER //
CREATE DEFINER=`root`@`localhost` FUNCTION `create_ob_pc_spec`(event_pattern_id_in varchar(50), node_type_in varchar(50),
                pc_spec_value_in varchar(50)) RETURNS int(11)
BEGIN
                declare pc_spec_id_var int;
                insert into ob_policy_spec (event_pattern_d, policy_spec_type, policy_spec_value) values (event_pattern_id_in, get_node_type_id(node_type_in), pc_spec_value_in);
                select MAX(policy_spec_id) into pc_spec_id_var from ob_policy_spec;
RETURN pc_spec_id_var;
END//
DELIMITER ;


-- Dumping structure for function policydb.create_ob_user_spec
DELIMITER //
CREATE DEFINER=`root`@`localhost` FUNCTION `create_ob_user_spec`(node_type_in varchar(50),
                user_spec_value_in varchar(50)) RETURNS int(11)
BEGIN
                declare user_spec_id_var int;
                insert into ob_user_spec (user_spec_type, user_spec_value) values (get_node_type_id(node_type_in), user_spec_value_in);
                select MAX(user_spec_id) into user_spec_id_var from ob_user_spec;
RETURN user_spec_id_var;
END//
DELIMITER ;


-- Dumping structure for function policydb.create_operand
DELIMITER //
CREATE DEFINER=`root`@`localhost` FUNCTION `create_operand`(operand_type varchar(200), op_num int(2),
                is_function tinyint(1), is_subgraph tinyint(1), is_compliment tinyint(2), expression varchar(300),
                expression_id varchar(50), action_id varchar(50), parent_function varchar(50)) RETURNS int(11)
BEGIN
DECLARE node_type_id_in int;
DECLARE inserted_operand_id int;
-- Insert in Node table
    insert into ob_operand (operand_type,operand_num,is_function,is_subgraph,is_compliment,
                                expression,expression_id,action_id,parent_function)
                values (get_operand_type_id(operand_type),op_num,is_function,is_subgraph,is_compliment,
    expression,expression_id,action_id,parent_function);

                SELECT MAX(operand_id) INTO inserted_operand_id FROM ob_operand;
  RETURN inserted_operand_id;
END//
DELIMITER ;


-- Dumping structure for procedure policydb.create_operation
DELIMITER //
CREATE DEFINER=`root`@`localhost` PROCEDURE `create_operation`(operation_name varchar(45), object_class_id_in int)
BEGIN
-- Insert in object_class table
                IF not exists ( SELECT OPERATION_ID FROM policydb.OPERATION WHERE UPPER(NAME) = UPPER(operation_name) ) THEN
                                INSERT INTO policydb.OPERATION (operation_type_id, name, description, object_class_id) values (1, operation_name, operation_name, object_class_id_in);
                END IF;
END//
DELIMITER ;


-- Dumping structure for procedure policydb.create_opset
DELIMITER //
CREATE DEFINER=`root`@`localhost` PROCEDURE `create_opset`(op_set_name varchar(45), operations varchar(1000))
BEGIN
DECLARE op_id int;
DECLARE new_node_id int;
DECLARE op_list varchar(1000);
                IF not exists ( SELECT NAME FROM policydb.NODE WHERE UPPER(NAME) = UPPER(op_set_name)) THEN
                                -- Insert in node table
                                INSERT INTO NODE (NODE_TYPE_ID, NAME, description) VALUES (7, op_set_name, op_set_name);
                                -- Insert in operation_set_details table
                                SELECT MAX(NODE_ID) INTO new_node_id FROM NODE;
                                SELECT formatCSL(operations) INTO op_list;
                                SET @separator = ',';
                                SET @separatorLength = CHAR_LENGTH(@separator);

                                WHILE operations != '' DO
                                                SET @currentValue = SUBSTRING_INDEX(operations, @separator, 1);
                                                SELECT get_operation_id(@currentValue) INTO op_id;
                                                INSERT INTO operation_set_details (operation_set_details_node_id, operation_id) VALUES (new_node_id, op_id);
                                                SET operations = SUBSTRING(operations, CHAR_LENGTH(@currentValue) + @separatorLength + 1);
                                END WHILE;

                END IF;

END//
DELIMITER ;


-- Dumping structure for procedure policydb.create_opset_detail
DELIMITER //
CREATE DEFINER=`root`@`localhost` PROCEDURE `create_opset_detail`(opset_id int, operation varchar(50))
BEGIN
DECLARE op_id int;
                IF exists ( SELECT NODE_ID FROM NODE WHERE NODE_ID = opset_id) THEN
                                IF EXISTS (SELECT OPERATION_ID FROM OPERATION WHERE UPPER(NAME) = UPPER(operation)) THEN
                                                SELECT OPERATION_ID INTO op_id FROM OPERATION WHERE UPPER(NAME) = UPPER(operation);
                                                INSERT INTO OPERATION_SET_DETAILS (OPERATION_SET_DETAILS_NODE_ID, OPERATION_ID) VALUES (opset_id, op_id);
                                END IF;
                END IF;
END//
DELIMITER ;


-- Dumping structure for function policydb.create_user_detail_fun
DELIMITER //
CREATE DEFINER=`root`@`localhost` FUNCTION `create_user_detail_fun`(user_id int, user_name varchar(20), full_name varchar(50), user_password varchar(1000), email_address varchar(100),
                user_host_id INT(11), user_property_in VARCHAR(200)) RETURNS int(11)
BEGIN
-- Insert in USER_DETAIL table
                IF exists ( SELECT NODE_ID FROM NODE WHERE NODE_ID = user_id) THEN
                                                INSERT INTO USER_DETAIL (USER_NODE_ID,USER_NAME, FULL_NAME, PASSWORD, EMAIL_ADDRESS, HOST_ID) VALUES (user_id,user_name,full_name,user_password,email_address,user_host_id);
                                END IF;
    RETURN USER_ID;
END//
DELIMITER ;


-- Dumping structure for function policydb.create_user_fun
DELIMITER //
CREATE DEFINER=`root`@`localhost` FUNCTION `create_user_fun`(user_id int, user_name varchar(20), full_name varchar(50), user_description varchar(200), base_id int(11), user_password varchar(100), email_address varchar(100),
                user_host_id INT(11), user_property_in VARCHAR(200)) RETURNS int(11)
BEGIN
DECLARE new_node_id int(11);
                IF not exists ( SELECT NODE_ID FROM NODE WHERE NODE_ID = user_id) THEN
      -- Insert into NODE table
      SELECT create_node_fun(user_id, user_name, 'u', user_description, base_id) INTO new_node_id FROM DUAL;
                                                -- Insert into USER_DETAIL table
      INSERT INTO USER_DETAIL (USER_NODE_ID,USER_NAME, FULL_NAME, PASSWORD, EMAIL_ADDRESS, HOST_ID) VALUES (new_node_id,user_name,full_name,user_password,email_address,user_host_id);
                                END IF;
    RETURN USER_ID;
END//
DELIMITER ;


-- Dumping structure for procedure policydb.delete_assignment
DELIMITER //
CREATE DEFINER=`root`@`localhost` PROCEDURE `delete_assignment`(start_node int, end_node int)
BEGIN
DECLARE path_id int;
declare cnt int;
                IF exists ( SELECT NODE_ID FROM policydb.NODE WHERE NODE_ID = start_node) THEN
    IF exists ( SELECT NODE_ID FROM policydb.NODE WHERE NODE_ID = END_NODE)  THEN
                                IF start_node <> end_node THEN
        -- get path_id
        SELECT ASSIGNMENT_PATH_ID INTO path_id FROM ASSIGNMENT
        WHERE START_NODE_ID = start_node
        AND END_NODE_ID = end_node;

        IF path_id is not null THEN
          DELETE FROM ASSIGNMENT WHERE ASSIGNMENT_PATH_ID = path_id;
        ELSE
          DELETE FROM ASSIGNMENT
          WHERE START_NODE_ID = start_node
          AND END_NODE_ID = end_node;
        END IF;
        -- if end_node is not assigned to any other node, assign it to the connector
        SELECT COUNT(*) INTO cnt FROM ASSIGNMENT WHERE end_node_id = end_node
        AND depth = 1;
        IF cnt = 0 THEN
          CALL create_assignment(1,end_node);
        END IF;
                  END IF;
  END IF;
END IF;
END//
DELIMITER ;


-- Dumping structure for procedure policydb.delete_deny
DELIMITER //
CREATE DEFINER=`root`@`localhost` PROCEDURE `delete_deny`(deny_id_in int(11))
BEGIN
    DELETE FROM DENY WHERE deny_id = deny_id_in;
END//
DELIMITER ;


-- Dumping structure for procedure policydb.delete_property
DELIMITER //
CREATE DEFINER=`root`@`localhost` PROCEDURE `delete_property`(property_in varchar(200))
BEGIN
      DELETE FROM NODE_PROPERTY WHERE UPPER(property_key) = UPPER(property_in);
END//
DELIMITER ;


-- Dumping structure for table policydb.deny
CREATE TABLE IF NOT EXISTS `deny` (
  `deny_id` int(11) NOT NULL AUTO_INCREMENT,
  `deny_name` varchar(50) NOT NULL,
  `deny_type_id` int(11) NOT NULL,
  `user_attribute_id` int(11) DEFAULT NULL,
  `process_id` int(9) DEFAULT NULL,
  `is_intersection` int(1) DEFAULT NULL,
  PRIMARY KEY (`deny_id`),
  KEY `user_attribute_id_idx` (`user_attribute_id`),
  KEY `deny_user_attribute_id_idx` (`user_attribute_id`),
  KEY `deny_type_id_idx` (`deny_type_id`),
  KEY `idx_deny_deny_name` (`deny_name`),
  CONSTRAINT `fk_deny_type_id` FOREIGN KEY (`deny_type_id`) REFERENCES `deny_type` (`deny_type_id`),
  CONSTRAINT `fk_deny_user_attribute_node_id` FOREIGN KEY (`user_attribute_id`) REFERENCES `node` (`node_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Deny';

-- Data exporting was unselected.


-- Dumping structure for table policydb.deny_obj_attribute
CREATE TABLE IF NOT EXISTS `deny_obj_attribute` (
  `deny_id` int(11) NOT NULL,
  `object_attribute_id` int(11) NOT NULL,
  `object_complement` int(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (`deny_id`,`object_attribute_id`),
  KEY `fk_deny_obj_attr` (`object_attribute_id`),
  CONSTRAINT `fk_deny_id` FOREIGN KEY (`deny_id`) REFERENCES `deny` (`deny_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_deny_obj_attr` FOREIGN KEY (`object_attribute_id`) REFERENCES `node` (`node_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table policydb.deny_operation
CREATE TABLE IF NOT EXISTS `deny_operation` (
  `deny_id` int(11) NOT NULL,
  `deny_operation_id` int(11) NOT NULL,
  PRIMARY KEY (`deny_id`,`deny_operation_id`),
  KEY `fk_deny_op_id` (`deny_operation_id`),
  CONSTRAINT `fk_deny_op_id` FOREIGN KEY (`deny_operation_id`) REFERENCES `operation` (`operation_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_op_deny_id` FOREIGN KEY (`deny_id`) REFERENCES `deny` (`deny_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table policydb.deny_type
CREATE TABLE IF NOT EXISTS `deny_type` (
  `deny_type_id` int(11) NOT NULL,
  `name` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`deny_type_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Deny types';

-- Data exporting was unselected.


-- Dumping structure for table policydb.email_attachment
CREATE TABLE IF NOT EXISTS `email_attachment` (
  `object_node_id` int(11) NOT NULL,
  `attachment_node_id` int(11) NOT NULL,
  PRIMARY KEY (`object_node_id`,`attachment_node_id`),
  KEY `fk_att_node_id_idx` (`attachment_node_id`),
  CONSTRAINT `fk_att_node_id` FOREIGN KEY (`attachment_node_id`) REFERENCES `node` (`node_id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='stores email attachments\n';

-- Data exporting was unselected.


-- Dumping structure for table policydb.email_detail
CREATE TABLE IF NOT EXISTS `email_detail` (
  `object_node_id` int(11) NOT NULL,
  `sender` varchar(254) NOT NULL,
  `recipient` varchar(254) NOT NULL,
  `timestamp` datetime NOT NULL,
  `email_subject` varchar(200) NOT NULL,
  `user_node_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`object_node_id`),
  KEY `fk_email_dtl_user_node_id_idx` (`user_node_id`),
  CONSTRAINT `fk_email_dtl_user_node_id` FOREIGN KEY (`user_node_id`) REFERENCES `node` (`node_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_email_object_node_id` FOREIGN KEY (`object_node_id`) REFERENCES `object_detail` (`object_node_id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='table to hold information for emails.  sender, recipient, etc';

-- Data exporting was unselected.


-- Dumping structure for function policydb.formatCSL
DELIMITER //
CREATE DEFINER=`root`@`localhost` FUNCTION `formatCSL`(
_text TEXT
) RETURNS text CHARSET utf8
    NO SQL
BEGIN

IF _text IS NULL THEN
    RETURN NULL;
END IF;

SET _text = TRIM(_text);

WHILE INSTR(_text, ' ,') DO
    SET _text = REPLACE(_text, ' ,', ',');
END WHILE;

WHILE INSTR(_text, ', ') DO
    SET _text = REPLACE(_text, ', ', ',');
END WHILE;

RETURN _text;

END//
DELIMITER ;


-- Dumping structure for function policydb.get_action_type_id
DELIMITER //
CREATE DEFINER=`root`@`localhost` FUNCTION `get_action_type_id`(action_type_in varchar(50)) RETURNS int(11)
BEGIN
DECLARE action_type_id_var int(11);
                SELECT action_type_id INTO action_type_id_var FROM ob_action_type
                WHERE UPPER(action_type_name) = UPPER(action_type_in);
RETURN action_type_id_var;
END//
DELIMITER ;


-- Dumping structure for function policydb.get_action_type_name
DELIMITER //
CREATE DEFINER=`root`@`localhost` FUNCTION `get_action_type_name`(action_type_id_in int(11)) RETURNS varchar(50) CHARSET utf8
BEGIN
DECLARE action_type_name_var varchar(50);
                SELECT action_type_name INTO action_type_name_var FROM ob_action_type
                WHERE UPPER(action_type_id) = UPPER(action_type_id_in);
RETURN action_type_name_var;
END//
DELIMITER ;


-- Dumping structure for function policydb.get_cond_type_id
DELIMITER //
CREATE DEFINER=`root`@`localhost` FUNCTION `get_cond_type_id`(cond_type_in varchar(50)) RETURNS int(11)
BEGIN
DECLARE cond_type_id_var int(11);
                SELECT cond_type_id INTO cond_type_id_var FROM ob_condition_type
                WHERE UPPER(cond_type) = UPPER(cond_type_in);
RETURN cond_type_id_var;
END//
DELIMITER ;


-- Dumping structure for function policydb.get_cond_type_name
DELIMITER //
CREATE DEFINER=`root`@`localhost` FUNCTION `get_cond_type_name`(cond_type_id_in int(11)) RETURNS varchar(50) CHARSET utf8
BEGIN
DECLARE cond_type_name_var varchar(50);
                SELECT cond_type INTO cond_type_name_var FROM ob_condition_type
                WHERE UPPER(cond_type_id) = UPPER(cond_type_id_in);
RETURN cond_type_name_var;
END//
DELIMITER ;


-- Dumping structure for function policydb.get_cont_spec_type_id
DELIMITER //
CREATE DEFINER=`root`@`localhost` FUNCTION `get_cont_spec_type_id`(cont_spec_type_name_in varchar(50)) RETURNS int(11)
BEGIN
DECLARE cont_spec_type_id_var int(11);
                SELECT cont_spec_type_id INTO cont_spec_type_id_var FROM ob_cont_spec_type
                WHERE UPPER(cont_spec_type) = UPPER(cont_spec_type_name_in);
RETURN cont_spec_type_id_var;
END//
DELIMITER ;


-- Dumping structure for procedure policydb.get_denied_ops
DELIMITER //
CREATE DEFINER=`root`@`localhost` PROCEDURE `get_denied_ops`(process_id_in int(9), user_id int, obj_id int)
BEGIN
    SELECT get_operation_name(DENY_OPERATION_ID) FROM DENY_OPERATION DO, DENY D
    WHERE DO.DENY_ID = D.deny_id
    AND (IS_ASCENDANT_OF(user_id, D.USER_ATTRIBUTE_ID) OR ifnull(process_id_in, D.process_id)=D.process_id)
    AND is_object_in_deny(obj_id, D.DENY_ID, D.IS_INTERSECTION);
END//
DELIMITER ;


-- Dumping structure for function policydb.get_deny_type_id
DELIMITER //
CREATE DEFINER=`root`@`localhost` FUNCTION `get_deny_type_id`(deny_type_name_in varchar(45)) RETURNS int(11)
BEGIN
DECLARE deny_type_id_out INT;
                SELECT deny_type.deny_type_id INTO deny_type_id_out FROM policydb.deny_type
                WHERE UPPER(name) = UPPER(deny_type_name_in);
RETURN deny_type_id_out;
END//
DELIMITER ;


-- Dumping structure for function policydb.get_host_id
DELIMITER //
CREATE DEFINER=`root`@`localhost` FUNCTION `get_host_id`(hostname varchar(50)) RETURNS int(11)
BEGIN
DECLARE hostid int;
                SELECT HOST_ID INTO hostid FROM HOST WHERE UPPER(HOST_NAME) = UPPER(HOSTNAME);
RETURN hostid;
END//
DELIMITER ;


-- Dumping structure for function policydb.get_host_name
DELIMITER //
CREATE DEFINER=`root`@`localhost` FUNCTION `get_host_name`(host_id_in int(11)) RETURNS varchar(63) CHARSET utf8
BEGIN
DECLARE host_name_out varchar(63);
                SELECT host_name INTO host_name_out FROM HOST WHERE host_id = host_id_in;
RETURN host_name_out;
END//
DELIMITER ;


-- Dumping structure for function policydb.GET_HOST_PATH
DELIMITER //
CREATE DEFINER=`root`@`localhost` FUNCTION `GET_HOST_PATH`(host_id_in int(11)) RETURNS varchar(300) CHARSET utf8
BEGIN
DECLARE workarea_path_out varchar(300);
                SELECT workarea_path INTO workarea_path_out FROM HOST WHERE host_id = host_id_in;
RETURN workarea_path_out;
END//
DELIMITER ;

-- Dumping structure for function policydb.GET_NODE_NAME
DELIMITER //
CREATE DEFINER=`root`@`localhost` FUNCTION `GET_NODE_NAME`(node_id_in int(11)) RETURNS varchar(100) CHARSET utf8
BEGIN
DECLARE node_name varchar(100);

SELECT name INTO node_name FROM NODE WHERE node_id = node_id_in;
RETURN node_name;
END//
DELIMITER ;

-- Dumping structure for function policydb.get_node_id
DELIMITER //
CREATE DEFINER=`root`@`localhost` FUNCTION `get_node_id`(node_name varchar(200), node_type varchar(50)) RETURNS int(11)
BEGIN
DECLARE node int;

SELECT DISTINCT NODE_ID
INTO node
FROM NODE
JOIN node_type
on node.node_type_id=node_type.node_type_id
WHERE UPPER(node.NAME) = UPPER(NODE_NAME)
AND UPPER(node_type.name) = UPPER(node_type);

RETURN node;
END//
DELIMITER ;


-- Dumping structure for function policydb.GET_NODE_TYPE
DELIMITER //
CREATE DEFINER=`root`@`localhost` FUNCTION `GET_NODE_TYPE`(node_id_in int(11)) RETURNS int(11)
BEGIN
DECLARE type_id INT;
                SELECT node_type_id INTO type_id FROM NODE
                WHERE node_id = node_id_in;
RETURN type_id;
END//
DELIMITER ;


-- Dumping structure for function policydb.get_node_type_id
DELIMITER //
CREATE DEFINER=`root`@`localhost` FUNCTION `get_node_type_id`(node_type_in varchar(50)) RETURNS int(11)
BEGIN
DECLARE node_type_id_var int(11);
                SELECT node_type_id INTO node_type_id_var FROM node_type
                WHERE UPPER(name) = UPPER(node_type_in);
RETURN node_type_id_var;
END//
DELIMITER ;


-- Dumping structure for function policydb.get_node_type_name
DELIMITER //
CREATE DEFINER=`root`@`localhost` FUNCTION `get_node_type_name`(node_id_in int(11)) RETURNS varchar(50) CHARSET utf8
BEGIN
DECLARE type_name varchar(50);
                SELECT node_type.name INTO type_name FROM node, node_type
                WHERE node.node_type_id = node_type.node_type_id
    AND node_id = node_id_in;
RETURN type_name;
END//
DELIMITER ;


-- Dumping structure for function policydb.get_object_class_name
DELIMITER //
CREATE DEFINER=`root`@`localhost` FUNCTION `get_object_class_name`(obj_class_id_in int(11)) RETURNS varchar(50) CHARSET utf8
BEGIN
DECLARE class_name varchar(50);
                SELECT object_class.name INTO class_name FROM object_class
                WHERE object_class.object_class_id = obj_class_id_in;
RETURN class_name;
END//
DELIMITER ;


-- Dumping structure for function policydb.get_operand_type_id
DELIMITER //
CREATE DEFINER=`root`@`localhost` FUNCTION `get_operand_type_id`(operand_type_in varchar(50)) RETURNS int(11)
BEGIN
DECLARE operand_type_id_var int(11);
                SELECT operand_type_id INTO operand_type_id_var FROM ob_operand_type
                WHERE UPPER(operand_type) = UPPER(operand_type_in);
RETURN operand_type_id_var;
END//
DELIMITER ;


-- Dumping structure for function policydb.get_operand_type_name
DELIMITER //
CREATE DEFINER=`root`@`localhost` FUNCTION `get_operand_type_name`(operand_type_id_in int(11)) RETURNS varchar(50) CHARSET utf8
BEGIN
DECLARE operand_type_name_var varchar(11);
                SELECT operand_type INTO operand_type_name_var FROM ob_operand_type
                WHERE UPPER(operand_type_id) = UPPER(operand_type_id_in);
RETURN operand_type_name_var;
END//
DELIMITER ;


-- Dumping structure for function policydb.get_operations
DELIMITER //
CREATE DEFINER=`root`@`localhost` FUNCTION `get_operations`(opset_id_in int) RETURNS varchar(500) CHARSET utf8
BEGIN
DECLARE ops_of_opset VARCHAR(500);
                SELECT group_concat(o.name SEPARATOR ',') into ops_of_opset
                from operation_set_details os, operation o
                where os.operation_id = o.operation_id
                GROUP BY os.operation_set_details_node_id
                having os.operation_set_details_node_id = opset_id_in;

                RETURN ops_of_opset;
END//
DELIMITER ;


-- Dumping structure for function policydb.get_operation_id
DELIMITER //
CREATE DEFINER=`root`@`localhost` FUNCTION `get_operation_id`(operation_name varchar(45)) RETURNS int(11)
BEGIN
DECLARE op_id INT;
                SELECT operation_id INTO op_id FROM policydb.operation
                WHERE UPPER(name) = UPPER(operation_name);
RETURN op_id;
END//
DELIMITER ;


-- Dumping structure for function policydb.get_operation_name
DELIMITER //
CREATE DEFINER=`root`@`localhost` FUNCTION `get_operation_name`(operation_id_in int(11)) RETURNS varchar(45) CHARSET utf8
BEGIN
DECLARE op_name varchar(45);
                SELECT name INTO op_name FROM policydb.operation
                WHERE UPPER(operation_id) = UPPER(operation_id_in);
RETURN op_name;
END//
DELIMITER ;


-- Dumping structure for function policydb.get_op_spec_type_id
DELIMITER //
CREATE DEFINER=`root`@`localhost` FUNCTION `get_op_spec_type_id`(op_spec_type_name_in varchar(50)) RETURNS int(11)
BEGIN
DECLARE op_spec_type_id_var int(11);
                SELECT event_id INTO op_spec_type_id_var FROM ob_op_spec_events
                WHERE UPPER(event_name) = UPPER(op_spec_type_name_in);
RETURN op_spec_type_id_var;
END//
DELIMITER ;


-- Dumping structure for function policydb.get_user_spec_type_id
DELIMITER //
CREATE DEFINER=`root`@`localhost` FUNCTION `get_user_spec_type_id`(user_spec_type_name_in varchar(50)) RETURNS int(11)
BEGIN
DECLARE user_spec_type_id_var int(11);
                SELECT user_spec_type_id INTO user_spec_type_id_var FROM ob_user_spec_type
                WHERE UPPER(user_spec_type) = UPPER(user_spec_type_name_in);
RETURN user_spec_type_id_var;
END//
DELIMITER ;


-- Dumping structure for table policydb.host
CREATE TABLE IF NOT EXISTS `host` (
  `host_id` int(11) NOT NULL AUTO_INCREMENT,
  `host_name` varchar(63) NOT NULL,
  `workarea_path` varchar(300) NOT NULL,
  PRIMARY KEY (`host_id`),
  KEY `idx_host_host_name` (`host_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='host machine info';

-- Data exporting was unselected.


-- Dumping structure for function policydb.isValidCSL
DELIMITER //
CREATE DEFINER=`root`@`localhost` FUNCTION `isValidCSL`(
_textIn TEXT
) RETURNS tinyint(1)
    NO SQL
BEGIN

RETURN _textIn IS NOT NULL && (_textIn = '' || _textIn REGEXP '^([1-9][0-9]{2},)*[1-9][0-9]{2}?$');

END//
DELIMITER ;


-- Dumping structure for function policydb.is_accessible
DELIMITER //
CREATE DEFINER=`root`@`localhost` FUNCTION `is_accessible`(ua_id_in int(11), oa_id_in int(11)) RETURNS tinyint(1)
BEGIN
DECLARE is_accessible boolean;
DECLARE policy_id_in int;
DECLARE done boolean DEFAULT FALSE;
DECLARE policies CURSOR FOR select a.start_node_id as policy_id from assignment a where get_node_type(a.start_node_id) = 2 and end_node_id = oa_id_in;
DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
  OPEN policies;

  check_loop: LOOP
    FETCH policies INTO policy_id_in;
    IF done THEN
      LEAVE check_loop;
    END IF;
    SELECT CASE WHEN COUNT(*) > 0 THEN TRUE ELSE FALSE END into is_accessible
    from association as a
    where is_member(ua_id_in, a.ua_id)
    and is_member(oa_id_in, a.oa_id)
    and is_member(ua_id_in, policy_id_in);
    IF not is_accessible then
                                leave check_loop;
                END IF;
  END LOOP;

  CLOSE policies;
RETURN is_accessible;
END//
DELIMITER ;


-- Dumping structure for function policydb.is_ascendant_of
DELIMITER //
CREATE DEFINER=`root`@`localhost` FUNCTION `is_ascendant_of`(ascendant_node_id int,descendant_node_id int) RETURNS tinyint(4)
BEGIN
DECLARE cnt INT;

SELECT COUNT(*) INTO cnt FROM ASSIGNMENT A
WHERE A.START_NODE_ID = descendant_node_id
AND A.END_NODE_ID = ascendant_node_id;

IF cnt > 0 THEN
  RETURN TRUE;
ELSE RETURN FALSE;
END IF;
END//
DELIMITER ;


-- Dumping structure for function policydb.is_member
DELIMITER //
CREATE DEFINER=`root`@`localhost` FUNCTION `is_member`(member_id_in int(11), container_id_in int(11)) RETURNS tinyint(1)
BEGIN
DECLARE is_member boolean;
                if member_id_in = container_id_in then
    return true;
    end if;
                SELECT CASE WHEN COUNT(*) > 0 THEN TRUE ELSE FALSE END into is_member
    from assignment
    where start_node_id = container_id_in
    and end_node_id = member_id_in;
    return is_member;
END//
DELIMITER ;


-- Dumping structure for function policydb.is_object_in_deny
DELIMITER //
CREATE DEFINER=`root`@`localhost` FUNCTION `is_object_in_deny`(obj_id int,deny_id_in int, is_intersection int) RETURNS tinyint(1)
BEGIN
DECLARE row_cnt INT;
DECLARE deny_obj_cnt INT;
SELECT COUNT(*) INTO deny_obj_cnt
FROM DENY_OBJ_ATTRIBUTE
WHERE DENY_ID = deny_id_in;
IF is_intersection THEN
    SELECT COUNT(*) INTO row_cnt FROM DENY_OBJ_ATTRIBUTE D
    WHERE D.deny_id = deny_id_in
    -- AND is_ascendant_of(obj_id,D.object_attribute_id) AND NOT object_complement;
    AND ((is_ascendant_of(obj_id,D.object_attribute_id) AND NOT object_complement)
                OR (!is_ascendant_of(obj_id,D.object_attribute_id) AND object_complement));
    IF row_cnt = 0 OR row_cnt < deny_obj_cnt THEN
      RETURN FALSE;
    ELSE
      RETURN TRUE;
    END IF;
ELSE  -- UNION
    SELECT COUNT(*) INTO row_cnt FROM DENY_OBJ_ATTRIBUTE D
    WHERE D.deny_id = deny_id_in
    AND ((is_ascendant_of(obj_id,D.object_attribute_id) AND NOT object_complement)
    OR (!is_ascendant_of(obj_id,D.object_attribute_id) AND object_complement));
    IF row_cnt > 0 THEN
      RETURN TRUE;
    ELSE
      RETURN FALSE;
    END IF;
END IF;
END//
DELIMITER ;


-- Dumping structure for table policydb.keystore
CREATE TABLE IF NOT EXISTS `keystore` (
  `host_id` int(11) NOT NULL AUTO_INCREMENT,
  `user_node_id` int(11) NOT NULL,
  `keystore_path` varchar(300) DEFAULT NULL,
  `truststore_path` varchar(300) DEFAULT NULL,
  PRIMARY KEY (`host_id`,`user_node_id`),
  KEY `user_id_idx` (`user_node_id`),
  CONSTRAINT `fk_host_id` FOREIGN KEY (`host_id`) REFERENCES `host` (`host_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_user_node_id` FOREIGN KEY (`user_node_id`) REFERENCES `node` (`node_id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='host machine info';

-- Data exporting was unselected.


-- Dumping structure for table policydb.node
CREATE TABLE IF NOT EXISTS `node` (
  `node_id` int(11) NOT NULL AUTO_INCREMENT,
  `node_type_id` int(11) NOT NULL,
  `name` varchar(200) DEFAULT NULL,
  `description` varchar(200) DEFAULT NULL,
  PRIMARY KEY (`node_id`),
  KEY `node_type_id_idx` (`node_type_id`),
  CONSTRAINT `fk_node_type_id` FOREIGN KEY (`node_type_id`) REFERENCES `node_type` (`node_type_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='This table contains all the nodes in the graph';

-- Data exporting was unselected.


-- Dumping structure for table policydb.node_property
CREATE TABLE IF NOT EXISTS `node_property` (
  `property_node_id` int(11) NOT NULL DEFAULT '0',
  `property_key` varchar(50) NOT NULL,
  `property_value` varchar(300) NOT NULL,
  PRIMARY KEY (`property_node_id`,`property_key`),
  CONSTRAINT `fk_property_node_id` FOREIGN KEY (`property_node_id`) REFERENCES `node` (`node_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table policydb.node_type
CREATE TABLE IF NOT EXISTS `node_type` (
  `node_type_id` int(11) NOT NULL,
  `name` varchar(50) DEFAULT NULL,
  `description` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`node_type_id`),
  KEY `idx_node_type_description` (`description`),
  KEY `idx_node_type_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='This table contains node types';

-- Data exporting was unselected.


-- Dumping structure for table policydb.object_class
CREATE TABLE IF NOT EXISTS `object_class` (
  `object_class_id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(20) NOT NULL,
  `description` varchar(100) DEFAULT NULL,
  PRIMARY KEY (`object_class_id`),
  KEY `idx_object_class_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Object Class';

-- Data exporting was unselected.


-- Dumping structure for table policydb.object_detail
CREATE TABLE IF NOT EXISTS `object_detail` (
  `object_node_id` int(11) NOT NULL AUTO_INCREMENT,
  `original_node_id` int(11) DEFAULT NULL,
  `object_class_id` int(11) DEFAULT NULL,
  `host_id` int(11) DEFAULT NULL,
  `path` varchar(300) DEFAULT NULL,
  `include_ascedants` int(1) DEFAULT '0',
  `template_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`object_node_id`),
  KEY `object_type_id_idx` (`object_class_id`),
  KEY `fk_object_host_id_idx` (`host_id`),
  KEY `fk_original_node_id_idx` (`original_node_id`),
  KEY `fk_obj_detail_tpl_id_idx` (`template_id`),
  CONSTRAINT `fk_obj_det_object_class_id` FOREIGN KEY (`object_class_id`) REFERENCES `object_class` (`object_class_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_obj_detail_tpl_id` FOREIGN KEY (`template_id`) REFERENCES `template` (`template_id`) ON DELETE SET NULL ON UPDATE NO ACTION,
  CONSTRAINT `fk_object_host_id` FOREIGN KEY (`host_id`) REFERENCES `host` (`host_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_original_node_id` FOREIGN KEY (`original_node_id`) REFERENCES `node` (`node_id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Object Details';

-- Data exporting was unselected.


-- Dumping structure for table policydb.ob_action
CREATE TABLE IF NOT EXISTS `ob_action` (
  `action_id` varchar(50) NOT NULL,
  `action_type` int(11) DEFAULT NULL,
  `is_intrasession` tinyint(1) DEFAULT NULL,
  `is_intersection` tinyint(1) DEFAULT NULL,
  `sequence` int(3) DEFAULT NULL,
  `rule_id` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`action_id`),
  KEY `fk_action_type` (`action_type`),
  KEY `fk_ob_action_rule_id_idx` (`rule_id`),
  CONSTRAINT `fk_action_type` FOREIGN KEY (`action_type`) REFERENCES `ob_action_type` (`action_type_id`),
  CONSTRAINT `fk_ob_action_rule_id` FOREIGN KEY (`rule_id`) REFERENCES `ob_rule` (`rule_id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table policydb.ob_action_type
CREATE TABLE IF NOT EXISTS `ob_action_type` (
  `action_type_id` int(11) NOT NULL AUTO_INCREMENT,
  `action_type_name` varchar(50) NOT NULL,
  PRIMARY KEY (`action_type_id`,`action_type_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table policydb.ob_condition
CREATE TABLE IF NOT EXISTS `ob_condition` (
  `condition_id` varchar(50) NOT NULL,
  `action_id` varchar(50) NOT NULL,
  `type` varchar(50) DEFAULT NULL,
  `is_negated` tinyint(1) DEFAULT NULL,
  PRIMARY KEY (`condition_id`),
  KEY `fk_cond_action_id_idx` (`action_id`),
  CONSTRAINT `fk_cond_action_id` FOREIGN KEY (`action_id`) REFERENCES `ob_action` (`action_id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table policydb.ob_condition_type
CREATE TABLE IF NOT EXISTS `ob_condition_type` (
  `cond_type_id` int(11) NOT NULL AUTO_INCREMENT,
  `cond_type` varchar(50) NOT NULL,
  PRIMARY KEY (`cond_type_id`,`cond_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table policydb.ob_cont_spec
CREATE TABLE IF NOT EXISTS `ob_cont_spec` (
  `event_pattern_id` varchar(50) NOT NULL,
  `cont_spec_type` int(11) NOT NULL,
  `cont_spec_value` varchar(50) NOT NULL DEFAULT '',
  PRIMARY KEY (`event_pattern_id`,`cont_spec_type`,`cont_spec_value`),
  KEY `fk_cont_spec_type_idx` (`cont_spec_type`),
  CONSTRAINT `fk_cont_spec_evtptn_id` FOREIGN KEY (`event_pattern_id`) REFERENCES `ob_event_pattern` (`event_pattern_id`) ON DELETE CASCADE ON UPDATE NO ACTION,
  CONSTRAINT `fk_cont_spec_type_id` FOREIGN KEY (`cont_spec_type`) REFERENCES `node_type` (`node_type_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table policydb.ob_cont_spec_type
CREATE TABLE IF NOT EXISTS `ob_cont_spec_type` (
  `cont_spec_type_id` int(11) NOT NULL AUTO_INCREMENT,
  `cont_spec_type` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`cont_spec_type_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table policydb.ob_event_pattern
CREATE TABLE IF NOT EXISTS `ob_event_pattern` (
  `event_pattern_id` varchar(50) NOT NULL,
  `rule_id` varchar(50) DEFAULT NULL,
  `is_active` tinyint(1) DEFAULT NULL,
  `is_any` tinyint(1) DEFAULT NULL,
  PRIMARY KEY (`event_pattern_id`),
  KEY `fk_evtptrn_rule_id` (`rule_id`),
  CONSTRAINT `fk_evtptrn_rule_id` FOREIGN KEY (`rule_id`) REFERENCES `ob_rule` (`rule_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table policydb.ob_obj_spec
CREATE TABLE IF NOT EXISTS `ob_obj_spec` (
  `event_pattern_id` varchar(50) NOT NULL,
  `obj_spec_type` int(11) NOT NULL,
  `obj_spec_value` varchar(50) NOT NULL DEFAULT '',
  PRIMARY KEY (`event_pattern_id`,`obj_spec_type`,`obj_spec_value`),
  KEY `fk_obj_spec_type` (`obj_spec_type`),
  CONSTRAINT `fk_obj_spec_event_pattern` FOREIGN KEY (`event_pattern_id`) REFERENCES `ob_event_pattern` (`event_pattern_id`) ON DELETE CASCADE ON UPDATE NO ACTION,
  CONSTRAINT `fk_obj_spec_type` FOREIGN KEY (`obj_spec_type`) REFERENCES `node_type` (`node_type_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table policydb.ob_operand
CREATE TABLE IF NOT EXISTS `ob_operand` (
  `operand_id` varchar(50) NOT NULL,
  `action_id` varchar(50) DEFAULT NULL,
  `condition_id` varchar(50) DEFAULT NULL,
  `operand_type` int(11) DEFAULT NULL,
  `operand_num` int(2) DEFAULT NULL,
  `sequence` int(4) DEFAULT NULL,
  `is_function` tinyint(1) DEFAULT NULL,
  `is_subgraph` tinyint(1) DEFAULT NULL,
  `is_compliment` tinyint(1) DEFAULT NULL,
  `expression` varchar(500) DEFAULT NULL,
  `expression_id` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`operand_id`),
  KEY `fk_operand_type` (`operand_type`),
  KEY `fk_ob_operand_action_id_idx` (`action_id`),
  KEY `fk_condition_id_idx` (`condition_id`),
  CONSTRAINT `fk_condition_id` FOREIGN KEY (`condition_id`) REFERENCES `ob_condition` (`condition_id`) ON DELETE CASCADE ON UPDATE NO ACTION,
  CONSTRAINT `fk_ob_operand_action_id` FOREIGN KEY (`action_id`) REFERENCES `ob_action` (`action_id`) ON DELETE CASCADE ON UPDATE NO ACTION,
  CONSTRAINT `fk_operand_type` FOREIGN KEY (`operand_type`) REFERENCES `ob_operand_type` (`operand_type_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table policydb.ob_operand_args
CREATE TABLE IF NOT EXISTS `ob_operand_args` (
  `operand_id` varchar(50) NOT NULL,
  `arg_operand_id` varchar(50) NOT NULL,
  `sequence` int(3) NOT NULL,
  PRIMARY KEY (`operand_id`,`arg_operand_id`,`sequence`),
  KEY `fk_operand_id` (`arg_operand_id`),
  CONSTRAINT `fk_arg_operand_id` FOREIGN KEY (`operand_id`) REFERENCES `ob_operand` (`operand_id`) ON DELETE CASCADE ON UPDATE NO ACTION,
  CONSTRAINT `fk_operand_id` FOREIGN KEY (`arg_operand_id`) REFERENCES `ob_operand` (`operand_id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table policydb.ob_operand_type
CREATE TABLE IF NOT EXISTS `ob_operand_type` (
  `operand_type_id` int(11) NOT NULL AUTO_INCREMENT,
  `operand_type` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`operand_type_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table policydb.ob_op_spec
CREATE TABLE IF NOT EXISTS `ob_op_spec` (
  `event_pattern_id` varchar(50) NOT NULL,
  `op_spec_event_id` int(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`event_pattern_id`,`op_spec_event_id`),
  KEY `fk_op_spec_event_id` (`op_spec_event_id`),
  CONSTRAINT `fk_ob_op_spec_event_id` FOREIGN KEY (`op_spec_event_id`) REFERENCES `ob_op_spec_events` (`event_id`) ON DELETE CASCADE ON UPDATE NO ACTION,
  CONSTRAINT `fk_op_spec_evtptn_id` FOREIGN KEY (`event_pattern_id`) REFERENCES `ob_event_pattern` (`event_pattern_id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table policydb.ob_op_spec_events
CREATE TABLE IF NOT EXISTS `ob_op_spec_events` (
  `event_id` int(11) NOT NULL AUTO_INCREMENT,
  `event_name` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`event_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table policydb.ob_policy_spec
CREATE TABLE IF NOT EXISTS `ob_policy_spec` (
  `event_pattern_id` varchar(50) NOT NULL,
  `policy_spec_type` int(11) NOT NULL,
  `policy_spec_value` varchar(50) NOT NULL DEFAULT '',
  PRIMARY KEY (`event_pattern_id`,`policy_spec_type`,`policy_spec_value`),
  KEY `fk_policy_spec_type` (`policy_spec_type`),
  CONSTRAINT `fk_policy_spec_evtptn_id` FOREIGN KEY (`event_pattern_id`) REFERENCES `ob_event_pattern` (`event_pattern_id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table policydb.ob_rule
CREATE TABLE IF NOT EXISTS `ob_rule` (
  `rule_id` varchar(50) NOT NULL,
  `rule_name` varchar(50) DEFAULT NULL,
  `count` int(3) DEFAULT NULL,
  `sequence` int(3) DEFAULT NULL,
  `script_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`rule_id`),
  KEY `fk_script_id` (`script_id`),
  CONSTRAINT `fk_ob_rule_script_id` FOREIGN KEY (`script_id`) REFERENCES `ob_script` (`script_id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table policydb.ob_script
CREATE TABLE IF NOT EXISTS `ob_script` (
  `script_id` int(11) NOT NULL AUTO_INCREMENT,
  `script_name` varchar(50) NOT NULL,
  `count` int(3) DEFAULT NULL,
  `enabled` tinyint(1) DEFAULT NULL,
  PRIMARY KEY (`script_id`,`script_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table policydb.ob_script_source
CREATE TABLE IF NOT EXISTS `ob_script_source` (
  `script_id` int(11) NOT NULL,
  `source` varchar(300) DEFAULT NULL,
  `order` int(4) NOT NULL,
  KEY `fk_script_source_id_idx` (`script_id`),
  CONSTRAINT `fk_script_source_id` FOREIGN KEY (`script_id`) REFERENCES `ob_script` (`script_id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table policydb.ob_user_spec
CREATE TABLE IF NOT EXISTS `ob_user_spec` (
  `event_pattern_id` varchar(50) NOT NULL,
  `user_spec_type` int(11) NOT NULL,
  `user_spec_value` varchar(50) NOT NULL DEFAULT '',
  PRIMARY KEY (`event_pattern_id`,`user_spec_type`,`user_spec_value`),
  KEY `fk_user_spec_type` (`user_spec_type`),
  CONSTRAINT `fk_user_spec_evtptn_id` FOREIGN KEY (`event_pattern_id`) REFERENCES `ob_event_pattern` (`event_pattern_id`) ON DELETE CASCADE ON UPDATE NO ACTION,
  CONSTRAINT `fk_user_spec_type` FOREIGN KEY (`user_spec_type`) REFERENCES `ob_user_spec_type` (`user_spec_type_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table policydb.ob_user_spec_type
CREATE TABLE IF NOT EXISTS `ob_user_spec_type` (
  `user_spec_type_id` int(11) NOT NULL AUTO_INCREMENT,
  `user_spec_type` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`user_spec_type_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table policydb.open_object
CREATE TABLE IF NOT EXISTS `open_object` (
  `session_id` int(11) NOT NULL,
  `object_node_id` int(11) NOT NULL,
  `count` int(2) DEFAULT NULL,
  PRIMARY KEY (`session_id`,`object_node_id`),
  KEY `fk_object_id_oo_idx` (`object_node_id`),
  CONSTRAINT `fk_object_id_oo` FOREIGN KEY (`object_node_id`) REFERENCES `node` (`node_id`) ON DELETE CASCADE ON UPDATE NO ACTION,
  CONSTRAINT `fk_session_id_oo` FOREIGN KEY (`session_id`) REFERENCES `session` (`session_id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='table for open objects';

-- Data exporting was unselected.


-- Dumping structure for table policydb.operation
CREATE TABLE IF NOT EXISTS `operation` (
  `operation_id` int(11) NOT NULL AUTO_INCREMENT,
  `operation_type_id` int(11) NOT NULL,
  `name` varchar(50) NOT NULL,
  `description` varchar(100) DEFAULT NULL,
  `object_class_id` int(11) DEFAULT NULL,
  PRIMARY KEY (`operation_id`),
  UNIQUE KEY `operation_id_UNIQUE` (`operation_id`),
  KEY `operation_type_id_idx` (`operation_type_id`),
  KEY `fk_operation_object_class_id_idx` (`object_class_id`),
  KEY `idx_operation_name` (`name`),
  CONSTRAINT `fk_operation_object_class_id` FOREIGN KEY (`object_class_id`) REFERENCES `object_class` (`object_class_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_operation_type_id` FOREIGN KEY (`operation_type_id`) REFERENCES `operation_type` (`operation_type_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Operation';

-- Data exporting was unselected.


-- Dumping structure for table policydb.operation_set_details
CREATE TABLE IF NOT EXISTS `operation_set_details` (
  `operation_set_details_node_id` int(11) NOT NULL,
  `operation_id` int(11) NOT NULL,
  PRIMARY KEY (`operation_set_details_node_id`,`operation_id`),
  KEY `fk_op_set_operation_id_idx` (`operation_id`),
  CONSTRAINT `fk_op_set_operation_id` FOREIGN KEY (`operation_id`) REFERENCES `operation` (`operation_id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `fk_operation_set_details_node_id` FOREIGN KEY (`operation_set_details_node_id`) REFERENCES `node` (`node_id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='This table contains the information for User operation node';

-- Data exporting was unselected.


-- Dumping structure for table policydb.operation_type
CREATE TABLE IF NOT EXISTS `operation_type` (
  `operation_type_id` int(11) NOT NULL,
  `name` varchar(50) NOT NULL,
  PRIMARY KEY (`operation_type_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='Operation types';

-- Data exporting was unselected.


-- Dumping structure for table policydb.record_components
CREATE TABLE IF NOT EXISTS `record_components` (
  `record_node_id` int(11) NOT NULL,
  `record_component_id` int(11) NOT NULL,
  `order` int(11) DEFAULT NULL,
  PRIMARY KEY (`record_node_id`,`record_component_id`),
  KEY `fk_record_component_id_idx` (`record_component_id`),
  CONSTRAINT `fk_record_component_id` FOREIGN KEY (`record_component_id`) REFERENCES `node` (`node_id`) ON DELETE CASCADE ON UPDATE NO ACTION,
  CONSTRAINT `fk_record_node_id` FOREIGN KEY (`record_node_id`) REFERENCES `node` (`node_id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='table to store the components of a record';

-- Data exporting was unselected.


-- Dumping structure for table policydb.record_key
CREATE TABLE IF NOT EXISTS `record_key` (
  `record_node_id` int(11) NOT NULL,
  `record_key` varchar(20) NOT NULL,
  `record_value` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`record_node_id`,`record_key`),
  CONSTRAINT `object_key_node_id` FOREIGN KEY (`record_node_id`) REFERENCES `object_detail` (`object_node_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for procedure policydb.reset_data
DELIMITER //
CREATE DEFINER=`root`@`localhost` PROCEDURE `reset_data`(session_id_in INT(11))
BEGIN

SET SQL_SAFE_UPDATES = 0;
delete from application;
delete from assignment where assignment_id > 100;
delete from assignment_path where assignment_path_id > 100;
delete from audit_information;
delete from deny where deny_id > 100;
delete from deny_obj_attribute where deny_id > 100;
delete from deny_operation where deny_id > 100;
delete from deny_type where deny_type_id > 100;
delete from email_attachment;
delete from email_detail;
delete from keystore;
delete from node where node_id > 100;
delete from node_property;
delete from node_type where node_type_id > 100;
delete from object_detail where object_node_id > 100;
delete from ob_script;
delete from open_object;
delete from operation where operation_id > 100;
delete from operation_set_details where operation_set_details_node_id > 100;
delete from operation_type where operation_type_id > 100;
delete from record_components;
delete from record_key;
delete from session where session_id <> session_id_in;
delete from template;
delete from template_component;
delete from template_key;
delete from user_detail where user_node_id > 100;
delete from host where host_id > 100;

alter table application auto_increment = 101;
alter table deny auto_increment = 101;
alter table host auto_increment = 101;
alter table keystore auto_increment = 101;
alter table node auto_increment = 101;
alter table object_class auto_increment = 101;
alter table object_detail auto_increment = 101;
alter table operation auto_increment = 101;
alter table session auto_increment = 101;
alter table template auto_increment = 101;
alter table assignment auto_increment = 101;
alter table assignment_path auto_increment = 101;
alter table assignment auto_increment = 101;

SET SQL_SAFE_UPDATES = 1;
END//
DELIMITER ;


-- Dumping structure for table policydb.session
CREATE TABLE IF NOT EXISTS `session` (
  `session_id` int(11) NOT NULL AUTO_INCREMENT,
  `session_name` varchar(150) DEFAULT NULL,
  `user_node_id` int(11) NOT NULL,
  `start_time` datetime NOT NULL,
  `host_id` int(11) NOT NULL,
  PRIMARY KEY (`session_id`),
  KEY `fk_session_user_node_id_idx` (`user_node_id`),
  KEY `idx_session_host_id` (`host_id`),
  CONSTRAINT `fk_session_user_node_id` FOREIGN KEY (`user_node_id`) REFERENCES `node` (`node_id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='This table stores sessions created for users. This will be temperory data and rows will be deleted from this table depending on retention policy. ';

-- Data exporting was unselected.


-- Dumping structure for procedure policydb.set_property
DELIMITER //
CREATE DEFINER=`root`@`localhost` PROCEDURE `set_property`(property_in varchar(200), property_value_in varchar(200), node_id int)
BEGIN
DECLARE count int;
-- Insert OR Update in NODE_PROPERTY table
                SELECT count(*) INTO count FROM NODE_PROPERTY WHERE UPPER(property) = UPPER(property_in) and PROPERTY_NODE_ID = node_id;
    IF count > 0 THEN
      UPDATE NODE_PROPERTY P SET P.PROPERTY_VALUE = property_value_in WHERE P.PROPERTY_NODE_ID = node_id;
    ELSE
      INSERT INTO NODE_PROPERTY (PROPERTY, PROPERTY_VALUE, PROPERTY_NODE_ID) VALUES (property_in, property_value_in, node_id);
    END IF;
END//
DELIMITER ;


-- Dumping structure for table policydb.template
CREATE TABLE IF NOT EXISTS `template` (
  `template_id` int(11) NOT NULL AUTO_INCREMENT,
  `template_name` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`template_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table policydb.template_component
CREATE TABLE IF NOT EXISTS `template_component` (
  `template_id` int(11) NOT NULL,
  `template_component_id` int(11) NOT NULL,
  `order` int(11) DEFAULT NULL,
  PRIMARY KEY (`template_id`,`template_component_id`),
  KEY `fk_cont_id_idx` (`template_component_id`),
  CONSTRAINT `fk_templ_cmpnt_id` FOREIGN KEY (`template_component_id`) REFERENCES `node` (`node_id`) ON DELETE CASCADE ON UPDATE NO ACTION,
  CONSTRAINT `fk_template_id` FOREIGN KEY (`template_id`) REFERENCES `template` (`template_id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table policydb.template_key
CREATE TABLE IF NOT EXISTS `template_key` (
  `template_id` int(11) NOT NULL,
  `template_key` varchar(50) NOT NULL,
  PRIMARY KEY (`template_id`,`template_key`),
  KEY `fk_tpl_key_node_id_idx` (`template_key`),
  CONSTRAINT `fk_tpl_id` FOREIGN KEY (`template_id`) REFERENCES `template` (`template_id`) ON DELETE CASCADE ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Data exporting was unselected.


-- Dumping structure for table policydb.user_detail
CREATE TABLE IF NOT EXISTS `user_detail` (
  `user_node_id` int(11) NOT NULL,
  `user_name` varchar(20) NOT NULL,
  `full_name` varchar(50) DEFAULT NULL,
  `password` varchar(1000) DEFAULT NULL,
  `email_address` varchar(254) DEFAULT NULL,
  `host_id` int(11) DEFAULT NULL,
  `pop_server` varchar(100) DEFAULT NULL,
  `smtp_server` varchar(100) DEFAULT NULL,
  `account_name` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`user_node_id`),
  UNIQUE KEY `user_name_UNIQUE` (`user_name`),
  KEY `fk_user_host_id_idx` (`host_id`),
  CONSTRAINT `fk_user_detail_node_id` FOREIGN KEY (`user_node_id`) REFERENCES `node` (`node_id`) ON DELETE CASCADE ON UPDATE NO ACTION,
  CONSTRAINT `fk_user_host_id` FOREIGN KEY (`host_id`) REFERENCES `host` (`host_id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='user - U';

-- Data exporting was unselected.

-- Dumping structure for view policydb.assignment_view
-- Removing temporary table and create final VIEW structure
CREATE 
    ALGORITHM = UNDEFINED 
    DEFINER = `root`@`localhost` 
    SQL SECURITY DEFINER
VIEW `policydb`.`assignment_view` AS
    SELECT 
        `policydb`.`assignment`.`start_node_id` AS `start_node_id`,
        GET_NODE_NAME(`policydb`.`assignment`.`start_node_id`) AS `start_node_name`,
        `policydb`.`assignment`.`end_node_id` AS `end_node_id`,
        GET_NODE_NAME(`policydb`.`assignment`.`end_node_id`) AS `end_node_name`,
        `policydb`.`assignment`.`depth` AS `depth`,
        `policydb`.`assignment`.`assignment_path_id` AS `assignment_path_id`
    FROM
        `policydb`.`assignment`
    WHERE
        ((GET_NODE_TYPE(`policydb`.`assignment`.`start_node_id`) <> 7)
            AND (GET_NODE_TYPE(`policydb`.`assignment`.`end_node_id`) <> 7)
            AND (`policydb`.`assignment`.`depth` > 0))
    ORDER BY `policydb`.`assignment`.`assignment_path_id` , `policydb`.`assignment`.`depth`;


-- Dumping structure for view policydb.association
-- Removing temporary table and create final VIEW structure
CREATE 
    ALGORITHM = UNDEFINED 
    DEFINER = `root`@`localhost` 
    SQL SECURITY DEFINER
VIEW `association` AS select
                                (select b.end_node_id
                                                from assignment b
                                                where ((b.start_node_id = a.end_node_id)
                                                                and isnull(b.assignment_path_id)
                                                                and (b.depth = 1)
                                                                and (GET_NODE_TYPE(b.start_node_id) = 7))) AS ua_id,
            a.end_node_id AS opset_id,
            a.start_node_id AS oa_id
                from assignment a
                where (isnull(a.assignment_path_id)
                                and (a.depth = 1)
                                and (GET_NODE_TYPE(a.end_node_id) = 7)) ;


