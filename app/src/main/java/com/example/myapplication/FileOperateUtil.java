package com.example.myapplication;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.text.TextUtils;

import com.zcitc.updatelibrary.R;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.ResponseBody;

/**
 * @ClassName: FileOperateUtil
 * @Description: 文件操作工具类
 */
public class FileOperateUtil {

    public final static String TAG = "FileOperateUtil";

    public final static int ROOT = 0;// 根目录
    public final static int TYPE_IMAGE = 1;// 图片
    public final static int TYPE_THUMBNAIL = 2;// 缩略图
    public final static int TYPE_VIDEO = 3;// 视频
    public final static int TYPE_OTHER = 4;// 其他
    public final static int TYPE_PDF = 6;// 其他
    public final static int TYPE_IMGCACH = 5;// 下载图片类

    /**
     * 获取文件夹路径
     *
     * @param type     文件夹类别
     * @param rootPath 根目录文件夹名字 为业务流水号
     * @return
     */
    public static String getFolderPath(Context context, int type, String rootPath) {
        // 本业务文件主目录
        StringBuilder pathBuilder = new StringBuilder();
        // 添加应用存储路径
        pathBuilder.append(context.getExternalFilesDir(null).getAbsolutePath());
        pathBuilder.append(File.separator);
        // 添加文件总目录
        pathBuilder.append(context.getString(R.string.Files));
        pathBuilder.append(File.separator);
        // 添加当然文件类别的路径
        pathBuilder.append(rootPath);
        pathBuilder.append(File.separator);
        switch (type) {
            case TYPE_IMAGE:
                pathBuilder.append(context.getString(R.string.Image));
                break;
            case TYPE_VIDEO:
                pathBuilder.append(context.getString(R.string.Video));
                break;
            case TYPE_THUMBNAIL:
                pathBuilder.append(context.getString(R.string.Thumbnail));
                break;
            case TYPE_OTHER:
                pathBuilder.append(context.getString(R.string.Other));
                break;
            case TYPE_IMGCACH:
                pathBuilder.append(context.getString(R.string.ImgCach));
                break;
            case TYPE_PDF:
                pathBuilder.append(context.getString(R.string.PDF));
                break;
            default:
                break;
        }
        File f = new File(pathBuilder.toString());
        if (!f.exists()) {
            f.mkdirs();
        }
        return pathBuilder.toString();
    }

    /**
     * 获取目标文件夹内指定后缀名的文件数组,按照修改日期排序
     *
     * @param file    目标文件夹路径
     * @param format  指定后缀名
     * @param content 包含的内容,用以查找视频缩略图
     * @return
     */
    public static List<File> listFiles(String file, final String format, String content) {
        return listFiles(new File(file), format, content);
    }

    public static List<File> listFiles(String file, final String format) {
        return listFiles(new File(file), format, null);
    }

