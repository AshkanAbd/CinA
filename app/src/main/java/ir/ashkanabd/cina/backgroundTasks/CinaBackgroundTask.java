package ir.ashkanabd.cina.backgroundTasks;

import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public abstract class CinaBackgroundTask<A, B, C> extends AsyncTask<A, B, C> {

    protected CinaBackgroundTask.OnBeforeTask onBeforeTask;
    protected CinaBackgroundTask.OnTaskStarted onTaskStarted;
    protected CinaBackgroundTask.OnPostTask onPostStartTask;
    protected CinaBackgroundTask.OnUpdateTask onUpdateTask;

    @Override
    protected C doInBackground(A... as) {
        return (C) onTaskStarted.onStart(as);
    }

    public void updateProgress(B... value) {
        this.publishProgress(value);
    }

    @Override
    protected void onPreExecute() {
        if (onBeforeTask != null)
            onBeforeTask.onBefore();
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(C c) {
        if (onPostStartTask != null)
            onPostStartTask.onPost(c);
        super.onPostExecute(c);
    }

    @Override
    protected void onProgressUpdate(B... values) {
        if (onUpdateTask != null)
            onUpdateTask.onUpdate(values);
        super.onProgressUpdate(values);
    }

    public void setOnBeforeTask(@Nullable CinaBackgroundTask.OnBeforeTask onBeforeTask) {
        this.onBeforeTask = onBeforeTask;
    }

    public void setOnTaskStarted(@NonNull CinaBackgroundTask.OnTaskStarted onTaskStarted) {
        this.onTaskStarted = onTaskStarted;
    }

    public void setOnPostStartTask(@Nullable CinaBackgroundTask.OnPostTask onPostStartTask) {
        this.onPostStartTask = onPostStartTask;
    }

    public void setOnUpdateTask(@Nullable CinaBackgroundTask.OnUpdateTask onUpdateTask) {
        this.onUpdateTask = onUpdateTask;
    }

    public interface OnTaskStarted {

        Object onStart(Object... a);

        default void update(CinaBackgroundTask task, Object... b) {
            task.publishProgress(b);
        }
    }

    public interface OnBeforeTask {
        void onBefore();
    }

    public interface OnPostTask {
        void onPost(Object c);
    }

    public interface OnUpdateTask {
        void onUpdate(Object... b);
    }
}
