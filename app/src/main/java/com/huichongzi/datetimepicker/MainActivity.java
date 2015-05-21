package com.huichongzi.datetimepicker;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by cuihz on 2015/5/21.
 */
public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SpinnerDatePickerDialog dialog = new SpinnerDatePickerDialog(this, null, 2015, 4, 4);
        dialog.show();
    }
}
