package ir.ashkanabd.cina;

import android.content.SharedPreferences;
import android.util.Log;
import android.widget.ArrayAdapter;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.rey.material.widget.EditText;
import com.rey.material.widget.Spinner;

public class SettingActivity extends AppCompatActivity {

    private Spinner themeSpinner;
    private Spinner langSpinner;
    private EditText cCompileParams;
    private EditText cppCompileParams;
    private SharedPreferences compilePreferences;
    private SharedPreferences appearancePreferences;
    private ArrayAdapter<String> themeSpinnerAdapter;
    private ArrayAdapter<String> langSpinnerAdapter;
    private String lang, theme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setAppAppearance();
        setContentView(R.layout.activity_setting);
        this.compilePreferences = getSharedPreferences("compile_params", MODE_PRIVATE);
        findView();
        prepareActivity();
    }

    /*
     * Get app appearance from shared preferences and set it.
     */
    private void setAppAppearance() {
        appearancePreferences = getSharedPreferences("appearance", MODE_PRIVATE);
        lang = appearancePreferences.getString("lang", "EN");
        theme = appearancePreferences.getString("theme", "light");
        // TODO: 3/23/19 Set lang to App language and theme to App theme
    }

    private void findView() {
        themeSpinner = findViewById(R.id.theme_spinner_setting_layout);
        langSpinner = findViewById(R.id.lang_spinner_setting_layout);
        cCompileParams = findViewById(R.id.c_compile_params_setting_layout);
        cppCompileParams = findViewById(R.id.cpp_compile_params_setting_layout);
    }

    /*
     * Prepare views of activity that got from SharedPreferences
     */
    private void prepareActivity() {
        cCompileParams.setText(compilePreferences.getString("c", ""));
        cppCompileParams.setText(compilePreferences.getString("c++", ""));
        themeSpinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        themeSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        themeSpinnerAdapter.add("Light");
        themeSpinnerAdapter.add("Dark");
        themeSpinner.setAdapter(themeSpinnerAdapter);
        themeSpinner.setOnItemSelectedListener((_a, _b, position, _c) -> themeSpinnerItemSelected(position));
        themeSpinner.setSelection(themeSpinnerAdapter.getPosition(theme));
        langSpinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item);
        langSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
        langSpinnerAdapter.add("English");
        langSpinnerAdapter.add("Persian");
        langSpinner.setAdapter(langSpinnerAdapter);
        langSpinner.setOnItemSelectedListener((_a, _b, position, _c) -> langSpinnerItemSelected(position));
        langSpinner.setSelection(langSpinnerAdapter.getPosition(lang.equals("EN") ? "English" : "Persian"));
    }

    /*
     * Theme spinner callback
     */
    public void themeSpinnerItemSelected(int position) {
        // TODO: 3/23/19 change app theme
        Log.e("CinA", themeSpinnerAdapter.getItem(position));
    }

    /*
     * Language spinner callback
     */
    public void langSpinnerItemSelected(int position) {
        // TODO: 3/23/19 change app language
        Log.e("CinA", langSpinnerAdapter.getItem(position));
    }

    @Override
    public void onBackPressed() {
        /*
         * Save changes in SharedPreferences
         */
        this.compilePreferences.edit().putString("c", cCompileParams.getText().toString()).apply();
        this.compilePreferences.edit().putString("c++", cppCompileParams.getText().toString()).apply();
        this.appearancePreferences.edit().putString("lang", langSpinner.getSelectedItem().equals("English") ? "EN" : "FA").apply();
        this.appearancePreferences.edit().putString("theme", ((String) themeSpinner.getSelectedItem())).apply();
        super.onBackPressed();
    }
}
