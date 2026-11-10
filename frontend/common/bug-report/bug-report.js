/*!
 * HouUniMall 全局 Bug 提交组件。只修改此权威源，通过 sync-assets.py 同步已有端。
 * 数据由本域 BFF 中继到 https://unimall.houjit.com，不直传用户 JWT。
 */
(function () {
  'use strict';
  if (window.__UNIMALL_BUG_REPORT__) return;
  window.__UNIMALL_BUG_REPORT__ = true;

  var ROOT_ID = 'unimall-bug-report-root';
  var ENDPOINT = '/web/bug-feedback/submit';
  var images = [];
  var networkLog = [];
  var busy = false;
  var el = {};
  var toastTimer;

  function text(value) { return String(value == null ? '' : value).trim(); }
  function esc(value) {
    return text(value).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;').replace(/'/g, '&#39;');
  }
  function safeUrl(value) {
    try {
      var url = new URL(String(value || ''), location.href);
      if (!/^https?:$/.test(url.protocol)) return '';
      return url.origin + url.pathname;
    } catch (e) { return ''; }
  }
  function hostToken() {
    try { return localStorage.getItem('token') || sessionStorage.getItem('token') || ''; }
    catch (e) { return ''; }
  }
  function recordNetwork(url, failed) {
    var clean = safeUrl(url);
    if (!clean || clean === location.origin + ENDPOINT) return;
    networkLog.push({ url: clean, failed: failed });
    if (networkLog.length > 30) networkLog.shift();
  }
  function currentApi() {
    for (var i = networkLog.length - 1; i >= 0; i--) {
      if (networkLog[i].failed) return networkLog[i].url;
    }
    return networkLog.length ? networkLog[networkLog.length - 1].url : '';
  }
  function patchNetwork() {
    if (typeof window.fetch === 'function' && !window.fetch.__ubrPatched) {
      var original = window.fetch;
      var wrapped = function (input) {
        var url = typeof input === 'string' || input instanceof URL ? String(input) : input && input.url;
        return original.apply(this, arguments).then(function (res) {
          recordNetwork(url, !res.ok); return res;
        }, function (err) { recordNetwork(url, true); throw err; });
      };
      wrapped.__ubrPatched = true;
      window.fetch = wrapped;
    }
    var XHR = window.XMLHttpRequest;
    if (XHR && !XHR.prototype.open.__ubrPatched) {
      var open = XHR.prototype.open;
      var send = XHR.prototype.send;
      XHR.prototype.open = function (method, url) {
        this.__ubrUrl = url; return open.apply(this, arguments);
      };
      XHR.prototype.open.__ubrPatched = true;
      XHR.prototype.send = function () {
        var xhr = this;
        xhr.addEventListener('loadend', function () {
          recordNetwork(xhr.__ubrUrl, xhr.status < 200 || xhr.status >= 300);
        }, { once: true });
        return send.apply(this, arguments);
      };
    }
  }
  function serviceCode() {
    var meta = document.querySelector('meta[name="unimall-service"]');
    return text(window.__UNIMALL_SERVICE__ || window.__PLUGIN_CODE__ ||
      (meta && meta.getAttribute('content')) || location.pathname.split('/').filter(Boolean)[0] || 'web').slice(0, 100);
  }
  function menuName() {
    var nodes = document.querySelectorAll('.el-breadcrumb__inner, .ant-breadcrumb a');
    var trail = [];
    for (var i = 0; i < nodes.length && trail.length < 6; i++) {
      var value = text(nodes[i].textContent);
      if (value && trail.indexOf(value) < 0) trail.push(value);
    }
    // 不采集 hash 路由及 query，避免把 URL 中的凭据混入菜单。
    return text(window.__UNIMALL_MENU_TRAIL__ || trail.join(' > ') || document.title || location.pathname).slice(0, 255);
  }

  var CSS = [
    '#unimall-bug-report-root{position:fixed;right:24px;bottom:92px;z-index:100001;pointer-events:none;font:14px/1.5 -apple-system,BlinkMacSystemFont,"Segoe UI","PingFang SC","Microsoft YaHei",sans-serif;color:#1f2329}',
    '#unimall-bug-report-root *{box-sizing:border-box}',
    '#unimall-bug-report-root .ubr-interactive{pointer-events:auto}',
    '.ubr-launcher{display:flex;flex-direction:column;align-items:flex-end;gap:10px}',
    '.ubr-main{width:48px;height:48px;border:0;border-radius:50%;background:#1f6feb;color:#fff;box-shadow:0 6px 20px rgba(31,111,235,.36);cursor:pointer;display:flex;align-items:center;justify-content:center;transition:transform .18s ease,box-shadow .18s ease}',
    '.ubr-main:hover{transform:translateY(-2px);box-shadow:0 8px 24px rgba(31,111,235,.48)}',
    '.ubr-main svg{width:22px;height:22px;fill:none;stroke:#fff;stroke-width:1.9;stroke-linecap:round;stroke-linejoin:round}',
    '.ubr-actions{display:flex;flex-direction:column;align-items:flex-end;gap:8px;opacity:0;visibility:hidden;transform:translateY(8px);pointer-events:none;transition:opacity .18s ease,transform .18s ease}',
    '.ubr-actions.is-open{opacity:1;visibility:visible;transform:translateY(0);pointer-events:auto}',
    '.ubr-action{border:1px solid #e4e7ed;background:#fff;color:#1f2329;border-radius:22px;padding:8px 14px;font-size:13px;cursor:pointer;white-space:nowrap;box-shadow:0 3px 12px rgba(0,0,0,.1);display:flex;align-items:center;gap:6px}',
    '.ubr-action:hover{border-color:#1f6feb;color:#1f6feb}',
    '.ubr-dot{width:7px;height:7px;border-radius:50%;background:currentColor;opacity:.75}',
    '.ubr-panel{width:388px;max-width:calc(100vw - 48px);max-height:min(72vh,720px);overflow:auto;background:#fff;border:1px solid #e4e7ed;border-radius:14px;box-shadow:0 12px 40px rgba(0,0,0,.16);margin-bottom:12px;padding:18px;display:none}',
    '.ubr-panel.is-open{display:block;animation:ubrIn .18s ease}',
    '@keyframes ubrIn{from{opacity:0;transform:translateY(10px)}to{opacity:1;transform:translateY(0)}}',
    '.ubr-head{display:flex;align-items:center;justify-content:space-between;margin-bottom:14px}',
    '.ubr-title{font-size:16px;font-weight:600;color:#1f2329}',
    '.ubr-close{border:0;background:transparent;color:#909399;font-size:20px;line-height:1;cursor:pointer;padding:2px 6px}',
    '.ubr-label{display:block;font-size:13px;color:#4e5969;margin:12px 0 6px}',
    '.ubr-required{color:#f5222d;margin-left:2px}',
    '.ubr-input{width:100%;border:1px solid #dcdfe6;border-radius:8px;padding:9px 10px;font-size:13px;color:#1f2329;background:#fff;outline:none}',
    '.ubr-input:focus{border-color:#1f6feb;box-shadow:0 0 0 2px rgba(31,111,235,.12)}',
    '.ubr-textarea{resize:vertical;min-height:96px;line-height:1.6;font-family:inherit}',
    '.ubr-drop{margin-top:6px;border:1px dashed #c9cdd4;border-radius:10px;background:#fafbfc;padding:18px 12px;text-align:center;cursor:pointer;color:#86909c;font-size:13px}',
    '.ubr-drop:hover,.ubr-drop.is-dragover{border-color:#1f6feb;background:#f0f6ff;color:#1f6feb}',
    '.ubr-drop strong{color:#1f6feb;font-weight:500}',
    '.ubr-thumbs{display:flex;flex-wrap:wrap;gap:8px;margin-top:10px}',
    '.ubr-thumb{position:relative;width:64px;height:64px;border:1px solid #e4e7ed;border-radius:8px;overflow:hidden;background:#f5f7fa}',
    '.ubr-thumb img{width:100%;height:100%;object-fit:cover;display:block}',
    '.ubr-thumb-del{position:absolute;top:0;right:0;width:18px;height:18px;border:0;border-radius:0 0 0 8px;background:rgba(0,0,0,.6);color:#fff;font-size:12px;cursor:pointer;padding:0}',
    '.ubr-context{margin-top:14px;padding:10px 12px;border-radius:8px;background:#f7f8fa;font-size:11px;color:#86909c;line-height:1.7;word-break:break-all}',
    '.ubr-footer{display:flex;justify-content:flex-end;gap:10px;margin-top:16px}',
    '.ubr-btn{border:1px solid #dcdfe6;background:#fff;color:#4e5969;border-radius:8px;padding:9px 18px;font-size:13px;cursor:pointer}',
    '.ubr-btn-primary{background:#1f6feb;border-color:#1f6feb;color:#fff}',
    '.ubr-btn[disabled]{opacity:.6;cursor:not-allowed}',
    '.ubr-status{margin-top:10px;font-size:12px;color:#1f6feb;min-height:16px}',
    '.ubr-toast{position:fixed;left:50%;top:24px;transform:translateX(-50%);max-width:min(90vw,520px);padding:12px 16px;border-radius:10px;font-size:13px;color:#fff;background:#d03050;opacity:0;pointer-events:none;z-index:100002;line-height:1.6}',
    '.ubr-toast.is-visible{opacity:1}', '.ubr-toast.is-success{background:#0a8f49}'
  ].join('');
  var BUG_ICON = '<svg viewBox="0 0 24 24" aria-hidden="true"><rect x="7" y="8" width="10" height="12" rx="5"/>' +
    '<path d="M9 8V6a3 3 0 0 1 6 0v2M3 12h4M17 12h4M3.5 17h4M16.5 17h4M4.5 7l2.5 2M19.5 7L17 9M12 11v9"/></svg>';
  function buildDom() {
    var style = document.createElement('style');
    style.id = 'unimall-bug-report-style'; style.textContent = CSS; document.head.appendChild(style);
    var root = document.createElement('div'); root.id = ROOT_ID;
    root.innerHTML = [
      '<div class="ubr-panel ubr-interactive" id="ubr-panel" role="dialog" aria-label="Bug 提交">',
      '<div class="ubr-head"><span class="ubr-title">Bug 提交</span><button type="button" class="ubr-close" id="ubr-close" aria-label="关闭">&times;</button></div>',
      '<form id="ubr-form"><label class="ubr-label" for="ubr-error-content">报错内容<span class="ubr-required">*</span></label>',
      '<textarea id="ubr-error-content" class="ubr-input ubr-textarea" required rows="4" maxlength="2000" placeholder="请描述报错现象，例如：点击保存后提示 500"></textarea>',
      '<label class="ubr-label" for="ubr-reporter">反馈人（选填，自报）</label><input id="ubr-reporter" class="ubr-input" maxlength="100">',
      '<label class="ubr-label">报错图</label><input type="file" id="ubr-file" accept="image/png,image/jpeg,image/webp" multiple hidden>',
      '<div class="ubr-drop" id="ubr-drop" tabindex="0" role="button">拖拽图片到此处，或 <strong>点击选择文件</strong><br><span style="font-size:11px">最多4张 PNG/JPEG/WebP，单张4MB，总计12MB；提交时才上传</span></div>',
      '<div class="ubr-thumbs" id="ubr-thumbs"></div><div class="ubr-context" id="ubr-context"></div>',
      '<label class="ubr-label"><input type="checkbox" id="ubr-privacy" required> 我已确认报错内容和截图不含密码、令牌及个人隐私，同意提交至 unimall.houjit.com。</label>',
      '<div class="ubr-status" id="ubr-status" role="status"></div><div class="ubr-footer"><button type="button" class="ubr-btn" id="ubr-cancel">取消</button><button type="submit" class="ubr-btn ubr-btn-primary" id="ubr-submit">提交</button></div></form></div>',
      '<div class="ubr-launcher ubr-interactive"><div class="ubr-actions" id="ubr-actions"><button type="button" class="ubr-action" id="ubr-knowledge"><span class="ubr-dot"></span>知识库</button><button type="button" class="ubr-action" id="ubr-bug"><span class="ubr-dot"></span>Bug 提交</button></div>',
      '<button type="button" class="ubr-main" id="ubr-main" title="全局 Bug 登记" aria-label="全局 Bug 登记" aria-expanded="false">' + BUG_ICON + '</button></div>',
      '<div class="ubr-toast" id="ubr-toast" role="status" aria-live="polite"></div>'
    ].join('');
    document.body.appendChild(root); el.root = root;
    ['panel', 'form', 'close', 'cancel', 'submit', 'file', 'drop', 'thumbs', 'context', 'status', 'toast', 'main', 'actions', 'bug', 'knowledge', 'privacy', 'reporter'].forEach(function (id) {
      el[id] = root.querySelector('#ubr-' + id);
    });
    el.content = root.querySelector('#ubr-error-content');
  }
  function toast(message, success) {
    el.toast.textContent = message; el.toast.className = 'ubr-toast is-visible' + (success ? ' is-success' : '');
    clearTimeout(toastTimer); toastTimer = setTimeout(function () { el.toast.className = 'ubr-toast'; }, 4200);
  }
  function refreshContext() {
    el.context.textContent = '服务/插件：' + serviceCode() + '\n菜单：' + menuName() + '\n当前报错接口：' + currentApi() + '\n统一提交平台：unimall.houjit.com（经本域中继）';
  }
  function renderThumbs() {
    el.thumbs.innerHTML = images.map(function (item, index) {
      return '<div class="ubr-thumb"><img src="' + esc(item.preview) + '" alt="截图 ' + (index + 1) + '"><button type="button" class="ubr-thumb-del" data-del="' + index + '" aria-label="移除">&times;</button></div>';
    }).join('');
  }
  function addFiles(files) {
    if (busy) return;
    Array.prototype.forEach.call(files || [], function (file) {
      var total = images.reduce(function (sum, item) { return sum + item.file.size; }, 0);
      if (images.length >= 4 || file.size > 4 * 1024 * 1024 || total + file.size > 12 * 1024 * 1024 ||
          !/^image\/(png|jpeg|webp)$/.test(file.type)) {
        toast('图片未加入：仅支持 PNG/JPEG/WebP，最多4张、单张4MB、总计12MB'); return;
      }
      images.push({ file: file, preview: URL.createObjectURL(file) });
    });
    renderThumbs();
  }
  function clearImages() {
    images.forEach(function (item) { URL.revokeObjectURL(item.preview); }); images = []; renderThumbs();
  }
  function setBusy(value) {
    busy = value;
    [el.submit, el.content, el.reporter, el.file, el.privacy].forEach(function (node) { node.disabled = value; });
    el.submit.textContent = value ? '正在提交…' : '提交';
    el.status.textContent = value ? '正在通过本域中继提交…' : '';
  }
  function submitForm(event) {
    event.preventDefault();
    if (busy) return;
    if (!text(el.content.value)) { toast('请填写报错内容'); return; }
    if (!el.privacy.checked) { toast('请先确认报错内容与截图隐私'); return; }
    var url = new URL(ENDPOINT, location.origin);
    if (url.origin !== location.origin) { toast('提交地址必须为本域'); return; }
    var payload = {
      errorContent: text(el.content.value), serviceCode: serviceCode(), menuName: menuName(),
      pageUrl: safeUrl(location.href), apiUrl: currentApi(), reporterName: text(el.reporter.value),
      clientVersion: text(window.__UNIMALL_CLIENT_VERSION__ || '').slice(0, 64), sourceSite: location.origin, analysis: ''
    };
    var body = new FormData(); body.append('payload', JSON.stringify(payload));
    images.forEach(function (item, index) {
      var extension = { 'image/png': 'png', 'image/jpeg': 'jpg', 'image/webp': 'webp' }[item.file.type];
      body.append('files', item.file, 'screenshot-' + (index + 1) + '.' + extension);
    });
    var headers = {};
    var token = text(hostToken()).replace(/^Bearer\s+/i, '');
    if (token) headers.Authorization = 'Bearer ' + token;
    var controller = new AbortController();
    var timer = setTimeout(function () { controller.abort(); }, 30000);
    setBusy(true);
    return window.fetch(url.href, { method: 'POST', body: body, headers: headers,
      credentials: 'same-origin', mode: 'same-origin', redirect: 'error', signal: controller.signal })
      .then(function (res) {
        if (!res.ok) throw new Error('HTTP ' + res.status);
        return res.json();
      }).then(function (res) {
        if (!res || res.code !== 1 || !res.data || !text(res.data.id)) throw new Error('平台未返回有效登记编号');
        toast('提交成功：Bug 已登记（' + text(res.data.id) + '）', true);
        el.form.reset(); clearImages(); el.panel.classList.remove('is-open');
      }).catch(function () {
        toast('提交失败或未收到有效回执，表单和截图已保留，请检查后重试');
      }).finally(function () { clearTimeout(timer); setBusy(false); });
  }
  async function openKnowledge() {
    el.actions.classList.remove('is-open'); el.main.setAttribute('aria-expanded', 'false');
    closeKnowledgeError();
    el.knowledge.disabled = true;
    try {
      var opened = false;
      if (typeof window.__HOU_OPEN_KNOWLEDGE__ === 'function') {
        opened = await window.__HOU_OPEN_KNOWLEDGE__();
      } else {
        // 兼容尚未重建宿主的静态发布：仅复用宿主已初始化的 SDK，不自行注入密钥或 SDK。
        var sdk = window.WeKnora;
        var panel = document.querySelector('div[style*="2147482999"]');
        if (sdk && panel && typeof sdk.open === 'function') {
          await sdk.open(); opened = true;
        }
      }
      if (!opened) toast('知识库入口已保留，当前宿主尚未配置或挂件未就绪，请稍后重试');
    } catch (ignored) {
      toast('知识库挂件暂时无法打开，请稍后重试');
    } finally { el.knowledge.disabled = false; }
  }
  function closeKnowledgeError() { clearTimeout(toastTimer); el.toast.className = 'ubr-toast'; }
  function bindEvents() {
    el.knowledge.addEventListener('click', openKnowledge);
    function closePanel() { el.panel.classList.remove('is-open'); }
    el.main.addEventListener('click', function () {
      var open = el.actions.classList.toggle('is-open'); el.main.setAttribute('aria-expanded', String(open));
      if (open) closePanel();
    });
    el.bug.addEventListener('click', function () {
      refreshContext(); el.panel.classList.add('is-open'); el.actions.classList.remove('is-open');
      el.main.setAttribute('aria-expanded', 'false'); el.content.focus();
    });
    el.close.addEventListener('click', closePanel); el.cancel.addEventListener('click', closePanel);
    el.form.addEventListener('submit', submitForm);
    el.drop.addEventListener('click', function () { if (!busy) el.file.click(); });
    el.drop.addEventListener('keydown', function (event) {
      if (event.key === 'Enter' || event.key === ' ') { event.preventDefault(); if (!busy) el.file.click(); }
    });
    el.file.addEventListener('change', function () { addFiles(el.file.files); el.file.value = ''; });
    ['dragenter', 'dragover', 'dragleave', 'drop'].forEach(function (name) {
      el.drop.addEventListener(name, function (event) {
        event.preventDefault(); event.stopPropagation();
        el.drop.classList.toggle('is-dragover', name === 'dragenter' || name === 'dragover');
        if (name === 'drop') addFiles(event.dataTransfer && event.dataTransfer.files);
      });
    });
    el.thumbs.addEventListener('click', function (event) {
      if (busy || !event.target.getAttribute) return;
      var index = event.target.getAttribute('data-del');
      if (index !== null && images[Number(index)]) {
        URL.revokeObjectURL(images[Number(index)].preview); images.splice(Number(index), 1); renderThumbs();
      }
    });
    document.addEventListener('click', function (event) {
      if (!el.root.contains(event.target)) { el.actions.classList.remove('is-open'); el.main.setAttribute('aria-expanded', 'false'); }
    });
    window.addEventListener('pagehide', function (event) { if (!event.persisted) clearImages(); });
  }
  function start() {
    if (document.getElementById(ROOT_ID)) return;
    patchNetwork(); buildDom(); bindEvents(); refreshContext();
  }
  // 知识库由宿主 runtime 配置独立初始化，本组件不注入、覆盖或保存其凭据。
  if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', start, { once: true });
  else start();
})();
