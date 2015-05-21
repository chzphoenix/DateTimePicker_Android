package com.huichongzi.datetimepicker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import java.util.Calendar;

public class SpinnerTimePickerDialog extends AlertDialog implements DialogInterface.OnClickListener, TimePicker.OnTimeChangedListener {

    private static final String HOUR = "hour";
    private static final String MINUTE = "minute";
    private static final String SECOND = "second";
    private final Calendar mCalendar;
    private final OnTimeSetListener mCallBack;
    private final TimePicker mTimePicker;

    public SpinnerTimePickerDialog(Context context, int theme, OnTimeSetListener callBack, int hour, int minute, int second) {
        super(context, theme);
        mCallBack = callBack;
        mCalendar = Calendar.getInstance();
        setButton(DialogInterface.BUTTON_POSITIVE,
                getContext().getText(R.string.date_time_done), (OnClickListener) this);
        setButton(DialogInterface.BUTTON_NEGATIVE,
                getContext().getText(R.string.date_time_cancel), (OnClickListener) this);
        setIcon(0);
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View view = inflater.inflate(R.layout.time_picker_dialog, null);
        setView(view);
        mTimePicker = (TimePicker) view.findViewById(R.id.timePicker);
        mTimePicker.init(hour, minute, second, this);
    }

    public SpinnerTimePickerDialog(Context context, OnTimeSetListener callBack, int hour, int minute, int second) {
        this(context, 0, callBack, hour, minute, second);
    }

    public SpinnerTimePickerDialog(Context context, OnTimeSetListener callBack) {
        this(context, 0, callBack, Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), Calendar.getInstance().get(Calendar.SECOND));
    }

    public TimePicker getTimePicker() {
        return mTimePicker;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            tryNotifyDateSet();
        }
    }

    @Override
    public void onTimeChanged(TimePicker view, int hour, int minute, int second) {
        mTimePicker.init(hour, minute, second, this);
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        int second = savedInstanceState.getInt(SpinnerTimePickerDialog.SECOND);
        int minute = savedInstanceState.getInt(SpinnerTimePickerDialog.MINUTE);
        int hour = savedInstanceState.getInt(SpinnerTimePickerDialog.HOUR);
        mTimePicker.init(hour, minute, second, this);
    }

    @Override
    public Bundle onSaveInstanceState() {
        Bundle state = super.onSaveInstanceState();
        state.putInt(SpinnerTimePickerDialog.HOUR, mTimePicker.getHour());
        state.putInt(SpinnerTimePickerDialog.MINUTE, mTimePicker.getMinute());
        state.putInt(SpinnerTimePickerDialog.SECOND, mTimePicker.getSecond());
        return state;
    }

    private void tryNotifyDateSet() {
        if (mCallBack != null) {
            mTimePicker.clearFocus();
            mCallBack.onTimeSet(mTimePicker.getCurrentTime(), mTimePicker.getHour(),
                    mTimePicker.getMinute(), mTimePicker.getSecond());
        }
    }

    public void updateTime(int hour, int minute, int second) {
        mTimePicker.updateTime(hour, minute, second);
    }

    public interface OnTimeSetListener {
        void onTimeSet(Calendar calendar, int hour, int minute, int second);
    }


    public void setTime(int hour, int minute, int second) {
        mCalendar.set(Calendar.HOUR_OF_DAY, hour);
        mCalendar.set(Calendar.MINUTE, minute);
        mCalendar.set(Calendar.SECOND, second);
        if (mTimePicker != null) {
           updateTime(hour, minute, second);
        }
    }
}
