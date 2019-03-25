package ir.ashkanabd.cina.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.Layout;
import android.text.Selection;
import android.util.AttributeSet;
import android.util.TypedValue;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import ir.ashkanabd.cina.R;

public class CodeEditor extends AppCompatEditText {

    private Paint currentLinePaint;
    private Paint lineNumberBackgroundPaint;
    private Paint lineNumberTextPaint;
    private TypedValue currentLineValue;
    private TypedValue lineNumberBackgroundValue;
    private TypedValue lineNumberTextValue;

    public CodeEditor(Context context) {
        super(context);
        initialize(context);
    }

    public CodeEditor(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize(context);
    }

    public CodeEditor(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(context);
    }

    /*
     * Initialize Paint for Line number and current line
     */
    public void initialize(Context context) {
        currentLineValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.editorCurrentLineTextColor, currentLineValue, true);
        lineNumberBackgroundValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.editorLineNumberBackground, lineNumberBackgroundValue, true);
        lineNumberTextValue = new TypedValue();
        context.getTheme().resolveAttribute(R.attr.editorLineNumberTextColor, lineNumberTextValue, true);
        currentLinePaint = new Paint(getPaint());
        currentLinePaint.setColor(currentLineValue.data);
        lineNumberBackgroundPaint = new Paint(getPaint());
        lineNumberBackgroundPaint.setColor(lineNumberBackgroundValue.data);
        lineNumberTextPaint = new Paint(getPaint());
        lineNumberTextPaint.setColor(lineNumberTextValue.data);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        /*
         * Change color of line number position
         */
        for (int i = 0; i < getLineCount(); i++) {
            canvas.drawRect(0, (i * getLineHeight()) + getPaddingTop() - 5, getPaddingLeft()
                    , ((i + 1) * getLineHeight() + 1) + getPaddingTop() + 5, lineNumberBackgroundPaint);
        }
        /*
         * Change current line color
         */
        int currentLine = getCurrentCursorLine();
        canvas.drawRect(0, (currentLine * getLineHeight()) + getPaddingTop() - 5, getWidth()
                , ((currentLine + 1) * getLineHeight() + 1) + getPaddingTop() + 5, currentLinePaint);
        /*
         * Draw main View
         */
        super.onDraw(canvas);
        /*
         * Put line number in there position
         */
        int baseline = getBaseline();
        for (int i = 0; i < getLineCount(); i++) {
            canvas.drawText(Integer.toString(i + 1), 10, baseline, lineNumberTextPaint);
            baseline += getLineHeight();
        }
    }

    /*
     * Get current line number from this view
     */
    public int getCurrentCursorLine() {
        int selectionStart = Selection.getSelectionStart(getText());
        Layout layout = getLayout();
        if (!(selectionStart == -1)) {
            return layout.getLineForOffset(selectionStart);
        }
        return -1;
    }

    public static float dpFromPx(final Context context, final float px) {
        return px / context.getResources().getDisplayMetrics().density;
    }

    public static float pxFromDp(final Context context, final float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }
}
