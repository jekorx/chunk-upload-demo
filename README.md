### 文件切片上传及断点续传

0. 基本概念
File: 文件，继承自 Blob
Blob: Binary Large Object，表示二进制类型的大对象，Blob 对象表示一个不可变、原始数据的类文件对象
    Blob.prototype.size: 所包含数据的大小（字节）
    Blob.prototype.type: 所包含数据的 MIME 类型
    Blob.prototype.slice(): 返回指定范围内的数据

      blob  ->  file
        ↑
    ArrayBuffer
        ↑
Int8Array/Uint8Array

file (32 bytes)
  ↓
0000 1111 2222 3333 4444 5555 6666 7777


1. 文件唯一性，完整性
根据文件内容计算文件 hash 值
MD5、SHA1、SHA256、SHA512：特定的hash算法，目的是检查数据的完整性
PGP：除了提供完整性校验以外，也能提供身份认证的能力，在拿到数据和对应的 PGP 签名后，如果不能使用已知的作者公钥验证，就无法证明这段数据是可信的

以 MD5 为例，使用 spark-md5 计算文件 hash 值


2. 文件切片，并计算 MD5 值 (load: 文件加载阶段)

const spark = new SparkMD5.ArrayBuffer()

file (29 bytes)
  ↓
0000 1111 2222 3333 4444 5555 6666 7
  ↓
chunkList: [
  0000 -> part 0: spark.append(0000)
  1111 -> part 1: spark.append(1111)
  2222 -> part 2: spark.append(2222)
  3333 -> part 3: spark.append(3333)
  4444 -> part 4: spark.append(4444)
  5555 -> part 5: spark.append(5555)
  6666 -> part 6: spark.append(6666)
  7    -> part 7: spark.append(7)
]
  ↓
spark.end() -> 2f1e861d563b5782f9dcd23ac8b98a38


3. 检查是否需要续传 (check: 文件检查阶段)
后端根据 MD5 值检索是否存在上传文件，并返回上传切片个数
 -> 存在，根据上传个数，续传剩余切片
 -> 不存在，正常上传


4. 递归上传文件切片 (upload: 文件上传阶段)
上传 load 阶段产生的 chunkList
到后端临时目录
以 MD5 值作为文件夹名，存放以索引为文件名的切片


5. 文件合并，校验文件完整性 (merge: 文件合并阶段)
创建合并后新文件对象 -> FileOutputStream -> BufferedOutputStream
根据 MD5 找到所有切片，根据索引遍历
for (int i = 0; i < chunkCount; i++) {
  切片文件 -> FileInputStream -> BufferedInputStream
  将内容逐一写入BufferedOutputStream
}
BufferedOutputStream.flush()
(注意：各种流关闭处理)

计算新文件 MD5，校验是否一致
删除临时文件


6. 返回最终结果
 -> 成功，新文件名
 -> 失败，错误原因
