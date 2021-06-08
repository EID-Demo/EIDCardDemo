package com.sunmi.readidcardemo.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.sunmi.eidlibrary.EidCall;
import com.sunmi.eidlibrary.EidConstants;
import com.sunmi.eidlibrary.EidReadCardCallBack;
import com.sunmi.eidlibrary.EidReader;
import com.sunmi.eidlibrary.EidSDK;
import com.sunmi.eidlibrary.IDCardType;
import com.sunmi.pay.hardware.aidl.AidlConstants;
import com.sunmi.pay.hardware.aidlv2.AidlConstantsV2;
import com.sunmi.pay.hardware.aidlv2.readcard.CheckCardCallbackV2;
import com.sunmi.readidcardemo.R;
import com.sunmi.readidcardemo.bean.BaseInfo;
import com.sunmi.readidcardemo.bean.ResultInfo;
import com.sunmi.readidcardemo.net.ReadCardServer;
import com.sunmi.readidcardemo.net.RetrofitWrapper;
import com.sunmi.readidcardemo.utils.ByteUtils;
import com.sunmi.readidcardemo.utils.Utils;
import com.zkteco.android.IDReader.IDCardPhoto;

import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import sunmi.paylib.SunmiPayKernel;

public class MainActivity extends AppCompatActivity implements EidCall {
    public static final String TAG = "hell0";

    @BindView(R.id.state)
    TextView mState;
    @BindView(R.id.version)
    TextView mVer;
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

    private int readType = 0;
    private IsoDep isodep;
    private boolean init;

