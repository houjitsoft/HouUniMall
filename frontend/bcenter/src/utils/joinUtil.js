export function dateFormat(date, fmt) {
  var newDate = new Date(date.replace(/-/g, '/'))
  var o = {
    'M+': newDate.getMonth() + 1, //月份
    'd+': newDate.getDate(), //日
    'h+': newDate.getHours() % 12 == 0 ? 12 : newDate.getHours() % 12, //小时
    'H+': newDate.getHours(), //小时
    'm+': newDate.getMinutes(), //分
    's+': newDate.getSeconds(), //秒
    'q+': Math.floor((newDate.getMonth() + 3) / 3), //季度
    S: newDate.getMilliseconds(), //毫秒
  }
  var week = {
    '0': '/u65e5',
    '1': '/u4e00',
    '2': '/u4e8c',
    '3': '/u4e09',
    '4': '/u56db',
    '5': '/u4e94',
    '6': '/u516d',
  }
  if (/(y+)/.test(fmt)) {
    fmt = fmt.replace(
      RegExp.$1,
      (newDate.getFullYear() + '').substr(4 - RegExp.$1.length)
    )
  }
  if (/(E+)/.test(fmt)) {
    fmt = fmt.replace(
      RegExp.$1,
      (RegExp.$1.length > 1
        ? RegExp.$1.length > 2
          ? '/u661f/u671f'
          : '/u5468'
        : '') + week[newDate.getDay() + '']
    )
  }
  for (var k in o) {
    if (new RegExp('(' + k + ')').test(fmt)) {
      fmt = fmt.replace(
        RegExp.$1,
        RegExp.$1.length == 1 ? o[k] : ('00' + o[k]).substr(('' + o[k]).length)
      )
    }
  }
  return fmt
}

export function getUrlKey(name) {
  return (
    decodeURIComponent(
      (new RegExp('[?|&]' + name + '=' + '([^&;]+?)(&|#|;|$)').exec(
        location.href
      ) || [, ''])[1].replace(/\+/g, '%20')
    ) || null
  )
}
