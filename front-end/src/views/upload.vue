<template>
  <div>
    <div>
      <FileSelector
        ref="fileSelectorRef"
        @change="fileChange"
      >选择文件</FileSelector>
      <Button type="info" @click="reset" style="margin-left: 10px">重置</Button>
      <Button type="danger" @click="stopHandle">暂停</Button>
      <Button type="success" @click="continueHandle">续传</Button>
      <span v-if="selectedFileName" v-text="`文件：${selectedFileName}`" style="margin-left: 10px"></span>
    </div>
    <div class="chunk-wrap">
      <span v-for="i in chunkCount" :key="i" :class="{ 'load': loadIndex >= i, 'upload': uploadIndex >= i }"></span>
    </div>
    <div v-if="imgUrl" class="img-wrap">
      <img :src="imgUrl" />
    </div>
  </div>
</template>
<script>
import FileSelector from '_c/file-selector'
import { ChunkUpload } from '@/utils'
import { Button } from 'element-ui'

const BASE_URL = 'http://127.0.0.1:8008/admin-api'

export default {
  name: 'Upload',
  components: { FileSelector, Button },
  computed: {
    imgUrl ({ newFileName }) {
      if (newFileName) {
        return `${BASE_URL}/${newFileName}`
      }
      return ''
    }
  },
  data () {
    return {
      selectedFileName: '',
      doStop: null,
      doContinue: null,
      chunkCount: 0,
      loadIndex: 0,
      uploadIndex: 0,
      newFileName: ''
    }
  },
  methods: {
    fileChange (files) {
      const file = files[0]
      if (!file) {
        return
      }
      this.selectedFileName = file.name
      const [promise, options] = new ChunkUpload(file, {
        next: this.uploadProcess
      })
      promise.then(filename => {
        this.newFileName = filename
      })
      this.doStop = options.doStop
      this.doContinue = options.doContinue
      this.chunkCount = options.chunkCount
    },
    uploadProcess (options) {
      this.loadIndex = options.loadIndex
      this.uploadIndex = options.uploadIndex
    },
    stopHandle () {
      if (typeof this.doStop === 'function') {
        this.doStop()
      }
    },
    continueHandle () {
      if (typeof this.doContinue === 'function') {
        this.doContinue()
      }
    },
    reset () {
      this.selectedFileName = ''
      this.doStop = null
      this.doContinue = null
      this.chunkCount = 0
      this.loadIndex = 0
      this.uploadIndex = 0
      this.newFileName = ''
      this.$refs.fileSelectorRef.reset()
    }
  }
}
</script>
<style lang="scss" scoped>
.chunk-wrap {
  padding: 10px 0;
  span {
    display: inline-block;
    height: 20px;
    width: 20px;
    border-radius: 2px;
    background-color: #EEE;
    transition: background-color 0.1s;
    margin: 0 1px;
    &.load {
      background-color: #409EFF;
    }
    &.upload {
      background-color: #67C23A;
    }
  }
}
.img-wrap {
  width: 300px;
  height: 300px;
  padding: 6px;
  border: 1px solid #EEE;
  img {
    max-width: 100%;
    max-height: 100%;
  }
}
</style>
