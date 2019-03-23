package ir.ashkanabd.cina;

import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.rey.material.widget.EditText;
import com.rey.material.widget.Spinner;

public class SettingActivity extends AppCompatActivity {

    private Spinner themeSpinner;
    private EditText cCompileParams;
    private EditText cppCompileParams;
    private SharedPreferences sharedPreferences;
    private ArrayAdapter<String> spinnerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        this.sharedPreferences = getSharedPreferences("compile_params", MODE_PRIVATE);
        findView();
        prepareActivity();
    }

    private void findView() {
        themeSpinner = findViewById(R.id.theme_spinner_setting_layout);
        cCompileParams = findViewById(R.id.c_compile_params_setting_layout);
        cppCompileParams = findViewById(R.id.cpp_compile_params_setting_layout);
    }

    private void prepareActivity() {
        cCompileParams.setText(sharedPreferences.getString("c", ""));
        cppCompileParams.setText(sharedPreferences.getString("c++", ""));
        spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        spinnerAdapter.add("Light");
        spinnerAdapter.add("Dark");
        themeSpinner.setAdapter(spinnerAdapter);
        themeSpinner.setOnItemSelectedListener(this::spinnerItemSelected);
    }

    public void spinnerItemSelected(Spinner parent, View view, int position, long id) {
        // TODO: 3/23/19 change app theme
        Log.e("CinA", spinnerAdapter.getItem(position));
    }

    @Override
    public void onBackPressed() {
        this.sharedPreferences.edit().putString("c", cCompileParams.getText().toString()).apply();
        this.sharedPreferences.edit().putString("c++", cppCompileParams.getText().toString()).apply();
        super.onBackPressed();
    }
}
