# Jenkins 部署需在服务器上配置 sudo（一次性）

流水线使用 `sudo systemctl restart mahjong-testing`（及 status/journalctl），  
Jenkins 运行用户须能**免密**执行这些命令，否则会报 “Interactive authentication required” 导致构建失败。

## 在部署目标服务器上执行（root 或已有 sudo 权限）

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

- 看构建日志里失败的那一行是 `systemctl restart` 还是 `sudo systemctl restart`。若是前者，说明流水线用的仍是旧版，需**推送最新 Jenkinsfile 并重新构建**。  
- 确认 `/etc/sudoers.d/jenkins-mahjong` 里的用户名、路径与 `which systemctl` 一致。
