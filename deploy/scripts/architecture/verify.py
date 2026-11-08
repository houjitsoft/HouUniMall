#!/usr/bin/env python3
"""Read-only architecture gate. Fails when directory/build ownership drifts."""
import json
import re
import sys
from pathlib import Path
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[3]
NS = {'m': 'http://maven.apache.org/POM/4.0.0'}

# 校验档位：
#   full —— 仓库主树（默认）。缺任何登记资产都算违规，用来卡「目录/归属漂移」。
#   dist —— 对外分发的裁切树。公开包里**有意**不包含商业插件、私有扩展与前端扩展目录，
#           所以「资产不存在」不是漂移；仍然校验已随包发出的东西是否自相一致
#           （规范路径、清单字段、外键依赖、旧目录残留、扩展包名归属）。
PROFILE = 'full'

# 插件宿主白名单：业务服务名单（services.json）之外，允许出现的「非业务服务」宿主。
# mall 不是 24 个业务服务之一，也不作为统一业务宿主（services.json 里仍禁止出现它），
# 但它本身是合法插件宿主——商城域插件（优惠券/秒杀/拼团/满赠/积分商城/抽奖/红包/
# 商品折扣）集中挂在 mall 下装载。见 AGENTS.md §零 与 public/docs/系统架构.md §1。
OPTIONAL_PLUGIN_HOSTS = {'mall'}

def miss(errors, warnings, message):
    """资产缺失：主树里是错误，分发树里是「本就不随包」，只记 warning。"""
    (warnings if PROFILE == 'dist' else errors).append(message)


