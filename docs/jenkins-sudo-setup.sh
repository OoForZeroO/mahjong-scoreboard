#!/bin/bash
# 在部署目标服务器上以 root 执行，为 Jenkins 用户配置免密 sudo（仅限 mahjong-testing/production 的 systemctl/journalctl）
set -e

echo "=== 检测 Jenkins 用户 ==="
# 常见：jenkins 进程或 JENKINS_USER 环境
JENKINS_USER=""
if id jenkins &>/dev/null; then
  JENKINS_USER=jenkins
fi
if [ -z "$JENKINS_USER" ] && [ -f /etc/default/jenkins ]; then
  JENKINS_USER=$(grep -E '^JENKINS_USER=' /etc/default/jenkins 2>/dev/null | cut -d= -f2 | tr -d '"' || true)
fi
if [ -z "$JENKINS_USER" ]; then
  JENKINS_USER=$(ps aux | grep -E '[j]enkins\.war|[j]enkins\.jar' | head -1 | awk '{print $1}')
fi
if [ -z "$JENKINS_USER" ] || [ "$JENKINS_USER" = "root" ]; then
  echo "请手动指定 Jenkins 运行用户，例如: JENKINS_USER=jenkins $0"
  exit 1
fi
echo "使用 Jenkins 用户: $JENKINS_USER"

echo "=== 检测 systemctl / journalctl 路径 ==="
SYSTEMCTL=$(command -v systemctl || echo "/usr/bin/systemctl")
JOURNALCTL=$(command -v journalctl || echo "/usr/bin/journalctl")
[ -x "$SYSTEMCTL" ] || { echo "未找到 systemctl"; exit 1; }
[ -x "$JOURNALCTL" ] || { echo "未找到 journalctl"; exit 1; }
echo "systemctl=$SYSTEMCTL journalctl=$JOURNALCTL"

echo "=== 写入 /etc/sudoers.d/jenkins-mahjong ==="
cat << SUDOERS | tee /etc/sudoers.d/jenkins-mahjong
Defaults:${JENKINS_USER} !requiretty
${JENKINS_USER} ALL=(ALL) NOPASSWD: ${SYSTEMCTL} restart mahjong-testing
${JENKINS_USER} ALL=(ALL) NOPASSWD: ${SYSTEMCTL} restart mahjong-production
${JENKINS_USER} ALL=(ALL) NOPASSWD: ${SYSTEMCTL} status mahjong-testing*
${JENKINS_USER} ALL=(ALL) NOPASSWD: ${SYSTEMCTL} status mahjong-production*
${JENKINS_USER} ALL=(ALL) NOPASSWD: ${SYSTEMCTL} is-active mahjong-testing
${JENKINS_USER} ALL=(ALL) NOPASSWD: ${SYSTEMCTL} is-active mahjong-production
${JENKINS_USER} ALL=(ALL) NOPASSWD: ${JOURNALCTL} -u mahjong-testing*
${JENKINS_USER} ALL=(ALL) NOPASSWD: ${JOURNALCTL} -u mahjong-production*
SUDOERS
chmod 440 /etc/sudoers.d/jenkins-mahjong

echo "=== 校验 sudoers 语法 ==="
visudo -c -f /etc/sudoers.d/jenkins-mahjong

echo "=== 测试免密执行（不应提示密码）==="
if sudo -u "$JENKINS_USER" sudo -n systemctl is-active mahjong-testing 2>/dev/null; then
  echo "mahjong-testing 状态: active"
else
  sudo -u "$JENKINS_USER" sudo -n systemctl is-active mahjong-testing || true
fi
echo "配置完成。请重新触发 Jenkins 部署。"
