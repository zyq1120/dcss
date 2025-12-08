INSERT INTO sys_role (id, role_name, role_key, status, deleted, create_time)
VALUES (1, '超级管理员', 'SUPER_ADMIN', 1, 0, CURRENT_TIMESTAMP);

INSERT INTO sys_user (id, username, password, nickname, email, phone, status, deleted, token_version, version, create_time, update_time)
VALUES (1, 'admin', '$2a$10$LLzr/qFGm4hMlnSMHrik4uOpZhjdvnFj85pO3C7Hlm7umlGVQMdC6', '超级管理员', 'admin@system.com', '13800138000', 1, 0, 0, 0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO sys_user_role (id, user_id, role_id)
VALUES (1, 1, 1);