def verify(root=ROOT):
    catalog = json.loads((root / 'architecture/services.json').read_text())
    errors = []
    warnings = []
    names = [item['name'] for item in catalog['services']]
    # 服务数量基线：25 → 26（2026-10-27 新增平台中台 ai）。这仍是刻意的硬数字：
    # 服务清单只该在有意识地新增服务时变化，意外增删（误删 someone 或重复登记）
    # 必须当场炸掉。下次新增服务时同步改这里。
    expected_services = 26
    if PROFILE != 'dist' and (len(names) != expected_services or len(set(names)) != expected_services or {'mall', 'commons'} & set(names)):
        errors.append(f'Expected {expected_services} unique business services; mall/commons are not runtime services')
    codes = set()
    paths = set()
    external_paths = set()
    for plugin in catalog['plugins']:
        code, host = plugin['code'], plugin['hostService']
        # routeOnly：纯粹的历史 URL 前缀路由映射（如 activity → order），
        # 没有插件目录/manifest/pom，不参与任何目录与清单校验。
        if plugin.get('routeOnly'):
            continue
        if code in codes or (host not in names and host not in OPTIONAL_PLUGIN_HOSTS):
            errors.append(f'{code}: duplicate code or unknown host {host}')
        codes.add(code)
        external = (code == 'aigateway' and host == 'thirdparty'
                    and plugin['path'] == 'plugins/aigateway'
                    and plugin['runtime'] == 'standalone'
                    and plugin.get('sourceLayout') == 'external')
        if plugin['path'] in paths:
            errors.append(f'{code}: duplicate plugin directory {plugin["path"]}')
        paths.add(plugin['path'])
        if external:
            external_paths.add(plugin['path'])
            if (root / f'plugins/{host}/{code}').exists():
                errors.append(f'{code}: conflicting service-scoped plugin directory')
        elif plugin['path'] != f'plugins/{host}/{code}' or plugin.get('sourceLayout') == 'external':
            errors.append(f'{code}: non-canonical directory')
        manifest = root / plugin['path'] / 'plugin.yaml'
        if not manifest.is_file():
            miss(errors, warnings, f'{code}: missing {manifest.relative_to(root)}')
            continue
        content = manifest.read_text()
        for key, expected in [('code', code), ('hostService', host), ('runtime', plugin['runtime'])]:
            match = re.search(rf'^{key}:\s*([^\s#]+)', content, re.M)
            if not match or match.group(1) != expected:
                errors.append(f'{code}: manifest {key} must be {expected}')
        if plugin['runtime'] == 'standalone' and not external and (root/plugin['path']/'runtime/java').exists():
            errors.append(f'{code}: service implementation remains in plugin directory')
        plugin_pom = root/plugin['path']/'pom.xml'
        if plugin['runtime'] == 'embedded' and plugin_pom.is_file():
            project = ET.parse(plugin_pom).getroot()
            if any(profile.findtext('m:id', namespaces=NS) == 'standalone'
                   for profile in project.findall('m:profiles/m:profile', NS)):
                errors.append(f'{code}: embedded extension must not build an extra standalone service')
    for service in catalog['services']:
        name = service['name']
        # 路径校验分三层：格式 → source/plugins 目录名一致 → 目录存在。
        # 2026-10-27 起「服务名 ≠ 源码目录名」合法（ai 服务的源码目录是 backend/paas/aiagent，
        # 并行任务线登记的条目），不再硬编码 backend/paas/{name}；但三个路径仍必须落在
        # 各自的规范位置下，「路径必须存在」由 miss() 兜底（dist 档降为 WARN）。
        declared = {
            'source': service.get('source'),
            'plugins': service.get('plugins'),
            'extension': service.get('extension'),
        }
        patterns = {
            'source': r'^backend/paas/[a-z][a-z0-9-]*$',
            'plugins': r'^plugins/[a-z][a-z0-9-]*$',
            'extension': r'^backend/extensions/custom-[a-z][a-z0-9-]*$',
        }
        for key, value in declared.items():
            if not value:
                # extension 允许缺省（服务声明没有扩展点，如平台中台 ai）；
                # source / plugins 缺失必须拦下 —— 那是登记残缺，不是形态差异。
                if key != 'extension':
                    errors.append(f'{name}: missing {key} path declaration')
                continue
            if not re.match(patterns[key], value):
                errors.append(f'{name}: invalid {key} path ({value!r})')
            if not (root / value).is_dir():
                miss(errors, warnings, f'{name}: missing {value}')
        if declared['source'] and declared['plugins']:
            dir_of = lambda p: p.rsplit('/', 1)[-1]
            if dir_of(declared['source']) != dir_of(declared['plugins']):
                errors.append(f'{name}: source/plugins directory mismatch '
                              f'({declared["source"]!r} vs {declared["plugins"]!r})')
        if not declared['source']:
            continue
        pom = root / service['source'] / 'pom.xml'
        if not pom.is_file():
            miss(errors, warnings, f'{name}: missing service POM')
            continue
        try:
            project = ET.parse(pom).getroot()
            for dependency in project.findall('.//m:dependency', NS):
                artifact = dependency.findtext('m:artifactId', namespaces=NS) or ''
                if artifact in names and artifact != name:
                    errors.append(f'{name}: directly depends on service implementation {artifact}')
                if artifact.startswith('custom-') and artifact != 'custom-' + name:
                    errors.append(f'{name}: loads foreign extension {artifact}')
            if not list((root/service['source']/'src/main/java').rglob('*Application.java')):
                errors.append(f'{name}: no independent application entry point')
        except ET.ParseError as exc:
            errors.append(f'{name}: invalid POM: {exc}')
    for library in catalog.get('libraries', []):
        directory = root/library['source']
        if not (directory/'pom.xml').is_file():
            miss(errors, warnings, f"Missing library module: {library['source']}")
        if (root/'backend/paas'/library['artifactId']).exists():
            errors.append(f"Duplicate obsolete library root: {library['artifactId']}")
    manifests = [*(root/'plugins').glob('*/plugin.yaml'), *(root/'plugins').glob('*/*/plugin.yaml')]
    actual_manifests = {p.parent.relative_to(root).as_posix() for p in manifests}
    actual_codes = set()
    for manifest in manifests:
        match = re.search(r'^code:\s*([^\s#]+)', manifest.read_text(), re.M)
        if match:
            if match[1] in actual_codes:
                errors.append(f'{match[1]}: duplicate plugin code in source directories')
            actual_codes.add(match[1])
    # routeOnly 条目（如 activity）只是「遗留 URL 前缀 → 承载服务」的路由映射占位，
    # 磁盘上没有插件目录也没有 plugin.yaml，不参与 manifest 比对。
    declared_manifests = {p['path'] for p in catalog['plugins'] if not p.get('routeOnly')}
    if PROFILE == 'dist':
        # 公开包只带通用 hello-world：允许少，不允许多（多出来就是没登记的野生插件）
        for extra in sorted(actual_manifests - declared_manifests):
            errors.append(f'Plugin manifest not registered in ownership catalog: {extra}')
    elif actual_manifests != declared_manifests:
        errors.append('Plugin manifests and ownership catalog differ')
    # 宿主目录名 == 服务名是常态，但 2026-10-27 起允许不一致（ai 服务的宿主目录是
    # plugins/aiagent）。归属白名单 = 服务名 ∪ 各服务 plugins 声明的目录名。
    host_dirs = set(names) | {
        service['plugins'].rsplit('/', 1)[-1]
        for service in catalog['services'] if service.get('plugins')
    }
    for directory in (root/'plugins').iterdir():
        if (directory.is_dir() and directory.name not in host_dirs
                and directory.relative_to(root).as_posix() not in external_paths):
            if (directory/'plugin.yaml').is_file():
                errors.append(f'Unregistered root plugin directory: {directory.name}')
                continue
            # 「源码已从catalog 下线，但生产目录仍留着运行时产物」的插件目录：
            # 典型是 frontend/index.mjs —— 删掉会让线上插件页面白屏（插件已登记在
            # hou_addon_plugin，产物由 base 通过 bind mount 的 plugins 根提供）。
            # 这不是架构违规，只是「Git 树」与「运行时目录」两种形态共存，降为 warning。
            #
            # 真正的空壳残留是**一个文件都没有**的目录（Git 里删干净、机器上只剩 mkdir 的残留），
            # 只有这种才阻断构建。
            files = [p for p in directory.rglob('*') if p.is_file()]
            if files:
                warnings.append(
                    f'Retained plugin asset directory not in ownership catalog: {directory.name} '
                    f'({len(files)} runtime file(s) still served online; '
                    'delete only after the plugin is retired in hou_addon_plugin)')
            else:
                errors.append(f'Non-service directory under plugins: {directory.name}')
    for service in catalog['services']:
        # 未声明 extension 的服务（如平台中台 ai）没有 admin/bcenter 前端扩展点，
        # 连带跳过「Java 包必须 service-scoped」的扫描。
        if not service.get('extension'):
            continue
        for terminal in ['admin','bcenter']:
            if not (root/f"frontend/{terminal}/src/extensions/{service['name']}").is_dir():
                miss(errors, warnings, f"Missing {terminal} extension directory for {service['name']}")
        extension = root / service['extension']
        for java in (extension/'src').rglob('*.java'):
            # 必须用 as_posix()：Windows 上 Path 的字符串形态是反斜杠，
            # 直接 str(java) 与 'com/houjiang/ext/<name>/' 比对会全部误判为该服务的★service-scoped 违规。
            if f"com/houjiang/ext/{service['name']}/" not in java.relative_to(root).as_posix():
                errors.append(f'Extension Java package is not service-scoped: {java.relative_to(root)}')
    for old in ['backend/extensions/custom-user', 'backend/extensions/custom-mall', 'plugin-services']:
        directory = root/old
        # 只按**文件**判断：机器上常留 Maven 生成的空 target/ 目录，那属于构建残留，
        # 与「旧扩展代码还在」不是一回事。只有真存在文件才说明旧代码未删干净。
        if directory.exists() and any(p.is_file() for p in directory.rglob('*')):
            errors.append(f'Obsolete extension root: {old}')
    verify.last_warnings = warnings
    return errors

def collect_warnings(root=ROOT):
    """warnings 不阻断构建，但必须可见（供 CI 日志与人工复核）。"""
    verify(root)
    return list(getattr(verify, 'last_warnings', []))

if __name__ == '__main__':
    import argparse
    ap = argparse.ArgumentParser(description='架构归属校验')
    ap.add_argument('--profile', choices=['full', 'dist'], default='full',
                    help='full=仓库主树（默认）；dist=对外分发裁切树，缺包内不含的资产只告警')
    PROFILE = ap.parse_args().profile
    failures = verify()
    for item in getattr(verify, 'last_warnings', []):
        print(f'WARN {item}')
    if failures:
        print('\n'.join(f'ERROR {item}' for item in failures))
        print(f'Architecture gate failed: {len(failures)} violation(s)')
        sys.exit(1)
    count = len(json.loads((ROOT / 'architecture/services.json').read_text())['services'])
    print(f'Directory/build ownership gate passed: {count} services '
          f'(profile={PROFILE}); runtime migration is a separate acceptance step')
