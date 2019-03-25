package ir.ashkanabd.cina.view;

import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.ikimuhendis.ldrawer.ActionBarDrawerToggle;
import com.ikimuhendis.ldrawer.DrawerArrowDrawable;


/**
 * Actionbar stays after opening drawer
 * <br> support AppCompatActivity
 * <br> {@link #ActionBarDrawerToggle}
 * <br> override methods {@link #setActionBarUpIndicator()} and {@link #setActionBarDescription()}
 * <br> change {@link android.app.Activity getActionBar() }to {@link AppCompatActivity getSupportActionBar()}
 */
public class ActionBarDrawerToggleCompat extends ActionBarDrawerToggle {

    /**
     * Change from @see #Activity to  @see #AppCompatActivity because of Actionbar
     */
    protected AppCompatActivity mActivity;

    public ActionBarDrawerToggleCompat(AppCompatActivity activity, DrawerLayout drawerLayout, DrawerArrowDrawable drawerImage
            , int openDrawerContentDescRes, int closeDrawerContentDescRes) {
        super(activity, drawerLayout, com.ikimuhendis.ldrawer.R.drawable.ic_drawer, openDrawerContentDescRes, closeDrawerContentDescRes);
        mActivity = activity;
        mDrawerLayout = drawerLayout;
        mOpenDrawerContentDescRes = openDrawerContentDescRes;
        mCloseDrawerContentDescRes = closeDrawerContentDescRes;
        mDrawerImage = drawerImage;
        animateEnabled = true;
    }

    protected void setActionBarUpIndicator() {
        if (mActivity != null) {
            try {
                mActivity.getSupportActionBar().setHomeAsUpIndicator(mDrawerImage);
                return;
            } catch (Exception e) {
                Log.e("CinA", "setActionBarUpIndicator error", e);
            }

            final View home = mActivity.findViewById(android.R.id.home);
            if (home == null) {
                return;
            }

            final ViewGroup parent = (ViewGroup) home.getParent();
            final int childCount = parent.getChildCount();
            if (childCount != 2) {
                return;
            }

            final View first = parent.getChildAt(0);
            final View second = parent.getChildAt(1);
            final View up = first.getId() == android.R.id.home ? second : first;

            if (up instanceof ImageView) {
                ImageView upV = (ImageView) up;
                upV.setImageDrawable(mDrawerImage);
            }
        }
    }

    protected void setActionBarDescription() {
        if (mActivity != null && mActivity.getSupportActionBar() != null) {
            try {
                mActivity.getSupportActionBar().setHomeActionContentDescription(mDrawerLayout.isDrawerOpen(GravityCompat.START)
                        ? mOpenDrawerContentDescRes : mCloseDrawerContentDescRes);
                if (Build.VERSION.SDK_INT <= 19) {
                    mActivity.getSupportActionBar().setSubtitle(mActivity.getSupportActionBar().getSubtitle());
                }
            } catch (Exception e) {
                Log.e("CinA", "setActionBarUpIndicator", e);
            }
        }
    }
}
