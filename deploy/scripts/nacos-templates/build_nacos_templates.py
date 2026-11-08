#!/usr/bin/env python3
"""把 Nacos 配置导出模板化（v3）。

对 public/docs/nacos_config_export_*/ 下的 yaml / properties 做占位符替换，生成：
  scripts/nacos-templates/templates/<group>/<dataId>      模板（含 ${VAR}）
  scripts/nacos-templates/variables.example                变量表（仅变量名，值为空）

替换范围（全部按「值」处理，保留行尾注释与缩进）：
  a) JDBC 串         -> jdbc:mysql://${MYSQL_HOST}:${MYSQL_PORT}/<库名>${MYSQL_PARAMS}
  b) IPv4(:port)     -> 已知端口映射语义变量（3306/9876/8848/9200/8091/8858/8391），其余按上下文命名
  c) 真实域名        -> ${*_DOMAIN}
  d) 凭据类 key      -> ${语义_变量}
  e) 高熵值兜底      -> 单 token、≥16 位、字母+数字同时出现，判定为密钥

安全约定：脚本不打印任何真实值；产物只含变量名与占位符；相同语义的同值凭据不跨语义归并。
用法：python3 build_nacos_templates.py [导出目录]
"""
import re
import sys
import pathlib

ROOT = pathlib.Path('/Users/amosliu/www/houcloud')
# 原始导出含明文生产凭据，已移出仓库（不进 Git）：~/.workbuddy/secrets/houcloud/nacos_config_export_20260827110047
DEFAULT_EXPORT = pathlib.Path.home() / '.workbuddy/secrets/houcloud/nacos_config_export_20260827110047'
EXPORT = pathlib.Path(sys.argv[1]) if len(sys.argv) > 1 else DEFAULT_EXPORT
if not EXPORT.exists():
    sys.exit(f'!! 找不到原始 Nacos 导出目录：{EXPORT}\n'
             f'   该目录含明文凭据，按约定不放进仓库；如需重建模板，请传入导出目录作为参数。')
OUT = ROOT / 'scripts/nacos-templates'
TPL = OUT / 'templates'

RE_JDBC = re.compile(r'jdbc:mysql://[^\s"\'<>]+')
RE_JDBC_DB = re.compile(r'jdbc:mysql://[^/\s]+/([A-Za-z0-9_\-]+)(\?[^\s"\'<>]*)?')
RE_IPV4_PORT = re.compile(r'\b(\d{1,3}(?:\.\d{1,3}){3})(:\d+)?\b')
RE_ENTROPY = re.compile(r'^[A-Za-z0-9+/=_\-]{16,}$')

PORT_VAR = {
    '3306': ('MYSQL_HOST', 'MYSQL_PORT'),
    '9876': ('ROCKETMQ_NAMESERVER', None),
    '8848': ('NACOS_SERVER_ADDR', None),
    '9200': ('ES_ADDR', None),
    '8091': ('SEATA_SERVER_ADDR', None),
    '8858': ('SEATA_SERVER_ADDR', None),
    '8391': ('BFF_ADDR', None),
}

SENSITIVE_KEY = re.compile(
    r'password|passwd|pwd|secret|token|accesskey|access-key|access-key-secret|'
    r'appkey|app-key|app-id|appid|mchid|mch-id|privatekey|private-key|api.?key|sign.?key|'
    r'security.?key|unique.?identification|serialno|serial-no', re.I)

FILE_SEMANTIC = [
    (r'redis', 'REDIS'), (r'rocketmq', 'ROCKETMQ'), (r'mybatis', 'MYBATIS'),
    (r'elastic|es\.', 'ES'), (r'seata', 'SEATA'), (r'mongo', 'MONGODB'),
    (r'sentinel', 'SENTINEL'), (r'job', 'JOB'), (r'rabbit', 'RABBITMQ'),
    (r'payment|pay', 'PAYMENT'), (r'thirdparty|third', 'THIRD'), (r'sms', 'SMS'),
    (r'auth', 'AUTH'), (r'userms', 'USERMS'), (r'goods', 'GOODS'),
    (r'order', 'ORDER'), (r'mall', 'MALL'), (r'permission', 'PERM'),
    (r'base', 'BASE'), (r'bff', 'BFF'), (r'workflow', 'WORKFLOW'),
    (r'dispose', 'DISPOSE'), (r'merchant', 'MERCHANT'),
]

