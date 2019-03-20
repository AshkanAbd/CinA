package ir.ashkanabd.cina.backgroundTasks;


import android.os.AsyncTask;
import androidx.annotation.Nullable;
import com.afollestad.materialdialogs.MaterialDialog;

public class StartTask extends AsyncTask<Void, Void, Void> {

    private MaterialDialog loadingDialog;
    private OnStartTask onStartTask;
    private OnPostStartTask onPostStartTask;

    public StartTask(@Nullable MaterialDialog loadingDialog) {
        this.loadingDialog = loadingDialog;
    }

    public void setTasks(OnStartTask onStartTask, OnPostStartTask onPostStartTask) {
        this.onStartTask = onStartTask;
        this.onPostStartTask = onPostStartTask;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        this.onStartTask.onStart();
        return null;
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
        onPostStartTask.postStartTask();
        if (loadingDialog != null)
            loadingDialog.dismiss();
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
    }
}
