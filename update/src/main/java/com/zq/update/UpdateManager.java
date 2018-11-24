package com.zq.update;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

public class UpdateManager {
    private static UpdateManager updateManager;
    private ThreadPoolExecutor threadPoolExecutor;
    private UpdateDownloadRequest request;

    private UpdateManager() {
        threadPoolExecutor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
    }

    static {
        updateManager = new UpdateManager();
    }

    public static UpdateManager getInstance() {
        return updateManager;
    }

    public void startDownload(String url, String localFilePath, UpdateDownloadListener listener) {
        if (request != null) {
            return;
        }
        checkLocalFilePath(localFilePath);
        //开始真正的去下载文件
        request = new UpdateDownloadRequest(url, localFilePath, listener);
        Future<?> future = threadPoolExecutor.submit(request);

    }

    //用来检查文件路径是否已经存在
    private void checkLocalFilePath(String path) {
        File dir = new File(path.substring(0, path.lastIndexOf("/") + 1));
        if (!dir.exists()) {
            dir.mkdir();
        }
        File file = new File(path);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();

            }
        }
    }
}
