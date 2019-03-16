package ir.ashkanabd.cina.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.Layout;
import android.text.Selection;
import android.util.AttributeSet;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;

public class CodeEditor extends AppCompatEditText {

    private Paint p1, p2;

    public CodeEditor(Context context) {
        super(context);
        initialize();
    }

    public CodeEditor(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public CodeEditor(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    /*
     * Initialize Paint for Line number and current line
     */
    public void initialize() {
        p1 = new Paint(getPaint());
        p1.setColor(Color.parseColor("#FFFFFAD6"));
        p2 = new Paint(getPaint());
        p2.setColor(Color.parseColor("#FFF0F0F0"));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        /*
         * Change color of line number position
         */
        int baseline = getBaseline();
        for (int i = 0; i < getLineCount(); i++) {
            canvas.drawRect(0, (i * getLineHeight()) + getPaddingTop() - 5, getPaddingLeft()
                    , ((i + 1) * getLineHeight() + 1) + getPaddingTop() + 5, p2);
        }
        /*
         * Change current line color
         */
        int currentLine = getCurrentCursorLine();
        canvas.drawRect(0, (currentLine * getLineHeight()) + getPaddingTop() - 5, getWidth()
                , ((currentLine + 1) * getLineHeight() + 1) + getPaddingTop() + 5, p1);
        /*
         * Draw main View
         */
        super.onDraw(canvas);
        /*
         * Put line number in there position
         */
        baseline = getBaseline();
        for (int i = 0; i < getLineCount(); i++) {
            canvas.drawText(Integer.toString(i + 1), 10, baseline, getPaint());
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
}