GENERIC_PARENTS = {
    'spring', 'cloud', 'config', 'configuration', 'properties', 'prop', 'data', 'info',
    'setting', 'settings', 'option', 'options', 'param', 'params', 'value', 'values',
    'main', 'default', 'global', 'custom', 'extra', 'detail', 'details', 'key', 'keys',
}

value_vars = {}   # (明文值, 语义名) -> 变量名
used_names = set()


def assign(value, preferred):
    key = (value, preferred)
    if key in value_vars:
        return value_vars[key]
    name, n = preferred, 2
    while name in used_names:
        name = f'{preferred}_{n}'
        n += 1
    used_names.add(name)
    value_vars[key] = name
    return name


def file_semantic(name):
    for pat, seg in FILE_SEMANTIC:
        if re.search(pat, name, re.I):
            return seg
    return 'APP'


def key_kind(key):
    k = key.lower()
    for pat, kind in [
        (r'password|passwd|pwd', 'PASSWORD'), (r'secret', 'SECRET'),
        (r'access.?key.?secret', 'AK_SECRET'), (r'access.?key.?id|accesskey|appid|app-id', 'AK_ID'),
        (r'username|user$', 'USERNAME'), (r'token', 'TOKEN'),
        (r'appkey|app-key|api.?key', 'APP_KEY'), (r'mchid|mch-id', 'MCH_ID'),
        (r'private.?key', 'PRIVATE_KEY'), (r'sign.?key|security.?key', 'SIGN_KEY'),
        (r'unique.?identification|serialno|serial-no', 'IDENTIFIER'),
        (r'host|addr|ip$|endpoint|url|uri', 'HOST'),
        (r'key$', 'KEY'),
    ]:
        if re.search(pat, k):
            return kind
    return 'KEY'


def contextual_name(stack, name, kind):
    seg = file_semantic(name)
    parents = [re.sub(r'[^A-Za-z0-9]', '', k).upper() for _, k in stack[-2:]]
    parents = [p for p in parents if p and p.lower() not in GENERIC_PARENTS]
    if parents and seg and parents[0].startswith(seg):
        parts = parents + [kind]
    elif seg:
        parts = [seg] + parents + [kind]
    else:
        parts = parents + [kind]
    return '_'.join(dict.fromkeys(parts))[:60]


def repair_mojibake(line: str) -> str:
    """修复导出文件的双重编码乱码：gb18030 编码回原始字节 -> utf-8 解码。"""
    if not any('\u4e00' <= c <= '\u9fff' for c in line):
        return line
    try:
        raw = line.encode('gb18030').replace(b'\xa2\xe3', b'\x80')
    except UnicodeEncodeError:
        return line
    try:
        fixed = raw.decode('utf-8')
    except UnicodeDecodeError:
        fixed = raw.decode('utf-8', errors='ignore')
        if sum(1 for c in fixed if '\u4e00' <= c <= '\u9fff') < 3:
            return line
    return fixed or line


def ip_sub_factory(rel, stack):
    def ip_sub(mm):
        ip, port = mm.group(1), (mm.group(2) or '')
        if ip == '127.0.0.1':
            return mm.group(0)
        if port and port[1:] in PORT_VAR:
            hostvar, portvar = PORT_VAR[port[1:]]
            used_names.add(hostvar)
            if portvar:
                used_names.add(portvar)
                return f'${{{hostvar}}}:${{{portvar}}}'
            return f'${{{hostvar}}}'
        return '${{{}}}'.format(assign(f'{ip}{port}', contextual_name(stack, rel.name, 'ADDR')))
    return ip_sub


