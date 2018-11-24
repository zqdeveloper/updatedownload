package com.zq.update;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.support.annotation.DrawableRes;
import android.text.TextUtils;

/**
 * @author :Created by zhangqing on 2018/10/28 12:37
 * @description :
 * @email :1423118197@qq.com
 * @classpath : com.zq.update.UpdateBuilder
 */
public class UpdateBuilder {
    private Updateparams mUpdateparams;

    private UpdateBuilder(Updateparams updateparams) {
        mUpdateparams = updateparams;
    }

    public void startDownLoad() {
        Intent intent = new Intent(mUpdateparams.mContext, UpdateService.class);
        intent.putExtra("apkUrl", mUpdateparams.apkUrl);
        intent.putExtra("filePath", mUpdateparams.filePath);
        intent.putExtra("smallIcon", mUpdateparams.smalllIcon);
        intent.putExtra("largeIcon", mUpdateparams.largeIcon);
        intent.putExtra("title", mUpdateparams.title);
        mUpdateparams.mContext.startService(intent);
    }

    public static Builder with(Context context) {
        return new Builder(context);
    }

    public static class Builder {
        private UpdateBuilder mUpdateBuilder;
        private Context mContext;
        private Updateparams mUpdateparams;

        private Builder(Context context) {
            mContext = context;
            mUpdateparams = new Updateparams(mContext);
        }

        public Builder setmallIcon(int smallIcon) {
            mUpdateparams.smalllIcon = smallIcon;
            return this;
        }

        public Builder setLargeIcon(int largeIcon) {
            mUpdateparams.largeIcon = largeIcon;
            return this;
        }

        public Builder setApkUrl(String apkUrl) {
            mUpdateparams.apkUrl = apkUrl;
            return this;
        }

        public Builder setApkFilePath(String apkFilePath) {
            mUpdateparams.filePath = apkFilePath;
            return this;
        }

        public Builder setTitle(String title) {
            mUpdateparams.title = title;
            return this;
        }

        public UpdateBuilder build() {
            if (mUpdateparams.mContext == null) {
                throw new IllegalArgumentException("context 不能为空!");
            }
            if (TextUtils.isEmpty(mUpdateparams.apkUrl)) {
                throw new IllegalArgumentException("apk下载地址不能为空!");
            }
            if (mUpdateparams.smalllIcon == -1) {
                mUpdateparams.smalllIcon = R.mipmap.ic_launcher;
            }
            if (mUpdateparams.largeIcon == -1) {
                mUpdateparams.largeIcon = R.mipmap.ic_launcher;
            }
            if (TextUtils.isEmpty(mUpdateparams.filePath)) {
                String packageName = mContext.getPackageName();
                mUpdateparams.filePath = Environment.getExternalStorageDirectory() + "/" + packageName + "/" + "git" + ".apk";
            }
            if (TextUtils.isEmpty(mUpdateparams.title)) {
                mUpdateparams.title = mContext.getResources().getString(R.string.app_name);
            }
            mUpdateBuilder = new UpdateBuilder(mUpdateparams);
            return mUpdateBuilder;
        }

        public void startDownLoad() {
            if (mUpdateBuilder == null) {
                mUpdateBuilder = build();
            }
            mUpdateBuilder.startDownLoad();
        }
    }

    private static class Updateparams {
        private Context mContext;
        private int smalllIcon = -1;
        private int largeIcon = -1;
        private String apkUrl = null;
        private String filePath = null;
        private String title = null;

        private Updateparams(Context context) {
            mContext = context;
        }
    }

}
