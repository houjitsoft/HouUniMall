/* HouCloud 安装向导 SPA（public/install/app.js）
 * 零依赖。API 约定见 deploy/install-agent/app.py：
 *   GET  /install/api/status      当前安装状态（进入页面即查，已安装直接渲染终态）
 *   POST /install/api/preflight   环境检测
 *   POST /install/api/test-connection  自定义模式连通测试
 *   POST /install/api/start       开始安装 {mode, config}
 *   GET  /install/api/progress    进度（日志尾 + 阶段 + mysql 表数）
 *   POST /install/api/retry       从失败阶段重试 {fromStep}
 *   POST /install/api/reset       重置 {confirm: RESET|PURGE}
 *   POST /install/api/finish      停向导、起 admin-web
 */
"use strict";

const API = "/install/api";
const $ = (id) => document.getElementById(id);

const STAGE_ORDER = [
  ["env", "生成配置"], ["env-custom", "生成配置"],
  ["infra", "中间件"], ["custom-db", "初始化数据库"], ["nacos-import", "导入配置"],
  ["build", "构建镜像"], ["apps", "启动服务"], ["smoke", "验收"],
];

let pollTimer = null;
let currentMode = null;          // docker | custom
let connOk = false;              // custom 模式测试连接结果

/* ---------------------------------------------------------------- utils */
async function api(path, opts) {
  const res = await fetch(API + path, {
    headers: { "Content-Type": "application/json" },
    ...opts,
    body: opts && opts.body ? JSON.stringify(opts.body) : undefined,
  });
  if (res.status === 204) return {};
  return res.json();
}

function goStep(n) {                    // 1..5
  for (let i = 1; i <= 5; i++) {
    $("step" + i).hidden = i !== n;
    const li = document.querySelector(`.steps li[data-step="${i}"]`);
    li.classList.toggle("active", i === n);
    li.classList.toggle("done", i < n);
  }
}

function setHeader(text, cls) {
  const el = $("headerState");
  el.textContent = text;
  el.className = "header-state" + (cls ? " " + cls : "");
}

