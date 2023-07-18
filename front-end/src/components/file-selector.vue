<template>
  <div class="file-selector">
    <Button type="primary" @click="triggerSelect">
      <slot></slot>
    </Button>
    <form ref="formRef">
      <input ref="inputRef" type="file" @change="fileChange" />
    </form>
  </div>
</template>
<script>
import { Button } from 'element-ui'

export default {
  name: 'FileSelector',
  components: { Button },
  methods: {
    triggerSelect () {
      this.$refs.inputRef.click()
    },
    fileChange (e) {
      this.$emit('change', e.target.files)
    },
    reset () {
      this.$refs.formRef.reset()
    }
  }
}
</script>
<style lang="scss" scoped>
.file-selector {
  display: inline-block;
  form {
    display: none !important;
  }
}
</style>
