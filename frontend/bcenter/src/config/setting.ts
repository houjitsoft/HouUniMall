const defaultSetting = {
    // 视觉版本：版本不一致时会丢弃本地旧主题缓存，保证改版后观感统一
    themeVersion: 3,
    showCrumb: true, // 是否显示面包屑
    showLogo: true, // 是否显示logo
    isUniqueOpened: false, //只展开一个一级菜单
    sideWidth: 232, //侧边栏宽度
    sideTheme: 'dark', //外壳主题（顶栏 + 栏目栏）
    sideDarkColor: '#23272e', //外壳深色主题颜色
    openMultipleTabs: true, // 是否开启多标签tab栏
    theme: '#ef4f55', //UniMall 品牌色
    successTheme: '#17855c', //成功主题色
    warningTheme: '#c0780e', //警告主题色
    dangerTheme: '#cf4a4a', //危险主题色
    errorTheme: '#cf4a4a', //错误主题色
    infoTheme: '#3c7ca0' //信息主题色
}
export default defaultSetting
