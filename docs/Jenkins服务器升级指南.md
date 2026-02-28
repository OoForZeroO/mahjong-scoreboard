# Jenkins 服务器升级指南

Jenkins 提示可升级时，在**服务器上**按你的安装方式选择对应步骤。

---

## 一、先确认当前安装方式

SSH 登录服务器后执行：

```bash
# 是否用 systemd 管理（包安装常见）
systemctl status jenkins 2>/dev/null && echo "--- 可能是 yum/dnf/apt 安装"

# 是否用 WAR 运行
ps aux | grep jenkins | grep -v grep
# 若看到 java -jar jenkins.war 或类似，则是 WAR 方式

# 当前版本（在 Jenkins 界面右上角「Manage Jenkins」也能看到）
rpm -q jenkins 2>/dev/null || dpkg -l jenkins 2>/dev/null || echo "非包安装，请看进程或界面"
```

---

## 二、方式 A：RHEL / CentOS / Fedora（yum / dnf）

### 1. 添加/更新官方仓库（若尚未使用官方源）

```bash
# CentOS 7 / RHEL 7
sudo wget -O /etc/yum.repos.d/jenkins.repo https://pkg.jenkins.io/redhat-stable/jenkins.repo
sudo rpm --import https://pkg.jenkins.io/redhat-stable/jenkins.io-2023.key

# CentOS 8+ / RHEL 8+ / Fedora（dnf）
sudo wget -O /etc/yum.repos.d/jenkins.repo https://pkg.jenkins.io/redhat-stable/jenkins.repo
sudo rpm --import https://pkg.jenkins.io/redhat-stable/jenkins.io-2023.key
```

### 2. 升级并重启

```bash
# yum
sudo yum upgrade jenkins -y

# 或 dnf
sudo dnf upgrade jenkins -y

# 重启服务
sudo systemctl restart jenkins
sudo systemctl status jenkins
```

### 3. 查看新版本

```bash
rpm -q jenkins
```

---

## 三、方式 B：Debian / Ubuntu（apt）

### 1. 添加/更新官方仓库

```bash
curl -fsSL https://pkg.jenkins.io/debian-stable/jenkins.io-2023.key | sudo tee /usr/share/keyrings/jenkins-keyring.asc > /dev/null
echo "deb [signed-by=/usr/share/keyrings/jenkins-keyring.asc]" https://pkg.jenkins.io/debian-stable binary/ | sudo tee /etc/apt/sources.list.d/jenkins.list > /dev/null
```

### 2. 升级并重启

```bash
sudo apt update
sudo apt install --only-upgrade jenkins -y
sudo systemctl restart jenkins
sudo systemctl status jenkins
```

### 3. 查看新版本

```bash
dpkg -l jenkins
```

---

## 四、方式 C：WAR 包方式运行

适用于用 `java -jar jenkins.war` 或把 WAR 放在 Tomcat 等容器里运行的情况。

### 1. 备份当前 WAR 与 JENKINS_HOME

```bash
# 假设 JENKINS_HOME 为默认
sudo tar czf /tmp/jenkins-home-backup-$(date +%Y%m%d).tar.gz /var/lib/jenkins

# 若 WAR 在 /opt/jenkins 等，也备份
sudo cp /path/to/jenkins.war /path/to/jenkins.war.bak
```

### 2. 下载新版本 WAR

到 [Jenkins 官网](https://www.jenkins.io/download/) 或直链下载 LTS 的 `jenkins.war`，例如：

```bash
cd /tmp
sudo wget https://get.jenkins.io/war-stable/latest/jenkins.war -O jenkins.war.new
```

### 3. 停止 Jenkins，替换 WAR，再启动

```bash
# 若用 systemd 管理的是自定义 service（名字可能不同）
sudo systemctl stop jenkins

# 替换 WAR（路径按你实际部署改）
sudo mv /path/to/jenkins.war /path/to/jenkins.war.old
sudo mv /tmp/jenkins.war.new /path/to/jenkins.war

sudo systemctl start jenkins
sudo systemctl status jenkins
```

若用 Tomcat 等，则停止容器 → 替换 `jenkins.war`（在 webapps 目录）→ 再启动容器。

---

## 五、方式 D：Docker 运行

若 Jenkins 跑在 Docker 里，升级即用新镜像重建容器：

```bash
# 拉取最新 LTS 镜像
docker pull jenkins/jenkins:lts

# 停止并删除旧容器（保留卷或先备份）
docker stop <容器名或ID>
docker rm <容器名或ID>

# 用相同卷/端口重新创建（按你原来的 docker run 或 compose 参数）
docker run -d --name jenkins -p 8080:8080 -v jenkins_home:/var/jenkins_home jenkins/jenkins:lts
```

若用 docker-compose，修改 `image` 为 `jenkins/jenkins:lts` 或具体版本号后：

```bash
docker-compose pull
docker-compose up -d
```

---

## 六、升级后建议

1. **浏览器访问**：`http://服务器IP:8080`（端口以实际为准），看是否正常并完成升级向导（若有）。
2. **插件**：升级后可在 **Manage Jenkins → Plugins** 中更新插件，避免与新版不兼容。
3. **若启动失败**：看日志排查：
   - 包安装：`sudo journalctl -u jenkins -n 100 -f`
   - WAR/脚本：看控制台输出或 `JENKINS_HOME` 下的 `logs/`。

---

## 七、简要对照

| 安装方式     | 升级命令/操作 |
|--------------|----------------|
| CentOS/RHEL  | `sudo yum upgrade jenkins -y` 或 `dnf upgrade jenkins -y`，再 `systemctl restart jenkins` |
| Debian/Ubuntu| `sudo apt update && sudo apt install --only-upgrade jenkins -y`，再 `systemctl restart jenkins` |
| WAR          | 备份 → 下载新 WAR → 停服务 → 替换 WAR → 启动 |
| Docker       | `docker pull jenkins/jenkins:lts`，用新镜像重建容器并保留数据卷 |

升级前建议先备份 `JENKINS_HOME`（一般为 `/var/lib/jenkins`）和当前 WAR（若适用）。
