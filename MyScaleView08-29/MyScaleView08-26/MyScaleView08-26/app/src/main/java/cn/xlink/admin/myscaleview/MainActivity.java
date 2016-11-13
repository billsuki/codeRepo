package cn.xlink.admin.myscaleview;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private ArrayList<DayBean> dayBeans;
    private TimePickerView tpv;
    private TextView tv1;
    private TextView tv2;
    private TextView tv3;
    private EditText et;
    private Button btn;
    private Button btnAdd;
    private SimpleDateFormat simpleDateFormat;
    private EditText etMin;
    private EditText etTime;
    private Button btnTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tpv = (TimePickerView)findViewById(R.id.tpv);
        tv1= (TextView) findViewById(R.id.tv1) ;
        tv2= (TextView) findViewById(R.id.tv2) ;
        tv3= (TextView) findViewById(R.id.tv3) ;
        et= (EditText) findViewById(R.id.toHour) ;
        etMin= (EditText) findViewById(R.id.toMinute) ;
        etTime= (EditText) findViewById(R.id.etTime) ;
        btnTime= (Button) findViewById(R.id.btnToTime) ;
        btn= (Button) findViewById(R.id.btnTo) ;
        btnAdd = (Button) findViewById(R.id.addEvent) ;

        initDayData();

        simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String hour = et.getText().toString().trim() ;
                String min = etMin.getText().toString().trim() ;
                if (!TextUtils.isEmpty(hour)&& !TextUtils.isEmpty(min)) {
//                    tpv.smoothScrollTo(Long.parseLong(p)*3600*1000);
                    long h = Long.parseLong(hour) ;
                    long m = Long.parseLong(min) ;

                    if (h>=0 && h<=24 && m>=0 && m<=60) {
                        if (h==24 && m>0) {
                            return;
                        }
                        long position = h*60*60*1000+m*60*1000;
                        tpv.smoothScrollTo(position);
                    }
                }
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tpv.setData(dayBeans);
            }
        });

        btnTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tpv.smoothScrollTo2(Long.parseLong(etTime.getText().toString().trim()));
            }
        });

        initPickerViewConfig();

        Calendar c = Calendar.getInstance() ;

        tpv.setOnScaleListener(new TimePickerView.OnScaleListener() {
            @Override
            public void onScaleChanged(long scale) {
//                tv1.setText(scale+"====>>"+toHour(scale));

                Date date = new Date(scale);
                tv1.setText(scale+"====>>\n"+simpleDateFormat.format(date));
            }

            @Override
            public void onScaleStart(long start) {
                Date date = new Date(start);
                tv2.setText(start+"====>>\n"+simpleDateFormat.format(date));
            }

            @Override
            public void onScaleEnd(long end) {
                Date date = new Date(end);
                tv3.setText(end+"====>>\n"+simpleDateFormat.format(date));
            }
        });
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnSelectData:
                Calendar mycalendar=Calendar.getInstance();
                Date mydate=new Date();
                mycalendar.setTime(mydate);

                int year = mycalendar.get(Calendar.YEAR);
                int month = mycalendar.get(Calendar.MONTH);
                int day = mycalendar.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dpd=new DatePickerDialog(MainActivity.this,Datelistener,year,month,day);
                dpd.show();
                break;
        }
    }

    private void initPickerViewConfig() {
        tpv.setShowBorderLine(true);
        tpv.addEventType(0, Color.parseColor("#20A66B"));
        tpv.addEventType(1, Color.parseColor("#20A66B"));
        tpv.addEventType(2, Color.parseColor("#bbcc00"));
        tpv.addEventType(3, Color.parseColor("#c3c3c3"));

        Calendar cal = Calendar.getInstance() ;
        cal.setTime(new Date());

        tpv.setCurrentData(cal.get(Calendar.YEAR),cal.get(Calendar.MONTH)+1 ,cal.get(Calendar.DAY_OF_MONTH));
        tpv.immediatelyScrollTo(cal.getTimeInMillis());
//        tpv.smoothScrollTo2(1472429400);
//        tpv.smoothScrollTo2(cal.getTimeInMillis());
    }

//    private void initDayData() {
//        dayBeans = new ArrayList<>() ;
//        for (int i=1;i<140;i++) {
//            DayBean db = new DayBean() ;
//            db.setStartNum(i*10*60*1000);
//            db.setEndNum(i*10*60*1000+5*60*1000);
//            if (i%2 == 0 ) {
//                db.setType(0);
//            } else {
//                db.setType(1);
//            }
//            dayBeans.add(db) ;
//        }
//    }

    private void initDayData() {

        try {
            dayBeans = new ArrayList<>() ;
            SimpleDateFormat sdf =new SimpleDateFormat("yyyy-MM-dd");

            Calendar calendar = Calendar.getInstance() ;
            calendar.setTime(new Date());


            StringBuilder sb = new StringBuilder() ;
            sb.append(
                    calendar.get(Calendar.YEAR))
                    .append("-")
                    .append(calendar.get(Calendar.MONTH)+1)
                    .append("-")
                    .append(calendar.get(Calendar.DAY_OF_MONTH)) ;

            Toast.makeText(MainActivity.this,sb.toString(),Toast.LENGTH_SHORT).show();
            Date date = sdf.parse(sb.toString());
            long mStartTimeStemp = date.getTime();
            long mEndTimeStemp = mStartTimeStemp+24*60*60*1000;
            // 添加今天的数据
            for (int i=1;i<140;i++) {
                DayBean db = new DayBean() ;
                db.setStartNum(mStartTimeStemp+i*10*60*1000);
                db.setEndNum(mStartTimeStemp+i*10*60*1000+5*60*1000);
                if (i%2 == 0 ) {
                    db.setType(0);
                } else {
                    db.setType(1);
                }
                dayBeans.add(db) ;
            }
            // 添加明天的数据
            for (int j=0;j<30;j++) {
                DayBean db = new DayBean() ;
                db.setStartNum(mEndTimeStemp+j*30*60*1000);
                db.setEndNum(mEndTimeStemp+j*30*60*1000+5*60*1000);
                if (j%2 == 0 ) {
                    db.setType(2);
                } else {
                    db.setType(3);
                }
                dayBeans.add(db) ;
            }

        } catch (Exception e) {

        }
    }



    private DatePickerDialog.OnDateSetListener Datelistener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int myyear, int monthOfYear, int dayOfMonth) {
            tpv.setCurrentData(myyear,monthOfYear+1,dayOfMonth);

        }
    };

}
