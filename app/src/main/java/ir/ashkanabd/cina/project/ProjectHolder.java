package ir.ashkanabd.cina.project;

import android.view.View;
import android.widget.TextView;
import androidx.appcompat.widget.AppCompatTextView;
import co.dift.ui.SwipeToAction;
import ir.ashkanabd.cina.R;

public class ProjectHolder extends SwipeToAction.ViewHolder<Project> {

    private AppCompatTextView projectName;
    private AppCompatTextView projectDescription;
    private AppCompatTextView projectLang;

    public ProjectHolder(View v) {
        super(v);
        projectName = v.findViewById(R.id.item_project_name);
        projectDescription = v.findViewById(R.id.item_project_description);
        projectLang = v.findViewById(R.id.item_project_lang);
    }

    public AppCompatTextView getProjectName() {
        return projectName;
    }

    public AppCompatTextView getProjectDescription() {
        return projectDescription;
    }

    public AppCompatTextView getProjectLang() {
        return projectLang;
    }
}
