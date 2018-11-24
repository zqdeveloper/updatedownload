package com.zq.update;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;

import java.io.File;

import static android.support.v4.app.NotificationCompat.VISIBILITY_SECRET;

public class UpdateService extends Service {
    private String apkUrl;
    private String filePath;
    private int smallIcon;
    private int largeIcon;
    private String title;
    private NotificationManager notificationManager;
    private Notification mNotification;


    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            notifyUser(getString(R.string.update_download_failed), getString(R.string.update_download_failed_msg), 0);
            stopSelf();
        }
        checkFilePath(intent);
        apkUrl = intent.getExtras().getString("apkUrl");
        smallIcon = intent.getExtras().getInt("smallIcon");
        largeIcon = intent.getExtras().getInt("largeIcon");
        title = intent.getExtras().getString("title");
        notifyUser(getString(R.string.update_download_start), getString(R.string.update_download_start), 0);
        startDownload();
        return super.onStartCommand(intent, flags, startId);
    }

    private void startDownload() {
        UpdateManager.getInstance().startDownload(apkUrl, filePath, new UpdateDownloadListener() {
            @Override
            public void onStarted() {
                notifyUser(getString(R.string.update_download_start), getString(R.string.update_download_start), 0);
            }

            @Override
            public void onProgressChanged(int progress, String downloadUrl) {
                notifyUser(getString(R.string.update_download_progress), getString(R.string.update_download_progress), progress);
            }

            @Override
            public void onFinished(int completeSize, String downloadUrl) {
                notifyUser(getString(R.string.update_download_finish), getString(R.string.update_download_finish), 100);
                stopSelf();
            }

            @Override
            public void onFailure() {
                notifyUser(getString(R.string.update_download_failed), getString(R.string.update_download_failed_msg), 0);
                stopSelf();
            }
        });
    }

    NotificationChannel notificationChannel = null;

    //更新我们的Notification来告知用户当前下载进度
    private void notifyUser(String result, String reason, int progress) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationChannel == null) {
                notificationChannel = new NotificationChannel("channel_id", "channel_name", NotificationManager.IMPORTANCE_DEFAULT);
                notificationChannel.canBypassDnd();//可否绕过请勿打扰模式
                notificationChannel.enableLights(true);//新消息来了，手机闪光
                notificationChannel.setLockscreenVisibility(VISIBILITY_SECRET);//锁屏显示通知
                notificationChannel.setLightColor(Color.RED);//指定闪光时的灯光颜色
                notificationChannel.canShowBadge();//消息来的时候，桌面图标是否显示消息角标
                notificationChannel.enableVibration(true);//是否允许振动
                notificationChannel.getAudioAttributes();//获取系统通知响铃声音的配置
                notificationChannel.getGroup();//获取通知渠道组
                notificationChannel.setBypassDnd(true);//可否绕过请勿打扰模式
                notificationChannel.setVibrationPattern(new long[]{100, 100, 100});//震动的模式,每100毫秒一次，震动三次
                notificationChannel.shouldShowLights();//是否闪光
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(smallIcon)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), largeIcon))
                .setContentTitle(title);
        if (progress > 0 && progress <= 100) {
            builder.setProgress(100, progress, false);
        } else {
            builder.setProgress(0, 0, false);

        }
        builder.setContentText(progress == 100 ? "下载完成，点击安装" : "正在下载");
        builder.setChannelId("channel_id");
        builder.setAutoCancel(true);
        builder.setWhen(System.currentTimeMillis());
        builder.setTicker(result);
        builder.setDefaults(Notification.FLAG_ONLY_ALERT_ONCE);
        builder.setContentIntent(progress == 100 ? getContentIntent() :
                PendingIntent.getActivity(this, 0,
                        new Intent(), PendingIntent.FLAG_UPDATE_CURRENT));
        builder.setStyle(new NotificationCompat.BigTextStyle());
        mNotification = builder.build();
        notificationManager.notify(0, mNotification);
    }

    private PendingIntent getContentIntent() {

        File apkFile = new File(filePath);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Uri contentUri = FileProvider.getUriForFile(this, getPackageName() + ".fileProvider", apkFile);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
        } else {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return pendingIntent;
    }


    private void checkFilePath(Intent intent) {
        String packageName = this.getPackageName();
        if (intent.getExtras().containsKey("filePath")) {
            filePath = intent.getExtras().getString("filePath", null);
            if (TextUtils.isEmpty(filePath)) {
                filePath = Environment.getExternalStorageDirectory() + "/" + packageName + "/" + packageName + ".apk";
            }
        } else {
            filePath = Environment.getExternalStorageDirectory() + "/" + packageName + "/" + packageName + ".apk";
        }


    }

}
