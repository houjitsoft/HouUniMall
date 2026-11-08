#!/usr/bin/env bash
# =====================================================================
# 生成 Ed25519 制品签名密钥（一次性），并打印要写进 Nacos 的公钥表。
#
# 用法：
#   bash deploy/scripts/plugin/gen-sign-keys.sh [输出目录] [kid]
#   例：bash deploy/scripts/plugin/gen-sign-keys.sh ~/.unimall/secrets k2026
#
# 背景：2026-10-27 起安装/升级默认强制验签（houcloud.update.require-signature=true）。
#      没有公钥时服务仍能启动，但任何未签名制品都会被拒绝安装，
#      日志里会提示按本脚本生成密钥。
#
# 产物：
#   <输出目录>/artifact-ed25519-<kid>.key  私钥（PKCS#8 Base64，权限 600，禁止进 git / 镜像）
#   <输出目录>/artifact-ed25519-<kid>.pub  公钥（X.509 Base64）
# 之后把 "kid=<公钥 Base64>" 写进 Nacos base.yaml：
#   houcloud:
#     artifact:
#       sign:
#         active-kid: <kid>
#         public-keys: <kid>=<Base64>
# 私钥只留在签名机上（发布制品时用），绝不进仓库与容器。
# =====================================================================
set -euo pipefail

OUT_DIR="${1:-$HOME/.unimall/secrets}"
KID="${2:-k2026}"

REPO_ROOT="$(cd "$(dirname "$0")/../../.." && pwd)"
CP_FILE="$REPO_ROOT/plugin-platform/release/signature/target/classes"
SRC_DIR="$REPO_ROOT/plugin-platform/release/signature/src/main/java"

if ! command -v javac >/dev/null 2>&1; then
  echo "!! 需要 JDK 21+（找不到 javac）"
  exit 1
fi

mkdir -p "$OUT_DIR"
chmod 700 "$OUT_DIR"

# 优先用已编译的 classes；没有就现场编译（只依赖 JDK，不需要 maven）
WORK_DIR="$(mktemp -d)"
trap 'rm -rf "$WORK_DIR"' EXIT

if [ -d "$CP_FILE" ] && [ -n "$(ls -A "$CP_FILE" 2>/dev/null)" ]; then
  CLASS_PATH="$CP_FILE"
else
  echo "==> 未找到已编译的 signature 模块，现场编译到临时目录"
  javac -d "$WORK_DIR" $(find "$SRC_DIR" -name '*.java')
  CLASS_PATH="$WORK_DIR"
fi

echo "==> 生成密钥：kid=${KID}  目录=${OUT_DIR}"
java -cp "$CLASS_PATH" com.houjiang.plugin.signature.Ed25519KeyGen "$OUT_DIR" "$KID"

echo
echo "==> 把下面这一行写进 Nacos base.yaml 的 houcloud.artifact.sign.public-keys："
if [ -f "$OUT_DIR/artifact-ed25519-${KID}.pub" ]; then
  echo "active-kid: ${KID}"
  echo "${KID}=$(tr -d '\n' < "$OUT_DIR/artifact-ed25519-${KID}.pub")"
else
  echo "（生成工具未产出 artifact-ed25519-${KID}.pub，请检查上面的输出）"
fi
echo
chmod 600 "$OUT_DIR/artifact-ed25519-${KID}.key" 2>/dev/null || true
echo "私钥位置：$OUT_DIR/artifact-ed25519-${KID}.key（勿提交、勿打进镜像）"