    /**
     * 获取目标文件夹内指定后缀名的文件数组,按照修改日期排序
     *
     * @param file      目标文件夹
     * @param extension 指定后缀名
     * @param content   包含的内容,用以查找视频缩略图
     * @return
     */
    public static List<File> listFiles(File file, final String extension, final String content) {
        File[] files = null;
        if (file == null || !file.exists() || !file.isDirectory())
            return null;
        files = file.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File arg0, String arg1) {
                if (content == null || content.equals(""))
                    return arg1.endsWith(extension);
                else {
                    return arg1.contains(content) && arg1.endsWith(extension);
                }
            }
        });
        if (files != null) {
            List<File> list = new ArrayList<File>(Arrays.asList(files));
            sortList(list, false);
            return list;
        }
        return null;
    }

    /**
     * 根据修改时间为文件列表排序
     *
     * @param list 排序的文件列表
     * @param asc  是否升序排序 true为升序 false为降序
     */
    public static void sortList(List<File> list, final boolean asc) {
        // 按修改日期排序
        Collections.sort(list, new Comparator<File>() {
            public int compare(File file, File newFile) {
                if (file.lastModified() > newFile.lastModified()) {
                    if (asc) {
                        return 1;
                    } else {
                        return -1;
                    }
                } else if (file.lastModified() == newFile.lastModified()) {
                    return 0;
                } else {
                    if (asc) {
                        return -1;
                    } else {
                        return 1;
                    }
                }

            }
        });
    }

    /**
     * @param extension 后缀名 如".jpg"
     * @return
     */
    public static String createFileName(String extension) {
        DateFormat format = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
        // 转换为字符串
        String formatDate = format.format(new Date());
        // 查看是否带"."
        if (!extension.startsWith("."))
            extension = "." + extension;
        return formatDate + extension;
    }

    /**
     * 删除缩略图 同时删除源图或源视频
     *
     * @param thumbPath 缩略图路径
     * @return
     */
    public static boolean deleteThumbFile(String thumbPath, Context context) {
        boolean flag = false;

        File file = new File(thumbPath);
        if (!file.exists()) { // 文件不存在直接返回
            return flag;
        }

        flag = file.delete();
        // 源文件路径
        String sourcePath = thumbPath.replace(context.getString(R.string.Thumbnail), context.getString(R.string.Image));
        file = new File(sourcePath);
        if (!file.exists()) { // 文件不存在直接返回
            return flag;
        }
        flag = file.delete();
        return flag;
    }

    /**
     * 删除源图或源视频 同时删除缩略图
     *
     * @param sourcePath 缩略图路径
     * @return
     */
    public static boolean deleteSourceFile(String sourcePath, Context context) {
        boolean flag = false;

        File file = new File(sourcePath);
        if (!file.exists()) { // 文件不存在直接返回
            return flag;
        }

        flag = file.delete();
        // 缩略图文件路径
        String thumbPath = sourcePath.replace(context.getString(R.string.Image), context.getString(R.string.Thumbnail));
        file = new File(thumbPath);
        if (!file.exists()) { // 文件不存在直接返回
            return flag;
        }
        flag = file.delete();
        return flag;
    }


    /**
     *  *根据路径删除指定的目录或文件，无论存在与否
     *  *@param sPath  要删除的目录或文件
     *  *@return 删除成功返回 true，否则返回 false。
     *  
     */
    public static boolean DeleteFolder(String sourcePath, Context context) {
        boolean flag = false;
        File file = new File(sourcePath);
        // 判断目录或文件是否存在 
        if (!file.exists()) {
            // 不存在返回 false 
            return flag;
        } else {
            // 判断是否为文件 
            if (file.isFile()) {
                // 为文件时调用删除文件方法 
                flag = deleteFile(sourcePath);
            } else {
                // 为目录时调用删除目录方法 
                flag = deleteDirectory(sourcePath);
            }

        }
        return flag;

    }

    /**
     * 删除单个文件
     *
     * @param sourcePath 被删除文件的文件名
     * @return 单个文件删除成功返回true，否则返回false
     */
    public static boolean deleteFile(String sourcePath) {
        boolean flag = false;
        File file = new File(sourcePath);
        // 路径为文件且不为空则进行删除
        if (file.isFile() && file.exists()) {
            file.delete();
            flag = true;
        }
        return flag;
    }

    /**
     * 删除目录（文件夹）以及目录下的文件
     *
     * @param sourcePath 被删除目录的文件路径
     * @return 目录删除成功返回true，否则返回false
     */
    public static boolean deleteDirectory(String sourcePath) {
        boolean flag = false;
        //如果sPath不以文件分隔符结尾，自动添加文件分隔符
        if (!sourcePath.endsWith(File.separator)) {
            sourcePath = sourcePath + File.separator;
        }
        File dirFile = new File(sourcePath);
        //如果dir对应的文件不存在，或者不是一个目录，则退出
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        flag = true;
        //删除文件夹下的所有文件(包括子目录)
        File[] files = dirFile.listFiles();
        for (int i = 0; i < files.length; i++) {
            //删除子文件
            if (files[i].isFile()) {
                flag = deleteFile(files[i].getAbsolutePath());
                if (!flag) break;
            } //删除子目录
            else {
                flag = deleteDirectory(files[i].getAbsolutePath());
                if (!flag) break;
            }
        }
        if (!flag) return false;
        //删除当前目录
        if (dirFile.delete()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 保存文件
     *
     * @param body
     * @param fileName
     * @param savefolder
     * @return
     */
    public static boolean writeResponseBodyToDisk(ResponseBody body, String fileName, String savefolder, OnProgressRefresh onProgressRefresh) {
        return writeResponseBodyToDisk(body, fileName, "", savefolder, onProgressRefresh);
    }

    public static boolean writeResponseBodyToDisk(ResponseBody body, String fileName, String fileMD5, String savefolder, OnProgressRefresh onProgressRefresh) {
        try {
            // todo change the file location/name according to your needs
            onProgressRefresh.onStart();

            File futureStudioIconFile = new File(savefolder, fileName);

            if (!TextUtils.isEmpty(fileMD5)) {
                //检查文件md5
                if (futureStudioIconFile.exists()) {
                    String md5 = fileToMD5(futureStudioIconFile);
                    if (fileMD5.toLowerCase().equals(md5)) {
                        onProgressRefresh.onSuccess(futureStudioIconFile.getAbsolutePath());
                        return true;
                    }
                }
            }

            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                byte[] fileReader = new byte[4096];
                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;
                inputStream = body.byteStream();
                outputStream = new FileOutputStream(futureStudioIconFile);
                while (true) {
                    int read = inputStream.read(fileReader);
                    if (read == -1) break;
                    outputStream.write(fileReader, 0, read);
                    fileSizeDownloaded += read;
//                    Log.d("writeResponseBodyToDisk", "file download: " + fileSizeDownloaded + " of " + fileSize);
                    onProgressRefresh.onProgress(fileSizeDownloaded * 100 / fileSize);
                }
                outputStream.flush();
                onProgressRefresh.onSuccess(futureStudioIconFile.getAbsolutePath());
                return true;
            } catch (IOException e) {
                onProgressRefresh.onFail();
                return false;
            } finally {
                if (inputStream != null)
                    inputStream.close();
                if (outputStream != null)
                    outputStream.close();
            }
        } catch (IOException e) {
            onProgressRefresh.onFail();
            return false;
        }
    }

    private static String fileToMD5(File file) {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file); // Create an FileInputStream instance according to the filepath
            byte[] buffer = new byte[1024]; // The buffer to read the file
            MessageDigest digest = MessageDigest.getInstance("MD5"); // Get a MD5 instance
            int numRead = 0; // Record how many bytes have been read
            while (numRead != -1) {
                numRead = inputStream.read(buffer);
                if (numRead > 0)
                    digest.update(buffer, 0, numRead); // Update the digest
            }
            byte[] md5Bytes = digest.digest(); // Complete the hash computing
            return convertHashToString(md5Bytes); // Call the function to convert to hex digits
        } catch (Exception e) {
            return null;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close(); // Close the InputStream
                } catch (Exception e) {
                }
            }
        }
    }

    private static String convertHashToString(byte[] hashBytes) {
        StringBuilder returnVal = new StringBuilder();
        for (byte hashByte : hashBytes) {
            returnVal.append(Integer.toString((hashByte & 0xff) + 0x100, 16).substring(1));
        }
        return returnVal.toString().toLowerCase();
    }

    public interface OnProgressRefresh {
        void onStart();

        void onProgress(long p);

        void onFail();

        void onSuccess(String path);
    }

    public static Bitmap add2Bitmap(Bitmap first, Bitmap second) {
        if (null == first || null == second) {
            return null;
        } else {
            int width = first.getWidth() + second.getWidth();
            int height = Math.max(first.getHeight(), second.getHeight());
            Bitmap result = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

            Canvas canvas = new Canvas(result);
            canvas.drawBitmap(first, 0, 0, null);
            canvas.drawBitmap(second, first.getWidth(), 0, null);
            return result;
        }
    }

    public static Bitmap getphoto(String Path, int SampleSize) {
        File mFile = new File(Path);
        // 若该文件存在
        if (mFile.exists()) {
            Bitmap bitmap = getimage(Path, SampleSize);
            return bitmap;
        }
        return null;
    }

    public static Bitmap getphoto(String Path) {
        File mFile = new File(Path);
        // 若该文件存在
        if (mFile.exists()) {
            Bitmap bitmap = getimage(Path);
            return bitmap;
        }
        return null;
    }

    private static Bitmap getimage(String srcPath) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        //开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts); //此时返回bm为空

        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        //横拍 480*640 分辨率
        float hh = 480f;//这里设置高度为800f
        float ww = 640f;//这里设置宽度为480f
        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        if (w > h && w > ww) {//如果宽度大的话根据宽度固定大小缩放
            be = (int) (newOpts.outWidth / ww);
        } else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放
            be = (int) (newOpts.outHeight / hh);
        }
        if (be <= 0)
            be = 1;

        newOpts.inSampleSize = be;//设置缩放比例
        //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
        return compressImage(bitmap);//压缩好比例大小后再进行质量压缩

    }

    private static Bitmap getimage(String srcPath, int SampleSize) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        //开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts); //此时返回bm为空
        newOpts.inJustDecodeBounds = false;
        newOpts.inSampleSize = SampleSize;//设置缩放比例
        //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
        return compressImage(bitmap);//压缩好比例大小后再进行质量压缩

    }

    private static Bitmap compressImage(Bitmap image) {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 100, baos);//质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
        int options = 100;
        while (baos.toByteArray().length / 1024 > 100) {    //循环判断如果压缩后图片是否大于100kb,大于继续压缩
            baos.reset();//重置baos即清空baos
            image.compress(Bitmap.CompressFormat.JPEG, options, baos);//这里压缩options%，把压缩后的数据存放到baos中
            options -= 10;//每次都减少10
        }
        if (null != image && !image.isRecycled()) {
            image.recycle();
        }
        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());//把压缩后的数据baos存放到ByteArrayInputStream中
        Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);//把ByteArrayInputStream数据生成图片
        return bitmap;
    }
}