    private static final String EID_APP_ID = ""; // TODO:  请替换应用appId

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        requestPerm();
        initNfc();
        showVersion();
        connectPayService();
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
            mState.setText("设备不支持NFC，金融设备请使金融读卡");
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (init) {
            try {
                // 释放金融-SDK
                if (Utils.isAppInstalled(this, "com.sunmi.pay.hardware_v3")) {
                    SunmiPayKernel.getInstance().mReadCardOptV2.cancelCheckCard();
                    SunmiPayKernel.getInstance().mReadCardOptV2.cardOff(AidlConstantsV2.CardType.NFC.getValue());
                }
                EidSDK.destroy();
            } catch (Exception e) {
                e.printStackTrace();
            }
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

    @OnClick({R.id.init, R.id.finance_read_card, R.id.clear, R.id.delay, R.id.destroy})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.init:
                if (TextUtils.isEmpty(EID_APP_ID)) {
                    mState.setText("请替换应用appId");
                    Toast.makeText(this, "请替换应用appId", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    //设置就走测试环境，不设置走正式环境
//                    EidSDK.setDebug(EidSDK.TEST_MODE);
                    //初始化；初始化成功后回调onCallData  code=1
                    EidSDK.init(getApplicationContext(), EID_APP_ID, this);
                } catch (Exception e) {
                    mState.setText(e.getMessage());
                }
                break;
            case R.id.finance_read_card:
                if (init) {
                    if (Utils.isAppInstalled(this, "com.sunmi.pay.hardware_v3")) {
                        try {
                            SunmiPayKernel.getInstance().mReadCardOptV2.cancelCheckCard();
                            SunmiPayKernel.getInstance().mReadCardOptV2.cardOff(AidlConstantsV2.CardType.NFC.getValue());
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                        checkCard();
                    } else {
                        mState.setText("当前设备非金融机具");
                        Toast.makeText(this, "当前设备非金融机具！", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    mState.setText("请先初始化SDK");
                    Toast.makeText(this, "请先初始化SDK", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.clear:
                clearData();
                break;
            case R.id.delay:
                if (init) {
                    EidSDK.getDelayTime(this, 3);
                } else {
                    mState.setText("请先初始化SDK");
                    Toast.makeText(this, "请先初始化SDK", Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.destroy:
                if (init) {
                    EidSDK.destroy();
                    init = false;
                } else {
                    mState.setText("请先初始化SDK");
                    Toast.makeText(this, "请先初始化SDK", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void initEidReader() {
        try {
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
                //通过card_id请求识读卡片的信息
                Log.d(TAG, "onCallData: reqId:" + msg);
                runOnUiThread(() -> mRequestId.setText("reqId:" + msg));
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
            //初始化成功
            case 1:
                init = true;
                initEidReader();
            default:
                runOnUiThread(() -> {
                    mState.setText(code + ":" + msg);
                });
                break;
        }
    }

    private void getIDCardInfo(String id) {
        if (!init) {
            runOnUiThread(() -> Toast.makeText(this, "请先初始化", Toast.LENGTH_SHORT).show());
            return;
        }
        runOnUiThread(() -> mRequestId.setText("request_id：" + id));

        if (TextUtils.isEmpty(RetrofitWrapper.URL)){
            mState.setText("请替换成自己的demo服务器地址");
            Toast.makeText(this, "请替换成自己的demo服务器地址", Toast.LENGTH_LONG).show();
            return;
        }
        ReadCardServer.getInstance()
                .parse(id)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ResultInfo>() {
                    @Override
                    public void onCompleted() {
                    }

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
        try {
            Bitmap photo = IDCardPhoto.getIDCardPhoto(imgBytes);
            mPic.setImageBitmap(photo);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ---------------------------------------------- 金融设备读卡 ------------------------------

    private void connectPayService() {
        if (Utils.isAppInstalled(this, "com.sunmi.pay.hardware_v3")) {
            SunmiPayKernel payKernel = SunmiPayKernel.getInstance();
            payKernel.initPaySDK(this, mConnectCallback);
        }
    }


    private SunmiPayKernel.ConnectCallback mConnectCallback = new SunmiPayKernel.ConnectCallback() {
        @Override
        public void onConnectPaySDK() {
            Log.e(TAG, "onConnectPaySDK");
        }

        @Override
        public void onDisconnectPaySDK() {
            Log.e(TAG, "onDisconnectPaySDK");
        }
    };

    private CheckCardCallbackV2.Stub mReadCardCallback = new CheckCardCallbackV2.Stub() {
        @Override
        public void findMagCard(Bundle bundle) throws RemoteException {
            Log.e(TAG, "findMagCard,bundle:" + bundle);
        }

        @Override
        public void findICCard(String atr) throws RemoteException {
            Log.e(TAG, "findICCard, atr:" + atr);
        }

        @Override
        public void findRFCard(String uuid) throws RemoteException {
            Log.e(TAG, "findRFCard, uuid:" + uuid);
            readCard();
        }

        @Override
        public void onError(final int code, final String msg) throws RemoteException {
            Log.e(TAG, "check card error,code:" + code + "message:" + msg);
        }

        @Override
        public void findICCardEx(Bundle bundle) throws RemoteException {
            Log.e(TAG, "findICCard, bundle:" + bundle);
        }

        @Override
        public void findRFCardEx(Bundle bundle) throws RemoteException {
            Log.e(TAG, "findRFCard, bundle:" + bundle);
            //readCard();
        }

        @Override
        public void onErrorEx(Bundle bundle) throws RemoteException {
            Log.e(TAG, "check card error, bundle:" + bundle);
        }
    };

    private void readCard() {
        try {
            eid.readCard(readType, new EidReadCardCallBack() {
                @Override
                public byte[] transceiveTypeB(byte[] bytes) {
                    try {
                        byte[] out = new byte[260];
                        int code = SunmiPayKernel.getInstance().mReadCardOptV2.smartCardExChangePASS(AidlConstants.CardType.NFC.getValue(), bytes, out);
                        if (code < 0) {
                            Log.e(TAG, "读卡失败..code:" + code);
                            return new byte[0];
                        }
                        int len = ByteUtils.unsignedShort2IntBE(out, 0);
                        byte[] valid = Arrays.copyOfRange(out, 2, len + 4);
                        return valid;
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    return new byte[0];
                }

                @Override
                public byte[] transceiveTypeA(byte[] bytes) {
                    try {
                        byte[] out = new byte[255];
                        int code = SunmiPayKernel.getInstance().mReadCardOptV2.transmitApdu(AidlConstants.CardType.NFC.getValue(), bytes, out);
                        if (code < 0) {
                            Log.e(TAG, "读卡失败..code:" + code);
                            return new byte[0];
                        }
                        return Arrays.copyOfRange(out, 0, code);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    return new byte[0];
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 刷卡
     */
    private void checkCard() {
        try {
            SunmiPayKernel.getInstance().mReadCardOptV2.checkCard(AidlConstants.CardType.NFC.getValue(), mReadCardCallback, 60);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
