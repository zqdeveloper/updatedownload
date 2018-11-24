package com.zq.update;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;

public class UpdateDownloadRequest implements Runnable {
    private String downloadUrl;
    private String localFilePath;
    private UpdateDownloadListener downloadListener;

    private boolean isDownloading = false;
    private long currentLength;
    private DownloadRequestHandler downloadRequestHandler;

    public UpdateDownloadRequest(String downloadUrl, String localFilePath, UpdateDownloadListener updateDownloadListener) {
        this.downloadUrl = downloadUrl;
        this.localFilePath = localFilePath;
        this.downloadListener = updateDownloadListener;
        this.isDownloading = true;
        this.downloadRequestHandler = new DownloadRequestHandler();
    }

    private void makeRequest() throws IOException, InterruptedException {
        if (!Thread.currentThread().isInterrupted()) {
            try {
                URL url = new URL(downloadUrl);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5 * 1000);
                connection.setRequestProperty("Collection", "Keep-Alive");
                connection.connect();//阻塞当前线程，放入子线程执行
                currentLength = connection.getContentLength();
                if (!Thread.currentThread().isInterrupted()) {
                    downloadRequestHandler.sendResponseMessage(connection.getInputStream());
                }
            } catch (IOException e) {
                throw e;
            }
        }
    }

    private String getTwoPointFloatStr(float value) {
        DecimalFormat fnum = new DecimalFormat("0.00");
        return fnum.format(value);
    }

    public enum FailureCode {
        UnKnownHost, Socket, SocketTimeOut, ConnectionTimeOut, IO, HttpResponse, JSON, Interrupted
    }

    @Override
    public void run() {
        try {
            makeRequest();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private class DownloadRequestHandler {
        protected static final int SUCCESS_MESSAGE = 0;
        protected static final int FAILURE_MESSAGE = 1;
        protected static final int START_MESSAGE = 2;
        protected static final int FINISH_MESSAGE = 3;
        protected static final int NETWORK_OFF = 4;
        protected static final int PROGRESS_CHANGED = 5;

        private int mCompleteSize = 0;
        private int progress = 0;
        private Handler handler;

        public DownloadRequestHandler() {
            handler = new Handler(Looper.getMainLooper()) {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    handleSelfMessage(msg);
                }
            };
        }

        protected void sendFinishMessage() {
            sendMessage(obtainmessage(FINISH_MESSAGE, null));
        }

        public void sendProgressChangedMessage(int progress) {
            sendMessage(obtainmessage(PROGRESS_CHANGED, new Object[]{progress}));
        }

        protected void sendFailureMessage(FailureCode failureCode) {
            sendMessage(obtainmessage(FAILURE_MESSAGE, new Object[]{failureCode}));
        }

        protected void sendMessage(Message message) {
            if (handler != null) {
                handler.sendMessage(message);
            } else {
                handleSelfMessage(message);
            }
        }

        protected Message obtainmessage(int responseMessage, Object response) {
            Message message = null;
            if (handler != null) {
                message = handler.obtainMessage(responseMessage, response);
            } else {
                message = Message.obtain();
                message.what = responseMessage;
                message.obj = response;
            }
            return message;
        }

        public void handleFailureMessage(FailureCode failureCode) {
            onFailure(failureCode);
        }

        public void handleProgressChangedMessage(Integer progress) {
            downloadListener.onProgressChanged(progress, "");
        }

        public void onFinish() {
            downloadListener.onFinished(mCompleteSize, "");
        }

        public void onFailure(FailureCode failureCode) {
            downloadListener.onFailure();
        }

        //真正的文件下载方法
        public void sendResponseMessage(InputStream inputStream) {
            RandomAccessFile randomAccessFile = null;
            mCompleteSize = 0;
            try {
                byte[] buffer = new byte[1024];
                int length = -1;
                int limit = 0;
                randomAccessFile = new RandomAccessFile(localFilePath, "rwd");
                while ((length = inputStream.read(buffer)) != -1) {
                    if (isDownloading) {
                        randomAccessFile.write(buffer, 0, length);
                        mCompleteSize += length;
                        if (mCompleteSize <= currentLength) {
                            progress = (int) Float.parseFloat(getTwoPointFloatStr(mCompleteSize * 100f / currentLength));
                            if (limit % 100 == 0 || progress == 100) {
                                sendProgressChangedMessage(progress);
                            }
                            limit++;
                        }
                    }
                }
                sendFinishMessage();
            } catch (IOException e) {
                sendFailureMessage(FailureCode.IO);
            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    if (randomAccessFile != null) {
                        randomAccessFile.close();
                    }
                } catch (IOException e) {
                    sendFailureMessage(FailureCode.IO);
                }
            }
        }

        protected void handleSelfMessage(Message message) {
            Object[] response;
            switch (message.what) {
                case FAILURE_MESSAGE:
                    response = (Object[]) message.obj;
                    handleFailureMessage((FailureCode) response[0]);
                    break;
                case PROGRESS_CHANGED:
                    response = (Object[]) message.obj;
                    handleProgressChangedMessage(((Integer) response[0]).intValue());
                    break;
                case FINISH_MESSAGE:
                    onFinish();
                    break;
            }
        }
    }
}
