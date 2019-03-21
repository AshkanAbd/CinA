package ir.ashkanabd.cina.backgroundTasks;

import com.afollestad.materialdialogs.MaterialDialog;

import androidx.annotation.Nullable;

public class GccTask extends CinaBackgroundTask<Void, Object, Object> {

    private MaterialDialog loadingDialog;

    public GccTask(@Nullable MaterialDialog loadingDialog) {
        this.loadingDialog = loadingDialog;
    }

    @Override
    protected Object doInBackground(Void... voids) {
        return super.doInBackground(voids);
    }

    @Override
    protected void onPreExecute() {
        if (loadingDialog != null)
            loadingDialog.show();
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Object str) {
        super.onPostExecute(str);
        if (loadingDialog != null)
            loadingDialog.setCancelable(true);
    }
}
