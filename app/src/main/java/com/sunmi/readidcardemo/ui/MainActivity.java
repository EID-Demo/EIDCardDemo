package com.sunmi.readidcardemo.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.sunmi.eidlibrary.EidCall;
import com.sunmi.eidlibrary.EidConstants;
import com.sunmi.eidlibrary.EidPic;
import com.sunmi.eidlibrary.EidReadCardCallBack;
import com.sunmi.eidlibrary.EidReader;
import com.sunmi.eidlibrary.EidSDK;
import com.sunmi.eidlibrary.IDCardType;
import com.sunmi.readidcardemo.R;
import com.sunmi.readidcardemo.net.ReadCardServer;
import com.sunmi.readidcardemo.bean.BaseInfo;
import com.sunmi.readidcardemo.bean.ResultInfo;

import java.io.File;
import java.io.IOException;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity implements EidCall {
    public static final String TAG = "hell0";

    @BindView(R.id.state)
    TextView mState;
    @BindView(R.id.version)
    TextView mVer;
    @BindView(R.id.read)
    Button mRead;
    @BindView(R.id.delay)
    Button mDelay;
    @BindView(R.id.name)
    TextView mName;
    @BindView(R.id.gender)
    TextView mGender;
    @BindView(R.id.race)
    TextView mRace;
    @BindView(R.id.pic)
    ImageView mPic;
    @BindView(R.id.date)
    TextView mDate;
    @BindView(R.id.address)
    TextView mAddress;
    @BindView(R.id.number)
    TextView mNumber;
    @BindView(R.id.office)
    TextView mOffice;
    @BindView(R.id.start)
    TextView mStart;
    @BindView(R.id.end)
    TextView mEnd;
    @BindView(R.id.appeidcode)
    TextView mAppEidCode;
    @BindView(R.id.dn)
    TextView mDn;
    @BindView(R.id.request_id)
    TextView mRequestId;

    private EidReader eid;
    private PendingIntent pi;
    private NfcAdapter nfcAdapter;

    private String fileNameBase = "/sdcard/eidSunmi";
    private int readType = 0;
    private IsoDep isodep;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        requestPerm();
        initNfc();
        showVersion();
        initEidReader();
    }

    private void showVersion() {
        mVer.setText(String.format("商米SDK.Ver:%s, 读卡模块Ver:%s", EidSDK.getSunmiEidSDKVersion(), EidSDK.getEidSDKVersion()));
    }

    /**
     * Android 标准初始化
     */
    private void initNfc() {
        Log.e("TAG", "initNfc  1 ");
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            mState.setText("设备不支持NFC");
            return;
        }
        if (!nfcAdapter.isEnabled()) {
            mState.setText("请在系统设置中先启用NFC功能");
            return;
        }
        pi = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            Log.e("tag", "onResume nfcAdapter  " + nfcAdapter);
            if (null != nfcAdapter) {
                nfcAdapter.enableForegroundDispatch(this, pi, null, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (null != nfcAdapter) {
            nfcAdapter.disableForegroundDispatch(this);
        }
    }

    private void requestPerm() {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_PHONE_STATE,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_FINE_LOCATION
                        }, 81);
            }
        }
    }


    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        if (requestCode == 81 && grantResults != null && grantResults.length > 0) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {// Permission Granted
//                finish();
                Toast.makeText(this, "无所需权限,请在设置中添加权限", Toast.LENGTH_LONG).show();
            }
        }
    }

    @OnClick({R.id.read, R.id.delay})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.read:
                clearData();
                break;
            case R.id.delay:
                EidSDK.getDelayTime(this, 3);
                break;
        }
    }

    private void initEidReader() {
        try {
            EidPic.init(this, fileNameBase);
            eid = EidSDK.getEidReaderForNfc(1, this);
        } catch (Exception e) {
            mState.setText(e.getMessage());
        }
    }

    @Override
    public void onCallData(int code, String msg) {
        switch (code) {
            case EidConstants.READ_CARD_START:
                runOnUiThread(() -> {
                    mState.setText("开始读卡，请勿移动");
                });
                Log.i(TAG, "开始读卡，请勿移动");
                break;
            case EidConstants.READ_CARD_SUCCESS:
                closeNFCReader();//电子身份证需要关闭
                Log.e("TAG", "正在获取身份信息，请稍等...");
                runOnUiThread(() -> {
                    mState.setText("正在获取身份信息，请稍等...");
                });
                File file = new File(fileNameBase, "zp.bmp");
                if (file.exists()) {
                    file.deleteOnExit();
                }
                //通过card_id请求识读卡片的信息
                getIDCardInfo(msg);
                break;
            case EidConstants.READ_CARD_FAILED:
                closeNFCReader();//电子身份证需要关闭
                Log.i(TAG, String.format(Locale.getDefault(), "读卡错误,请重新贴卡：%s", msg));
                runOnUiThread(() -> {
                    mState.setText(String.format(Locale.getDefault(), "读卡错误,请重新贴卡：%s", msg));
                });
                break;
            case EidConstants.READ_CARD_DELAY:
                Log.e("TAG", String.format(Locale.getDefault(), "延迟 %sms", msg));
                runOnUiThread(() -> {
                    mState.setText(String.format(Locale.getDefault(), "延迟 %sms", msg));
                });
                break;
            default:
                runOnUiThread(() -> {
                    mState.setText(code + ":" + msg);
                });
                break;
        }
    }

    private void getIDCardInfo(String id) {
        runOnUiThread(() -> mRequestId.setText("request_id：" + id));
        ReadCardServer.getInstance()
                .parse(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ResultInfo>() {
                    @Override
                    public void onCompleted() {}

                    @Override
                    public void onError(Throwable e) {
                        mState.setText("解析失败，请重试");
                        e.printStackTrace();
                    }

                    @Override
                    public void onNext(ResultInfo result) {
                        if (result.res == 0) {
                            Log.i(TAG, "onNext: " + result.toString());
                            mState.setText("读卡成功，业务状态：" + result.res + ":" + result.errMsg);
                            parseData(result);
                        } else {
                            Log.i(TAG, "onNext: " + result.toString());
                            mState.setText("读卡失败，请重试(" + result.res + ":" + result.errMsg + ")");
                        }
                    }
                });
    }

    private void parseData(ResultInfo data) {
        try {
            BaseInfo info = data.info;
            mName.setText(String.format("姓名：%s", info.name));
            mGender.setText(String.format("性别：%s", info.sex));
            mRace.setText(String.format("民族：%s", info.nation));
            mDate.setText(String.format("出生年月：%s", info.birthDate));
            mAddress.setText(String.format("地址：%s", info.address));
            mNumber.setText(String.format("身份证号码：%s", info.idnum));
            mOffice.setText(String.format("签发机关：%s", info.signingOrganization));
            mStart.setText(String.format("有效起始时间：%s", info.beginTime));
            mEnd.setText(String.format("有效结束时间：%s", info.endTime));
            mAppEidCode.setText(String.format("appeidcode：%s", data.appeidcode));
            mDn.setText(String.format("DN码：%s", data.dn));
            String picture = data.picture;
            decodePic(picture);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("SetTextI18n")
    private void clearData() {
        mState.setText("请读卡");
        mRequestId.setText("request_id:");
        mName.setText("姓名：");
        mGender.setText("性别：");
        mRace.setText("民族：");
        mDate.setText("出生年月：");
        mAddress.setText("地址：");
        mNumber.setText("身份证号码：");
        mOffice.setText("签发机关：");
        mStart.setText("有效起始时间：");
        mEnd.setText("有效结束时间：");
        mAppEidCode.setText("appeidcode：");
        mDn.setText("DN码：");
        mPic.setImageDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(1, 1, 1, "通用卡类型");
        menu.add(2, 2, 2, "身份证");
        menu.add(3, 3, 3, "电子身份证");
        return true;
    }

    /**
     * 读取类型 通用类型{@link IDCardType#CARD}，身份证{@link IDCardType#IDCARD}，电子证照{@link IDCardType#ECCARD}
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                readType = IDCardType.CARD;
                mState.setText("请读卡");
                break;
            case 2:
                readType = IDCardType.IDCARD;
                mState.setText("请读身份证");
                break;
            case 3:
                readType = IDCardType.ECCARD;
                mState.setText("请读电子身份证");
                break;
        }
        return true;
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d(TAG, "onNewIntent: " + intent.getAction());
        if (readType == IDCardType.CARD || readType == IDCardType.IDCARD) {
            Log.d(TAG, "onNewIntent: 普通身份证或通用类型");
            eid.nfcReadCard(intent);
        } else if (readType == IDCardType.ECCARD) {
            Log.d(TAG, "onNewIntent: 电子身份证");
            try {
                Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                try {
                    isodep = IsoDep.get(tagFromIntent);
                    isodep.connect();
                    if (isodep.isConnected()) {
                        eid.readCard(IDCardType.ECCARD, new EidReadCardCallBack() {
                            @Override
                            public byte[] transceiveTypeB(byte[] data) {
                                return data;
                            }

                            @Override
                            public byte[] transceiveTypeA(byte[] data) {
                                byte[] outData = new byte[data.length];
                                try {
                                    outData = isodep.transceive(data);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                return outData;
                            }
                        });
                    } else {
                        isodep.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void closeNFCReader() {
        if (isodep != null) {
            try {
                isodep.close();
                isodep = null;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void decodePic(String imgBytes) {
        EidPic.decodePic(imgBytes);
        Glide.with(MainActivity.this)
                .load(new File(fileNameBase, "zp.bmp"))
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(mPic);
    }
}
