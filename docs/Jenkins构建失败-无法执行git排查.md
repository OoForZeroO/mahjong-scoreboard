# Jenkins 构建失败：无法执行 git 命令 — 排查与修复

## 现象

构建在**流水线刚开始**就失败（尚未进入 Checkout/Build 等阶段），日志中出现：

- `java.io.IOException: Cannot run program "git" (in directory "/var/lib/jenkins/caches/git-...")`
- `Failed to exec spawn helper: pid: ..., exit value: 1`
- `hudson.plugins.git.GitException: Could not init /var/lib/jenkins/caches/git-...`

说明：Jenkins 在从 SCM 加载 Pipeline（或做 Git 操作）时，无法在指定目录下执行 `git` 命令。

---

## 原因概览

| 可能原因 | 说明 |
|----------|------|
| Git 未安装 | 节点/机器上没有安装 git |
| PATH 中无 git | Jenkins 进程的 PATH 里找不到 `git`（常见于以服务方式启动的 Jenkins） |
| 权限问题 | Jenkins 用户无法执行 `git` 或无法在 cache 目录写/执行 |
| 安全/环境限制 | noexec、容器、SELinux 等导致无法 fork/exec |

---

## 一、在 Jenkins 服务器上做的检查（SSH 登录执行）

### 1. 确认 git 已安装且可执行

```bash
# 用 root 或当前用户
which git
git --version
```

若找不到或报错，先安装（示例为 CentOS/RHEL）：

```bash
sudo yum install -y git
# 或
sudo dnf install -y git
```

### 2. 确认 Jenkins 进程的运行用户

```bash
ps aux | grep jenkins
# 或
ps -ef | grep jenkins
```

记下用户名，一般为 `jenkins`。

### 3. 用 Jenkins 用户验证能否执行 git

```bash
# 若 Jenkins 用户为 jenkins
sudo su - jenkins -s /bin/bash -c 'which git && git --version'
```

若这里就找不到 `git` 或报错，说明对该用户而言 `git` 不可用（PATH 或安装位置问题）。

### 4. 检查 PATH（Jenkins 启动时的环境）

- **systemd 启动**：看 Jenkins 的 service 文件里有没有设置 `Environment=PATH=...`，以及是否包含 git 所在目录（如 `/usr/bin`）。
- **Tomcat/其他方式**：看启动脚本或环境配置中的 PATH。

可临时在 Pipeline 里加一段诊断（若能先改成非 Pipeline from SCM 的方式跑一次）：

```groovy
sh 'echo PATH=$PATH && which git || true'
```

若 Jenkins 是用 systemd 起的，可直接在 server 上为 Jenkins 显式加 PATH：

```bash
# 编辑
sudo systemctl edit jenkins
# 或
sudo vi /etc/systemd/system/jenkins.service.d/override.conf
```

加入（路径按你机器上 `which git` 结果调整）：

```ini
[Service]
Environment="PATH=/usr/bin:/usr/local/bin:/bin"
```

然后：

```bash
sudo systemctl daemon-reload
sudo systemctl restart jenkins
```

### 5. 检查 Git 缓存目录权限

错误里出现的目录形如：`/var/lib/jenkins/caches/git-0b710bb49dfd93a8394b447294cea944`。

```bash
ls -la /var/lib/jenkins/caches
sudo -u jenkins touch /var/lib/jenkins/caches/test_write && rm /var/lib/jenkins/caches/test_write
```

若 `caches` 不存在，创建并授权：

```bash
sudo mkdir -p /var/lib/jenkins/caches
sudo chown jenkins:jenkins /var/lib/jenkins/caches
sudo chmod 755 /var/lib/jenkins/caches
```

### 6. 检查 noexec 挂载（会导致“无法执行”）

若 `/var/lib/jenkins` 或 `/var` 挂载了 `noexec`，可能影响在该目录下执行程序（包括 git）：

```bash
mount | grep -E ' /var | /var/lib | / '
```

若有 `noexec`，可以考虑：
- 把 Jenkins 工作目录/cache 改到未加 noexec 的分区；或
- 与运维协商调整挂载选项。

### 7. 检查 SELinux（若启用）

```bash
getenforce
```

若为 `Enforcing`，可先临时设为 Permissive 做验证：

```bash
sudo setenforce 0
# 再触发一次构建
```

若这样能通过，再考虑给 Jenkins 目录设合适的 context 或放行规则，而不是长期关 SELinux。

---

## 二、在 Jenkins 管理界面可做的配置

### 1. 指定 Git 可执行文件路径

- **Manage Jenkins → Global Tool Configuration**
- 找到 **Git** 的 **Path to Git executable**，填写服务器上实际路径，例如：`/usr/bin/git`
- 保存后重新运行任务。

### 2. 确认 Jenkins 自己的 PATH（若支持）

部分版本在 **Manage Jenkins → System** 或 **节点配置** 里可以设置环境变量，若有 **PATH** 或 **全局环境变量**，可加上包含 `git` 的目录。

---

## 三、建议操作顺序（快速修复）

1. **在服务器上**：安装/确认 git，并用 Jenkins 用户测试能否执行（见 1、3）。
2. **在服务器上**：确认 `/var/lib/jenkins/caches` 存在且 Jenkins 用户可写（见 5）。
3. **在 Jenkins 界面**：Global Tool Configuration 里把 Git 可执行路径设为绝对路径，如 `/usr/bin/git`（见二、1）。
4. **若仍失败**：检查 Jenkins 进程的 PATH（systemd 或启动脚本），必要时为 Jenkins 服务显式设置 PATH（见 4）。
5. **若在 noexec/SELinux 环境**：按 6、7 排查。

---

## 四、与本项目的关系

- 失败发生在 **加载/检出 Pipeline 的 Git 阶段**，尚未执行 `Jenkinsfile` 里的 Checkout/Build 等 stage。
- 因此**不需要改 Jenkinsfile 或仓库代码**，问题在 Jenkins 所在机器上的 Git 可用性与权限。

按上述步骤在 Jenkins 服务器上逐项检查并修正后，再重新触发构建即可。
