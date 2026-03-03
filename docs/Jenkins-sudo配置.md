# Jenkins 部署需在服务器上配置 sudo（一次性）

流水线使用 `sudo systemctl restart mahjong-testing` 等命令。  
若未配置免密，会报 **`sudo: a password is required`** / **`a terminal is required to read the password`**，脚本误判“服务未运行”导致构建失败。  
必须在部署目标服务器上为 **Jenkins 运行用户** 配置免密 sudo。

---

## 方式一：一键脚本（推荐）

在**部署目标服务器**上以 **root** 执行（脚本会自动检测 Jenkins 用户和 systemctl 路径）：

```bash
# 若服务器上已有项目代码（例如 Jenkins workspace）
cd /var/lib/jenkins/workspace/你的任务名/mahjong-scoreboard
sudo bash docs/jenkins-sudo-setup.sh
```

或从本机把脚本拷到服务器后执行：

```bash
# 本机
scp -P 22 docs/jenkins-sudo-setup.sh root@你的服务器IP:/tmp/

# 服务器
sudo bash /tmp/jenkins-sudo-setup.sh
```

脚本会写入 `/etc/sudoers.d/jenkins-mahjong` 并做一次免密测试。

---

## 方式二：手动配置

### 1. 确认 Jenkins 运行用户与命令路径

```bash
# Jenkins 用户（第一列）
ps aux | grep -E "jenkins|java.*jenkins" | grep -v grep | head -1

# systemctl / journalctl 实际路径（若为 /bin/systemctl 则下面用 /bin/）
which systemctl journalctl
```

记下用户名（如 `jenkins`）和路径（如 `/usr/bin` 或 `/bin`）。

### 2. 添加 sudoers 规则（多行更稳妥）

**把下面 `jenkins` 换成你上一步看到的用户名，`/usr/bin` 若在 `which` 里是 `/bin` 则改成 `/bin`。**

```bash
sudo tee /etc/sudoers.d/jenkins-mahjong << 'EOF'
Defaults:jenkins !requiretty
jenkins ALL=(ALL) NOPASSWD: /usr/bin/systemctl restart mahjong-testing
jenkins ALL=(ALL) NOPASSWD: /usr/bin/systemctl restart mahjong-production
jenkins ALL=(ALL) NOPASSWD: /usr/bin/systemctl status mahjong-testing*
jenkins ALL=(ALL) NOPASSWD: /usr/bin/systemctl status mahjong-production*
jenkins ALL=(ALL) NOPASSWD: /usr/bin/systemctl is-active mahjong-testing
jenkins ALL=(ALL) NOPASSWD: /usr/bin/systemctl is-active mahjong-production
jenkins ALL=(ALL) NOPASSWD: /usr/bin/journalctl -u mahjong-testing*
jenkins ALL=(ALL) NOPASSWD: /usr/bin/journalctl -u mahjong-production*
EOF
sudo chmod 440 /etc/sudoers.d/jenkins-mahjong
```

### 3. 校验并测试

```bash
# 语法检查
sudo visudo -c -f /etc/sudoers.d/jenkins-mahjong

# 用 Jenkins 用户执行（把 jenkins 换成实际用户）
sudo -u jenkins sudo -n systemctl restart mahjong-testing
```

- `sudo -n` 表示“不交互”，若还要求密码会直接报错。  
- 无报错即表示免密 sudo 已生效，Jenkins 再部署应能通过。

### 4. 若仍失败

- 确认 Jenkins 构建的是最新分支（日志里应有 `=== 使用 sudo 重启`）。
- 确认 `/etc/sudoers.d/jenkins-mahjong` 中的用户名与 `ps aux | grep jenkins` 第一列一致，路径与 `which systemctl` 一致。

---

## 无法拷贝文件时：在服务器上粘贴整段执行

SSH 到服务器后，以 **root** 粘贴并执行下面整段（会自动检测用户与路径并写入 sudoers）：

```bash
JENKINS_USER=$(ps aux | grep -E '[j]enkins\.war|[j]enkins\.jar' | head -1 | awk '{print $1}')
[ -z "$JENKINS_USER" ] && JENKINS_USER=jenkins
SYSTEMCTL=$(command -v systemctl); JOURNALCTL=$(command -v journalctl)
cat << EOF | tee /etc/sudoers.d/jenkins-mahjong
Defaults:${JENKINS_USER} !requiretty
${JENKINS_USER} ALL=(ALL) NOPASSWD: ${SYSTEMCTL} restart mahjong-testing
${JENKINS_USER} ALL=(ALL) NOPASSWD: ${SYSTEMCTL} restart mahjong-production
${JENKINS_USER} ALL=(ALL) NOPASSWD: ${SYSTEMCTL} status mahjong-testing*
${JENKINS_USER} ALL=(ALL) NOPASSWD: ${SYSTEMCTL} status mahjong-production*
${JENKINS_USER} ALL=(ALL) NOPASSWD: ${SYSTEMCTL} is-active mahjong-testing
${JENKINS_USER} ALL=(ALL) NOPASSWD: ${SYSTEMCTL} is-active mahjong-production
${JENKINS_USER} ALL=(ALL) NOPASSWD: ${JOURNALCTL} -u mahjong-testing*
${JENKINS_USER} ALL=(ALL) NOPASSWD: ${JOURNALCTL} -u mahjong-production*
EOF
chmod 440 /etc/sudoers.d/jenkins-mahjong
visudo -c -f /etc/sudoers.d/jenkins-mahjong
echo "配置完成。Jenkins 用户: $JENKINS_USER"
```
