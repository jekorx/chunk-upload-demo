import SparkMD5 from 'spark-md5'
import axios from 'axios'
import qs from 'qs'

const blobSlice = File.prototype.slice || File.prototype.mozSlice || File.prototype.webkitSlice
axios.defaults.baseURL = '/admin-api/'
axios.defaults.transformRequest = [params => qs.stringify(params)]

export class ChunkUpload {
  constructor (file, options = {}) {
    if (!file) {
      throw new Error('The file parameter cannot be empty! ')
    }
    const { name, size } = file
    console.log(name, file)
    const {
      chunkSize = 1024 * 10,
      fileSuffixResolver,
      next
    } = options
    // 待处理文件
    this.file = file
    // 文件大小
    this.fileSize = size
    // 文件后缀
    this.fileSuffix = name.substring(name.lastIndexOf('.'))
    if (typeof fileSuffixResolver === 'function') {
      this.fileSuffix = fileSuffixResolver(file)
    }
    // 切片大小
    this.chunkSize = chunkSize
    // 切片数量
    this.chunkCount = Math.ceil(size / this.chunkSize)
    // 切片文件数组
    this.chunkList = []
    // MD5
    this.md5 = ''
    // 实例化spark用于计算文件md5
    this.spark = new SparkMD5.ArrayBuffer()
    // 通过FileReader读取文件进行切分、计算文件md5
    this.fileReader = new FileReader()
    // 加载切片索引
    this.loadIndex = 0
    // 上传切片索引
    this.uploadIndex = 0
    // 状态 'pending' | 'loading' | 'checking' | 'uploading' | 'merging' | 'completed' | 'canceled'
    this.status = 'pending'
    // 执行回调
    this.next = () => {}
    if (typeof next === 'function') {
      this.next = () => next({
        status: this.status,
        uploadIndex: this.uploadIndex,
        loadIndex: this.loadIndex
      })
    }

    // 停止前状态
    this._beforeStopedStatus = ''
    // 取消控制对象
    this._abortController = null

    this._resolve = null
    this._reject = null
    return [
      new Promise((resolve, reject) => {
        this._resolve = resolve
        this._reject = reject

        // 初始化
        this.init()
      }),
      {
        chunkCount: this.chunkCount,
        doStop: () => this.stop(),
        doContinue: () => this.continue()
      }
    ]
  }

  init () {
    const { chunkCount, spark, fileReader } = this
    fileReader.onload = ({ target }) => {
      // spark-md5读取当前切片
      spark.append(target.result)

      this.loadIndex++

      // 递归加载
      if (this.loadIndex < chunkCount) {
        this.load()
      } else {
        // 加载完成，计算md5
        this.md5 = spark.end()
        // 检查文件是否存在，确定是否续传
        this.check()
      }
    }
    // 开始加载文件
    this.load()
  }

  // 文件切片
  load () {
    if (this.status === 'canceled') {
      return
    }
    // 设置状态
    this.status = 'loading'

    // 触发进度变化
    this.next()

    const { chunkSize, fileSize, file, loadIndex, fileReader } = this
    const start = loadIndex * chunkSize
    const end = ((start + chunkSize) >= fileSize) ? fileSize : start + chunkSize
    const blob = blobSlice.call(file, start, end)
    this.chunkList.push(blob)
    fileReader.readAsArrayBuffer(blob)
  }

  async check () {
    if (this.status === 'canceled') {
      return
    }
    // 设置状态
    this.status = 'checking'

    // 触发进度变化
    this.next()

    const { md5 } = this
    try {
      this._abortController = new AbortController()
      const { status, data } = await axios.post(`upload/v1/file/check/${md5}`, {
        signal: this._abortController.signal
      })
      if (status === 200 && data.code === 1 && data.data > 0) {
        // 根据已上传切片数量确定，上传索引
        this.uploadIndex = data.data - 1
      } else {
        console.warn('ChunkUpload check', data.msg)
      }
    } catch (error) {
      console.warn('ChunkUpload check', error)
    } finally {
      // 执行下一阶段前，将取消控制对象置空
      if (this._abortController) {
        this._abortController = null
      }
      // 进入上传阶段，如有异常也正常进入上传阶段
      this.upload()
    }
  }

