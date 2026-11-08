#!/usr/bin/env bash
# =====================================================================
# UniMall 开源包安装入口（薄壳：转发到 deploy/install.sh，避免两份脚本漂移）
#
# 用法（在包根目录）：
#   bash install.sh              # 最小可用集：登录 + 插件中心 + 插件市场
#   bash install.sh --with-admin  # 额外起管理后台前端
#   bash install.sh --wizard      # 界面化安装向导
#   bash install.sh --help        # 完整用法
#
# 完整实现与阶段说明见 deploy/install.sh（本文件只做转发）。
# 注意：deploy/build-jars.sh 以「树根存在本文件」识别开源分发树形态
# （dist 门禁口径：商业插件源码不随包属预期，架构校验对缺失资产只告警）。
# =====================================================================
set -euo pipefail
exec bash "$(cd "$(dirname "$0")" && pwd)/deploy/install.sh" "$@"
