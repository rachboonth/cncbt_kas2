package com.example.android.cncbt_kas2;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.ToneGenerator;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import com.example.android.cnccommander.C0013R;

public class MainActivity extends Activity implements OnSharedPreferenceChangeListener {
    public static final String DEVICE_NAME = "device_name";
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_TOAST = 5;
    public static final int MESSAGE_WRITE = 3;
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    public static final String TOAST = "toast";
    private static final int[] idArray;
    public static com.example.android.cncbt_kas2.BluetoothRfcommClient mRfcommClient;
    String MacAddress;
    String TAG;
    public String addressMAC;
    private boolean autoConnect;
    private Button[] bt;
    private String[] btStrCmd;
    private ArrayList<Integer> bufferList;
    Builder builder;
    private int buttonFeedback;
    private boolean commandCodeDisplay;
    private boolean commentDisplay;
    boolean connectOnce;
    private int droDisplay;
    private int droResolution;
    private Editor editor;
    private String fileToRead;
    private boolean grblReset;
    InputMethodManager imm;
    EditText inputCmd;
    EditText inputName;
    private boolean isStreaming;
    private LinearLayout layoutButtons;
    private LinearLayout layoutDRO;
    private LinearLayout layoutGRBL;
    long longPressTimeout;
    private ListView lv;
    private BluetoothAdapter mBluetoothAdapter;
    private Button mButtonLoad;
    private Button mButtonReset;
    private TextView mButtonStatus3;
    private Button mButtonStreamFile;
    private Button mButtonTxtSend;
    private String mConnectedDeviceName;
    private final Handler mHandler;
    private MenuItem mItemAbout;
    private MenuItem mItemConnect;
    private MenuItem mItemOptions;
    private MenuItem mItemResetOpt;
    private ImageButton mOptionButton;
    private TextView mResponse;
    private TextView mTxtStatus;
    EditText mTxtToSend;
    private long mUpdatePeriod;
    private Timer mUpdateTimer;
    private boolean noDRO;
    ColorStateList oldColor;
    ColorStateList oldColor2;
    private boolean optimize;
    private boolean pause;
    private SharedPreferences prefs;
    byte[] qMark;
    String readMessage;
    public boolean responseAvailable;
    int selectedButton;
    ToneGenerator tg;
    private int timeoutMax;
    TextView txtCmd;
    private TextView txtDRO_X;
    private TextView txtDRO_Xm;
    private TextView txtDRO_Y;
    private TextView txtDRO_Ym;
    private TextView txtDRO_Z;
    private TextView txtDRO_Zm;
    TextView txtName;
    private TextView txtSTAT;
    View view;

    /* renamed from: om.example.android.cncbt_kas2.14 */
    class AnonymousClass14 implements Runnable {
        private final /* synthetic */ String val$comment;

        AnonymousClass14(String str) {
            val$comment = str;
        }

        public void run() {
           mResponse.append("\n" + val$comment);
        }
    }

    /* renamed from: om.example.android.cncbt_kas2.15 */
    class AnonymousClass15 implements Runnable {
        private final /* synthetic */ String val$sent;

        AnonymousClass15(String str) {
            val$sent = str;
        }

        public void run() {
           mResponse.append("\n" + val$sent);
        }
    }

    /* renamed from: om.example.android.cncbt_kas2.16 */
    class AnonymousClass16 implements Runnable {
        private final /* synthetic */ String val$sent;

        AnonymousClass16(String str) {
            val$sent = str;
        }

        public void run() {
           mResponse.append("\n" + val$sent);
        }
    }

