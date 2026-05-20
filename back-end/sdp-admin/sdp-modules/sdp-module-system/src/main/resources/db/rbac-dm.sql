-- RBAC DDL + seed for Dameng; execute manually. Login: admin / password (BCrypt).
-- Alter existing tables (ignore errors if columns exist):
-- ALTER TABLE sys_user ADD dept_id BIGINT NULL;
-- ALTER TABLE sys_login_log ADD log_type SMALLINT DEFAULT 1;

CREATE TABLE sys_organization (
    id BIGINT NOT NULL IDENTITY(1,1),
    parent_id BIGINT DEFAULT 0 NOT NULL,
    org_code VARCHAR(64) NOT NULL,
    org_name VARCHAR(128) NOT NULL,
    sort_order INT DEFAULT 0,
    status SMALLINT DEFAULT 1,
    PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uk_sys_org_code ON sys_organization (org_code);

CREATE TABLE sys_role (
    id BIGINT NOT NULL IDENTITY(1,1),
    role_code VARCHAR(64) NOT NULL,
    role_name VARCHAR(128) NOT NULL,
    sort_order INT DEFAULT 0,
    status SMALLINT DEFAULT 1,
    PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uk_sys_role_code ON sys_role (role_code);

CREATE TABLE sys_menu (
    id BIGINT NOT NULL IDENTITY(1,1),
    parent_id BIGINT DEFAULT 0 NOT NULL,
    menu_name VARCHAR(128) NOT NULL,
    path VARCHAR(256),
    component VARCHAR(128),
    perms VARCHAR(256),
    menu_type SMALLINT DEFAULT 0,
    icon VARCHAR(64),
    sort_order INT DEFAULT 0,
    visible SMALLINT DEFAULT 1,
    status SMALLINT DEFAULT 1,
    PRIMARY KEY (id)
);

CREATE TABLE sys_user_role (
    id BIGINT NOT NULL IDENTITY(1,1),
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uk_sys_user_role ON sys_user_role (user_id, role_id);

CREATE TABLE sys_role_menu (
    id BIGINT NOT NULL IDENTITY(1,1),
    role_id BIGINT NOT NULL,
    menu_id BIGINT NOT NULL,
    PRIMARY KEY (id)
);
CREATE UNIQUE INDEX uk_sys_role_menu ON sys_role_menu (role_id, menu_id);

INSERT INTO sys_organization (parent_id, org_code, org_name, sort_order, status) VALUES (0, 'ROOT', '根组织', 0, 1);

INSERT INTO sys_role (role_code, role_name, sort_order, status) VALUES ('SUPER_ADMIN', '超级管理员', 0, 1);

INSERT INTO sys_menu (parent_id, menu_name, path, component, perms, menu_type, icon, sort_order, visible, status)
VALUES (0, '生产管理', '/production', NULL, NULL, 0, 'FolderOpened', 10, 1, 1);

INSERT INTO sys_menu (parent_id, menu_name, path, component, perms, menu_type, icon, sort_order, visible, status)
SELECT id, '生产工单', '/production/order', 'ProductionOrder', 'production:order:view', 1, 'Document', 1, 1, 1 FROM sys_menu WHERE path = '/production' AND menu_type = 0;

INSERT INTO sys_menu (parent_id, menu_name, path, component, perms, menu_type, icon, sort_order, visible, status)
SELECT id, '排产计划', '/production/schedule', 'ProductionSchedule', 'production:schedule:view', 1, 'Calendar', 2, 1, 1 FROM sys_menu WHERE path = '/production' AND menu_type = 0;

INSERT INTO sys_menu (parent_id, menu_name, path, component, perms, menu_type, icon, sort_order, visible, status)
VALUES (0, '质量管理', '/quality', NULL, NULL, 0, 'Checked', 20, 1, 1);

INSERT INTO sys_menu (parent_id, menu_name, path, component, perms, menu_type, icon, sort_order, visible, status)
SELECT id, '质量检验', '/quality/check', 'QualityCheck', 'quality:check:view', 1, 'Histogram', 1, 1, 1 FROM sys_menu WHERE path = '/quality' AND menu_type = 0;

INSERT INTO sys_menu (parent_id, menu_name, path, component, perms, menu_type, icon, sort_order, visible, status)
VALUES (0, '系统管理', '/system', NULL, NULL, 0, 'Setting', 90, 1, 1);

INSERT INTO sys_menu (parent_id, menu_name, path, component, perms, menu_type, icon, sort_order, visible, status)
SELECT id, '组织架构', '/system/org', 'SystemOrg', 'system:org:list', 1, 'OfficeBuilding', 1, 1, 1 FROM sys_menu WHERE path = '/system' AND menu_type = 0;

INSERT INTO sys_menu (parent_id, menu_name, path, component, perms, menu_type, icon, sort_order, visible, status)
SELECT id, '角色管理', '/system/role', 'SystemRole', 'system:role:list', 1, 'UserFilled', 2, 1, 1 FROM sys_menu WHERE path = '/system' AND menu_type = 0;

INSERT INTO sys_menu (parent_id, menu_name, path, component, perms, menu_type, icon, sort_order, visible, status)
SELECT id, '菜单管理', '/system/menu', 'SystemMenu', 'system:menu:list', 1, 'Menu', 3, 1, 1 FROM sys_menu WHERE path = '/system' AND menu_type = 0;

INSERT INTO sys_menu (parent_id, menu_name, path, component, perms, menu_type, icon, sort_order, visible, status)
SELECT id, '用户授权', '/system/user', 'SystemUser', 'system:user:list', 1, 'Avatar', 4, 1, 1 FROM sys_menu WHERE path = '/system' AND menu_type = 0;

INSERT INTO sys_menu (parent_id, menu_name, path, component, perms, menu_type, icon, sort_order, visible, status)
SELECT id, '在线用户', '/system/online-user', 'SystemOnlineUser', 'system:online:list', 1, 'Monitor', 5, 1, 1 FROM sys_menu WHERE path = '/system' AND menu_type = 0;

INSERT INTO sys_menu (parent_id, menu_name, path, component, perms, menu_type, icon, sort_order, visible, status)
SELECT id, '组织维护', NULL, NULL, 'system:org:edit', 2, NULL, 10, 0, 1 FROM sys_menu WHERE path = '/system/org' AND menu_type = 1;

INSERT INTO sys_menu (parent_id, menu_name, path, component, perms, menu_type, icon, sort_order, visible, status)
SELECT id, '角色维护', NULL, NULL, 'system:role:edit', 2, NULL, 11, 0, 1 FROM sys_menu WHERE path = '/system/role' AND menu_type = 1;

INSERT INTO sys_menu (parent_id, menu_name, path, component, perms, menu_type, icon, sort_order, visible, status)
SELECT id, '角色授权菜单', NULL, NULL, 'system:role:assign', 2, NULL, 12, 0, 1 FROM sys_menu WHERE path = '/system/role' AND menu_type = 1;

INSERT INTO sys_menu (parent_id, menu_name, path, component, perms, menu_type, icon, sort_order, visible, status)
SELECT id, '菜单维护', NULL, NULL, 'system:menu:edit', 2, NULL, 13, 0, 1 FROM sys_menu WHERE path = '/system/menu' AND menu_type = 1;

INSERT INTO sys_menu (parent_id, menu_name, path, component, perms, menu_type, icon, sort_order, visible, status)
SELECT id, '用户分配角色', NULL, NULL, 'system:user:assign', 2, NULL, 14, 0, 1 FROM sys_menu WHERE path = '/system/user' AND menu_type = 1;

INSERT INTO sys_menu (parent_id, menu_name, path, component, perms, menu_type, icon, sort_order, visible, status)
SELECT id, '在线用户下线', NULL, NULL, 'system:online:kick', 2, NULL, 15, 0, 1 FROM sys_menu WHERE path = '/system/online-user' AND menu_type = 1;

INSERT INTO sys_role_menu (role_id, menu_id) SELECT r.id, m.id FROM sys_role r, sys_menu m WHERE r.role_code = 'SUPER_ADMIN';

UPDATE sys_user SET password = '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', dept_id = (SELECT TOP 1 id FROM sys_organization WHERE org_code = 'ROOT') WHERE username = 'admin';

INSERT INTO sys_user_role (user_id, role_id) SELECT u.id, r.id FROM sys_user u, sys_role r WHERE u.username = 'admin' AND r.role_code = 'SUPER_ADMIN';