def split_value_comment(rest: str):
    """把 '<值>  # 注释' 拆成 (值, 注释)，保留注释原样。"""
    m = re.match(r'^(\s*)(.*?)(\s+#.*)?$', rest)
    return m.group(2), (m.group(3) or '')


def transform_value(value: str, key: str, rel, stack):
    """返回替换后的值；不需要替换时原样返回。"""
    new = value
    if RE_JDBC.search(new):
        dbm = RE_JDBC_DB.search(new)
        dbname, params = (dbm.group(1), dbm.group(2) or '') if dbm else ('DB', '')
        if params:
            used_names.add('MYSQL_PARAMS')
        used_names.update({'MYSQL_HOST', 'MYSQL_PORT'})
        new = RE_JDBC.sub(f'jdbc:mysql://${{MYSQL_HOST}}:${{MYSQL_PORT}}/{dbname}${{MYSQL_PARAMS}}', new)
    new = RE_IPV4_PORT.sub(ip_sub_factory(rel, stack), new)
    if 'houjit' in new:
        vn = assign('__houjit__' + rel.name, contextual_name(stack, rel.name, 'DOMAIN'))
        new = re.sub(r'[A-Za-z0-9.\-]*houjit[A-Za-z0-9.\-]*', f'${{{vn}}}', new)
        return new
    clean = new.strip().strip('"\'')
    if not clean or clean.startswith('${') or re.match(r'^-?\d+$', clean):
        return new
    if SENSITIVE_KEY.search(key):
        vn = assign(clean, contextual_name(stack, rel.name, key_kind(key)))
        return new.replace(clean, f'${{{vn}}}', 1)
    # 高熵兜底：单 token、≥16 位、字母与数字同时出现
    if RE_ENTROPY.match(clean) and re.search(r'[A-Za-z]', clean) and re.search(r'\d', clean) \
            and len(clean) >= 16 and not clean.lower() in {'true', 'false'}:
        vn = assign(clean, contextual_name(stack, rel.name, 'CREDENTIAL'))
        return new.replace(clean, f'${{{vn}}}', 1)
    return new


def write_template(rel: pathlib.Path, lines):
    dst = TPL / rel.parent
    dst.mkdir(parents=True, exist_ok=True)
    (dst / rel.name).write_text('\n'.join(lines) + '\n', encoding='utf-8')


def process_yaml(src: pathlib.Path, rel: pathlib.Path):
    out, stack = [], []
    for raw in [repair_mojibake(l) for l in src.read_text(encoding='utf-8', errors='replace').splitlines()]:
        m = re.match(r'^(\s*)(-?\s*)("?)([\w.\-\u4e00-\u9fff]+)\3\s*:(.*)$', raw)
        if not m:
            out.append(raw)
            continue
        indent, dash, _q, key, rest = m.groups()
        ind = len(indent.expandtabs(2))
        while stack and stack[-1][0] >= ind:
            stack.pop()
        stack.append((ind, key))
        value, comment = split_value_comment(rest)
        if not value.strip() or value.strip().startswith('#'):
            out.append(raw)
            continue
        new_value = transform_value(value, key, rel, stack[:-1])
        out.append(f'{indent}{dash}{key}:{rest.replace(value, new_value, 1)}' if new_value != value else raw)
    write_template(rel, out)


def process_properties(src: pathlib.Path, rel: pathlib.Path):
    out = []
    for raw in [repair_mojibake(l) for l in src.read_text(encoding='utf-8', errors='replace').splitlines()]:
        m = re.match(r'^([\w.\-]+)\s*=\s*(.*)$', raw)
        if not m:
            out.append(raw)
            continue
        key, rest = m.groups()
        value, comment = split_value_comment(rest)
        if not value.strip() or value.strip().startswith('#'):
            out.append(raw)
            continue
        new_value = transform_value(value, key, rel, [])
        out.append(f'{key}={rest.replace(value, new_value, 1)}' if new_value != value else raw)
    write_template(rel, out)


