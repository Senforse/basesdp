# GitLab 项目初始化清单（192.168.204.111）
#
# 在 GitLab Web 上完成（一次性）：
# 1. 登录 http://192.168.204.111
# 2. New project → Create blank project → 名称 sdp
# 3. 记下项目 URL，例如：http://192.168.204.111/mygroup/sdp.git
# 4. Settings → Repository → Default branch 设为 develop（可选）
# 5. Settings → CI/CD → General pipelines → 确保 CI/CD 已启用
#
# 本地 Windows（PowerShell，在仓库根目录）：
#   .\deploy\scripts\setup-local-git.ps1 -GitLabUrl "http://192.168.204.111/mygroup/sdp.git"
#
# 或手动：
#   git remote add origin http://192.168.204.111/<group>/sdp.git
#   git checkout -b develop
#   git push -u origin develop
