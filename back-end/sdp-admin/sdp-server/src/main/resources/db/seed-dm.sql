-- 默认 superadmin / password（BCrypt）。角色 SUPER_ADMIN 与 RbacPermissionService 一致。
DECLARE
  V_ORG BIGINT;
  V_ROLE BIGINT;
  V_USER BIGINT;
  V_PROD BIGINT;
  V_QUAL BIGINT;
  V_SYS BIGINT;
BEGIN
  INSERT INTO sys_organization (parent_id, org_code, org_name, sort_order, status) VALUES (0, 'ROOT', '根组织', 0, 1);
  SELECT ID INTO V_ORG FROM sys_organization WHERE org_code = 'ROOT';

  INSERT INTO sys_role (role_code, role_name, sort_order, status) VALUES ('SUPER_ADMIN', '超级管理员', 0, 1);
  SELECT ID INTO V_ROLE FROM sys_role WHERE role_code = 'SUPER_ADMIN';

  INSERT INTO sys_user (username, password, display_name, status, dept_id)
    VALUES ('superadmin', '$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG', '超级管理员', 1, V_ORG);
  SELECT ID INTO V_USER FROM sys_user WHERE username = 'superadmin';

  INSERT INTO sys_user_role (user_id, role_id) VALUES (V_USER, V_ROLE);

  INSERT INTO sys_menu (parent_id, menu_name, path, component, perms, menu_type, icon, sort_order, visible, status)
    VALUES (0, '生产管理', '/production', NULL, NULL, 0, 'FolderOpened', 10, 1, 1);
  SELECT ID INTO V_PROD FROM sys_menu WHERE path = '/production' AND parent_id = 0;

  INSERT INTO sys_menu (parent_id, menu_name, path, component, perms, menu_type, icon, sort_order, visible, status) VALUES
    (V_PROD, '生产工单', '/production/order', 'ProductionOrder', 'production:order', 1, 'Document', 1, 1, 1);
  INSERT INTO sys_menu (parent_id, menu_name, path, component, perms, menu_type, icon, sort_order, visible, status) VALUES
    (V_PROD, '排产计划', '/production/schedule', 'ProductionSchedule', 'production:schedule', 1, 'Calendar', 2, 1, 1);

  INSERT INTO sys_menu (parent_id, menu_name, path, component, perms, menu_type, icon, sort_order, visible, status)
    VALUES (0, '质量管理', '/quality', NULL, NULL, 0, 'CircleCheck', 20, 1, 1);
  SELECT ID INTO V_QUAL FROM sys_menu WHERE path = '/quality' AND parent_id = 0;

  INSERT INTO sys_menu (parent_id, menu_name, path, component, perms, menu_type, icon, sort_order, visible, status) VALUES
    (V_QUAL, '质量检验', '/quality/check', 'QualityCheck', 'quality:check', 1, 'Finished', 1, 1, 1);

  INSERT INTO sys_menu (parent_id, menu_name, path, component, perms, menu_type, icon, sort_order, visible, status)
    VALUES (0, '系统管理', '/system', NULL, NULL, 0, 'Setting', 90, 1, 1);
  SELECT ID INTO V_SYS FROM sys_menu WHERE path = '/system' AND parent_id = 0;

  INSERT INTO sys_menu (parent_id, menu_name, path, component, perms, menu_type, icon, sort_order, visible, status) VALUES
    (V_SYS, '组织管理', '/system/org', 'AdminOrg', 'system:org:list,system:org:edit', 1, 'OfficeBuilding', 1, 1, 1);
  INSERT INTO sys_menu (parent_id, menu_name, path, component, perms, menu_type, icon, sort_order, visible, status) VALUES
    (V_SYS, '菜单管理', '/system/menu', 'AdminMenu', 'system:menu:list,system:menu:edit', 1, 'Menu', 2, 1, 1);
  INSERT INTO sys_menu (parent_id, menu_name, path, component, perms, menu_type, icon, sort_order, visible, status) VALUES
    (V_SYS, '角色管理', '/system/role', 'AdminRole', 'system:role:list,system:role:edit,system:role:assign', 1, 'UserFilled', 3, 1, 1);
  INSERT INTO sys_menu (parent_id, menu_name, path, component, perms, menu_type, icon, sort_order, visible, status) VALUES
    (V_SYS, '用户管理', '/system/user', 'AdminUser', 'system:user:list,system:user:assign,system:user:edit', 1, 'Avatar', 4, 1, 1);
  INSERT INTO sys_menu (parent_id, menu_name, path, component, perms, menu_type, icon, sort_order, visible, status) VALUES
    (V_SYS, '在线用户', '/system/online-user', 'SystemOnlineUser', 'system:online:list,system:online:kick', 1, 'Monitor', 5, 1, 1);
  INSERT INTO sys_menu (parent_id, menu_name, path, component, perms, menu_type, icon, sort_order, visible, status) VALUES
    (V_SYS, '数据字典', '/system/dict', 'SystemDict', 'system:dict:list,system:dict:edit', 1, 'Database', 6, 1, 1);

  INSERT INTO sys_role_menu (role_id, menu_id) SELECT V_ROLE, ID FROM sys_menu;

  COMMIT;
END;
/
