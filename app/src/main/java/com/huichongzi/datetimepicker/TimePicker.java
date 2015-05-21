package com.huichongzi.datetimepicker;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.huichongzi.datetimepicker.base.NumberPicker;
import com.huichongzi.datetimepicker.base.NumberPickerEditText;

import java.util.Calendar;
import java.util.Locale;

public class TimePicker extends FrameLayout {
    private final Callback callback = new Callback();
    private final NumberPicker secondSpinner, minuteSpinner, hourSpinner;
    private final InputMethodManager inputMethodManager;
    private final LinearLayout spinners;
    private Locale locale;
    private OnTimeChangedListener onTimeChangedListener;
    private Calendar tempTime, currentTime;

    public TimePicker(Context context) {
        this(context, null);
    }

    public TimePicker(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.timePickerStyle);
    }

    public TimePicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.TimePicker, defStyle, R.style.Holo_TimePicker);
        boolean spinnersShown = a.getBoolean(
                R.styleable.TimePicker_spinnersShown, true);
        boolean forceShownState = a.getBoolean(
                R.styleable.TimePicker_forceShownState, false);
        int layoutResourceId = a.getResourceId(R.styleable.TimePicker_layout,
                R.layout.time_picker_holo);
        a.recycle();
        inputMethodManager = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        setLocale(Locale.getDefault());
        LayoutInflater.from(context).inflate(layoutResourceId, this, true);
        spinners = (LinearLayout) findViewById(R.id.pickers);
        secondSpinner = (NumberPicker) findViewById(R.id.second);
        minuteSpinner = (NumberPicker) findViewById(R.id.minute);
        hourSpinner = (NumberPicker) findViewById(R.id.hour);
        secondSpinner.setFormatter(NumberPicker.TWO_DIGIT_FORMATTER);
        secondSpinner.setMinValue(0);
        secondSpinner.setMaxValue(59);
        secondSpinner.setOnLongPressUpdateInterval(200);
        secondSpinner.setOnValueChangedListener(callback);
        minuteSpinner.setFormatter(NumberPicker.TWO_DIGIT_FORMATTER);
        minuteSpinner.setMinValue(0);
        minuteSpinner.setMaxValue(59);
        minuteSpinner.setOnLongPressUpdateInterval(200);
        minuteSpinner.setOnValueChangedListener(callback);
        hourSpinner.setMinValue(0);
        hourSpinner.setMaxValue(23);
        hourSpinner.setOnLongPressUpdateInterval(100);
        hourSpinner.setOnValueChangedListener(callback);
        if (spinnersShown || forceShownState) {
            setSpinnersShown(spinnersShown);
        } else {
            setSpinnersShown(true);
        }
        tempTime.clear();
        currentTime.setTimeInMillis(System.currentTimeMillis());
        init(currentTime.get(Calendar.HOUR_OF_DAY), currentTime.get(Calendar.MINUTE),
                currentTime.get(Calendar.SECOND), null);
        reorderSpinners();
    }

    private static Calendar getCalendarForLocale(Calendar oldCalendar,
                                                 Locale locale) {
        if (oldCalendar == null) {
            return Calendar.getInstance(locale);
        } else {
            final long currentTimeMillis = oldCalendar.getTimeInMillis();
            Calendar newCalendar = Calendar.getInstance(locale);
            newCalendar.setTimeInMillis(currentTimeMillis);
            return newCalendar;
        }
    }

    private static void setContentDescription(View parent, int childId,
                                              int textId) {
        if (parent == null) {
            return;
        }
        View child = parent.findViewById(childId);
        if (child != null) {
            child.setContentDescription(parent.getContext().getText(textId));
        }
    }

    private void checkInputState(NumberPicker... spinners) {
        for (NumberPicker spinner : spinners) {
            NumberPickerEditText input = spinner.getInputField();
            if (inputMethodManager.isActive(input)) {
                input.clearFocus();
                inputMethodManager.hideSoftInputFromWindow(getWindowToken(), 0);
            }
        }
    }

    @Override
    protected void dispatchRestoreInstanceState(
            SparseArray<Parcelable> container) {
        dispatchThawSelfOnly(container);
    }

    public int getSecond() {
        return currentTime.get(Calendar.SECOND);
    }

    public int getMinute() {
        return currentTime.get(Calendar.MINUTE);
    }

    public OnTimeChangedListener getOnTimeChangedListener() {
        return onTimeChangedListener;
    }

    public void setOnTimeChangedListener(
            OnTimeChangedListener onTimeChangedListener) {
        this.onTimeChangedListener = onTimeChangedListener;
    }

    public boolean getSpinnersShown() {
        return spinners.isShown();
    }

    public void setSpinnersShown(boolean shown) {
        spinners.setVisibility(shown ? View.VISIBLE : View.GONE);
    }

    public int getHour() {
        return currentTime.get(Calendar.HOUR_OF_DAY);
    }

    public Calendar getCurrentTime(){
        return currentTime;
    }

    public void init(int hour, int minute, int second,
                     OnTimeChangedListener onDateChangedListener) {
        setOnTimeChangedListener(onDateChangedListener);
        setTime(hour, minute, second);
        updateSpinners();
    }

    private boolean isNewDate(int hour, int minute, int second) {
        return currentTime.get(Calendar.HOUR_OF_DAY) != hour
                || currentTime.get(Calendar.MINUTE) != minute
                || currentTime.get(Calendar.SECOND) != second;
    }

    private void notifyDateChanged() {
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED);
        if (onTimeChangedListener != null) {
            onTimeChangedListener.onTimeChanged(this, getHour(), getMinute(),
                    getSecond());
        }
    }

    @Override
    @SuppressLint("NewApi")
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setLocale(newConfig.locale);
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        setTime(ss.hour, ss.minute, ss.second);
        updateSpinners();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        return new SavedState(super.onSaveInstanceState(), getHour(),
                getMinute(), getSecond());
    }

    private void pushSpinner(NumberPicker spinner, int spinnerCount, int i) {
        if (spinner.getParent() != null
                && spinner.getParent() instanceof ViewGroup) {
            ViewGroup parent = (ViewGroup) spinner.getParent();
            if (parent.getChildAt(i) != spinner) {
                parent.removeView(spinner);
                parent.addView(spinner);
                setImeOptions(spinner, spinnerCount, i);
            }
        }
    }

    private void reorderSpinners() {
        char[] order = DateFormat.getDateFormatOrder(getContext());
        final int spinnerCount = order.length;
        for (int i = 0; i < spinnerCount; i++) {
            switch (order[i]) {
                case DateFormat.HOUR_OF_DAY:
                    pushSpinner(secondSpinner, spinnerCount, i);
                    break;
                case DateFormat.MINUTE:
                    pushSpinner(minuteSpinner, spinnerCount, i);
                    break;
                case DateFormat.SECONDS:
                    pushSpinner(hourSpinner, spinnerCount, i);
                    break;
            }
        }
    }

    private void setTime(int hour, int minute, int second) {
        Calendar calendar = Calendar.getInstance();
        currentTime.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH), hour, minute, second);
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (isEnabled() == enabled) {
            return;
        }
        super.setEnabled(enabled);
        secondSpinner.setEnabled(enabled);
        minuteSpinner.setEnabled(enabled);
        hourSpinner.setEnabled(enabled);
    }

    private void setImeOptions(NumberPicker spinner, int spinnerCount,
                               int spinnerIndex) {
        final int imeOptions;
        if (spinnerIndex < spinnerCount - 1) {
            imeOptions = EditorInfo.IME_ACTION_NEXT;
        } else {
            imeOptions = EditorInfo.IME_ACTION_DONE;
        }
        spinner.getInputField().setImeOptions(imeOptions);
    }

    public void setLocale(Locale locale) {
        if (locale == null || locale.equals(this.locale)) {
            return;
        }
        this.locale = locale;
        tempTime = TimePicker.getCalendarForLocale(tempTime, locale);
        currentTime = TimePicker.getCalendarForLocale(currentTime, locale);
    }

    public void updateTime(int hour, int minute, int second) {
        if (!isNewDate(hour, minute, second)) {
            return;
        }
        setTime(hour, minute, second);
        updateSpinners();
        notifyDateChanged();
    }

    private void updateInputState() {
        if (inputMethodManager != null) {
            checkInputState(hourSpinner, minuteSpinner, secondSpinner);
        }
    }

    private void updateSpinners() {
        hourSpinner.setValue(currentTime.get(Calendar.HOUR_OF_DAY));
        minuteSpinner.setValue(currentTime.get(Calendar.MINUTE));
        secondSpinner.setValue(currentTime.get(Calendar.SECOND));
    }

    public interface OnTimeChangedListener {
        void onTimeChanged(TimePicker view, int hour, int minute, int second);
    }

    private static class SavedState extends BaseSavedState {
        @SuppressWarnings("all")
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        private final int hour, minute, second;

        private SavedState(Parcel in) {
            super(in);
            hour = in.readInt();
            minute = in.readInt();
            second = in.readInt();
        }

        private SavedState(Parcelable superState, int hour, int minute, int second) {
            super(superState);
            this.hour = hour;
            this.minute = minute;
            this.second = second;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(hour);
            dest.writeInt(minute);
            dest.writeInt(second);
        }
    }

    private final class Callback implements NumberPicker.OnValueChangeListener,
            CalendarView.OnDateChangeListener {
        @Override
        public void onSelectedDayChange(CalendarView view, int hour, int minute, int second) {
            setTime(hour, minute, second);
            updateSpinners();
            notifyDateChanged();
        }

        @Override
        public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
            updateInputState();
            tempTime.setTimeInMillis(currentTime.getTimeInMillis());
            if (picker == secondSpinner) {
                if (oldVal == 59 && newVal == 0) {
                    tempTime.add(Calendar.SECOND, 1);
                } else if (oldVal == 0 && newVal == 59) {
                    tempTime.add(Calendar.SECOND, -1);
                } else {
                    tempTime.add(Calendar.SECOND, newVal - oldVal);
                }
            } else if (picker == minuteSpinner) {
                if (oldVal == 59 && newVal == 0) {
                    tempTime.add(Calendar.MINUTE, 1);
                } else if (oldVal == 0 && newVal == 59) {
                    tempTime.add(Calendar.MINUTE, -1);
                } else {
                    tempTime.add(Calendar.MINUTE, newVal - oldVal);
                }
            }  else if (picker == hourSpinner) {
                tempTime.set(Calendar.HOUR_OF_DAY, newVal);
            } else {
                return;
            }
            setTime(tempTime.get(Calendar.HOUR_OF_DAY), tempTime.get(Calendar.MINUTE),
                    tempTime.get(Calendar.SECOND));
            updateSpinners();
            notifyDateChanged();
        }
    }
}