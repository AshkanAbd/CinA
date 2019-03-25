package ir.ashkanabd.cina;

import android.content.SharedPreferences;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class PurchaseActivity extends AppCompatActivity {

    private SharedPreferences appearancePreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setAppAppearance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase);
    }

    /*
     * Get app appearance from shared preferences and set it.
     */
    protected void setAppAppearance() {
        appearancePreferences = getSharedPreferences("appearance", MODE_PRIVATE);
        String lang = appearancePreferences.getString("lang", "EN");
        String theme = appearancePreferences.getString("theme", "light");
        // TODO: 3/23/19 Set lang to App language and theme to App theme
    }

}
