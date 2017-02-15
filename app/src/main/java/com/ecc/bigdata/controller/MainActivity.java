package com.ecc.bigdata.controller;

import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ecc.bigdata.controller.conf.XResourceClient;
import com.ecc.bigdata.controller.conf.XUIClient;
import com.ecc.bigdata.controller.listener.LoadPagerListener;
import com.ecc.bigdata.controller.util.Utils;

import org.xwalk.core.XWalkPreferences;
import org.xwalk.core.XWalkSettings;
import org.xwalk.core.XWalkView;
import org.xwalk.core.internal.XWalkSettingsInternal;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class MainActivity extends AppCompatActivity {

    @BindView(R.id.xWalkView)
    XWalkView xWalkView;
    XWalkSettings mSettings;

    @BindView(R.id.loadingLayout)
    RelativeLayout loadingLayout;
    @BindView(R.id.btn_retry)
    TextView btnRetry;
    @BindView(R.id.loadFail)
    LinearLayout loadFailLayout;
    private boolean isError;
    private long exitTime;
    XLoadPagerListener loadPagerListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initEnv();
        initWalkView();
        setWalkView();
    }

    private void initEnv() {
        try {
            if (Integer.parseInt(Build.VERSION.SDK) >= 11) {
                getWindow()
                        .setFlags(
                                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setWalkView() {
        loadPagerListener = new XLoadPagerListener();
        xWalkView.setUIClient(new XUIClient(xWalkView, loadPagerListener));
        xWalkView.setResourceClient(new XResourceClient(xWalkView,loadPagerListener));
    }

    private void initWalkView() {
        xWalkView.loadUrl(getResources().getString(R.string.api_url));
        xWalkView.setDrawingCacheEnabled(true);
        //获取setting
        mSettings = xWalkView.getSettings();
        //支持空间导航
        mSettings.setSupportSpatialNavigation(false);
        mSettings.setBuiltInZoomControls(false);
        mSettings.setSupportZoom(false);
        mSettings.setDomStorageEnabled(true);
        mSettings.setCacheMode(XWalkSettingsInternal.LOAD_DEFAULT);
        mSettings.setUseWideViewPort(true);
        mSettings.setLoadWithOverviewMode(true);

        //添加对javascript支持
        XWalkPreferences.setValue("enable-javascript", true);
        //开启调式,支持谷歌浏览器调式
        XWalkPreferences.setValue(XWalkPreferences.REMOTE_DEBUGGING, true);
        //置是否允许通过file url加载的Javascript可以访问其他的源,包括其他的文件和http,https等其他的源
        XWalkPreferences.setValue(XWalkPreferences.ALLOW_UNIVERSAL_ACCESS_FROM_FILE, true);
        //JAVASCRIPT_CAN_OPEN_WINDOW
        XWalkPreferences.setValue(XWalkPreferences.JAVASCRIPT_CAN_OPEN_WINDOW, true);
    }

    @Override
    protected void onResume() {
        if (xWalkView != null) {
            xWalkView.resumeTimers();
            xWalkView.onShow();
        }
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (xWalkView != null) {
            xWalkView.pauseTimers();
            xWalkView.onHide();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (xWalkView != null) {
            xWalkView.onDestroy();
        }
        super.onDestroy();
    }

    @OnClick(R.id.btn_retry)
    public void onClick() {
        loadPagerListener.setLoadOkFlag(true);
        xWalkView.reload(XWalkView.RELOAD_IGNORE_CACHE);
    }

    @Override
    public void onBackPressed() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - exitTime > 2000){
            Toast.makeText(this,"再按一次退出程序",Toast.LENGTH_SHORT).show();
            exitTime = currentTime;
            return;
        }
        super.onBackPressed();
    }
    class XLoadPagerListener extends LoadPagerListener{
        @Override
        public void onPageStarted() {
            loadFailLayout.setVisibility(View.GONE);
            loadingLayout.setVisibility(View.VISIBLE);
        }

        @Override
        public void onPageFinished() {
            if (!getLoadOkFlag()){
                loadingLayout.setVisibility(View.GONE);
                loadFailLayout.setVisibility(View.VISIBLE);
            }else {
                loadFailLayout.setVisibility(View.GONE);
                loadingLayout.setVisibility(View.GONE);
            }
        }

        @Override
        public void onReceivedError() {
            loadingLayout.setVisibility(View.GONE);
            loadFailLayout.setVisibility(View.VISIBLE);
        }
    }
}