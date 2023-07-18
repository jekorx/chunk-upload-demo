<template>
  <div class="wrap">
    <span>chunkSize：</span>
    <Input v-model="chunkSize" style="width: 100px; margin-right: 10px" />
    <FileSelector
      ref="fileSelectorRef"
      @change="fileChange"
    >选择文件</FileSelector>
  </div>
</template>
<script>
import SparkMD5 from 'spark-md5'
import FileSelector from '_c/file-selector'
import { Input } from 'element-ui'

export default {
  components: { FileSelector, Input },
  data () {
    return {
      spark0: new SparkMD5.ArrayBuffer(),
      spark1: new SparkMD5.ArrayBuffer(),
      spark2: new SparkMD5.ArrayBuffer(),
      fileReader: new FileReader(),
      fileSize: 0,
      chunkSize: 1024 * 10,
      chunkCount: 0,
      loadIndex1: 0,
      loadIndex2: 0
    }
  },
  methods: {
    fileChange (files) {
      const file = files[0]
      if (!file) {
        return
      }
      this.$refs.fileSelectorRef.reset()
      file.arrayBuffer().then(ab => {
        this.spark0.append(ab)
        const md5 = this.spark0.end()
        console.log('spark0 file md5 without split:', md5)
      })
      this.spark0.append(file)

      this.fileSize = file.size
      this.chunkCount = Math.ceil(this.fileSize / this.chunkSize)

      this.load(file)
      this.loadByPromise(file)

      this.fileReader.onload = ({ target }) => {
        console.log('reader:', target.result)
        this.spark1.append(target.result)
        this.loadIndex1++
        if (this.loadIndex1 < this.chunkCount) {
          this.load(file)
        } else {
          const spark1Md5 = this.spark1.end()
          console.log('spark1 arrayBuffer md5 with split:', spark1Md5)
          this.loadIndex1 = 0
          this.spark1 = new SparkMD5.ArrayBuffer()
          this.fileReader = new FileReader()
        }
      }
    },
    load (file) {
      const start = this.loadIndex1 * this.chunkSize
      const end = ((start + this.chunkSize) >= this.fileSize) ? this.fileSize : start + this.chunkSize
      const b = file.slice(start, end)
      this.fileReader.readAsArrayBuffer(b)
    },
    loadByPromise (file) {
      const start = this.loadIndex2 * this.chunkSize
      const end = ((start + this.chunkSize) >= this.fileSize) ? this.fileSize : start + this.chunkSize
      const b = file.slice(start, end)
      b.arrayBuffer().then(ab => {
        this.spark2.append(ab)
        this.loadIndex2++
        if (this.loadIndex2 < this.chunkCount) {
          this.loadByPromise(file)
        } else {
          const spark2Md5 = this.spark2.end()
          console.log('spark2 arrayBuffer md5 with split:', spark2Md5)
          this.loadIndex2 = 0
          this.spark2 = new SparkMD5.ArrayBuffer()
        }
      })
    }
  }
}
</script>
<style lang="scss" scoped>
.wrap {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}
</style>
