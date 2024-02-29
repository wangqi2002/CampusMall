<!--
 * @Author: wangqi2002 1722009706@qq.com
 * @Date: 2024-02-28 15:06:50
 * @LastEditors: wangqi2002 1722009706@qq.com
 * @LastEditTime: 2024-02-28 19:11:02
 * @FilePath: \CampusMall\admin\src\views\login\auth-redirect.vue
 * @Description:
 *
-->
<template />
<script>
export default {
  name: 'AuthRedirect',
  created() {
    const hash = window.location.search.slice(1)
    if (window.localStorage) {
      window.localStorage.setItem('x-admin-oauth-code', hash)
      window.close()
    }
  },
  render: function(h) {
    return h() // avoid warning message
  }
}
</script>

