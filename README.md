# updatedownload
应用更新组件

1.使用方式，在model的dependencies 的节点下增加如下依赖：

     dependencies {
        implementation fileTree(include: ['*.jar'], dir: 'libs')
        implementation 'com.android.support:appcompat-v7:28.0.0'
        implementation 'com.android.support.constraint:constraint-layout:1.1.3'
        testImplementation 'junit:junit:4.12'
        androidTestImplementation 'com.android.support.test:runner:1.0.2'
        androidTestImplementation 'com.android.support.test.espresso:espresso-core:3.0.2'
        implementation 'com.zq.component:update:1.0.1'
    }
    
  2.在需要更新的地方使用：
      
        UpdateBuilder.with(context)//
                .setTitle("新消息来了")//通知栏显示的消息
                .setApkUrl("apkurl")//apk的地址
                .setLargeIcon(R.mipmap.ic_launcher)//通知栏显示的大图标
                .setmallIcon(R.mipmap.ic_launcher)//通知栏的小图标
                .startDownLoad();
  
  3.等待着下载完成，然后安装吧！
