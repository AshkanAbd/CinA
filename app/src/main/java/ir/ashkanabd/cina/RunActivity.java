package ir.ashkanabd.cina;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import es.dmoral.toasty.Toasty;
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

    /*
     * Setup and run program on background thread.
     */
    private void run() {
        try {
            gccRun = new GccRun(this, runningProject);
            gccRun.setOnUpdateTask(this::setOutput);
            gccRun.setOnPostTask(this::setEnd);
            gccRun.execute();
        } catch (Exception e) {
            Toasty.error(this, this.getString(R.string.run_compiled_file_error), Toasty.LENGTH_LONG, true).show();
        }
    }

    /*
     * Method that calls after running.
     */
    private void setEnd(Object o) {
        int exitValue = (int) o;
        String outputTextString = outputTextView.getText().toString();
        String msg = outputTextString + "\nProgram finished with exit code " + exitValue + ".\n";
        outputTextView.setText(msg);
        userEditText.setEnabled(false);
    }

    /*
     * Method that calls on running progress(Show program output in UI thread).
     */
    private void setOutput(Object... o) {
        String str = (String) o[0];
        String msg = outputTextView.getText().toString() + str;
        outputTextView.setText(msg);
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

            /*
             * Detect EOL and clear edit text and put it on text view(user can't change it).
             * Then send it to background thread for program.
             */
            @Override
            public void afterTextChanged(Editable s) {
                String str = s.toString();
                if (str.endsWith("\n")) {
                    String msg = userTextView.getText().toString() + str;
                    userTextView.setText(msg);
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
