package ir.ashkanabd.cina.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.Layout;
import android.text.Selection;
import android.util.AttributeSet;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;

public class CodeEditor extends AppCompatEditText {
    public CodeEditor(Context context) {
        super(context);
    }

    public CodeEditor(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CodeEditor(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Paint paint = getPaint();
        paint.setColor(Color.parseColor("#FFFFFAD6"));

        int currentLine = getCurrentCursorLine();
        canvas.drawRect(0, (currentLine * getLineHeight()) + getPaddingTop() - 5, getWidth()
                , ((currentLine + 1) * getLineHeight() + 1) + getPaddingTop() + 5, paint);
        super.onDraw(canvas);
    }

    public int getCurrentCursorLine() {
        int selectionStart = Selection.getSelectionStart(getText());
        Layout layout = getLayout();
        if (!(selectionStart == -1)) {
            return layout.getLineForOffset(selectionStart);
        }
        return -1;
    }
}
