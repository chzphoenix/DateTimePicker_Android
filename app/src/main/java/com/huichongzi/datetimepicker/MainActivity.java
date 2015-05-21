package com.huichongzi.datetimepicker;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import java.util.Calendar;

/**
 * Created by cuihz on 2015/5/21.
 */
public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SpinnerTimePickerDialog dialog = new SpinnerTimePickerDialog(this, new SpinnerTimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(Calendar calendar, int h, int m, int s) {
                Log.e("", calendar.getTimeInMillis() + "");
            }
        });
        dialog.show();
    }
}