    /* renamed from: om.example.android.cncbt_kas2.1 */
    class C00001 extends Handler {
        C00001() {
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case REQUEST_CONNECT_DEVICE /*1*/:
                    switch (msg.arg1) {
                        case 0:
                            ToneGenerator tg2 = new ToneGenerator(MESSAGE_TOAST, 80);
                            tg2.startTone(24);
                            tg2.release();
                           // C0013R.string.title_not_connected;
                           mTxtStatus.setTextColor(Color.rgb(255, 0, 0));
                           mTxtStatus.setText(R.string.title_not_connected);
                        case REQUEST_ENABLE_BT /*2*/:
                           mTxtStatus.setTextColor(Color.rgb(255, 0, 0));
                           mTxtStatus.setText(R.string.title_connecting);
                        case MESSAGE_WRITE /*3*/:
                           mTxtStatus.setTextColor(Color.rgb(0, 255, 0));
                           mTxtStatus.setText(R.string.title_connected_to);
                           mTxtStatus.append(" " + mConnectedDeviceName);
                           connectOnce = false;
                           mResponse.append("$10=3\n");
                        default:
                    }
                case REQUEST_ENABLE_BT /*2*/:
                   readMessage = (String) msg.obj;
                    if (readMessage.contains("MPos:") ||readMessage.contains("WPos:")) {
                        if (droDisplay != MESSAGE_WRITE) {
                           parseStatus(readMessage);
                        } else {
                           mResponse.setText(readMessage);
                        }
                    } else if (readMessage.contains("['$' for help]")) {
                       grblReset = true;
                       mResponse.setText("** GRBL reset **    " +readMessage);
                       mButtonStreamFile.setText("Start");
                       mButtonStreamFile.setTextColor(oldColor2);
                       enableButton(true);
                    } else if (readMessage.contains("error") ||readMessage.toLowerCase(Locale.US).startsWith("alarm")) {
                       mResponse.setTextColor(0x65536);
                       mResponse.append("\n");
                       isStreaming = false;
                       responseAvailable = false;
                       bufferList.clear();
                       mButtonStreamFile.setText("Start");
                       mButtonStreamFile.setTextColor(oldColor2);
                       enableButton(true);
                       mResponse.append("\n" +readMessage);
                    } else {
                       mResponse.setTextColor(oldColor);
                        if (readMessage.contains("ok")) {
                           responseAvailable = true;
                            if (optimize &&isStreaming &&bufferList.size() > 0) {
                               bufferList.remove(0);
                            }
                            if (commandCodeDisplay) {
                               mResponse.append("*" +readMessage);
                                return;
                            }
                            return;
                        }
                        if (readMessage.contains("[Pgm End]")) {
                           tg.startTone(24);
                           mResponse.append("\n");
                        }
                        if (isStreaming) {
                           mResponse.append("\n");
                        }
                       mResponse.append(readMessage);
                    }
                case MESSAGE_DEVICE_NAME /*4*/:
                   mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected to " +mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                case MESSAGE_TOAST /*5*/:
                    Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST), Toast.LENGTH_SHORT).show();
                default:
            }
        }
    }

    /* renamed from: om.example.android.cncbt_kas2.2 */
    class C00012 implements OnClickListener {
        C00012() {
        }

        public void onClick(View v) {
            if (buttonFeedback == REQUEST_ENABLE_BT) {
               tg.startTone(24);
            } else if (buttonFeedback == MESSAGE_DEVICE_NAME) {
               mOptionButton.performHapticFeedback(REQUEST_CONNECT_DEVICE);
            }
           openOptionsMenu();
        }
    }

    /* renamed from: om.example.android.cncbt_kas2.3 */
    class C00023 implements DialogInterface.OnClickListener {
        C00023() {
        }

        public void onClick(DialogInterface dialog, int which) {
           getWindow().setSoftInputMode(MESSAGE_WRITE);
            String name =inputName.getText().toString();
            String cmd =inputCmd.getText().toString();
           editor =prefs.edit();
            if (name.equals("")) {
                name = "B" + (selectedButton + REQUEST_CONNECT_DEVICE);
            }
           editor.putString("btn_name" +selectedButton, name);
           editor.putString("ButtonStrCmd" +selectedButton, cmd);
           bt[selectedButton].setText(name);
           btStrCmd[selectedButton] = cmd;
           editor.commit();
        }
    }

    /* renamed from: om.example.android.cncbt_kas2.4 */
    class C00034 implements DialogInterface.OnClickListener {
        C00034() {
        }

        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
        }
    }

    /* renamed from: om.example.android.cncbt_kas2.5 */
    class C00045 implements OnTouchListener {
        private final /* synthetic */ AlertDialog val$ad;
        private final /* synthetic */ int val$b;

        C00045(int i, AlertDialog alertDialog) {
            val$b = i;
            val$ad = alertDialog;
        }

        public boolean onTouch(View v, MotionEvent event) {
            if (v.isPressed() && event.getAction() == REQUEST_CONNECT_DEVICE) {
                v.performClick();
                if (buttonFeedback == REQUEST_ENABLE_BT) {
                   tg.startTone(24);
                } else if (buttonFeedback == MESSAGE_DEVICE_NAME) {
                   bt[val$b].performHapticFeedback(REQUEST_CONNECT_DEVICE);
                }
               mResponse.setText("");
                if (event.getEventTime() - event.getDownTime() >longPressTimeout) {
                   inputName.setText(prefs.getString(new StringBuilder("btn_name").append(val$b).toString(), "").equals("") ? "B" + (val$b + REQUEST_CONNECT_DEVICE) :prefs.getString("btn_name" + val$b, "B" + (val$b + REQUEST_CONNECT_DEVICE)));
                   inputCmd.setText(prefs.getString(new StringBuilder("ButtonStrCmd").append(val$b).toString(), "").equals("") ? "" :prefs.getString("ButtonStrCmd" + val$b, ""));
                   selectedButton = val$b;
                    val$ad.show();
                } else if (btStrCmd[val$b].trim().length() <= 0) {
                    Toast.makeText(getApplicationContext(), "Button #" + (val$b + REQUEST_CONNECT_DEVICE) + " not configured", Toast.LENGTH_SHORT).show();
                } else if (mRfcommClient.getState() == MESSAGE_WRITE) {

                    while (mRfcommClient.mConnectedThread.mmInStream != null) {
                        try {
                            mRfcommClient.mConnectedThread.mmInStream.read();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                    if (lv.isShown()) {
                       lv.setVisibility(View.VISIBLE);
                       layoutGRBL.setVisibility(View.VISIBLE);
                    }
                   sendMessage(new StringBuilder(String.valueOf(btStrCmd[val$b].trim())).append("\n").toString());
                } else {
                    Toast.makeText(getApplicationContext(), "** Not connected **", Toast.LENGTH_SHORT).show();
                }
            }
            return false;
        }
    }

    /* renamed from: om.example.android.cncbt_kas2.6 */
    class C00056 implements OnClickListener {
        C00056() {
        }

        public void onClick(View view) {
            if (buttonFeedback == REQUEST_ENABLE_BT) {
               tg.startTone(24);
            } else if (buttonFeedback == MESSAGE_DEVICE_NAME) {
               mButtonStreamFile.performHapticFeedback(REQUEST_CONNECT_DEVICE);
            }
           mButtonTxtSend.clearFocus();
           imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            if (mTxtToSend.getText().toString().trim().length() <= 0) {
                Toast.makeText(getApplicationContext(), "Nothing to send", Toast.LENGTH_SHORT).show();
            } else if (mRfcommClient.getState() == MESSAGE_WRITE) {

                while (mRfcommClient.mConnectedThread.mmInStream!= null) {
                    try {
                        mRfcommClient.mConnectedThread.mmInStream.read();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
               mResponse.setText("");
               mResponse.scrollTo(0, 0);
                if (lv.isShown()) {
                   lv.setVisibility(View.VISIBLE);
                   layoutGRBL.setVisibility(View.VISIBLE);
                }
               sendMessage(new StringBuilder(String.valueOf(mTxtToSend.getText().toString().trim())).append("\n").toString());
               mTxtToSend.setText("");
            } else {
                Toast.makeText(getApplicationContext(), "** Not connected **", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /* renamed from: om.example.android.cncbt_kas2.7 */
    class C00077 implements OnClickListener {

        /* renamed from: om.example.android.cncbt_kas2.7.1 */
        class C00061 extends Thread {
            C00061() {
            }

            public void run() {
               sendStreamData();
            }
        }

        C00077() {
        }

        public void onClick(View view) {
            if (buttonFeedback == REQUEST_ENABLE_BT) {
               tg.startTone(24);
            } else if (buttonFeedback == MESSAGE_DEVICE_NAME) {
               mButtonStreamFile.performHapticFeedback(REQUEST_CONNECT_DEVICE);
            }
            if (mButtonStatus3.getText().toString().equals("please select a file ...")) {
                Toast.makeText(getApplicationContext(), "No file loaded", Toast.LENGTH_SHORT).show();
            } else if (mButtonStreamFile.getText().toString().contains("Start")) {
                if (mRfcommClient.getState() == MESSAGE_WRITE) {
                   pause = false;
                   grblReset = false;
                   isStreaming = true;
                   mButtonStreamFile.setText("Pause");
                   mButtonStreamFile.setTextColor(0xe67300);
                   enableButton(false);
                   mResponse.setTextColor(oldColor);
                   mResponse.scrollTo(0, 0);
                   mResponse.setText("** sending " +mButtonStatus3.getText() + " **");
                    if (lv.isShown()) {
                       lv.setVisibility(View.VISIBLE);
                       layoutGRBL.setVisibility(View.VISIBLE);
                    }
                    new C00061().start();
                    return;
                }
                Toast.makeText(getApplicationContext(), "** Not connected **", Toast.LENGTH_SHORT).show();
            } else if (mButtonStreamFile.getText().toString().contains("Pause")) {
               pause = true;
               sendMessage("!");
               enableButton(true);
               mButtonStreamFile.setText("Resume");
               mButtonStreamFile.setTextColor(0xace600);
               mResponse.append("\n < pause >");
            } else if (mButtonStreamFile.getText().toString().contains("Resume")) {
               pause = false;
               sendMessage("~");
               enableButton(false);
               mButtonStreamFile.setText("Pause");
               mButtonStreamFile.setTextColor(0xe67300);
               mResponse.append("\n < resume >\n");
            }
        }
    }

    /* renamed from: om.example.android.cncbt_kas2.8 */
    class C00088 implements OnItemClickListener {
        C00088() {
        }

        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
           fileToRead = String.valueOf(parent.getItemAtPosition(position));
           mButtonStatus3.setTextColor(oldColor);
           mButtonStatus3.setText(fileToRead);
           fileToRead = new StringBuilder(String.valueOf(Environment.getExternalStorageDirectory().getPath())).append("/download/").append(fileToRead).toString();
           lv.setVisibility(View.VISIBLE);
           layoutGRBL.setVisibility(View.VISIBLE);
        }
    }

    /* renamed from: om.example.android.cncbt_kas2.9 */
    class C00099 implements OnClickListener {
        C00099() {
        }

        public void onClick(View view) {
            if (lv.isShown()) {
               lv.setVisibility(View.VISIBLE);
               layoutGRBL.setVisibility(View.VISIBLE);
                return;
            }
           mResponse.setText("");
           layoutGRBL.setVisibility(View.VISIBLE);
           lv.setVisibility(View.VISIBLE );
            if (buttonFeedback == REQUEST_ENABLE_BT) {
               tg.startTone(24);
            } else if (buttonFeedback == MESSAGE_DEVICE_NAME) {
               mButtonStreamFile.performHapticFeedback(REQUEST_CONNECT_DEVICE);
            }
            ArrayList<String> FilesInFolder =GetFiles(new StringBuilder(String.valueOf(Environment.getExternalStorageDirectory().getPath())).append("/download").toString());
            if (FilesInFolder != null) {
               lv.setAdapter(new ArrayAdapter(getApplicationContext(), R.layout.mylist, FilesInFolder));
            } else {
                Toast.makeText(getApplicationContext(), "** No CNC files **", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public MainActivity() {
        TAG = "kas";
        mConnectedDeviceName = null;
        mBluetoothAdapter = null;
        buttonFeedback = MESSAGE_DEVICE_NAME;
        autoConnect = false;
        MacAddress = "";
        connectOnce = true;
        readMessage = "";
        longPressTimeout = 1500;
        selectedButton = 0;
        timeoutMax = 30;
        responseAvailable = false;
        pause = false;
        byte[] bArr = new byte[REQUEST_CONNECT_DEVICE];
        bArr[0] = (byte) 63;
        qMark = bArr;
        tg = null;
        bufferList = new ArrayList();
        optimize = true;
        isStreaming = false;
        bt = new Button[idArray.length];
        btStrCmd = new String[idArray.length];
        noDRO = false;
        mHandler = new C00001();
    }

    static {
        mRfcommClient = null;
        idArray = new int[]{R.id.buttonCmd1, R.id.buttonCmd2, R.id.buttonCmd3, R.id.buttonCmd4, R.id.buttonCmd5, R.id.buttonCmd6, R.id.buttonCmd7, R.id.buttonCmd8, R.id.buttonCmd9, R.id.buttonCmd10, R.id.buttonCmd11, R.id.buttonCmd12, R.id.buttonCmdA, R.id.buttonCmdB, R.id.buttonCmdC};
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(128);
        requestWindowFeature(REQUEST_CONNECT_DEVICE);
        setContentView(R.layout.activity_main);
        getWindow().setSoftInputMode(REQUEST_ENABLE_BT);
        view = getCurrentFocus();
        getWindow().setFlags(1024, 1024);
        //imm = (InputMethodManager) getSystemService(InputMethodManager);
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        prefs.registerOnSharedPreferenceChangeListener(this);
        editor = prefs.edit();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Bluetooth is not available", REQUEST_CONNECT_DEVICE).show();
            finish();
            return;
        }
        if (!mBluetoothAdapter.isEnabled()) {
            startActivityForResult(new Intent("android.bluetooth.adapter.action.REQUEST_ENABLE"), REQUEST_ENABLE_BT);
        }
        mRfcommClient = new com.example.android.cncbt_kas2.BluetoothRfcommClient(getApplicationContext(), mHandler);
        mButtonStatus3 = (TextView) findViewById(R.id.text_Data3);
        oldColor = mButtonStatus3.getTextColors();
        mButtonStatus3.setTextColor(0xFF0000);
        mButtonStatus3.setText("please select a file ...");
        mResponse = (TextView) findViewById(R.id.text_feedBack);
        mResponse.setMovementMethod(ScrollingMovementMethod.getInstance());
        mResponse.setClickable(false);
        mResponse.setLongClickable(false);
        autoConnect = prefs.getBoolean("Auto_Connect", false);
        optimize = prefs.getBoolean("smart_streaming", false);
        buttonFeedback = Integer.parseInt(prefs.getString("button_feedback", "4"));
        timeoutMax = Integer.parseInt(prefs.getString("timeout", "60"));
        droResolution = Integer.parseInt(prefs.getString("dro_res", "2"));
        mUpdatePeriod = Long.parseLong(prefs.getString("dro_refresh", "500"));
        switch (Integer.parseInt(prefs.getString("grbl_feedback", "3"))) {
            case REQUEST_CONNECT_DEVICE /*1*/:
                commentDisplay = true;
                commandCodeDisplay = false;
                break;
            case REQUEST_ENABLE_BT /*2*/:
                commentDisplay = false;
                commandCodeDisplay = true;
                break;
            case MESSAGE_WRITE /*3*/:
                commentDisplay = true;
                commandCodeDisplay = true;
                break;
        }
        mTxtStatus = (TextView) findViewById(R.id.txt_status);
        tg = new ToneGenerator(MESSAGE_TOAST, 50);
        mOptionButton = (ImageButton) findViewById(R.id.Button_Op);
        mOptionButton.setOnClickListener(new C00012());
        builder = new Builder(getApplicationContext()).setCancelable(false);
        builder.setTitle("Button configuration");
        inputName = new EditText(getApplicationContext());
        inputName.setHint("Name");
        inputCmd = new EditText(getApplicationContext());
        inputCmd.setHint("Command");
        LinearLayout ll = new LinearLayout(getApplicationContext());
        ll.setOrientation(REQUEST_CONNECT_DEVICE);
        ll.addView(inputName);
        ll.addView(inputCmd);
        builder.setView(ll);
        builder.setPositiveButton("Submit", new C00023());
        builder.setNegativeButton("Cancel", new C00034());
        AlertDialog ad = builder.create();
        for (int i = 0; i < idArray.length; i += REQUEST_CONNECT_DEVICE) {
            int b = i;
            bt[b] = (Button) findViewById(idArray[b]);
            bt[b].setText(prefs.getString("btn_name" + b, "B" + (b + REQUEST_CONNECT_DEVICE)));
            btStrCmd[b] = prefs.getString("ButtonStrCmd" + b, "");
            bt[b].setOnTouchListener(new C00045(b, ad));
        }
        mTxtToSend = (EditText) findViewById(R.id.editSend);
        layoutGRBL = (LinearLayout) findViewById(R.id.Layout_GRBL);
        layoutButtons = (LinearLayout) findViewById(R.id.LinearLayoutCmd);
        mButtonTxtSend = (Button) findViewById(R.id.ButtonSend);
        mButtonTxtSend.setOnClickListener(new C00056());
        mButtonStreamFile = (Button) findViewById(R.id.buttonstart);
        oldColor2 = mButtonStreamFile.getTextColors();
        mButtonStreamFile.setOnClickListener(new C00077());
        lv = (ListView) findViewById(R.id.filelist1);
        lv.setVisibility(MESSAGE_DEVICE_NAME);
        lv.setOnItemClickListener(new C00088());
        mButtonLoad = (Button) findViewById(R.id.Buttonload);
        mButtonLoad.setOnClickListener(new C00099());
        mButtonReset = (Button) findViewById(R.id.Buttonreset);
        mButtonReset.setTextColor(0xFF0000);
        mButtonReset.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (buttonFeedback == REQUEST_ENABLE_BT) {
                   tg.startTone(24);
                } else if (buttonFeedback == MESSAGE_DEVICE_NAME) {
                   mButtonReset.performHapticFeedback(REQUEST_CONNECT_DEVICE);
                }
               noDRO = true;
               sendMessage("!");
               noDRO = false;
               grblReset = true;
                byte[] ctrlX = new byte[REQUEST_CONNECT_DEVICE];
                ctrlX[0] = (byte) 24;
                mRfcommClient.write(ctrlX);
               isStreaming = false;
            }
        });
        if (connectOnce && autoConnect) {
            try {
                MacAddress = prefs.getString("LastMacAddress", "");
                if (MacAddress.length() == 17) {
                    mRfcommClient.connect(mBluetoothAdapter.getRemoteDevice(MacAddress));
                }
            } catch (Exception e) {
            }
            connectOnce = false;
        }
        layoutDRO = (LinearLayout) findViewById(R.id.layoutdro);
        txtSTAT = (TextView) findViewById(R.id.textstatus);
        txtDRO_X = (TextView) findViewById(R.id.textviewX);
        txtDRO_Y = (TextView) findViewById(R.id.textviewY);
        txtDRO_Z = (TextView) findViewById(R.id.textviewZ);
        txtDRO_Xm = (TextView) findViewById(R.id.textviewXm);
        txtDRO_Ym = (TextView) findViewById(R.id.textviewYm);
        txtDRO_Zm = (TextView) findViewById(R.id.textviewZm);
        mUpdateTimer = new Timer();
        droDisplay = Integer.parseInt(prefs.getString("dro_display", "3"));
        switch (droDisplay) {
            case REQUEST_CONNECT_DEVICE /*1*/:
                showDro(layoutDRO, 0);
                mUpdateTimer = new Timer();
                mUpdateTimer.schedule(new TimerTask() {
                    public void run() {
                        if (!noDRO && mRfcommClient.getState() == MESSAGE_WRITE) {
                            mRfcommClient.write(qMark);
                        }
                    }
                }, 1500, mUpdatePeriod);
            case REQUEST_ENABLE_BT /*2*/:
                showDro(layoutDRO, 0);
                mUpdateTimer = new Timer();
                mUpdateTimer.schedule(new TimerTask() {
                    public void run() {
                        if (!noDRO && mRfcommClient.getState() == MESSAGE_WRITE) {
                            mRfcommClient.write(qMark);
                        }
                    }
                }, 1500, mUpdatePeriod);
            case MESSAGE_WRITE /*3*/:
                showDro(layoutDRO, MESSAGE_DEVICE_NAME);
            default:
        }
    }

    void parseStatus(String statusString) {
        try {
            if (statusString.contains("M") && statusString.contains("W")) {
                String[] separated = statusString.substring(REQUEST_CONNECT_DEVICE, statusString.length() - 2).split("\\:");
                String[] status = separated[0].split("\\,");
                String[] posW = separated[REQUEST_CONNECT_DEVICE].split("\\,");
                String[] posM = separated[REQUEST_ENABLE_BT].split("\\,");
                txtSTAT.setText(status[0]);
                if (droResolution == REQUEST_CONNECT_DEVICE) {
                    txtDRO_X.setText(posW[0].substring(0, posW[0].length() - 2));
                    txtDRO_Y.setText(posW[REQUEST_CONNECT_DEVICE].substring(0, posW[REQUEST_CONNECT_DEVICE].length() - 2));
                    txtDRO_Z.setText(posW[REQUEST_ENABLE_BT].substring(0, posW[REQUEST_ENABLE_BT].length() - 2));
                    if (droDisplay == REQUEST_ENABLE_BT) {
                        txtDRO_Xm.setText(posM[0].substring(0, posM[0].length() - 2));
                        txtDRO_Ym.setText(posM[REQUEST_CONNECT_DEVICE].substring(0, posM[REQUEST_CONNECT_DEVICE].length() - 2));
                        txtDRO_Zm.setText(posM[REQUEST_ENABLE_BT].substring(0, posM[REQUEST_ENABLE_BT].length() - 2));
                        return;
                    }
                    return;
                } else if (droResolution == REQUEST_ENABLE_BT) {
                    txtDRO_X.setText(posW[0].substring(0, posW[0].length() - 1));
                    txtDRO_Y.setText(posW[REQUEST_CONNECT_DEVICE].substring(0, posW[REQUEST_CONNECT_DEVICE].length() - 1));
                    txtDRO_Z.setText(posW[REQUEST_ENABLE_BT].substring(0, posW[REQUEST_ENABLE_BT].length() - 1));
                    if (droDisplay == REQUEST_ENABLE_BT) {
                        txtDRO_Xm.setText(posM[0].substring(0, posM[0].length() - 1));
                        txtDRO_Ym.setText(posM[REQUEST_CONNECT_DEVICE].substring(0, posM[REQUEST_CONNECT_DEVICE].length() - 1));
                        txtDRO_Zm.setText(posM[REQUEST_ENABLE_BT].substring(0, posM[REQUEST_ENABLE_BT].length() - 1));
                        return;
                    }
                    return;
                } else {
                    txtDRO_X.setText(posW[0]);
                    txtDRO_Y.setText(posW[REQUEST_CONNECT_DEVICE]);
                    txtDRO_Z.setText(posW[REQUEST_ENABLE_BT]);
                    if (droDisplay == REQUEST_ENABLE_BT) {
                        txtDRO_Xm.setText(posM[0]);
                        txtDRO_Ym.setText(posM[REQUEST_CONNECT_DEVICE]);
                        txtDRO_Zm.setText(posM[REQUEST_ENABLE_BT]);
                        return;
                    }
                    return;
                }
            }
            txtDRO_X.setText("err#2");
            txtDRO_Y.setText("XXX");
            txtDRO_Z.setText("XXX");
        } catch (Exception e) {
            txtDRO_X.setText("err#1");
            txtDRO_Y.setText("XXX");
            txtDRO_Z.setText("XXX");
        }
    }

    private void enableButton(boolean enable) {
        mButtonLoad.setEnabled(enable);
        for (int i = 0; i < layoutButtons.getChildCount(); i += REQUEST_CONNECT_DEVICE) {
            View child = layoutButtons.getChildAt(i);
            child.setEnabled(enable);
            if (child instanceof ViewGroup) {
                ViewGroup group = (ViewGroup) child;
                for (int j = 0; j < group.getChildCount(); j += REQUEST_CONNECT_DEVICE) {
                    group.getChildAt(j).setEnabled(enable);
                }
            }
        }
    }

    private void showDro(LinearLayout layout, int visibility) {
        for (int i = 0; i < layout.getChildCount(); i += REQUEST_CONNECT_DEVICE) {
            layout.getChildAt(i).setVisibility(visibility);
        }
    }

    public ArrayList<String> GetFiles(String DirectoryPath) {
        ArrayList<String> MyFiles = new ArrayList();
        File f = new File(DirectoryPath);
        f.mkdirs();
        File[] cncfiles = f.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".nc") || name.endsWith(".gcode") || name.endsWith(".txt");
            }
        });
        if (cncfiles.length == 0) {
            return null;
        }
        for (int i = 0; i < cncfiles.length; i += REQUEST_CONNECT_DEVICE) {
            MyFiles.add(cncfiles[i].getName());
        }
        return MyFiles;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void sendStreamData() {
        /*
        r14 = this;
        r8 = 0;
        r2 = 0;
        r5 = "";
        r4 = 0;
        r3 = new java.io.FileInputStream;	 Catch:{ FileNotFoundException -> 0x0130 }
        r12 = r14.fileToRead;	 Catch:{ FileNotFoundException -> 0x0130 }
        r3.<init>(r12);	 Catch:{ FileNotFoundException -> 0x0130 }
        r2 = r3;
    L_0x000e:
        r6 = new java.io.BufferedReader;
        r12 = new java.io.InputStreamReader;
        r12.<init>(r2);
        r6.<init>(r12);
        if (r2 != 0) goto L_0x0035;
    L_0x001a:
        return;
    L_0x001b:
        r5 = r5.trim();	 Catch:{ IOException -> 0x0053 }
        r12 = "(";
        r13 = 0;
        r12 = r5.startsWith(r12, r13);	 Catch:{ IOException -> 0x0053 }
        if (r12 == 0) goto L_0x0055;
    L_0x0028:
        r12 = r14.commentDisplay;	 Catch:{ IOException -> 0x0053 }
        if (r12 == 0) goto L_0x0035;
    L_0x002c:
        r1 = r5;
        r12 = new om.example.android.cncbt_kas2.MainActivity$14;	 Catch:{ IOException -> 0x0053 }
        r12.<init>(r1);	 Catch:{ IOException -> 0x0053 }
        r14.runOnUiThread(r12);	 Catch:{ IOException -> 0x0053 }
    L_0x0035:
        r5 = r6.readLine();	 Catch:{ IOException -> 0x0053 }
        if (r5 == 0) goto L_0x003f;
    L_0x003b:
        r12 = r14.isStreaming;	 Catch:{ IOException -> 0x0053 }
        if (r12 != 0) goto L_0x001b;
    L_0x003f:
        r12 = 0;
        r14.isStreaming = r12;	 Catch:{ IOException -> 0x0053 }
        r2.close();	 Catch:{ IOException -> 0x0053 }
        r12 = r14.bufferList;	 Catch:{ IOException -> 0x0053 }
        r12.clear();	 Catch:{ IOException -> 0x0053 }
        r12 = new om.example.android.cncbt_kas2.MainActivity$19;	 Catch:{ IOException -> 0x0053 }
        r12.<init>();	 Catch:{ IOException -> 0x0053 }
        r14.runOnUiThread(r12);	 Catch:{ IOException -> 0x0053 }
        goto L_0x001a;
    L_0x0053:
        r12 = move-exception;
        goto L_0x001a;
    L_0x0055:
        r12 = 5;
        java.lang.Thread.sleep(r12);	 Catch:{ InterruptedException -> 0x0126 }
    L_0x005a:
        r12 = 1;
        r14.noDRO = r12;	 Catch:{ IOException -> 0x0053 }
        r12 = new java.lang.StringBuilder;	 Catch:{ IOException -> 0x0053 }
        r13 = java.lang.String.valueOf(r5);	 Catch:{ IOException -> 0x0053 }
        r12.<init>(r13);	 Catch:{ IOException -> 0x0053 }
        r13 = "\n";
        r12 = r12.append(r13);	 Catch:{ IOException -> 0x0053 }
        r12 = r12.toString();	 Catch:{ IOException -> 0x0053 }
        r14.sendMessage(r12);	 Catch:{ IOException -> 0x0053 }
        r12 = 0;
        r14.noDRO = r12;	 Catch:{ IOException -> 0x0053 }
        r12 = 2;
        java.lang.Thread.sleep(r12);	 Catch:{ InterruptedException -> 0x0129 }
    L_0x007b:
        r12 = r14.optimize;	 Catch:{ IOException -> 0x0053 }
        if (r12 == 0) goto L_0x00af;
    L_0x007f:
        r12 = r14.bufferList;	 Catch:{ IOException -> 0x0053 }
        r13 = r5.length();	 Catch:{ IOException -> 0x0053 }
        r13 = r13 + 1;
        r13 = java.lang.Integer.valueOf(r13);	 Catch:{ IOException -> 0x0053 }
        r12.add(r13);	 Catch:{ IOException -> 0x0053 }
        r0 = 64;
        r12 = r14.commandCodeDisplay;	 Catch:{ IOException -> 0x0053 }
        if (r12 == 0) goto L_0x009d;
    L_0x0094:
        r7 = r5;
        r12 = new om.example.android.cncbt_kas2.MainActivity$15;	 Catch:{ IOException -> 0x0053 }
        r12.<init>(r7);	 Catch:{ IOException -> 0x0053 }
        r14.runOnUiThread(r12);	 Catch:{ IOException -> 0x0053 }
    L_0x009d:
        r12 = r14.bufferList;	 Catch:{ IOException -> 0x0053 }
        r12 = r14.getBufferSum(r12);	 Catch:{ IOException -> 0x0053 }
        r13 = 96;
        if (r12 >= r13) goto L_0x0133;
    L_0x00a7:
        r12 = 5;
        java.lang.Thread.sleep(r12);	 Catch:{ InterruptedException -> 0x00ad }
        goto L_0x0035;
    L_0x00ad:
        r12 = move-exception;
        goto L_0x0035;
    L_0x00af:
        r0 = 999; // 0x3e7 float:1.4E-42 double:4.936E-321;
        r12 = r14.commandCodeDisplay;	 Catch:{ IOException -> 0x0053 }
        if (r12 == 0) goto L_0x0133;
    L_0x00b5:
        r7 = r5;
        r12 = new om.example.android.cncbt_kas2.MainActivity$16;	 Catch:{ IOException -> 0x0053 }
        r12.<init>(r7);	 Catch:{ IOException -> 0x0053 }
        r14.runOnUiThread(r12);	 Catch:{ IOException -> 0x0053 }
        r10 = r8;
    L_0x00bf:
        r12 = r14.responseAvailable;	 Catch:{ IOException -> 0x012c }
        if (r12 == 0) goto L_0x00d5;
    L_0x00c3:
        r12 = r14.bufferList;	 Catch:{ IOException -> 0x012c }
        r12 = r14.getBufferSum(r12);	 Catch:{ IOException -> 0x012c }
        if (r12 > r0) goto L_0x00d5;
    L_0x00cb:
        r8 = 0;
        r12 = 0;
        r14.responseAvailable = r12;	 Catch:{ IOException -> 0x0053 }
        r12 = 0;
        r14.responseAvailable = r12;	 Catch:{ IOException -> 0x0053 }
        goto L_0x0035;
    L_0x00d5:
        r12 = r14.pause;	 Catch:{ IOException -> 0x012c }
        if (r12 != 0) goto L_0x00ff;
    L_0x00d9:
        r12 = 1;
        r8 = r10 + r12;
        r12 = r14.timeoutMax;	 Catch:{ IOException -> 0x0053 }
        r12 = r12 * 100;
        r12 = (long) r12;	 Catch:{ IOException -> 0x0053 }
        r12 = (r10 > r12 ? 1 : (r10 == r12 ? 0 : -1));
        if (r12 > 0) goto L_0x00ea;
    L_0x00e6:
        r12 = r14.isStreaming;	 Catch:{ IOException -> 0x0053 }
        if (r12 != 0) goto L_0x0100;
    L_0x00ea:
        r12 = 0;
        r14.responseAvailable = r12;	 Catch:{ IOException -> 0x0053 }
        r2.close();	 Catch:{ IOException -> 0x0053 }
        r12 = r14.bufferList;	 Catch:{ IOException -> 0x0053 }
        r12.clear();	 Catch:{ IOException -> 0x0053 }
        r12 = new om.example.android.cncbt_kas2.MainActivity$17;	 Catch:{ IOException -> 0x0053 }
        r12.<init>();	 Catch:{ IOException -> 0x0053 }
        r14.runOnUiThread(r12);	 Catch:{ IOException -> 0x0053 }
        goto L_0x001a;
    L_0x00ff:
        r8 = r10;
    L_0x0100:
        r12 = r14.grblReset;	 Catch:{ IOException -> 0x0053 }
        if (r12 == 0) goto L_0x011c;
    L_0x0104:
        r12 = 0;
        r14.isStreaming = r12;	 Catch:{ IOException -> 0x0053 }
        r12 = 0;
        r14.responseAvailable = r12;	 Catch:{ IOException -> 0x0053 }
        r2.close();	 Catch:{ IOException -> 0x0053 }
        r12 = r14.bufferList;	 Catch:{ IOException -> 0x0053 }
        r12.clear();	 Catch:{ IOException -> 0x0053 }
        r12 = new om.example.android.cncbt_kas2.MainActivity$18;	 Catch:{ IOException -> 0x0053 }
        r12.<init>();	 Catch:{ IOException -> 0x0053 }
        r14.runOnUiThread(r12);	 Catch:{ IOException -> 0x0053 }
        goto L_0x001a;
    L_0x011c:
        r12 = 10;
        java.lang.Thread.sleep(r12);	 Catch:{ InterruptedException -> 0x0123 }
        r10 = r8;
        goto L_0x00bf;
    L_0x0123:
        r12 = move-exception;
        r10 = r8;
        goto L_0x00bf;
    L_0x0126:
        r12 = move-exception;
        goto L_0x005a;
    L_0x0129:
        r12 = move-exception;
        goto L_0x007b;
    L_0x012c:
        r12 = move-exception;
        r8 = r10;
        goto L_0x001a;
    L_0x0130:
        r12 = move-exception;
        goto L_0x000e;
    L_0x0133:
        r10 = r8;
        goto L_0x00bf;
        */
        throw new UnsupportedOperationException("Method not decompiled: om.example.android.cncbt_kas2.sendStreamData():void");
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (isStreaming) {
            mItemOptions.setEnabled(false);
            mItemResetOpt.setEnabled(false);
        } else {
            mItemOptions.setEnabled(true);
            mItemResetOpt.setEnabled(true);
        }
        return true;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        mItemConnect = menu.add("Connect");
        mItemOptions = menu.add("Options");
        mItemResetOpt = menu.add("reset Options");
        mItemAbout = menu.add("About");
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item == mItemConnect) {
            startActivityForResult(new Intent(getApplicationContext(), com.example.android.cncbt_kas2.DeviceListActivity.class), REQUEST_CONNECT_DEVICE);
        } else if (item == mItemOptions) {
            startActivity(new Intent(getApplicationContext(), com.example.android.cncbt_kas2.OptionsActivity.class));
        } else if (item == mItemResetOpt) {
            Builder dlg = new Builder(getApplicationContext());
            dlg.setMessage("Revert to default factory config ??\n (will reset BT communication)");
            dlg.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    if (mRfcommClient != null) {
                        mRfcommClient.stop();
                    }
                   editor.clear();
                   editor.commit();
                   finish();
                   overridePendingTransition(0, 0);
                   startActivity(getIntent());
                   overridePendingTransition(0, 0);
                }
            });
            dlg.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });
            dlg.show();
        } else if (item == mItemAbout) {
            try {
                //((TextView) new Builder(this).setTitle(getString(R.string.app_long_name) + "  V" + getPackageManager().getPackageInfo(getPackageName(), 0).versionName + "\n           @kas 2015").setMessage("from an original idea by billcat").show().findViewById(16908299)).setMovementMethod(LinkMovementMethod.getInstance());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if (key.equals("Auto_Connect")) {
            autoConnect = prefs.getBoolean("Auto_Connect", false);
        } else if (key.equals("smart_streaming")) {
            optimize = prefs.getBoolean("smart_streaming", false);
        } else if (key.equals("button_feedback")) {
            buttonFeedback = Integer.parseInt(prefs.getString("button_feedback", "4"));
        } else if (key.equals("timeout")) {
            timeoutMax = Integer.parseInt(prefs.getString("timeout", "60"));
        } else if (key.equals("dro_res")) {
            droResolution = Integer.parseInt(prefs.getString("dro_res", "2"));
        } else if (key.equals("grbl_feedback")) {
            switch (Integer.parseInt(prefs.getString("grbl_feedback", "3"))) {
                case REQUEST_CONNECT_DEVICE /*1*/:
                    commentDisplay = true;
                    commandCodeDisplay = false;
                case REQUEST_ENABLE_BT /*2*/:
                    commentDisplay = false;
                    commandCodeDisplay = true;
                case MESSAGE_WRITE /*3*/:
                    commentDisplay = true;
                    commandCodeDisplay = true;
                default:
            }
        } else if (key.equals("dro_display")) {
            droDisplay = Integer.parseInt(prefs.getString("dro_display", "3"));
            switch (droDisplay) {
                case REQUEST_CONNECT_DEVICE /*1*/:
                    mUpdateTimer.cancel();
                    mUpdateTimer.purge();
                    mUpdateTimer = new Timer();
                    mUpdateTimer.schedule(new TimerTask() {
                        public void run() {
                            if (!noDRO && mRfcommClient.getState() == MESSAGE_WRITE) {
                                mRfcommClient.write(qMark);
                            }
                        }
                    }, 500, mUpdatePeriod);
                    sendMessage("$10=3\n");
                    txtDRO_Xm.setText("");
                    txtDRO_Ym.setText("");
                    txtDRO_Zm.setText("");
                    showDro(layoutDRO, 0);
                case REQUEST_ENABLE_BT /*2*/:
                    mUpdateTimer.cancel();
                    mUpdateTimer.purge();
                    mUpdateTimer = new Timer();
                    mUpdateTimer.schedule(new TimerTask() {
                        public void run() {
                            if (!noDRO && mRfcommClient.getState() == MESSAGE_WRITE) {
                                mRfcommClient.write(qMark);
                            }
                        }
                    }, 500, mUpdatePeriod);
                    sendMessage("$10=3\n");
                    showDro(layoutDRO, 0);
                case MESSAGE_WRITE /*3*/:
                    mUpdateTimer.cancel();
                    mUpdateTimer.purge();
                    txtDRO_X.setText("");
                    txtDRO_Y.setText("");
                    txtDRO_Z.setText("");
                    txtDRO_Xm.setText("");
                    txtDRO_Ym.setText("");
                    txtDRO_Zm.setText("");
                    showDro(layoutDRO, MESSAGE_DEVICE_NAME);
                default:
            }
        } else if (key.equals("dro_refresh")) {
            mUpdatePeriod = Long.parseLong(prefs.getString("dro_refresh", "500"));
            mUpdateTimer.cancel();
            mUpdateTimer.purge();
            mUpdateTimer = new Timer();
            mUpdateTimer.schedule(new TimerTask() {
                public void run() {
                    if (!noDRO && mRfcommClient.getState() == MESSAGE_WRITE) {
                        mRfcommClient.write(qMark);
                    }
                }
            }, 500, mUpdatePeriod);
        }
    }

    protected void onPause() {
        super.onPause();
    }

    protected void onStop() {
        super.onStop();
    }

    public synchronized void onResume() {
        super.onResume();
        if (mRfcommClient != null && mRfcommClient.getState() == 0) {
            mRfcommClient.start();
        }
    }

    public void onDestroy() {
        mUpdateTimer.cancel();
        mUpdateTimer.purge();
        if (mRfcommClient != null) {
            mRfcommClient.stop();
        }
        super.onDestroy();
    }

    public void onBackPressed() {
        new Builder(getApplicationContext()).setTitle(R.string.app_long_name)
                .setMessage("Close this controller ??")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (mRfcommClient != null) {
                    mRfcommClient.stop();
                }
               finish();
            }
        }).setNegativeButton("No", null).show();
    }

    private void sendMessage(String message) {
        if (message.length() > 0 && mRfcommClient.getState() == MESSAGE_WRITE) {
            mRfcommClient.write(message.getBytes());
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE /*1*/:
                if (resultCode == -1) {
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(data.getExtras().getString(com.example.android.cncbt_kas2.DeviceListActivity.EXTRA_DEVICE_ADDRESS));
                    mRfcommClient.connect(device);
                    editor.putString("LastMacAddress", device.toString());
                    editor.commit();
                }
            case REQUEST_ENABLE_BT /*2*/:
                if (resultCode != -1) {
                    Toast.makeText(getApplicationContext(), R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    finish();
                }
            default:
        }
    }

    private int getBufferSum(ArrayList<Integer> bufferList) {
        int sum = 0;
        Iterator<Integer> iter = bufferList.iterator();
        while (iter.hasNext()) {
            try {
                sum += ((Integer) iter.next()).intValue();
            } catch (Exception e) {
                return 999;
            }
        }
        return sum;
    }
}
