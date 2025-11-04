-- ===============================
--  USERS
-- ===============================
CREATE TABLE users (
                       id            VARCHAR(36) NOT NULL PRIMARY KEY,
                       email         VARCHAR(255) NOT NULL UNIQUE,
                       google_id     VARCHAR(255),
                       display_name  VARCHAR(255) NOT NULL,
                       status        ENUM('active', 'disabled') DEFAULT 'active' NOT NULL,
                       last_login_at DATETIME NULL,
                       created_at    DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
                       updated_at    DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP,
                       deleted_at    DATETIME NULL,
                       is_admin      TINYINT(1) DEFAULT 0 NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ===============================
--  COMPANIES
-- ===============================
CREATE TABLE companies (
                           id          CHAR(36) NOT NULL PRIMARY KEY,
                           name        VARCHAR(255) NOT NULL,
                           logo_url    VARCHAR(255),
                           created_at  DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
                           updated_at  DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP,
                           archived_at DATETIME NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ===============================
--  WORKSPACES
-- ===============================
CREATE TABLE workspaces (
                            id          CHAR(36) NOT NULL PRIMARY KEY,
                            company_id  CHAR(36) NOT NULL,
                            name        VARCHAR(255) NOT NULL,
                            description TEXT NULL,
                            created_at  DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
                            updated_at  DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP,
                            archived_at DATETIME NULL,
                            CONSTRAINT fk_workspaces_company FOREIGN KEY (company_id) REFERENCES companies (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_workspaces_company ON workspaces (company_id);

-- ===============================
--  PROJECTS
-- ===============================
CREATE TABLE projects (
                          id           CHAR(36) NOT NULL PRIMARY KEY,
                          workspace_id CHAR(36) NOT NULL,
                          name         VARCHAR(255) NOT NULL,
                          description  TEXT NULL,
                          status       ENUM('active', 'archived') DEFAULT 'active' NOT NULL,
                          color        VARCHAR(50),
                          created_at   DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
                          updated_at   DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP,
                          archived_at  DATETIME NULL,
                          CONSTRAINT fk_projects_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_projects_workspace ON projects (workspace_id);

-- ===============================
--  ROLES
-- ===============================
CREATE TABLE roles (
                       id          INT AUTO_INCREMENT PRIMARY KEY,
                       name        VARCHAR(255) NOT NULL,
                       description TEXT NULL,
                       scope       ENUM('company', 'workspace', 'project') NOT NULL,
                       created_at  DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
                       updated_at  DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ===============================
--  PERMISSIONS
-- ===============================
CREATE TABLE permission (
                            id          INT AUTO_INCREMENT PRIMARY KEY,
                            key_name    VARCHAR(255) NOT NULL,
                            scope       ENUM('company', 'workspace', 'project') NOT NULL,
                            description TEXT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ===============================
--  ROLE_PERMISSION
-- ===============================
CREATE TABLE role_permission (
                                 role_id       INT NOT NULL,
                                 permission_id INT NOT NULL,
                                 PRIMARY KEY (role_id, permission_id),
                                 CONSTRAINT fk_rp_role FOREIGN KEY (role_id) REFERENCES roles (id),
                                 CONSTRAINT fk_rp_permission FOREIGN KEY (permission_id) REFERENCES permission (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_rp_permission ON role_permission (permission_id);

-- ===============================
--  COMPANY_MEMBERS
-- ===============================
CREATE TABLE company_members (
                                 id            INT AUTO_INCREMENT PRIMARY KEY,
                                 company_id    CHAR(36) NOT NULL,
                                 user_id       VARCHAR(36) NOT NULL,
                                 role_id       INT NULL,
                                 invited_email VARCHAR(255),
                                 invited_by    VARCHAR(36),
                                 joined_at     DATETIME,
                                 is_owner      TINYINT(1) DEFAULT 0 NOT NULL,
                                 CONSTRAINT fk_cm_company FOREIGN KEY (company_id) REFERENCES companies (id),
                                 CONSTRAINT fk_cm_user FOREIGN KEY (user_id) REFERENCES users (id),
                                 CONSTRAINT fk_cm_invited_by FOREIGN KEY (invited_by) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_cm_company ON company_members (company_id);

-- ===============================
--  WORKSPACE_MEMBERS
-- ===============================
CREATE TABLE workspace_members (
                                   id           CHAR(36) NOT NULL PRIMARY KEY,
                                   workspace_id CHAR(36) NOT NULL,
                                   user_id      VARCHAR(36) NOT NULL,
                                   role_id      INT NOT NULL,
                                   is_owner     TINYINT(1) DEFAULT 0 NOT NULL,
                                   CONSTRAINT fk_wm_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces (id),
                                   CONSTRAINT fk_wm_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_wm_workspace ON workspace_members (workspace_id);

-- ===============================
--  PROJECT_MEMBERS
-- ===============================
CREATE TABLE project_members (
                                 id         INT AUTO_INCREMENT PRIMARY KEY,
                                 project_id CHAR(36) NOT NULL,
                                 user_id    VARCHAR(36) NOT NULL,
                                 role_id    INT NOT NULL,
                                 is_owner   TINYINT(1) DEFAULT 0 NOT NULL,
                                 CONSTRAINT fk_pm_project FOREIGN KEY (project_id) REFERENCES projects (id),
                                 CONSTRAINT fk_pm_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_pm_project ON project_members (project_id);

-- ===============================
--  BOARDS
-- ===============================
CREATE TABLE boards (
                        id         INT AUTO_INCREMENT PRIMARY KEY,
                        project_id CHAR(36) NOT NULL,
                        name       VARCHAR(255) NOT NULL,
                        CONSTRAINT fk_board_project FOREIGN KEY (project_id) REFERENCES projects (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_board_project ON boards (project_id);

-- ===============================
--  BOARD_COLUMNS
-- ===============================
CREATE TABLE board_columns (
                               id       INT AUTO_INCREMENT PRIMARY KEY,
                               board_id INT NOT NULL,
                               name     VARCHAR(255) NOT NULL,
                               position INT NOT NULL,
                               UNIQUE (board_id, position),
                               CONSTRAINT fk_bc_board FOREIGN KEY (board_id) REFERENCES boards (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ===============================
--  EPICS
-- ===============================
CREATE TABLE epics (
                       id          INT AUTO_INCREMENT PRIMARY KEY,
                       project_id  CHAR(36) NOT NULL,
                       title       VARCHAR(255) NOT NULL,
                       description TEXT NULL,
                       status      ENUM('planned', 'in_progress', 'done', 'archived'),
                       start_date  DATE,
                       end_date    DATE,
                       CONSTRAINT fk_epic_project FOREIGN KEY (project_id) REFERENCES projects (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_epic_project ON epics (project_id);

-- ===============================
--  SPRINTS
-- ===============================
CREATE TABLE sprints (
                         id         INT AUTO_INCREMENT PRIMARY KEY,
                         project_id CHAR(36) NOT NULL,
                         name       VARCHAR(255) NOT NULL,
                         start_date DATE NOT NULL,
                         end_date   DATE NOT NULL,
                         status     ENUM('planned', 'active', 'completed', 'cancelled') NOT NULL,
                         is_backlog TINYINT(1),
                         CONSTRAINT fk_sprint_project FOREIGN KEY (project_id) REFERENCES projects (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_sprint_project ON sprints (project_id);

-- ===============================
--  TASKS
-- ===============================
CREATE TABLE tasks (
                       id           INT AUTO_INCREMENT PRIMARY KEY,
                       project_id   CHAR(36) NOT NULL,
                       epic_id      INT NULL,
                       sprint_id    INT NULL,
                       title        VARCHAR(255) NOT NULL,
                       description  TEXT,
                       type         ENUM('task', 'story', 'bug') DEFAULT 'task' NOT NULL,
                       status       ENUM('to_do', 'in_progress') DEFAULT 'to_do' NOT NULL,
                       priority     ENUM('highest', 'high', 'medium', 'low') DEFAULT 'medium' NOT NULL,
                       assignee_id  VARCHAR(36),
                       reporter_id  VARCHAR(36),
                       due_date     DATE,
                       story_points INT,
                       created_at   DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
                       updated_at   DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL ON UPDATE CURRENT_TIMESTAMP,
                       archived_at  DATETIME,
                       CONSTRAINT fk_task_project FOREIGN KEY (project_id) REFERENCES projects (id),
                       CONSTRAINT fk_task_epic FOREIGN KEY (epic_id) REFERENCES epics (id),
                       CONSTRAINT fk_task_sprint FOREIGN KEY (sprint_id) REFERENCES sprints (id),
                       CONSTRAINT fk_task_assignee FOREIGN KEY (assignee_id) REFERENCES users (id),
                       CONSTRAINT fk_task_reporter FOREIGN KEY (reporter_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_task_project ON tasks (project_id);
CREATE INDEX idx_task_epic ON tasks (epic_id);
CREATE INDEX idx_task_sprint ON tasks (sprint_id);

-- ===============================
--  TAGS
-- ===============================
CREATE TABLE tags (
                      id         INT AUTO_INCREMENT PRIMARY KEY,
                      project_id CHAR(36) NOT NULL,
                      name       VARCHAR(255) NOT NULL,
                      color      VARCHAR(50),
                      CONSTRAINT fk_tag_project FOREIGN KEY (project_id) REFERENCES projects (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_tag_project ON tags (project_id);

-- ===============================
--  TASK_TAGS
-- ===============================
CREATE TABLE task_tags (
                           task_id INT NOT NULL,
                           tag_id  INT NOT NULL,
                           PRIMARY KEY (task_id, tag_id),
                           CONSTRAINT fk_tt_task FOREIGN KEY (task_id) REFERENCES tasks (id),
                           CONSTRAINT fk_tt_tag FOREIGN KEY (tag_id) REFERENCES tags (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_tt_tag ON task_tags (tag_id);

-- ===============================
--  COMMENTS
-- ===============================
CREATE TABLE comments (
                          id         BIGINT AUTO_INCREMENT PRIMARY KEY,
                          task_id    INT NOT NULL,
                          author_id  VARCHAR(36) NOT NULL,
                          content    TEXT NOT NULL,
                          created_at DATETIME DEFAULT CURRENT_TIMESTAMP NOT NULL,
                          CONSTRAINT fk_comment_task FOREIGN KEY (task_id) REFERENCES tasks (id),
                          CONSTRAINT fk_comment_author FOREIGN KEY (author_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE INDEX idx_comment_task ON comments (task_id);

-- ===============================
--  COMMENT_MENTIONS
-- ===============================
CREATE TABLE comment_mentions (
                                  comment_id        BIGINT NOT NULL,
                                  mentioned_user_id VARCHAR(36) NOT NULL,
                                  PRIMARY KEY (comment_id, mentioned_user_id),
                                  CONSTRAINT fk_cm_comment FOREIGN KEY (comment_id) REFERENCES comments (id),
                                  CONSTRAINT fk_cm_mentioned_user FOREIGN KEY (mentioned_user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
