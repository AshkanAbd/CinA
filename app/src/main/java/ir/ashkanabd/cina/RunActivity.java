package ir.ashkanabd.cina;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import ir.ashkanabd.cina.backgroundTasks.CinaBackgroundTask;
import ir.ashkanabd.cina.compileAndRun.GccRun;
import ir.ashkanabd.cina.project.Project;

public class RunActivity extends AppCompatActivity {

    private TextView outputTextView;
    private TextView userTextView;
    private EditText userEditText;
    private Project runningProject;
    private GccRun gccRun;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.run_layout);
        findViews();
        getProject();
        run();
    }

    private void run() {
        try {
            gccRun = new GccRun(this, runningProject);
            gccRun.setOnUpdateTask(this::setOutput);
            gccRun.setOnPostTask(this::setEnd);
            gccRun.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setEnd(Object o) {
        int exitValue = (int) o;
        String outputTextString = outputTextView.getText().toString();
        outputTextView.setText(outputTextString + "\nProgram finished with exit code " + exitValue + ".\n");
        userEditText.setEnabled(false);
    }

    private void setOutput(Object... o) {
        String str = (String) o[0];
        outputTextView.setText(outputTextView.getText() + str);
    }

    private void findViews() {
        outputTextView = findViewById(R.id.program_text_view_run_layout);
        userTextView = findViewById(R.id.user_text_view_run_layout);
        userEditText = findViewById(R.id.user_edit_text_run_layout);
        userEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String str = s.toString();
                if (str.endsWith("\n")) {
                    userTextView.setText(userTextView.getText().toString() + str);
                    gccRun.writeUserInput(str);
                    userEditText.setText("");
                }
            }
        });
    }

    private void getProject() {
        runningProject = (Project) getIntent().getSerializableExtra("project");
    }
}
