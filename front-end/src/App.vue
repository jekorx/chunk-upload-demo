<template>
  <div id="app">
    <Demo />
    <Upload />
  </div>
</template>
<script>
import Demo from '_v/demo'
import Upload from '_v/upload'

export default {
  name: 'App',
  components: { Demo, Upload }
}
</script>
<style lang="scss" scoped>
#app {
  padding: 16px;
  box-sizing: border-box;
}
</style>
