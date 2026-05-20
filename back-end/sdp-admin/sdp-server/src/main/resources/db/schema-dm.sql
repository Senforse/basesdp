-- sdp-admin RBAC（Dameng DM），与实体对齐：sys_organization、sys_menu.visible、sys_user.dept_id 等
DROP TABLE IF EXISTS sys_role_menu;
DROP TABLE IF EXISTS sys_user_role;
DROP TABLE IF EXISTS sys_audit_log;
DROP TABLE IF EXISTS sys_api_access_log;
DROP TABLE IF EXISTS sys_login_log;
DROP TABLE IF EXISTS sys_user;
DROP TABLE IF EXISTS sys_menu;
DROP TABLE IF EXISTS sys_role;
DROP TABLE IF EXISTS sys_organization;

CREATE TABLE sys_organization (
  id BIGINT NOT NULL IDENTITY(1,1),
  parent_id BIGINT DEFAULT 0,
  org_code VARCHAR(64),
  org_name VARCHAR(100) NOT NULL,
  sort_order INT DEFAULT 0,
  status SMALLINT DEFAULT 1,
  PRIMARY KEY (id)
);

CREATE TABLE sys_role (
  id BIGINT NOT NULL IDENTITY(1,1),
  role_code VARCHAR(64) NOT NULL,
  role_name VARCHAR(100) NOT NULL,
  sort_order INT DEFAULT 0,
  status SMALLINT DEFAULT 1,
  PRIMARY KEY (id),
  UNIQUE (role_code)
);

CREATE TABLE sys_menu (
  id BIGINT NOT NULL IDENTITY(1,1),
  parent_id BIGINT DEFAULT 0,
  menu_name VARCHAR(100) NOT NULL,
  path VARCHAR(255),
  component VARCHAR(128),
  perms VARCHAR(500),
  menu_type SMALLINT DEFAULT 0,
  icon VARCHAR(64),
  sort_order INT DEFAULT 0,
  visible SMALLINT DEFAULT 1,
  status SMALLINT DEFAULT 1,
  remark VARCHAR(500),
  PRIMARY KEY (id)
);

CREATE TABLE sys_user (
  id BIGINT NOT NULL IDENTITY(1,1),
  username VARCHAR(64) NOT NULL,
  password VARCHAR(200),
  display_name VARCHAR(100),
  status SMALLINT DEFAULT 1,
  dept_id BIGINT,
  PRIMARY KEY (id),
  UNIQUE (username)
);

CREATE TABLE sys_user_role (
  id BIGINT NOT NULL IDENTITY(1,1),
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE sys_role_menu (
  id BIGINT NOT NULL IDENTITY(1,1),
  role_id BIGINT NOT NULL,
  menu_id BIGINT NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE sys_login_log (
  id BIGINT NOT NULL IDENTITY(1,1),
  user_id BIGINT,
  username VARCHAR(64),
  login_ip VARCHAR(64),
  user_agent VARCHAR(500),
  login_status SMALLINT,
  log_type SMALLINT,
  remark VARCHAR(500),
  login_time TIMESTAMP DEFAULT SYSDATE,
  PRIMARY KEY (id)
);

CREATE TABLE sys_api_access_log (
  id BIGINT NOT NULL IDENTITY(1,1),
  trace_id VARCHAR(64),
  user_id BIGINT,
  username VARCHAR(64),
  client_ip VARCHAR(64),
  request_uri VARCHAR(500),
  http_method VARCHAR(16),
  query_string VARCHAR(2048),
  status_code INT,
  cost_ms BIGINT,
  access_time TIMESTAMP DEFAULT SYSDATE,
  PRIMARY KEY (id)
);

CREATE TABLE sys_audit_log (
  id BIGINT NOT NULL IDENTITY(1,1),
  trace_id VARCHAR(64),
  user_id BIGINT,
  username VARCHAR(64),
  client_ip VARCHAR(64),
  request_uri VARCHAR(500),
  http_method VARCHAR(16),
  biz_type VARCHAR(64),
  biz_id VARCHAR(128),
  action VARCHAR(128),
  before_json CLOB,
  after_json CLOB,
  result_code INT,
  success_flag SMALLINT,
  cost_ms BIGINT,
  error_message VARCHAR(500),
  op_time TIMESTAMP DEFAULT SYSDATE,
  PRIMARY KEY (id)
);

CREATE INDEX idx_api_access_trace_time ON sys_api_access_log(trace_id, access_time);
CREATE INDEX idx_api_access_uri_time ON sys_api_access_log(request_uri, access_time);
CREATE INDEX idx_audit_trace_time ON sys_audit_log(trace_id, op_time);
CREATE INDEX idx_audit_biz_time ON sys_audit_log(biz_type, biz_id, op_time);
