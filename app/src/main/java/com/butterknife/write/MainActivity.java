package com.butterknife.write;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import com.butterknife.ButterKnife;
import com.butterknife.Unbinder;
import com.butterknife.annotations.BindView;

/**
 * Author: 信仰年轻
 * Date: 2021-06-18 14:48
 * Email: hydznsqk@163.com
 * Des:
 */
public class MainActivity extends AppCompatActivity {

    @BindView(R.id.tv1)
    TextView textView1;

    @BindView(R.id.tv2)
    TextView textView2;

    private Unbinder mUnbinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mUnbinder = ButterKnife.bind(this);
        textView1.setText("今天天气真晴朗");
        textView2.setText("适合去泡妹");
    }

    @Override
    protected void onDestroy() {
        mUnbinder.unbind();
        super.onDestroy();
    }

}
