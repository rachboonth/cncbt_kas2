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

    /* renamed from: om.example.android.cncbt_kas2.MainActivity.14 */
    class AnonymousClass14 implements Runnable {
        private final /* synthetic */ String val$comment;

        AnonymousClass14(String str) {
            this.val$comment = str;
        }

        public void run() {
            MainActivity.this.mResponse.append("\n" + this.val$comment);
        }
    }

    /* renamed from: om.example.android.cncbt_kas2.MainActivity.15 */
    class AnonymousClass15 implements Runnable {
        private final /* synthetic */ String val$sent;

        AnonymousClass15(String str) {
            this.val$sent = str;
        }

        public void run() {
            MainActivity.this.mResponse.append("\n" + this.val$sent);
        }
    }

    /* renamed from: om.example.android.cncbt_kas2.MainActivity.16 */
    class AnonymousClass16 implements Runnable {
        private final /* synthetic */ String val$sent;

        AnonymousClass16(String str) {
            this.val$sent = str;
        }

        public void run() {
            MainActivity.this.mResponse.append("\n" + this.val$sent);
        }
    }

    /* renamed from: om.example.android.cncbt_kas2.MainActivity.1 */
    class C00001 extends Handler {
        C00001() {
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MainActivity.REQUEST_CONNECT_DEVICE /*1*/:
                    switch (msg.arg1) {
                        case 0:
                            ToneGenerator tg2 = new ToneGenerator(MainActivity.MESSAGE_TOAST, 80);
                            tg2.startTone(24);
                            tg2.release();
                            C0013R.string.title_not_connected;
                            MainActivity.this.mTxtStatus.setTextColor(Color.rgb(255, 0, 0));
                            MainActivity.this.mTxtStatus.setText(C0013R.string.title_not_connected);
                        case MainActivity.REQUEST_ENABLE_BT /*2*/:
                            MainActivity.this.mTxtStatus.setTextColor(Color.rgb(255, 0, 0));
                            MainActivity.this.mTxtStatus.setText(C0013R.string.title_connecting);
                        case MainActivity.MESSAGE_WRITE /*3*/:
                            MainActivity.this.mTxtStatus.setTextColor(Color.rgb(0, 255, 0));
                            MainActivity.this.mTxtStatus.setText(C0013R.string.title_connected_to);
                            MainActivity.this.mTxtStatus.append(" " + MainActivity.this.mConnectedDeviceName);
                            MainActivity.this.connectOnce = false;
                            MainActivity.this.sendMessage("$10=3\n");
                        default:
                    }
                case MainActivity.REQUEST_ENABLE_BT /*2*/:
                    MainActivity.this.readMessage = (String) msg.obj;
                    if (MainActivity.this.readMessage.contains("MPos:") || MainActivity.this.readMessage.contains("WPos:")) {
                        if (MainActivity.this.droDisplay != MainActivity.MESSAGE_WRITE) {
                            MainActivity.this.parseStatus(MainActivity.this.readMessage);
                        } else {
                            MainActivity.this.mResponse.setText(MainActivity.this.readMessage);
                        }
                    } else if (MainActivity.this.readMessage.contains("['$' for help]")) {
                        MainActivity.this.grblReset = true;
                        MainActivity.this.mResponse.setText("** GRBL reset **    " + MainActivity.this.readMessage);
                        MainActivity.this.mButtonStreamFile.setText("Start");
                        MainActivity.this.mButtonStreamFile.setTextColor(MainActivity.this.oldColor2);
                        MainActivity.this.enableButton(true);
                    } else if (MainActivity.this.readMessage.contains("error") || MainActivity.this.readMessage.toLowerCase(Locale.US).startsWith("alarm")) {
                        MainActivity.this.mResponse.setTextColor(-65536);
                        MainActivity.this.mResponse.append("\n");
                        MainActivity.this.isStreaming = false;
                        MainActivity.this.responseAvailable = false;
                        MainActivity.this.bufferList.clear();
                        MainActivity.this.mButtonStreamFile.setText("Start");
                        MainActivity.this.mButtonStreamFile.setTextColor(MainActivity.this.oldColor2);
                        MainActivity.this.enableButton(true);
                        MainActivity.this.mResponse.append("\n" + MainActivity.this.readMessage);
                    } else {
                        MainActivity.this.mResponse.setTextColor(MainActivity.this.oldColor);
                        if (MainActivity.this.readMessage.contains("ok")) {
                            MainActivity.this.responseAvailable = true;
                            if (MainActivity.this.optimize && MainActivity.this.isStreaming && MainActivity.this.bufferList.size() > 0) {
                                MainActivity.this.bufferList.remove(0);
                            }
                            if (MainActivity.this.commandCodeDisplay) {
                                MainActivity.this.mResponse.append("*" + MainActivity.this.readMessage);
                                return;
                            }
                            return;
                        }
                        if (MainActivity.this.readMessage.contains("[Pgm End]")) {
                            MainActivity.this.tg.startTone(24);
                            MainActivity.this.mResponse.append("\n");
                        }
                        if (MainActivity.this.isStreaming) {
                            MainActivity.this.mResponse.append("\n");
                        }
                        MainActivity.this.mResponse.append(MainActivity.this.readMessage);
                    }
                case MainActivity.MESSAGE_DEVICE_NAME /*4*/:
                    MainActivity.this.mConnectedDeviceName = msg.getData().getString(MainActivity.DEVICE_NAME);
                    Toast.makeText(MainActivity.this.getApplicationContext(), "Connected to " + MainActivity.this.mConnectedDeviceName, 0).show();
                case MainActivity.MESSAGE_TOAST /*5*/:
                    Toast.makeText(MainActivity.this.getApplicationContext(), msg.getData().getString(MainActivity.TOAST), 0).show();
                default:
            }
        }
    }

    /* renamed from: om.example.android.cncbt_kas2.MainActivity.2 */
    class C00012 implements OnClickListener {
        C00012() {
        }

        public void onClick(View v) {
            if (MainActivity.this.buttonFeedback == MainActivity.REQUEST_ENABLE_BT) {
                MainActivity.this.tg.startTone(24);
            } else if (MainActivity.this.buttonFeedback == MainActivity.MESSAGE_DEVICE_NAME) {
                MainActivity.this.mOptionButton.performHapticFeedback(MainActivity.REQUEST_CONNECT_DEVICE);
            }
            MainActivity.this.openOptionsMenu();
        }
    }

    /* renamed from: om.example.android.cncbt_kas2.MainActivity.3 */
    class C00023 implements DialogInterface.OnClickListener {
        C00023() {
        }

        public void onClick(DialogInterface dialog, int which) {
            MainActivity.this.getWindow().setSoftInputMode(MainActivity.MESSAGE_WRITE);
            String name = MainActivity.this.inputName.getText().toString();
            String cmd = MainActivity.this.inputCmd.getText().toString();
            MainActivity.this.editor = MainActivity.this.prefs.edit();
            if (name.equals("")) {
                name = "B" + (MainActivity.this.selectedButton + MainActivity.REQUEST_CONNECT_DEVICE);
            }
            MainActivity.this.editor.putString("btn_name" + MainActivity.this.selectedButton, name);
            MainActivity.this.editor.putString("ButtonStrCmd" + MainActivity.this.selectedButton, cmd);
            MainActivity.this.bt[MainActivity.this.selectedButton].setText(name);
            MainActivity.this.btStrCmd[MainActivity.this.selectedButton] = cmd;
            MainActivity.this.editor.commit();
        }
    }

    /* renamed from: om.example.android.cncbt_kas2.MainActivity.4 */
    class C00034 implements DialogInterface.OnClickListener {
        C00034() {
        }

        public void onClick(DialogInterface dialog, int which) {
            dialog.dismiss();
        }
    }

    /* renamed from: om.example.android.cncbt_kas2.MainActivity.5 */
    class C00045 implements OnTouchListener {
        private final /* synthetic */ AlertDialog val$ad;
        private final /* synthetic */ int val$b;

        C00045(int i, AlertDialog alertDialog) {
            this.val$b = i;
            this.val$ad = alertDialog;
        }

        public boolean onTouch(View v, MotionEvent event) {
            if (v.isPressed() && event.getAction() == MainActivity.REQUEST_CONNECT_DEVICE) {
                v.performClick();
                if (MainActivity.this.buttonFeedback == MainActivity.REQUEST_ENABLE_BT) {
                    MainActivity.this.tg.startTone(24);
                } else if (MainActivity.this.buttonFeedback == MainActivity.MESSAGE_DEVICE_NAME) {
                    MainActivity.this.bt[this.val$b].performHapticFeedback(MainActivity.REQUEST_CONNECT_DEVICE);
                }
                MainActivity.this.mResponse.setText("");
                if (event.getEventTime() - event.getDownTime() > MainActivity.this.longPressTimeout) {
                    MainActivity.this.inputName.setText(MainActivity.this.prefs.getString(new StringBuilder("btn_name").append(this.val$b).toString(), "").equals("") ? "B" + (this.val$b + MainActivity.REQUEST_CONNECT_DEVICE) : MainActivity.this.prefs.getString("btn_name" + this.val$b, "B" + (this.val$b + MainActivity.REQUEST_CONNECT_DEVICE)));
                    MainActivity.this.inputCmd.setText(MainActivity.this.prefs.getString(new StringBuilder("ButtonStrCmd").append(this.val$b).toString(), "").equals("") ? "" : MainActivity.this.prefs.getString("ButtonStrCmd" + this.val$b, ""));
                    MainActivity.this.selectedButton = this.val$b;
                    this.val$ad.show();
                } else if (MainActivity.this.btStrCmd[this.val$b].trim().length() <= 0) {
                    Toast.makeText(MainActivity.this, "Button #" + (this.val$b + MainActivity.REQUEST_CONNECT_DEVICE) + " not configured", 0).show();
                } else if (MainActivity.mRfcommClient.getState() == MainActivity.MESSAGE_WRITE) {
                    while (MainActivity.mRfcommClient.mConnectedThread.mmInStream.available() > 0) {
                        try {
                            MainActivity.mRfcommClient.mConnectedThread.mmInStream.read();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (MainActivity.this.lv.isShown()) {
                        MainActivity.this.lv.setVisibility(MainActivity.MESSAGE_DEVICE_NAME);
                        MainActivity.this.layoutGRBL.setVisibility(0);
                    }
                    MainActivity.this.sendMessage(new StringBuilder(String.valueOf(MainActivity.this.btStrCmd[this.val$b].trim())).append("\n").toString());
                } else {
                    Toast.makeText(MainActivity.this, "** Not connected **", 0).show();
                }
            }
            return false;
        }
    }

    /* renamed from: om.example.android.cncbt_kas2.MainActivity.6 */
    class C00056 implements OnClickListener {
        C00056() {
        }

        public void onClick(View view) {
            if (MainActivity.this.buttonFeedback == MainActivity.REQUEST_ENABLE_BT) {
                MainActivity.this.tg.startTone(24);
            } else if (MainActivity.this.buttonFeedback == MainActivity.MESSAGE_DEVICE_NAME) {
                MainActivity.this.mButtonStreamFile.performHapticFeedback(MainActivity.REQUEST_CONNECT_DEVICE);
            }
            MainActivity.this.mButtonTxtSend.clearFocus();
            MainActivity.this.imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            if (MainActivity.this.mTxtToSend.getText().toString().trim().length() <= 0) {
                Toast.makeText(MainActivity.this, "Nothing to send", 0).show();
            } else if (MainActivity.mRfcommClient.getState() == MainActivity.MESSAGE_WRITE) {

                while (MainActivity.mRfcommClient.mConnectedThread.mmInStream.available() > 0) {
                    try {
                        MainActivity.mRfcommClient.mConnectedThread.mmInStream.read();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                MainActivity.this.mResponse.setText("");
                MainActivity.this.mResponse.scrollTo(0, 0);
                if (MainActivity.this.lv.isShown()) {
                    MainActivity.this.lv.setVisibility(View.VISIBLE);
                    MainActivity.this.layoutGRBL.setVisibility(View.VISIBLE);
                }
                MainActivity.this.sendMessage(new StringBuilder(String.valueOf(MainActivity.this.mTxtToSend.getText().toString().trim())).append("\n").toString());
                MainActivity.this.mTxtToSend.setText("");
            } else {
                Toast.makeText(MainActivity.this, "** Not connected **", 0).show();
            }
        }
    }

    /* renamed from: om.example.android.cncbt_kas2.MainActivity.7 */
    class C00077 implements OnClickListener {

        /* renamed from: om.example.android.cncbt_kas2.MainActivity.7.1 */
        class C00061 extends Thread {
            C00061() {
            }

            public void run() {
                MainActivity.this.sendStreamData();
            }
        }

        C00077() {
        }

        public void onClick(View view) {
            if (MainActivity.this.buttonFeedback == MainActivity.REQUEST_ENABLE_BT) {
                MainActivity.this.tg.startTone(24);
            } else if (MainActivity.this.buttonFeedback == MainActivity.MESSAGE_DEVICE_NAME) {
                MainActivity.this.mButtonStreamFile.performHapticFeedback(MainActivity.REQUEST_CONNECT_DEVICE);
            }
            if (MainActivity.this.mButtonStatus3.getText().toString().equals("please select a file ...")) {
                Toast.makeText(MainActivity.this, "No file loaded", 0).show();
            } else if (MainActivity.this.mButtonStreamFile.getText().toString().contains("Start")) {
                if (MainActivity.mRfcommClient.getState() == MainActivity.MESSAGE_WRITE) {
                    MainActivity.this.pause = false;
                    MainActivity.this.grblReset = false;
                    MainActivity.this.isStreaming = true;
                    MainActivity.this.mButtonStreamFile.setText("Pause");
                    MainActivity.this.mButtonStreamFile.setTextColor(-65536);
                    MainActivity.this.enableButton(false);
                    MainActivity.this.mResponse.setTextColor(MainActivity.this.oldColor);
                    MainActivity.this.mResponse.scrollTo(0, 0);
                    MainActivity.this.mResponse.setText("** sending " + MainActivity.this.mButtonStatus3.getText() + " **");
                    if (MainActivity.this.lv.isShown()) {
                        MainActivity.this.lv.setVisibility(MainActivity.MESSAGE_DEVICE_NAME);
                        MainActivity.this.layoutGRBL.setVisibility(0);
                    }
                    new C00061().start();
                    return;
                }
                Toast.makeText(MainActivity.this, "** Not connected **", 0).show();
            } else if (MainActivity.this.mButtonStreamFile.getText().toString().contains("Pause")) {
                MainActivity.this.pause = true;
                MainActivity.this.sendMessage("!");
                MainActivity.this.enableButton(true);
                MainActivity.this.mButtonStreamFile.setText("Resume");
                MainActivity.this.mButtonStreamFile.setTextColor(-16711936);
                MainActivity.this.mResponse.append("\n < pause >");
            } else if (MainActivity.this.mButtonStreamFile.getText().toString().contains("Resume")) {
                MainActivity.this.pause = false;
                MainActivity.this.sendMessage("~");
                MainActivity.this.enableButton(false);
                MainActivity.this.mButtonStreamFile.setText("Pause");
                MainActivity.this.mButtonStreamFile.setTextColor(-65536);
                MainActivity.this.mResponse.append("\n < resume >\n");
            }
        }
    }

    /* renamed from: om.example.android.cncbt_kas2.MainActivity.8 */
    class C00088 implements OnItemClickListener {
        C00088() {
        }

        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            MainActivity.this.fileToRead = String.valueOf(parent.getItemAtPosition(position));
            MainActivity.this.mButtonStatus3.setTextColor(MainActivity.this.oldColor);
            MainActivity.this.mButtonStatus3.setText(MainActivity.this.fileToRead);
            MainActivity.this.fileToRead = new StringBuilder(String.valueOf(Environment.getExternalStorageDirectory().getPath())).append("/download/").append(MainActivity.this.fileToRead).toString();
            MainActivity.this.lv.setVisibility(MainActivity.MESSAGE_DEVICE_NAME);
            MainActivity.this.layoutGRBL.setVisibility(0);
        }
    }

    /* renamed from: om.example.android.cncbt_kas2.MainActivity.9 */
    class C00099 implements OnClickListener {
        C00099() {
        }

        public void onClick(View view) {
            if (MainActivity.this.lv.isShown()) {
                MainActivity.this.lv.setVisibility(View.VISIBLE);
                MainActivity.this.layoutGRBL.setVisibility(View.VISIBLE);
                return;
            }
            MainActivity.this.mResponse.setText("");
            MainActivity.this.layoutGRBL.setVisibility(MainActivity.MESSAGE_DEVICE_NAME);
            MainActivity.this.lv.setVisibility(0);
            if (MainActivity.this.buttonFeedback == MainActivity.REQUEST_ENABLE_BT) {
                MainActivity.this.tg.startTone(24);
            } else if (MainActivity.this.buttonFeedback == MainActivity.MESSAGE_DEVICE_NAME) {
                MainActivity.this.mButtonStreamFile.performHapticFeedback(MainActivity.REQUEST_CONNECT_DEVICE);
            }
            ArrayList<String> FilesInFolder = MainActivity.this.GetFiles(new StringBuilder(String.valueOf(Environment.getExternalStorageDirectory().getPath())).append("/download").toString());
            if (FilesInFolder != null) {
                MainActivity.this.lv.setAdapter(new ArrayAdapter(MainActivity.this, C0013R.layout.mylist, FilesInFolder));
            } else {
                Toast.makeText(MainActivity.this, "** No CNC files **", 0).show();
            }
        }
    }

    public MainActivity() {
        this.TAG = "kas";
        this.mConnectedDeviceName = null;
        this.mBluetoothAdapter = null;
        this.buttonFeedback = MESSAGE_DEVICE_NAME;
        this.autoConnect = false;
        this.MacAddress = "";
        this.connectOnce = true;
        this.readMessage = "";
        this.longPressTimeout = 1500;
        this.selectedButton = 0;
        this.timeoutMax = 30;
        this.responseAvailable = false;
        this.pause = false;
        byte[] bArr = new byte[REQUEST_CONNECT_DEVICE];
        bArr[0] = (byte) 63;
        this.qMark = bArr;
        this.tg = null;
        this.bufferList = new ArrayList();
        this.optimize = true;
        this.isStreaming = false;
        this.bt = new Button[idArray.length];
        this.btStrCmd = new String[idArray.length];
        this.noDRO = false;
        this.mHandler = new C00001();
    }

    static {
        mRfcommClient = null;
        idArray = new int[]{C0013R.id.buttonCmd1, C0013R.id.buttonCmd2, C0013R.id.buttonCmd3, C0013R.id.buttonCmd4, C0013R.id.buttonCmd5, C0013R.id.buttonCmd6, C0013R.id.buttonCmd7, C0013R.id.buttonCmd8, C0013R.id.buttonCmd9, C0013R.id.buttonCmd10, C0013R.id.buttonCmd11, C0013R.id.buttonCmd12, C0013R.id.buttonCmdA, C0013R.id.buttonCmdB, C0013R.id.buttonCmdC};
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(128);
        requestWindowFeature(REQUEST_CONNECT_DEVICE);
        setContentView(C0013R.layout.main);
        getWindow().setSoftInputMode(REQUEST_ENABLE_BT);
        this.view = getCurrentFocus();
        getWindow().setFlags(1024, 1024);
        this.imm = (InputMethodManager) getSystemService("input_method");
        this.prefs = PreferenceManager.getDefaultSharedPreferences(this);
        this.prefs.registerOnSharedPreferenceChangeListener(this);
        this.editor = this.prefs.edit();
        this.mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (this.mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", REQUEST_CONNECT_DEVICE).show();
            finish();
            return;
        }
        if (!this.mBluetoothAdapter.isEnabled()) {
            startActivityForResult(new Intent("android.bluetooth.adapter.action.REQUEST_ENABLE"), REQUEST_ENABLE_BT);
        }
        mRfcommClient = new om.example.android.cncbt_kas2.BluetoothRfcommClient(this, this.mHandler);
        this.mButtonStatus3 = (TextView) findViewById(C0013R.id.text_Data3);
        this.oldColor = this.mButtonStatus3.getTextColors();
        this.mButtonStatus3.setTextColor(-65536);
        this.mButtonStatus3.setText("please select a file ...");
        this.mResponse = (TextView) findViewById(C0013R.id.text_feedBack);
        this.mResponse.setMovementMethod(ScrollingMovementMethod.getInstance());
        this.mResponse.setClickable(false);
        this.mResponse.setLongClickable(false);
        this.autoConnect = this.prefs.getBoolean("Auto_Connect", false);
        this.optimize = this.prefs.getBoolean("smart_streaming", false);
        this.buttonFeedback = Integer.parseInt(this.prefs.getString("button_feedback", "4"));
        this.timeoutMax = Integer.parseInt(this.prefs.getString("timeout", "60"));
        this.droResolution = Integer.parseInt(this.prefs.getString("dro_res", "2"));
        this.mUpdatePeriod = Long.parseLong(this.prefs.getString("dro_refresh", "500"));
        switch (Integer.parseInt(this.prefs.getString("grbl_feedback", "3"))) {
            case REQUEST_CONNECT_DEVICE /*1*/:
                this.commentDisplay = true;
                this.commandCodeDisplay = false;
                break;
            case REQUEST_ENABLE_BT /*2*/:
                this.commentDisplay = false;
                this.commandCodeDisplay = true;
                break;
            case MESSAGE_WRITE /*3*/:
                this.commentDisplay = true;
                this.commandCodeDisplay = true;
                break;
        }
        this.mTxtStatus = (TextView) findViewById(C0013R.id.txt_status);
        this.tg = new ToneGenerator(MESSAGE_TOAST, 50);
        this.mOptionButton = (ImageButton) findViewById(C0013R.id.Button_Op);
        this.mOptionButton.setOnClickListener(new C00012());
        this.builder = new Builder(this).setCancelable(false);
        this.builder.setTitle("Button configuration");
        this.inputName = new EditText(this);
        this.inputName.setHint("Name");
        this.inputCmd = new EditText(this);
        this.inputCmd.setHint("Command");
        LinearLayout ll = new LinearLayout(this);
        ll.setOrientation(REQUEST_CONNECT_DEVICE);
        ll.addView(this.inputName);
        ll.addView(this.inputCmd);
        this.builder.setView(ll);
        this.builder.setPositiveButton("Submit", new C00023());
        this.builder.setNegativeButton("Cancel", new C00034());
        AlertDialog ad = this.builder.create();
        for (int i = 0; i < idArray.length; i += REQUEST_CONNECT_DEVICE) {
            int b = i;
            this.bt[b] = (Button) findViewById(idArray[b]);
            this.bt[b].setText(this.prefs.getString("btn_name" + b, "B" + (b + REQUEST_CONNECT_DEVICE)));
            this.btStrCmd[b] = this.prefs.getString("ButtonStrCmd" + b, "");
            this.bt[b].setOnTouchListener(new C00045(b, ad));
        }
        this.mTxtToSend = (EditText) findViewById(C0013R.id.editSend);
        this.layoutGRBL = (LinearLayout) findViewById(C0013R.id.Layout_GRBL);
        this.layoutButtons = (LinearLayout) findViewById(C0013R.id.LinearLayoutCmd);
        this.mButtonTxtSend = (Button) findViewById(C0013R.id.ButtonSend);
        this.mButtonTxtSend.setOnClickListener(new C00056());
        this.mButtonStreamFile = (Button) findViewById(C0013R.id.buttonstart);
        this.oldColor2 = this.mButtonStreamFile.getTextColors();
        this.mButtonStreamFile.setOnClickListener(new C00077());
        this.lv = (ListView) findViewById(C0013R.id.filelist1);
        this.lv.setVisibility(MESSAGE_DEVICE_NAME);
        this.lv.setOnItemClickListener(new C00088());
        this.mButtonLoad = (Button) findViewById(C0013R.id.Buttonload);
        this.mButtonLoad.setOnClickListener(new C00099());
        this.mButtonReset = (Button) findViewById(C0013R.id.Buttonreset);
        this.mButtonReset.setTextColor(-65536);
        this.mButtonReset.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (MainActivity.this.buttonFeedback == MainActivity.REQUEST_ENABLE_BT) {
                    MainActivity.this.tg.startTone(24);
                } else if (MainActivity.this.buttonFeedback == MainActivity.MESSAGE_DEVICE_NAME) {
                    MainActivity.this.mButtonReset.performHapticFeedback(MainActivity.REQUEST_CONNECT_DEVICE);
                }
                MainActivity.this.noDRO = true;
                MainActivity.this.sendMessage("!");
                MainActivity.this.noDRO = false;
                MainActivity.this.grblReset = true;
                byte[] ctrlX = new byte[MainActivity.REQUEST_CONNECT_DEVICE];
                ctrlX[0] = (byte) 24;
                MainActivity.mRfcommClient.write(ctrlX);
                MainActivity.this.isStreaming = false;
            }
        });
        if (this.connectOnce && this.autoConnect) {
            try {
                this.MacAddress = this.prefs.getString("LastMacAddress", "");
                if (this.MacAddress.length() == 17) {
                    mRfcommClient.connect(this.mBluetoothAdapter.getRemoteDevice(this.MacAddress));
                }
            } catch (Exception e) {
            }
            this.connectOnce = false;
        }
        this.layoutDRO = (LinearLayout) findViewById(C0013R.id.layoutdro);
        this.txtSTAT = (TextView) findViewById(C0013R.id.textstatus);
        this.txtDRO_X = (TextView) findViewById(C0013R.id.textviewX);
        this.txtDRO_Y = (TextView) findViewById(C0013R.id.textviewY);
        this.txtDRO_Z = (TextView) findViewById(C0013R.id.textviewZ);
        this.txtDRO_Xm = (TextView) findViewById(C0013R.id.textviewXm);
        this.txtDRO_Ym = (TextView) findViewById(C0013R.id.textviewYm);
        this.txtDRO_Zm = (TextView) findViewById(C0013R.id.textviewZm);
        this.mUpdateTimer = new Timer();
        this.droDisplay = Integer.parseInt(this.prefs.getString("dro_display", "3"));
        switch (this.droDisplay) {
            case REQUEST_CONNECT_DEVICE /*1*/:
                showDro(this.layoutDRO, 0);
                this.mUpdateTimer = new Timer();
                this.mUpdateTimer.schedule(new TimerTask() {
                    public void run() {
                        if (!MainActivity.this.noDRO && MainActivity.mRfcommClient.getState() == MainActivity.MESSAGE_WRITE) {
                            MainActivity.mRfcommClient.write(MainActivity.this.qMark);
                        }
                    }
                }, 1500, this.mUpdatePeriod);
            case REQUEST_ENABLE_BT /*2*/:
                showDro(this.layoutDRO, 0);
                this.mUpdateTimer = new Timer();
                this.mUpdateTimer.schedule(new TimerTask() {
                    public void run() {
                        if (!MainActivity.this.noDRO && MainActivity.mRfcommClient.getState() == MainActivity.MESSAGE_WRITE) {
                            MainActivity.mRfcommClient.write(MainActivity.this.qMark);
                        }
                    }
                }, 1500, this.mUpdatePeriod);
            case MESSAGE_WRITE /*3*/:
                showDro(this.layoutDRO, MESSAGE_DEVICE_NAME);
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
                this.txtSTAT.setText(status[0]);
                if (this.droResolution == REQUEST_CONNECT_DEVICE) {
                    this.txtDRO_X.setText(posW[0].substring(0, posW[0].length() - 2));
                    this.txtDRO_Y.setText(posW[REQUEST_CONNECT_DEVICE].substring(0, posW[REQUEST_CONNECT_DEVICE].length() - 2));
                    this.txtDRO_Z.setText(posW[REQUEST_ENABLE_BT].substring(0, posW[REQUEST_ENABLE_BT].length() - 2));
                    if (this.droDisplay == REQUEST_ENABLE_BT) {
                        this.txtDRO_Xm.setText(posM[0].substring(0, posM[0].length() - 2));
                        this.txtDRO_Ym.setText(posM[REQUEST_CONNECT_DEVICE].substring(0, posM[REQUEST_CONNECT_DEVICE].length() - 2));
                        this.txtDRO_Zm.setText(posM[REQUEST_ENABLE_BT].substring(0, posM[REQUEST_ENABLE_BT].length() - 2));
                        return;
                    }
                    return;
                } else if (this.droResolution == REQUEST_ENABLE_BT) {
                    this.txtDRO_X.setText(posW[0].substring(0, posW[0].length() - 1));
                    this.txtDRO_Y.setText(posW[REQUEST_CONNECT_DEVICE].substring(0, posW[REQUEST_CONNECT_DEVICE].length() - 1));
                    this.txtDRO_Z.setText(posW[REQUEST_ENABLE_BT].substring(0, posW[REQUEST_ENABLE_BT].length() - 1));
                    if (this.droDisplay == REQUEST_ENABLE_BT) {
                        this.txtDRO_Xm.setText(posM[0].substring(0, posM[0].length() - 1));
                        this.txtDRO_Ym.setText(posM[REQUEST_CONNECT_DEVICE].substring(0, posM[REQUEST_CONNECT_DEVICE].length() - 1));
                        this.txtDRO_Zm.setText(posM[REQUEST_ENABLE_BT].substring(0, posM[REQUEST_ENABLE_BT].length() - 1));
                        return;
                    }
                    return;
                } else {
                    this.txtDRO_X.setText(posW[0]);
                    this.txtDRO_Y.setText(posW[REQUEST_CONNECT_DEVICE]);
                    this.txtDRO_Z.setText(posW[REQUEST_ENABLE_BT]);
                    if (this.droDisplay == REQUEST_ENABLE_BT) {
                        this.txtDRO_Xm.setText(posM[0]);
                        this.txtDRO_Ym.setText(posM[REQUEST_CONNECT_DEVICE]);
                        this.txtDRO_Zm.setText(posM[REQUEST_ENABLE_BT]);
                        return;
                    }
                    return;
                }
            }
            this.txtDRO_X.setText("err#2");
            this.txtDRO_Y.setText("XXX");
            this.txtDRO_Z.setText("XXX");
        } catch (Exception e) {
            this.txtDRO_X.setText("err#1");
            this.txtDRO_Y.setText("XXX");
            this.txtDRO_Z.setText("XXX");
        }
    }

    private void enableButton(boolean enable) {
        this.mButtonLoad.setEnabled(enable);
        for (int i = 0; i < this.layoutButtons.getChildCount(); i += REQUEST_CONNECT_DEVICE) {
            View child = this.layoutButtons.getChildAt(i);
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
        throw new UnsupportedOperationException("Method not decompiled: om.example.android.cncbt_kas2.MainActivity.sendStreamData():void");
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (this.isStreaming) {
            this.mItemOptions.setEnabled(false);
            this.mItemResetOpt.setEnabled(false);
        } else {
            this.mItemOptions.setEnabled(true);
            this.mItemResetOpt.setEnabled(true);
        }
        return true;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        this.mItemConnect = menu.add("Connect");
        this.mItemOptions = menu.add("Options");
        this.mItemResetOpt = menu.add("reset Options");
        this.mItemAbout = menu.add("About");
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item == this.mItemConnect) {
            startActivityForResult(new Intent(this, om.example.android.cncbt_kas2.DeviceListActivity.class), REQUEST_CONNECT_DEVICE);
        } else if (item == this.mItemOptions) {
            startActivity(new Intent(this, om.example.android.cncbt_kas2.OptionsActivity.class));
        } else if (item == this.mItemResetOpt) {
            Builder dlg = new Builder(this);
            dlg.setMessage("Revert to default factory config ??\n (will reset BT communication)");
            dlg.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    if (MainActivity.mRfcommClient != null) {
                        MainActivity.mRfcommClient.stop();
                    }
                    MainActivity.this.editor.clear();
                    MainActivity.this.editor.commit();
                    MainActivity.this.finish();
                    MainActivity.this.overridePendingTransition(0, 0);
                    MainActivity.this.startActivity(MainActivity.this.getIntent());
                    MainActivity.this.overridePendingTransition(0, 0);
                }
            });
            dlg.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });
            dlg.show();
        } else if (item == this.mItemAbout) {
            try {
                ((TextView) new Builder(this).setTitle(getString(C0013R.string.app_long_name) + "  V" + getPackageManager().getPackageInfo(getPackageName(), 0).versionName + "\n           @kas 2015").setMessage("from an original idea by billcat").show().findViewById(16908299)).setMovementMethod(LinkMovementMethod.getInstance());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if (key.equals("Auto_Connect")) {
            this.autoConnect = prefs.getBoolean("Auto_Connect", false);
        } else if (key.equals("smart_streaming")) {
            this.optimize = prefs.getBoolean("smart_streaming", false);
        } else if (key.equals("button_feedback")) {
            this.buttonFeedback = Integer.parseInt(prefs.getString("button_feedback", "4"));
        } else if (key.equals("timeout")) {
            this.timeoutMax = Integer.parseInt(prefs.getString("timeout", "60"));
        } else if (key.equals("dro_res")) {
            this.droResolution = Integer.parseInt(prefs.getString("dro_res", "2"));
        } else if (key.equals("grbl_feedback")) {
            switch (Integer.parseInt(prefs.getString("grbl_feedback", "3"))) {
                case REQUEST_CONNECT_DEVICE /*1*/:
                    this.commentDisplay = true;
                    this.commandCodeDisplay = false;
                case REQUEST_ENABLE_BT /*2*/:
                    this.commentDisplay = false;
                    this.commandCodeDisplay = true;
                case MESSAGE_WRITE /*3*/:
                    this.commentDisplay = true;
                    this.commandCodeDisplay = true;
                default:
            }
        } else if (key.equals("dro_display")) {
            this.droDisplay = Integer.parseInt(prefs.getString("dro_display", "3"));
            switch (this.droDisplay) {
                case REQUEST_CONNECT_DEVICE /*1*/:
                    this.mUpdateTimer.cancel();
                    this.mUpdateTimer.purge();
                    this.mUpdateTimer = new Timer();
                    this.mUpdateTimer.schedule(new TimerTask() {
                        public void run() {
                            if (!MainActivity.this.noDRO && MainActivity.mRfcommClient.getState() == MainActivity.MESSAGE_WRITE) {
                                MainActivity.mRfcommClient.write(MainActivity.this.qMark);
                            }
                        }
                    }, 500, this.mUpdatePeriod);
                    sendMessage("$10=3\n");
                    this.txtDRO_Xm.setText("");
                    this.txtDRO_Ym.setText("");
                    this.txtDRO_Zm.setText("");
                    showDro(this.layoutDRO, 0);
                case REQUEST_ENABLE_BT /*2*/:
                    this.mUpdateTimer.cancel();
                    this.mUpdateTimer.purge();
                    this.mUpdateTimer = new Timer();
                    this.mUpdateTimer.schedule(new TimerTask() {
                        public void run() {
                            if (!MainActivity.this.noDRO && MainActivity.mRfcommClient.getState() == MainActivity.MESSAGE_WRITE) {
                                MainActivity.mRfcommClient.write(MainActivity.this.qMark);
                            }
                        }
                    }, 500, this.mUpdatePeriod);
                    sendMessage("$10=3\n");
                    showDro(this.layoutDRO, 0);
                case MESSAGE_WRITE /*3*/:
                    this.mUpdateTimer.cancel();
                    this.mUpdateTimer.purge();
                    this.txtDRO_X.setText("");
                    this.txtDRO_Y.setText("");
                    this.txtDRO_Z.setText("");
                    this.txtDRO_Xm.setText("");
                    this.txtDRO_Ym.setText("");
                    this.txtDRO_Zm.setText("");
                    showDro(this.layoutDRO, MESSAGE_DEVICE_NAME);
                default:
            }
        } else if (key.equals("dro_refresh")) {
            this.mUpdatePeriod = Long.parseLong(prefs.getString("dro_refresh", "500"));
            this.mUpdateTimer.cancel();
            this.mUpdateTimer.purge();
            this.mUpdateTimer = new Timer();
            this.mUpdateTimer.schedule(new TimerTask() {
                public void run() {
                    if (!MainActivity.this.noDRO && MainActivity.mRfcommClient.getState() == MainActivity.MESSAGE_WRITE) {
                        MainActivity.mRfcommClient.write(MainActivity.this.qMark);
                    }
                }
            }, 500, this.mUpdatePeriod);
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
        this.mUpdateTimer.cancel();
        this.mUpdateTimer.purge();
        if (mRfcommClient != null) {
            mRfcommClient.stop();
        }
        super.onDestroy();
    }

    public void onBackPressed() {
        new Builder(this).setTitle(R.string.app_long_name)
                .setMessage("Close this controller ??")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (MainActivity.mRfcommClient != null) {
                    MainActivity.mRfcommClient.stop();
                }
                MainActivity.this.finish();
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
                    BluetoothDevice device = this.mBluetoothAdapter.getRemoteDevice(data.getExtras().getString(om.example.android.cncbt_kas2.DeviceListActivity.EXTRA_DEVICE_ADDRESS));
                    mRfcommClient.connect(device);
                    this.editor.putString("LastMacAddress", device.toString());
                    this.editor.commit();
                }
            case REQUEST_ENABLE_BT /*2*/:
                if (resultCode != -1) {
                    Toast.makeText(this, C0013R.string.bt_not_enabled_leaving, 0).show();
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
