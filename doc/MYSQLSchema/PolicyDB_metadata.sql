INSERT INTO node_type (node_type_id, name, description) VALUES
                (1, 'c', 'Connector'),
                (2, 'p', 'Policy Class'),
                (3, 'a', 'User Attribute'),
                (4, 'u', 'User'),
                (5, 'b', 'Object Attribute'),
                (6, 'o', 'Object'),
                (7, 's', 'Operation Set');
INSERT INTO node (node_id, node_type_id, name, description) VALUES
                (1, 1, 'PM', ''),
                (2, 2, 'admin', ''),
                (3, 3, 'superAdmin', ''),
                (4, 4, 'super', ''),
                (5, 5, 'everything', 'Object mapped to all entities.'),
                (7, 7, 'all ops', 'Op set containing all operations');
INSERT INTO object_class (object_class_id, name, description) VALUES
                (1, 'class', 'Class of all object classes'),
                (2, 'File', 'Class of files'),
                (3, 'Directory', 'Class of directories'),
                (4, 'User', 'Class of PM users'),
                (5, 'User attribute', 'Class of PM user attributes'),
                (6, 'Object', 'Class of PM objects'),
                (7, 'Object attribute', 'Class of PM object attributes'),
                (8, 'Connector', 'Class of the PM connector node'),
                (9, 'Policy class', 'Class of PM policy classes'),
                (10, 'Operation set', 'Class of PM operation sets'),
                (11, '*', 'Class any class');
INSERT INTO operation_type (operation_type_id, name) VALUES
                (1, 'Resource Operations'),
                (2, 'Admin Operations');
INSERT INTO operation (operation_id, operation_type_id, name, description, object_class_id) VALUES
                (1, 1, 'Class create class', 'Class create class', 1),
                (2, 1, 'Class delete class', 'Class delete class', 2),
                (3, 1, '*', '*', 11),
                (4, 1, 'File modify', 'File modify', 2),
                (5, 1, 'File read and execute', 'File read and execute', 2),
                (6, 1, 'File read', 'File read', 2),
                (7, 1, 'File write', 'File write', 2),
                (8, 1, 'Dir modify', 'Dir modify', 3),
                (9, 1, 'Dir read and execute', 'Dir read and execute', 3),
                (10, 1, 'Dir list contents', 'Dir list contents', 3),
                (11, 1, 'Dir read', 'Dir read', 3),
                (12, 1, 'Dir write', 'Dir write', 3),
                (13, 1, 'User create user attribute', 'User create user attribute', 4),
                (14, 1, 'User assign', 'User assign', 4),
                (15, 1, 'User delete', 'User delete', 4),
                (16, 1, 'User delete assign', 'User delete assign', 4),
                (17, 1, 'Entity represent', 'Entity represent', 4),
                (18, 1, 'User attribute create user attribute', 'User attribute create user attribute', 5),
                (19, 1, 'User attribute create user', 'User attribute create user', 5),
                (20, 1, 'User attribute delete user', 'User attribute delete user', 5),
                (21, 1, 'User attribute create operation set', 'User attribute create operation set', 5),
                (22, 1, 'User attribute assign to operation set', 'User attribute assign to operation set', 5),
                (23, 1, 'User attribute assign', 'User attribute assign', 5),
                (24, 1, 'User attribute assign to', 'User attribute assign to', 5),
                (25, 1, 'User attribute delete', 'User attribute delete', 5),
                (26, 1, 'User attribute delete assign', 'User attribute delete assign', 5),
                (27, 1, 'User attribute delete assign to', 'User attribute delete assign to', 5),
                (28, 1, 'Object delete', 'Object delete', 6),
                (29, 1, 'Object attribute create object', 'Object attribute create object', 7),
                (30, 1, 'Object attribute delete object', 'Object attribute delete object', 7),
                (31, 1, 'Object attribute create object attribute', 'Object attribute create object attribute', 7),
                (32, 1, 'Object attribute delete object attribute', 'Object attribute delete object attribute', 7),
                (33, 1, 'Object attribute create operation set', 'Object attribute create operation set', 7),
                (34, 1, 'Object attribute assign', 'Object attribute assign', 7),
                (35, 1, 'Object attribute assign to', 'Object attribute assign to', 7),
                (36, 1, 'Object attribute delete', 'Object attribute delete', 7),
                (37, 1, 'Object attribute delete assign', 'Object attribute delete assign', 7),
                (38, 1, 'Object attribute delete assign to', 'Object attribute delete assign to', 7),
                (39, 1, 'Policy class create user attribute', 'Policy class create user attribute', 9),
                (40, 1, 'Policy class delete user attribute', 'Policy class delete user attribute', 9),
                (41, 1, 'Policy class create object attribute', 'Policy class create object attribute', 9),
                (42, 1, 'Policy class delete object attribute', 'Policy class delete object attribute', 9),
                (43, 1, 'Policy class create object', 'Policy class create object', 9),
                (44, 1, 'Policy class assign', 'Policy class assign', 9),
                (45, 1, 'Policy class assign to', 'Policy class assign to', 9),
                (46, 1, 'Policy class delete', 'Policy class delete', 9),
                (47, 1, 'Policy class delete assign', 'Policy class delete assign', 9),
                (48, 1, 'Policy class delete assign to', 'Policy class delete assign to', 9),
                (49, 1, 'Operation set assign', 'Operation set assign', 10),
                (50, 1, 'Operation set assign to', 'Operation set assign to', 10),
                (51, 1, 'Operation set delete', 'Operation set delete', 10),
                (52, 1, 'Operation set delete assign', 'Operation set delete assign', 10),
                (53, 1, 'Operation set delete assign to', 'Operation set delete assign to', 10),
                (54, 1, 'Connector create policy class', 'Connector create policy class', 8),
                (55, 1, 'Connector delete policy class', 'Connector delete policy class', 8),
                (56, 1, 'Connector create user', 'Connector create user', 8),
                (57, 1, 'Connector delete user', 'Connector delete user', 8),
                (58, 1, 'Connector create user attribute', 'Connector create user attribute', 8),
                (59, 1, 'Connector delete user attribute', 'Connector delete user attribute', 8),
                (60, 1, 'Connector create object attribute', 'Connector create object attribute', 8),
                (61, 1, 'Connector delete object attribute', 'Connector delete object attribute', 8),
                (62, 1, 'Connector create object', 'Connector create object', 8),
                (63, 1, 'Connector create operation set', 'Connector create operation set', 8),
                (64, 1, 'Connector assign to', 'Connector assign to', 8),
                (65, 1, 'Connector delete assign to', 'Connector delete assign to', 8);
