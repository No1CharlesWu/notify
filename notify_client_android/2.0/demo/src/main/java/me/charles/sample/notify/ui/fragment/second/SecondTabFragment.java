package me.charles.sample.notify.ui.fragment.second;

import android.app.Service;
import android.graphics.Rect;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.greenrobot.eventbus.Subscribe;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Map;

import me.charles.eventbusactivityscope.EventBusActivityScope;
import me.charles.sample.R;
import me.charles.sample.notify.adapter.AlertAdapter;
import me.charles.sample.notify.adapter.HistoryMsgAdapter;
import me.charles.sample.notify.base.BaseMainFragment;
import me.charles.sample.notify.entity.HistoryMsg;
import me.charles.sample.notify.event.TabSelectedEvent;
import me.charles.sample.notify.listener.OnItemClickListener;
import me.charles.sample.notify.net.MySharePreference;
import me.charles.sample.notify.ui.fragment.MainFragment;

public class SecondTabFragment extends BaseMainFragment {
    private Toolbar mToolbar;
    private EditText mEditText;
    private Button mButton;
    private EditText mKeyword;
    private Vibrator vibrator;

    private Socket socket;
    private String msg;
    private int number;
    private String getMsg;


    private MySharePreference service;
    private Map<String,String> params;

    private HistoryMsgAdapter historyMsgAdapter;

    public static SecondTabFragment newInstance() {
        Bundle args = new Bundle();
        SecondTabFragment fragment = new SecondTabFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.wechat_fragment_tab_second, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        mToolbar = (Toolbar) view.findViewById(R.id.toolbar);
        mToolbar.setTitle(R.string.send_message);
        mButton = (Button) view.findViewById(R.id.btn_send);
        mEditText = (EditText) view.findViewById(R.id.et_send_message);
        mKeyword = (EditText)view.findViewById(R.id.et_send_keyword);
        vibrator = (Vibrator) getContext().getSystemService(Service.VIBRATOR_SERVICE);



        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                msg = mEditText.getText().toString();
                number = Integer.valueOf(mKeyword.getText().toString());

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        mysocket(number, msg);

                        Message message=new Message();
                        message.what=1;
                        mHandler.sendMessage(message);
                    }
                });
                thread.start();

            }
        });

        service = new MySharePreference(getContext());
        params = service.getPreferences();

    }
    private void mysocket(int number, String msg){
        try {
            // 创建一个Socket对象，并指定服务端的IP及端口号
            socket = new Socket(params.get("ip"), Integer.valueOf(params.get("port")));
            // 获取Socket的OutputStream对象用于发送数据。
            OutputStream outputStream = socket.getOutputStream();

            //JSON
            JSONObject jsonObject = new JSONObject();
            //写入对应属性
            jsonObject.put("msg", msg);
            jsonObject.put("keyword", number);
            System.out.println(jsonObject);

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));
            writer.write(jsonObject.toString());
            writer.flush();

            // 创建一个InputStream用户读取要发送的文件。
            InputStream inputStream = socket.getInputStream();
            ByteArrayOutputStream out = null;
            if (inputStream != null) {
                out = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024 * 4];
                int len = -1;
                if ((len = inputStream.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                }
                byte[] data = out.toByteArray();
                getMsg = new String(data, "utf-8");
                System.out.println(getMsg);
            }

        } catch (Exception e){
            e.printStackTrace();
        }
    }
    public Handler mHandler=new Handler()
    {
        public void handleMessage(Message msg)
        {
            switch(msg.what)
            {
                case 1:
                    setAlert(getMsg);
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private void setAlert(String getMsg){
        try {
            JSONObject obj = new JSONObject(getMsg);
            String level = obj.getString("level");
            String msg = obj.getString("msg");

            if (level.equals("debug")){
                debug_alert();
            }else if (level.equals("info")){
                info_alert();
            }else if (level.equals("warning")){
                warning_alert();
            }else if (level.equals("error")){
                error_alert();
            }else if (level.equals("critical")){
                critical_alert();
            }else {
                Toast.makeText(getContext(), "nothing", Toast.LENGTH_SHORT).show();
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //静默，什么都不做
    private void debug_alert(){
        Toast.makeText(getContext(), "debug", Toast.LENGTH_SHORT).show();
    }

    //短震动
    private void info_alert(){
        vibrator.vibrate(Integer.valueOf(params.get("info_s"))* 1000);

        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        final Ringtone r = RingtoneManager.getRingtone(getContext(), notification);
        r.play();
        new Handler().postDelayed(new Runnable() {
            public void run() {
                r.stop();
            }
        }, Integer.valueOf(params.get("info_r")) * 1000);
        Toast.makeText(getContext(), "info", Toast.LENGTH_SHORT).show();
    }

    //短响铃
    private void warning_alert(){
        vibrator.vibrate(Integer.valueOf(params.get("warning_s"))* 1000);

        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        final Ringtone r = RingtoneManager.getRingtone(getContext(), notification);
        r.play();
        new Handler().postDelayed(new Runnable() {
            public void run() {
                r.stop();
            }
        }, Integer.valueOf(params.get("warning_r")) * 1000);
        Toast.makeText(getContext(), "warning", Toast.LENGTH_SHORT).show();
    }

    //短震动 + 短响铃
    private void error_alert(){
        vibrator.vibrate(Integer.valueOf(params.get("error_s"))* 1000);

        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        final Ringtone r = RingtoneManager.getRingtone(getContext(), notification);
        r.play();
        new Handler().postDelayed(new Runnable() {
            public void run() {
                r.stop();
            }
        }, Integer.valueOf(params.get("error_r")) * 1000);
        Toast.makeText(getContext(), "error", Toast.LENGTH_SHORT).show();
    }

    //长震动 + 长响铃
    private void critical_alert(){
        vibrator.vibrate(Integer.valueOf(params.get("critical_s")) * 1000);

        Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
        final Ringtone r = RingtoneManager.getRingtone(getContext(), notification);
        r.play();
        new Handler().postDelayed(new Runnable() {
            public void run() {
                r.stop();
            }
        }, Integer.valueOf(params.get("critical_r")) * 1000);
        Toast.makeText(getContext(), "critical", Toast.LENGTH_SHORT).show();
    }



    @Override
    public void onLazyInitView(@Nullable Bundle savedInstanceState) {
        super.onLazyInitView(savedInstanceState);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}