  async upload () {
    if (this.status === 'canceled') {
      return
    }
    // 设置状态
    this.status = 'uploading'

    // 触发进度变化
    this.next()

    const { md5, uploadIndex, chunkList, _reject } = this
    if (uploadIndex > chunkList.length - 1) {
      // 执行下一阶段前，将取消控制对象置空
      if (this._abortController) {
        this._abortController = null
      }
      // 进入文件合并阶段
      this.merge()
      return
    }
    try {
      const formData = new FormData()
      // 普通表单数据
      formData.append('md5', md5)
      // 文件数据
      formData.append('file', chunkList[uploadIndex])

      this._abortController = new AbortController()
      const { status, data } = await axios.post(`upload/v1/chunk/${uploadIndex}`, formData, {
        signal: this._abortController.signal,
        // 覆盖默认设置中的transformRequest设置
        transformRequest: [(params, headers) => {
          // 请求头Content-Type 为multipart/form-data
          headers = {
            'Content-Type': 'multipart/form-data'
          }
          // 取消qs参数转换
          return params
        }]
      })
      if (status === 200 && data.code === 1) {
        this.uploadIndex++
        this.upload()
      } else {
        _reject(new Error(data.msg))
      }
    } catch (error) {
      console.warn('ChunkUpload upload', uploadIndex, error)
      // 排除取消请求引起的报错
      if (!axios.isCancel(error)) {
        _reject(error)
      }
    }
  }

  async merge () {
    if (this.status === 'canceled') {
      return
    }
    // 设置状态
    this.status = 'merging'

    // 触发进度变化
    this.next()

    const { md5, chunkCount, fileSuffix, _resolve, _reject } = this
    try {
      this._abortController = new AbortController()
      const { status, data } = await axios.post(`upload/v1/file/merge/${md5}`, {
        chunks: chunkCount,
        suffix: fileSuffix
      }, {
        signal: this._abortController.signal
      })
      if (status === 200 && data.code === 1) {
        this.status = 'completed'

        // 触发进度变化
        this.next()

        if (_resolve) {
          // 返回文件名
          _resolve(data.data)
        }
      } else {
        _reject(new Error(data.msg))
      }
    } catch (error) {
      console.warn('ChunkUpload merge', error)
      // 排除取消请求引起的报错
      if (!axios.isCancel(error)) {
        _reject(error)
      }
    } finally {
      // 清空取消控制对象
      if (this._abortController) {
        this._abortController = null
      }
    }
  }

  stop () {
    const currentStatus = this.status
    if (['checking', 'uploading', 'merging'].includes(currentStatus)) {
      // 由于检查、上传、合并阶段存在请求，如果控制对象不为空，取消当前请求
      if (this._abortController && typeof this._abortController.abort === 'function') {
        this._abortController.abort()
      }
    }
    if (currentStatus !== 'canceled') {
      // 保存关闭前状态
      this._beforeStopedStatus = currentStatus
      // 将状态置为取消
      this.status = 'canceled'
    }
  }

  continue () {
    const currentStatus = this._beforeStopedStatus
    // 将状态恢复取消前状态
    this.status = currentStatus

    // 根据取消前状态之下对应请求
    const methodName = {
      pending: 'init',
      loading: 'load',
      checking: 'check',
      uploading: 'upload',
      merging: 'merge'
    }[currentStatus]

    if (typeof this[methodName] === 'function') {
      this[methodName]()
    }

    /* // 根据取消前状态之下对应请求
    switch (this._beforeStopedStatus) {
      case 'pending':
        this.init()
        break
      case 'loading':
        this.load()
        break
      case 'checking':
        this.check()
        break
      case 'uploading':
        this.upload()
        break
      case 'merging':
        this.merge()
        break
      default:
        break
    } */
  }
}
