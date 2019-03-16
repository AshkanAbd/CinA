package ir.ashkanabd.cina.view;

import android.content.Context;
import android.util.AttributeSet;
import com.tt.whorlviewlibrary.WhorlView;

public class LoadingView extends WhorlView {
    public LoadingView(Context context) {
        super(context);
        start();
    }

    public LoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        start();
    }

    public LoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        start();
    }
}