def main():
    files = sorted(list(EXPORT.rglob('*.yaml')) + list(EXPORT.rglob('*.properties')))
    for src in files:
        rel = src.relative_to(EXPORT)
        if src.suffix == '.properties':
            process_properties(src, rel)
        else:
            process_yaml(src, rel)

    desc = {
        'MYSQL_HOST': 'MySQL 主机', 'MYSQL_PORT': 'MySQL 端口（默认 3306）',
        'MYSQL_PARAMS': 'JDBC 附加参数（见文末示例）',
        'REDIS_ADDR': 'Redis 主机', 'REDIS_PASSWORD': 'Redis 密码',
        # redis.yaml 的 host/port 是两个独立字段（host 不能带端口），模板用这两个占位符；
        # 漏掉它们会把 ${REDIS_HOST} 原样写进 Nacos，服务启动报缺 spring.redis.* 配置。
        'REDIS_HOST': 'Redis 主机名（由 REDIS_ADDR 拆分，Docker 轨自动）',
        'REDIS_PORT': 'Redis 端口（由 REDIS_ADDR 拆分，Docker 轨自动）',
        'ROCKETMQ_NAMESERVER': 'RocketMQ NameServer 地址',
        'NACOS_SERVER_ADDR': 'Nacos 地址（如 127.0.0.1:8848）',
        'ES_ADDR': 'Elasticsearch 地址（可选）',
        'SEATA_SERVER_ADDR': 'Seata 地址（可选）',
        'BFF_ADDR': 'bff-web 地址（可选）',
        'AUTH_JWT_SECRET': 'JWT 签名密钥（请自行生成随机串）',
        'APP_DATASOURCE_PASSWORD': '主库密码（与 MYSQL 用户一致）',
    }
    section1 = ['MYSQL_HOST', 'MYSQL_PORT', 'MYSQL_PARAMS', 'APP_DATASOURCE_PASSWORD',
                'REDIS_ADDR', 'REDIS_PASSWORD', 'REDIS_HOST', 'REDIS_PORT',
                'NACOS_SERVER_ADDR', 'ROCKETMQ_NAMESERVER', 'AUTH_JWT_SECRET']
    section2 = ['ES_ADDR', 'SEATA_SERVER_ADDR', 'BFF_ADDR']
    lines = [
        '# HouCloud Nacos 配置变量表',
        '# 用法：cp variables.example variables.env -> 填入真实值 -> python3 apply_templates.py -> bash import_to_nacos.sh',
        '# 说明：值不带引号；# 之后为注释；未赋值的变量会以 ${VAR} 原样保留（对应服务可能起不来）。',
        '# 注意：同一把口令可能以不同语义各占一个变量（如 APP_/MALL_/USERMS_/WORKFLOW_DATASOURCE_PASSWORD），',
        '#       单库部署（所有服务连同一个 meoc 库）时它们填同一个值即可。',
        '',
        '# ============ 一、基础中间件（必填，否则服务无法启动） ============',
        'MYSQL_HOST=',
        'MYSQL_PORT=3306',
        'MYSQL_PARAMS=?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true',
        'APP_DATASOURCE_PASSWORD=',
        '',
        'REDIS_ADDR=',
        'REDIS_PASSWORD=',
        '',
        'NACOS_SERVER_ADDR=',
        'ROCKETMQ_NAMESERVER=',
        '',
        'AUTH_JWT_SECRET=',
        '',
        '# ============ 二、可选中间件（不用则留空） ============',
        'ES_ADDR=',
        'SEATA_SERVER_ADDR=',
        'BFF_ADDR=',
        '',
        '# ============ 三、业务第三方凭据（按需填写，不填则相关功能不可用） ============',
    ]
    for name in sorted(used_names):
        if name in section1 or name in section2 or name in {'__houjit__'}:
            continue
        if name.startswith('__'):
            continue
        lines.append(f'{name}=' + (f'    # {desc[name]}' if name in desc else ''))
    (OUT / 'variables.example').write_text('\n'.join(lines) + '\n', encoding='utf-8')

    print(f'模板化完成：{len(files)} 个配置 -> {TPL}')
    print(f'变量数：{len(used_names)}')


if __name__ == '__main__':
    main()
