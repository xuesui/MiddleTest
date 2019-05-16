package com.example.tools.downloadtool;

public interface DownloadListener {
    public void onProgress(int progress);

    public void onSuccess();

    public void onPaused();

    public void onCanceled();

    public void onFailed();
}
//回调接口
