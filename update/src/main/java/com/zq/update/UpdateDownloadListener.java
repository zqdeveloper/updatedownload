package com.zq.update;

public interface UpdateDownloadListener {
    public void onStarted();

    public void onProgressChanged(int progress, String downloadUrl);

    public void onFinished(int completeSize, String downloadUrl);

    public void onFailure();
}