function consoleAppend(logs) {
  const el = $("console");
  el.innerHTML = (logs || []).map((l) => {
    const esc = (l.line || "").replace(/[&<>]/g, (c) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;" }[c]));
    let cls = "";
    if (/✗|失败|ERROR|error|refused|denied/i.test(l.line)) cls = "ln-err";
    else if (/✓|成功|ok|OK|healthy/.test(l.line)) cls = "ln-ok";
    else if (/^== 开始阶段|^== 安装/.test(l.line)) cls = "ln-stage";
    return `<span class="${cls}">[${l.ts}] ${esc}</span>`;
  }).join("\n");
  el.scrollTop = el.scrollHeight;
}

/* ---------------------------------------------------------------- boot */
async function boot() {
  let st;
  try {
    st = await api("/status");
  } catch (e) {
    // agent 不可达：admin-web 托管本页（安装已结束）或 install-web 未起
    goStep(5);
    $("doneTitle").textContent = "安装服务未运行";
    $("doneSub").textContent = "无法连接安装代理（/install/api 不可达）。";
    $("doneInfo").innerHTML = "";
    $("btnFinish").hidden = true;
    $("agentDownNote").hidden = false;
    setHeader("安装服务未运行", "bad");
    return;
  }

  if (st.state === "installed") {
    renderDone(st);
    return;
  }
  if (st.state === "installing") {
    setHeader("安装进行中 · " + (st.stepTitle || st.step), "ok");
    goStep(4);
    $("failedActions").hidden = true;
    startPolling();
    return;
  }
  if (st.state === "failed") {
    renderFailed(st, true);
    return;
  }
  // none / 未知 → 全新安装流程
  setHeader("未安装 · 欢迎使用", "");
  goStep(1);
  runPreflight();
}

/* ---------------------------------------------------------------- 1 preflight */
async function runPreflight() {
  $("preflightList").innerHTML = '<div class="loading">检测中…</div>';
  $("btnPreflightNext").disabled = true;
  let r;
  try {
    r = await api("/preflight", { method: "POST" });
  } catch (e) {
    $("preflightList").innerHTML = `<div class="loading" style="color:var(--bad)">安装服务不可达：${e}</div>`;
    return;
  }
  const html = (r.items || []).map((i) => {
    const cls = i.ok ? "ok" : (i.fatal ? "bad" : "warn");
    const mark = i.ok ? "✓" : (i.fatal ? "✗" : "!");
    return `<div class="row ${cls}"><span class="mark">${mark}</span>
      <div><span>${i.name}${i.ok || !i.fatal ? "" : '<span class="fatal-tag">致命</span>'}</span>
      <div class="detail">${(i.detail || "").replace(/[<>]/g, "")}</div></div></div>`;
  }).join("");
  $("preflightList").innerHTML = html || '<div class="loading">无检测项</div>';
  $("btnPreflightNext").disabled = !r.pass;
}

/* ---------------------------------------------------------------- 2 mode */
function chooseMode(mode) {
  currentMode = mode;
  $("cardDocker").classList.toggle("selected", mode === "docker");
  $("cardCustom").classList.toggle("selected", mode === "custom");
  goStep(3);
  $("dockerPreview").hidden = mode !== "docker";
  $("customForm").hidden = mode !== "custom";
  $("btnTestConn").hidden = mode !== "custom";
  $("btnStartInstall").disabled = mode === "custom";   // custom 须先过测试
}

/* ---------------------------------------------------------------- 3 custom conn */
function collectConfig() {
  return {
    mysql: {
      host: $("cMysqlHost").value.trim(), port: +$("cMysqlPort").value.trim() || 3306,
      user: $("cMysqlUser").value.trim() || "meoc",
      password: $("cMysqlPass").value, database: $("cMysqlDb").value.trim() || "meoc",
    },
    redis: { addr: $("cRedisAddr").value.trim(), password: $("cRedisPass").value },
    nacos: {
      addr: $("cNacosAddr").value.trim(), user: $("cNacosUser").value.trim() || "nacos",
      password: $("cNacosPass").value, namespace: $("cNacosNs").value.trim() || "houeoc",
    },
    rocketmq: { addr: $("cMqAddr").value.trim() },
  };
}

async function testConn() {
  const box = $("connResult");
  box.hidden = false;
  box.innerHTML = '<div class="loading">测试中…</div>';
  $("btnStartInstall").disabled = true;
  let r;
  try {
    r = await api("/test-connection", { method: "POST", body: { config: collectConfig() } });
  } catch (e) {
    box.innerHTML = `<div class="row bad">安装服务不可达：${e}</div>`;
    return;
  }
  const names = { mysql: "MySQL", redis: "Redis", nacos: "Nacos", rocketmq: "RocketMQ" };
  let html = "";
  let mustOk = true;
  for (const k of ["mysql", "redis", "nacos", "rocketmq"]) {
    const it = r[k] || { ok: false, detail: "无结果" };
    const required = k !== "rocketmq";
    const shown = required ? it.ok : true;
    if (required && !it.ok) mustOk = false;
    html += `<div class="row ${it.ok ? "ok" : "bad"}"><span class="name">${names[k]}</span>
             <span>${it.ok ? "✓ " : "✗ "}${(it.detail || "").replace(/[<>]/g, "")}</span>
             ${required ? "" : '<span style="color:#98a2b8">（选填，未填不阻断）</span>'}</div>`;
  }
  box.innerHTML = html;
  connOk = mustOk;
  $("btnStartInstall").disabled = !mustOk;
}

/* ---------------------------------------------------------------- 4 install */
async function startInstall() {
  const body = { mode: currentMode };
  if (currentMode === "custom") body.config = collectConfig();
  try {
    const r = await api("/start", { method: "POST", body });
    if (r.error) { alert(r.error); return; }
  } catch (e) {
    alert("无法开始安装：" + e);
    return;
  }
  goStep(4);
  $("failedActions").hidden = true;
  $("installTitle").textContent = "正在安装";
  setHeader("安装进行中", "ok");
  startPolling();
}

function renderStageBar(st) {
  const seq = STAGE_ORDER.map(([key]) => key);
  const curIdx = st.step ? seq.indexOf(st.step) : -1;
  // docker 与 custom 阶段混排：按实际 mode 过滤显示
  const show = STAGE_ORDER.filter(([k]) => {
    if (st.mode === "docker") return !["env-custom", "custom-db", "nacos-import"].includes(k);
    if (st.mode === "custom") return !["env"].includes(k);
    return true;
  });
  $("stageBar").innerHTML = show.map(([key, title]) => {
    const idx = seq.indexOf(key);
    let cls = "chip";
    if (idx < curIdx) cls += " done";
    else if (idx === curIdx) cls += (st.state === "failed" ? " fail" : " doing");
    return `<span class="${cls}">${title}</span>`;
  }).join("");
}

async function pollProgress() {
  let st;
  try {
    st = await api("/progress");
  } catch (e) {
    return; // 网络抖动：下轮再试
  }
  consoleAppend(st.logs);
  renderStageBar(st);
  const meta = [];
  if (st.stepTitle) meta.push(`当前阶段：<b>${st.stepTitle}</b>`);
  if (st.mysqlTables != null) meta.push(`数据库已建表：<b>${st.mysqlTables}</b> 张`);
  $("installMeta").innerHTML = meta.join(" · ");

  if (st.state === "installed") {
    stopPolling();
    const full = await api("/status");
    renderDone(full);
  } else if (st.state === "failed") {
    stopPolling();
    renderFailed(st, false);
  }
}

function startPolling() {
  stopPolling();
  pollProgress();
  pollTimer = setInterval(pollProgress, 2500);
}
function stopPolling() {
  if (pollTimer) { clearInterval(pollTimer); pollTimer = null; }
}

function renderFailed(st, fromBoot) {
  goStep(4);
  stopPolling();
  $("installTitle").textContent = "安装失败";
  setHeader("安装失败 · 可从失败阶段重试", "bad");
  let meta = document.createElement("div");
  meta.className = "error-box";
  meta.textContent = (st.stepTitle ? `阶段【${st.stepTitle}】失败\n\n` : "") + (st.error || "未知错误");
  $("installMeta").innerHTML = "";
  $("installMeta").appendChild(meta);
  if (!fromBoot) consoleAppend([{ ts: "", line: "== 阶段失败：" + (st.error || "").slice(-300) }]);
  $("failedActions").hidden = false;
}

async function retryFromFailed() {
  const st = await api("/status");
  const r = await api("/retry", { method: "POST", body: { fromStep: st.step } });
  if (r.error) { alert(r.error); return; }
  $("failedActions").hidden = true;
  $("installTitle").textContent = "正在安装（续跑）";
  setHeader("安装进行中（续跑）", "ok");
  startPolling();
}

async function doReset() {
  const v = $("resetInput").value.trim().toUpperCase();
  if (v !== "RESET" && v !== "PURGE") { alert("请输入 RESET 或 PURGE（全词）"); return; }
  if (!confirm(v === "PURGE" ? "确认删除全部数据卷？数据库数据将不可恢复！" : "确认停止全部服务并重置安装状态？")) return;
  const btn = $("btnResetDo");
  btn.disabled = true; btn.textContent = "执行中（最长十几分钟）…";
  try {
    const r = await api("/reset", { method: "POST", body: { confirm: v } });
    alert(r.message || r.error || "完成");
    if (r.ok !== false) location.reload();
  } catch (e) {
    alert("重置请求失败：" + e);
  } finally {
    btn.disabled = false; btn.textContent = "执行";
  }
}

/* ---------------------------------------------------------------- 5 done */
function renderDone(st) {
  stopPolling();
  goStep(5);
  const info = st.info || {};
  const host = location.hostname;
  const webPort = (info.adminUrl || "").match(/:(\d+)\//);
  const adminUrl = `http://${host}${webPort ? ":" + webPort[1] : ""}/`;

  $("doneTitle").textContent = "安装完成";
  $("doneSub").textContent = st.mode === "custom"
    ? "已使用外部中间件完成安装，连接确认信息如下。"
    : "以下容器服务已创建并运行，访问信息如下（口令均在服务器的 .env 中，未在此页回显）。";
  setHeader("已安装完成", "ok");

  let html = `<table class="tbl"><thead><tr><th>服务</th><th>访问信息</th><th>说明</th></tr></thead><tbody>`;
  for (const s of info.services || []) {
    html += `<tr><td>${s.name}</td><td class="addr">${(s.addr || "").replace(/<你的域名或IP>[:\d]*\//, host + "/")}</td>
             <td>${(s.note || "").replace(/[<>]/g, "")}</td></tr>`;
  }
  html += `</tbody></table>
    <p style="font-size:13px;color:var(--ink-2)">
      初始管理员：<b>admin</b>（初始口令见服务器 <code>deploy/scripts/sql/init/99-seed.sql</code>，首次登录后请立即修改）<br>
      Nacos 控制台口令：服务器执行 <code>${info.nacosPasswordHint || "grep '^NACOS_PASSWORD=' .env"}</code> ·
      支付配置口令：<code>${info.payAdminTokenHint || "grep '^HOU_PLUGIN_PAY_ADMIN_TOKEN=' .env"}</code>
    </p>`;
  $("doneInfo").innerHTML = html;
  $("sandboxWarn").hidden = info.sandbox !== "true";
  $("finishNote").hidden = false;
  $("finalUrl").textContent = adminUrl;
  $("btnFinish").hidden = false;
}

async function doFinish() {
  const btn = $("btnFinish");
  btn.disabled = true; btn.textContent = "正在切换…";
  try {
    await api("/finish", { method: "POST" });
  } catch (e) { /* 202 已回，agent 自停导致连接中断属预期 */ }
  btn.textContent = "已发出切换指令 · 约 30 秒后请访问 " + ($("finalUrl").textContent || "域名根路径");
  setHeader("安装完成 · 向导退出中", "ok");
}

/* ---------------------------------------------------------------- wire up */
$("btnPreflightRetry").onclick = runPreflight;
$("btnPreflightNext").onclick = () => goStep(2);
$("cardDocker").onclick = () => chooseMode("docker");
$("cardCustom").onclick = () => chooseMode("custom");
$("btnBackTo2").onclick = () => goStep(2);
$("btnTestConn").onclick = testConn;
$("btnStartInstall").onclick = startInstall;
$("btnRetry").onclick = retryFromFailed;
$("btnResetShow").onclick = () => { $("resetBox").hidden = false; $("resetInput").focus(); };
$("btnResetCancel").onclick = () => { $("resetBox").hidden = true; };
$("btnResetDo").onclick = doReset;
$("btnFinish").onclick = doFinish;

boot();