INSERT INTO operation_set_details (operation_set_details_node_id, operation_id) VALUES
                (7, 3);
INSERT INTO host (host_id, host_name, workarea_path) VALUES
                (1, 'Dummy_host', 'dummy'),
(100, 'P860658', 'C:\\PMWorkarea');
INSERT INTO user_detail (user_node_id, user_name, full_name, password, email_address, host_id, pop_server, smtp_server, account_name) VALUES
                (4, 'super', 'SuperFirst', '100fd8433286961fcfa4cee541667c90dd6eaf22632fe284e4fded3f340c9d9b2245bb6224eec853f513c11a68ccaf74900a42f11277d6c3145d58daf1b99ce1cdd1b5882fd1d7f1e453f6923e6a95ca5d3', NULL, NULL, NULL, NULL, NULL);
INSERT INTO assignment_path (assignment_path_id, assignment_node_id) VALUES
                (1, 2),
                (2, 3),
                (3, 4),
                (4, 5);
INSERT INTO assignment (assignment_id, start_node_id, end_node_id, depth, assignment_path_id) VALUES
                (1, 2, 2, 0, NULL),
                (2, 3, 3, 0, NULL),
                (3, 2, 3, 1, 2),
                (4, 1, 3, 2, 2),
                (5, 4, 4, 0, NULL),
                (6, 3, 4, 1, 3),
                (7, 2, 4, 2, 3),
                (8, 1, 4, 3, 3),
                (9, 5, 5, 0, NULL),
                (10, 2, 5, 1, 4),
                (11, 1, 5, 2, 4),
                (12, 7, 7, 0, NULL),
                (13, 1, 1, 0, NULL),
                (14, 1, 2, 1, 1),
                (15, 5, 7, 1, NULL),
                (16, 7, 3, 1, NULL);
INSERT INTO deny_type (deny_type_id, name) VALUES
                (1, 'user id'),
                (2, 'user set'),
                (3, 'process');
INSERT INTO object_detail (object_node_id, original_node_id, object_class_id, host_id, path, include_ascedants) VALUES
                (5, 1, 8, 100, NULL, 1);
INSERT INTO ob_action_type VALUES (1,'assign'),(2,'assign like'),(3,'grant'),(4,'create'),(5,'deny'),(6,'delete assignment'),(7,'delete deny'),(8,'delete rule');
INSERT INTO ob_condition_type values (1,'user'),(2,'user attribute'),(3,'object'),(4,'object attribute'),(5,'policy');
INSERT INTO ob_op_spec_events VALUES (1,'Object write'),(2,'Object create'),(3,'Object read'),(4,'User create');
INSERT INTO ob_operand_type VALUES (1,'u'),(2,'p'),(3,'op'),(4,'b'),(5,'rule'),(6,'k');
INSERT INTO ob_user_spec_type VALUES (1,'u'),(2,'a'),(3,'ses'),(4,'proc');
INSERT INTO ob_cont_spec_type values (1,'b'),(2,'rec'),(3,'oc');
