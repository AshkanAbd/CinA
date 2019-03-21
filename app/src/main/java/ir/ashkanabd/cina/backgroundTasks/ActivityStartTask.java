package ir.ashkanabd.cina.backgroundTasks;


import androidx.annotation.Nullable;
import com.afollestad.materialdialogs.MaterialDialog;

public class ActivityStartTask extends CinaBackgroundTask<Void, Void, Void> {

    private MaterialDialog loadingDialog;

    public ActivityStartTask(@Nullable MaterialDialog loadingDialog) {
        this.loadingDialog = loadingDialog;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        return super.doInBackground(voids);
    }

    @Override
    protected void onPreExecute() {
        if (loadingDialog != null)
            loadingDialog.show();
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        if (loadingDialog != null)
            loadingDialog.dismiss();
    }
}
