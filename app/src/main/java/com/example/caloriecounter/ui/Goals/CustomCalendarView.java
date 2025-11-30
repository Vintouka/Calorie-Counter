package com.example.caloriecounter.ui.Goals;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class CustomCalendarView extends View {
    private Paint textPaint;
    private Paint circlePaint;
    private Paint outlinePaint;
    private Paint headerPaint;
    private Paint dayNamePaint;

    private Calendar calendar;
    private Calendar today;
    private int displayMonth;
    private int displayYear;

    private float cellWidth;
    private float cellHeight;
    private float monthHeaderHeight;
    private float dayNameHeight;

    private Set<String> datesWithEntries;
    private Map<String, Boolean> goalStatusMap;

    private OnDateClickListener dateClickListener;
    private GestureDetector gestureDetector;

    private static final String[] DAYS = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};

    public interface OnDateClickListener {
        void onDateClick(int year, int month, int day);
    }

    public CustomCalendarView(Context context) {
        super(context);
        init();
    }

    public CustomCalendarView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // Make view clickable
        setClickable(true);
        setFocusable(true);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(42f);
        textPaint.setTextAlign(Paint.Align.CENTER);

        circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        circlePaint.setStyle(Paint.Style.FILL);

        outlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        outlinePaint.setStyle(Paint.Style.STROKE);
        outlinePaint.setStrokeWidth(4f);
        outlinePaint.setColor(Color.BLUE);

        headerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        headerPaint.setColor(Color.DKGRAY);
        headerPaint.setTextSize(48f);
        headerPaint.setTextAlign(Paint.Align.CENTER);
        headerPaint.setFakeBoldText(true);

        dayNamePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dayNamePaint.setColor(Color.GRAY);
        dayNamePaint.setTextSize(36f);
        dayNamePaint.setTextAlign(Paint.Align.CENTER);

        calendar = Calendar.getInstance();
        today = Calendar.getInstance();
        displayMonth = calendar.get(Calendar.MONTH);
        displayYear = calendar.get(Calendar.YEAR);

        monthHeaderHeight = 100f;
        dayNameHeight = 80f;

        gestureDetector = new GestureDetector(getContext(), new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                handleTap(e.getX(), e.getY());
                return true;
            }

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }
        });
    }

    public void setDatesWithEntries(Set<String> dates) {
        this.datesWithEntries = dates;
        invalidate();
    }

    public void setGoalStatusMap(Map<String, Boolean> statusMap) {
        this.goalStatusMap = statusMap;
        invalidate();
    }

    public void setOnDateClickListener(OnDateClickListener listener) {
        this.dateClickListener = listener;
    }

    public void nextMonth() {
        displayMonth++;
        if (displayMonth > 11) {
            displayMonth = 0;
            displayYear++;
        }
        invalidate();
    }

    public void previousMonth() {
        displayMonth--;
        if (displayMonth < 0) {
            displayMonth = 11;
            displayYear--;
        }
        invalidate();
    }

    public void setMonth(int month, int year) {
        displayMonth = month;
        displayYear = year;
        invalidate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        cellWidth = w / 7f;
        // 6 rows for dates
        cellHeight = (h - monthHeaderHeight - dayNameHeight) / 6f;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw month/year header
        String monthYear = new SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                .format(getCalendarForMonth().getTime());
        canvas.drawText(monthYear, getWidth() / 2f, 70f, headerPaint);

        // Draw day names
        float dayNameY = monthHeaderHeight + 55f;
        for (int i = 0; i < 7; i++) {
            float x = i * cellWidth + cellWidth / 2f;
            canvas.drawText(DAYS[i], x, dayNameY, dayNamePaint);
        }

        // Draw dates
        Calendar cal = getCalendarForMonth();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1;
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        int row = 0;
        int col = firstDayOfWeek;

        for (int day = 1; day <= daysInMonth; day++) {
            float x = col * cellWidth + cellWidth / 2f;
            float y = monthHeaderHeight + dayNameHeight + row * cellHeight + cellHeight / 2f;

            String dateStr = formatDate(displayYear, displayMonth, day);
            boolean isToday = isSameDay(displayYear, displayMonth, day,
                    today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));
            boolean isPast = isPastDate(displayYear, displayMonth, day);

            // Draw background circle for days with entries
            if (datesWithEntries != null && datesWithEntries.contains(dateStr) && isPast) {
                Boolean metGoal = goalStatusMap != null ? goalStatusMap.get(dateStr) : null;
                if (metGoal != null) {
                    circlePaint.setColor(metGoal ? Color.GREEN : Color.RED);
                    circlePaint.setAlpha(100);
                    float radius = Math.min(cellWidth, cellHeight) / 3.5f;
                    canvas.drawCircle(x, y, radius, circlePaint);
                }
            }

            // Draw outline for today
            if (isToday) {
                float radius = Math.min(cellWidth, cellHeight) / 3.5f;
                canvas.drawCircle(x, y, radius, outlinePaint);
            }

            // Draw day number
            canvas.drawText(String.valueOf(day), x, y + 15f, textPaint);

            col++;
            if (col == 7) {
                col = 0;
                row++;
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
    }

    private void handleTap(float x, float y) {
        // Check if tap is in month header area (for future month/year picker)
        if (y < monthHeaderHeight) {
            return;
        }

        // Check if tap is in day names area
        if (y < monthHeaderHeight + dayNameHeight) {
            return;
        }

        int col = (int) (x / cellWidth);
        int row = (int) ((y - monthHeaderHeight - dayNameHeight) / cellHeight);

        if (row < 0 || col < 0 || col > 6 || row > 5) return;

        Calendar cal = getCalendarForMonth();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1;
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        int dayNumber = row * 7 + col - firstDayOfWeek + 1;

        if (dayNumber > 0 && dayNumber <= daysInMonth && dateClickListener != null) {
            dateClickListener.onDateClick(displayYear, displayMonth, dayNumber);
        }
    }

    private Calendar getCalendarForMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, displayYear);
        cal.set(Calendar.MONTH, displayMonth);
        cal.set(Calendar.DAY_OF_MONTH, 1);
        return cal;
    }

    private boolean isSameDay(int year1, int month1, int day1, int year2, int month2, int day2) {
        return year1 == year2 && month1 == month2 && day1 == day2;
    }

    private boolean isPastDate(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);

        return cal.before(today);
    }

    private String formatDate(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(cal.getTime());
    }
}