package com.admin.api.utils;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.admin.api.config.BusinessException;
import com.admin.api.constant.ResultEnums;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;

/**
 * 文件操作工具类
 *
 * @author wang_dgang
 * @since 2021-03-18 09:49:00
 */
public class FileUtil {

    /**
     * 获取项目中指定目录
     * @param path
     * @return String
     */
    public static String getPath(String path) {
        try {
            String basePath = ResourceUtils.getURL("classpath:").getPath();
            return String.format("%s%s", basePath, path);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 系统临时目录中，获取文件夹目录下文件数量
     * @param path 目录
     * @return int
     */
    public static int fileCount(String path) {
        // 如果path为空，直接返回0
        if (StrUtil.isEmpty(path)) {
            return 0;
        }
        File dir = new File(String.format("%s/%s", getPath("tmp"), path));
        // 存在该目录且为文件夹
        if (dir.exists() && dir.isDirectory()) {
            // 获取目录文件名数组
            String[] filenames = dir.list();
            if (filenames != null) {
                // 返回数量
                return filenames.length;
            }
        }
        return 0;
    }

    /**
     * 系统临时目录中文件是否存在
     * @param path
     * @param name
     * @return boolean
     */
    public static boolean fileExists(String path, String name) {
        File file = new File(String.format("%s/%s/%s", getPath("tmp"), path, name));
        // 文件存在，为文件，文件大小大于0
        return file.exists() && file.isFile() && file.length() > 0;
    }

    /**
     * MultipartFile 转 File，并存放到系统临时目录中
     * @param file 文件
     * @return File
     * @throws IOException
     */
    public static File multipartFile2File(MultipartFile file) throws IOException {
        return multipartFile2File(file, null, null);
    }

    /**
     * MultipartFile 转 File，并存放到系统临时目录中
     * @param file 文件
     * @param path 路径（不带 /）
     * @param name 文件名
     * @return File
     * @throws IOException
     */
    public static File multipartFile2File(MultipartFile file, String path, String name) throws IOException {
        // 处理路径为空的情况
        if (StrUtil.isEmpty(path)) {
            path = "";
        } else {
            path += "/";
        }
        if (StrUtil.isEmpty(name)) {
            // 文件名为空，设置默认，文件名前增加4位随机字符串，防止重复
            name = RandomUtil.randomString(4) + file.getOriginalFilename();
        }
        String tmpPath = getPath("tmp");
        File dir = new File(String.format("%s/%s", tmpPath, path));
        // 目录不存在，创建目录
        if (!dir.exists()) {
            dir.mkdirs();
        }
        // 将转换后的新文件暂存到临时目录，文件使用完后需删除
        File newFile = new File(String.format("%s/%s%s", tmpPath, path, name));
        BufferedInputStream bis = null;
        BufferedOutputStream bos = null;
        InputStream is = null;
        FileOutputStream fos = null;
        try {
            is = file.getInputStream();
            bis = new BufferedInputStream(is);
            fos = new FileOutputStream(newFile);
            bos = new BufferedOutputStream(fos);
            int bytesRead;
            byte[] buffer = new byte[8192];
            while ((bytesRead = bis.read(buffer, 0, 8192)) >= 0) {
                bos.write(buffer, 0, bytesRead);
            }
            bos.flush();
        } catch (IOException e) {
            if (bos != null) {
                bos.close();
                bos = null;
            }
            if (bis != null) {
                bis.close();
                bis = null;
            }
            if (fos != null) {
                fos.close();
                fos = null;
            }
            if (is != null) {
                is.close();
                is = null;
            }
            // 如果出现异常，删除该文件，需先关闭流
            newFile.delete();
            throw e;
        } finally {
            if (bos != null) bos.close();
            if (bis != null) bis.close();
            if (fos != null) fos.close();
            if (is != null) is.close();
        }
        return newFile;
    }

    public static void main(String[] args) {
        // test files merge
        try {
            File file = fileMerge("6adf0ca5ca7031e998a2757005407f2c", 11, String.format("%s.png", RandomUtil.randomString(6)));
            System.out.println(file.getName());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 系统临时目录中，合并文件
     * @param md5 文件md5
     * @param chunks 切片数量
     * @param newFileFullName 新文件名+后缀
     * @return File
     * @throws IOException
     * @throws BusinessException
     */
    public static File fileMerge(String md5, int chunks, String newFileFullName) throws IOException, BusinessException {
        String tmpPath = getPath("tmp");
        File dir = new File(String.format("%s/%s", tmpPath, md5));
        // 如果不存在或者不为文件夹，直接返回null
        if (!dir.exists() || !dir.isDirectory()) {
            throw new BusinessException(ResultEnums.FAILED.getCode(), "切片文件不存在，请重新上传");
        }
        File[] files = dir.listFiles();
        if (files == null || files.length < chunks) {
            throw new BusinessException(ResultEnums.FAILED.getCode(), "切片文件缺失，请重新上传");
        }
        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        // 合并后新文件
        File newFile = new File(String.format("%s/%s", getPath("static"), newFileFullName));
        File[] toDelFiles = new File[chunks];
        try {
            fos = new FileOutputStream(newFile);
            bos = new BufferedOutputStream(fos);
            FileInputStream fis;
            BufferedInputStream bis;
            byte[] buffer;
            int bytesRead;
            // 循环将切片文件数据写入新文件
            for (int i = 0; i < chunks; i++) {
                File f = new File(String.format("%s/%s/%s", tmpPath, md5, i));
                if (f.isFile() && f.exists()) {
                    fis = new FileInputStream(f);
                    bis = new BufferedInputStream(fis);
                    buffer = new byte[1024];
                    while ((bytesRead = bis.read(buffer, 0, 1024)) >= 0) {
                        bos.write(buffer, 0, bytesRead);
                    }
                    bis.close();
                    fis.close();
                }
                toDelFiles[i] = f;
            }
            bos.flush();
        } catch (IOException e) {
            // 如有异常，删除该文件
            newFile.delete();
        } finally {
            if (bos != null) bos.close();
            if (fos != null) fos.close();
        }
        // 删除切片文件
        for (int i = 0; i < toDelFiles.length; i++) {
            toDelFiles[i].delete();
        }
        // 删除文件夹中的文件后才能删除文件
        dir.delete();
        // 校验文件MD5是否与上传前一致
        if (StrUtil.isNotEmpty(md5)) {
            String newFileMd5 = SecureUtil.md5(newFile);
            if (!md5.equals(newFileMd5)) {
                // 删除该文件
                newFile.delete();
                // 文件校验失败提示
                throw new BusinessException(ResultEnums.FAILED.getCode(), "文件合并失败，请重新上传");
            }
        }
        // 返回合并后的新文件
        return newFile;
    }
}
