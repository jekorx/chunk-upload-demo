import SparkMD5 from 'spark-md5'

/**
 * blob demo
 */
console.log('-------- blob demo start ---------')
const obj = { hello: 'world' }
const blob = new Blob(
  [JSON.stringify(obj)],
  { type: 'application/json' }
)

console.log(blob)
console.log('blob size:', blob.size)
console.log('blob type:', blob.type)

blob.arrayBuffer().then(ab => {
  console.log(ab)
  console.log('--------- blob demo end ----------')
})

/**
 * spark-md5
 */
console.log('--------- spark-md5 demo start ----------')
const chunkSize = 5
const chunkCount = Math.ceil(blob.size / chunkSize)

// load blob object
const spark0 = new SparkMD5.ArrayBuffer()
blob.arrayBuffer().then(ab => {
  spark0.append(ab)
  const md5 = spark0.end()
  console.log('spark0 blob md5 without split:', md5)
})

// load arrayBuffer by fileReader
const spark1 = new SparkMD5.ArrayBuffer()
const fileReader = new FileReader()
let loadIndex1 = 0
function load () {
  const start = loadIndex1 * chunkSize
  const end = ((start + chunkSize) >= blob.size) ? blob.size : start + chunkSize
  const b = blob.slice(start, end)
  fileReader.readAsArrayBuffer(b)
}
fileReader.onload = ({ target }) => {
  spark1.append(target.result)
  loadIndex1++
  if (loadIndex1 < chunkCount) {
    load()
  } else {
    const spark1Md5 = spark1.end()
    console.log('spark1 arrayBuffer md5 with split:', spark1Md5)
    console.log('--------- spark-md5 demo end ----------')
  }
}
load()

// load arrayBuffer by promise
const spark2 = new SparkMD5.ArrayBuffer()
let loadIndex2 = 0
function loadByPromise () {
  const start = loadIndex2 * chunkSize
  const end = ((start + chunkSize) >= blob.size) ? blob.size : start + chunkSize
  const b = blob.slice(start, end)
  b.arrayBuffer().then(ab => {
    spark2.append(ab)
    loadIndex2++
    if (loadIndex2 < chunkCount) {
      loadByPromise()
    } else {
      const spark2Md5 = spark2.end()
      console.log('spark2 arrayBuffer md5 with split:', spark2Md5)
      console.log('--------- spark-md5 demo end ----------')
    }
  })
}
loadByPromise()
