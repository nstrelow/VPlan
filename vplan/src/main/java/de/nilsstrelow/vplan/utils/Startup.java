package de.nilsstrelow.vplan.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.view.Display;
import android.view.ViewGroup;
import android.widget.Toast;

import com.espian.showcaseview.OnShowcaseEventListener;
import com.espian.showcaseview.ShowcaseView;
import com.espian.showcaseview.ShowcaseViewBuilder;
import com.espian.showcaseview.targets.ActionItemTarget;
import com.espian.showcaseview.targets.ViewTarget;

import de.nilsstrelow.vplan.R;
import de.nilsstrelow.vplan.activities.Settings;

/**
 * methods needed on startup
 * Created by djnilse on 13.04.2014.
 */
public class Startup implements OnShowcaseEventListener {

    public static final String UPDATE_NOTIFICATION = "showed_get_update_notifications";
    public static final String NEW_VERSION_MSG = "new_version_msg" + "290";
    private Activity activity;
    private SharedPreferences sharedPreferences;
    private boolean isTutorialMode = false;
    private boolean isNewVersion = false;
    private ShowcaseView homeShowcaseView;

    public Startup(Activity activity, SharedPreferences sharedPreferences) {
        this.activity = activity;
        this.sharedPreferences = sharedPreferences;
        isTutorialMode = sharedPreferences.getBoolean(Settings.SHOW_TUTORIAL_PREF, true);
        isNewVersion = sharedPreferences.getBoolean(Startup.NEW_VERSION_MSG, true);
    }

    public void start() {
        if (isTutorialMode)
            setupRefreshGuide();
    }

    public boolean isTutorialMode() {
        return isTutorialMode;
    }

    public boolean isNewVersion() {
        return isNewVersion;
    }

    public void hideShowcaseView() {
        if (homeShowcaseView != null)
            homeShowcaseView.hide();
    }

    public void setupNewVersionGuide() {

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(NEW_VERSION_MSG, false);
        editor.commit();

        final SharedPreferences s = sharedPreferences;

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setCancelable(false);
        builder.setTitle("Version 2.9 : Welcome ERS");
        builder.setMessage(
                "*Vertretungsplan für die Ernst-Reuter-Schule I\n" +
                        "*Schulliste im Seitenmenu (NavigationDrawer)\n\n\n"
        );

        builder.setPositiveButton("One week left", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(activity, "You should go where you belong", Toast.LENGTH_LONG).show();
            }
        });

        Dialog dialog = builder.create();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (s.getBoolean(UPDATE_NOTIFICATION, true)) {
                    AlertDialog switchUpdateNotification = new AlertDialog.Builder(activity)
                            .setTitle(activity.getResources().getString(R.string.get_update_notifications_title))
                            .setMessage(activity.getResources().getString(R.string.get_update_notifications_msg))
                            .setCancelable(false)
                            .setPositiveButton("Einschalten", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    SharedPreferences.Editor editor = s.edit();
                                    editor.putBoolean(Settings.CHECK_FOR_UPDATE, true);
                                    editor.commit();
                                }
                            })
                            .setNegativeButton("Ausschalten", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    SharedPreferences.Editor editor = s.edit();
                                    editor.putBoolean(Settings.CHECK_FOR_UPDATE, false);
                                    editor.commit();

                                    Toast.makeText(activity, "Solltest du dich umentscheiden, die Benachrichtigungen lassen sich auch in den Einstellungen aktivieren.", Toast.LENGTH_LONG).show();
                                }
                            })
                            .create();
                    switchUpdateNotification.show();
                    //  Remember that this was now shown
                    SharedPreferences.Editor editor = s.edit();
                    editor.putBoolean(UPDATE_NOTIFICATION, false);
                    editor.commit();
                }
            }
        });
        dialog.show();

    }

    private void setupRefreshGuide() {

        ShowcaseView.ConfigOptions co = new ShowcaseView.ConfigOptions();
        co.hideOnClickOutside = false;
        co.block = true;
        co.shotType = ShowcaseView.TYPE_ONE_SHOT;
        ShowcaseViewBuilder builder = new ShowcaseViewBuilder(activity);
        builder.setConfigOptions(co);
        builder.setText(R.string.refresh_button_showcase_title, R.string.refresh_button_showcase_message);
        ActionItemTarget refreshItemTarget = new ActionItemTarget(activity, R.id.action_refresh);
        ShowcaseView showcaseView = builder.build();
        showcaseView.setShowcase(refreshItemTarget, true);
        showcaseView.setOnShowcaseEventListener(this);
        showcaseView.setScaleMultiplier(0.5f);
        showcaseView.setTag("0");
        ((ViewGroup) activity.getWindow().getDecorView()).addView(showcaseView);
    }

    private void setupDrawerGuide() {

        ShowcaseView.ConfigOptions co = new ShowcaseView.ConfigOptions();
        co.hideOnClickOutside = false;
        co.shotType = ShowcaseView.TYPE_ONE_SHOT;
        co.noButton = false;
        ShowcaseViewBuilder builder = new ShowcaseViewBuilder(activity);
        builder.setConfigOptions(co);
        builder.setText(R.string.home_button_showcase_title, R.string.home_button_showcase_message);

        Display display = activity.getWindowManager().getDefaultDisplay();
        int width = display.getWidth() / 2;  // deprecated
        int height = display.getHeight() / 2;  // deprecated

        homeShowcaseView = builder.build();
        homeShowcaseView.setShowcaseItem(ShowcaseView.ITEM_ACTION_HOME, android.R.id.home, activity);
        homeShowcaseView.setOnShowcaseEventListener(this);
        homeShowcaseView.setScaleMultiplier(0.7f);
        homeShowcaseView.animateGesture(0, height / 2, width / 2, height / 2);
        homeShowcaseView.setTag("1");
        ((ViewGroup) activity.getWindow().getDecorView()).addView(homeShowcaseView);

    }

    public void setupSpinnerGuide() {

        ShowcaseView.ConfigOptions co = new ShowcaseView.ConfigOptions();
        co.hideOnClickOutside = false;
        co.shotType = ShowcaseView.TYPE_ONE_SHOT;
        co.noButton = false;
        ShowcaseViewBuilder builder = new ShowcaseViewBuilder(activity);
        builder.setConfigOptions(co);
        builder.setText(R.string.actionbar_spinner_showcase_title, R.string.actionbar_spinner_showcase_message);

        homeShowcaseView = builder.build();
        homeShowcaseView.setShowcaseItem(ShowcaseView.ITEM_SPINNER, 0, activity);
        homeShowcaseView.setOnShowcaseEventListener(this);
        homeShowcaseView.setScaleMultiplier(0.4f);
        homeShowcaseView.setTag("2");
        ((ViewGroup) activity.getWindow().getDecorView()).addView(homeShowcaseView);

    }

    public void setupSwipeGuide() {
        ShowcaseView.ConfigOptions co = new ShowcaseView.ConfigOptions();
        co.shotType = ShowcaseView.TYPE_ONE_SHOT;
        ShowcaseViewBuilder builder = new ShowcaseViewBuilder(activity);
        builder.setConfigOptions(co);
        builder.setText(R.string.swipe_gesture_showcase_title, R.string.swipe_gesture_showcase_message);

        Display display = activity.getWindowManager().getDefaultDisplay();
        int width = display.getWidth() / 2;  // deprecated
        int height = display.getHeight() / 2;  // deprecated

        ViewTarget swipeTarget = new ViewTarget(R.id.pager, activity);
        ShowcaseView showcaseView = builder.build();
        showcaseView.setShowcase(swipeTarget, true);
        showcaseView.setOnShowcaseEventListener(this);
        showcaseView.animateGesture(width + width / 2.7f, height, width - width / 2, height);
        showcaseView.setTag("3");
        ((ViewGroup) activity.getWindow().getDecorView()).addView(showcaseView);

    }

    private void setupColorGuide() {

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setCancelable(false);
        builder.setTitle(R.string.colors_showcase_title);
        builder.setMessage(R.string.colors_showcase_message);
        builder.setPositiveButton("Weiter", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                setupBemerkungsGuide();
                dialog.dismiss();
            }
        });

        Dialog dialog = builder.create();
        dialog.show();


    }

    private void setupBemerkungsGuide() {

        /* set isTutorialMode false to prevent setupSwipeGuide() to fire again and save that tutorial was showed */
        isTutorialMode = false;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(Settings.SHOW_TUTORIAL_PREF, false);
        editor.commit();

        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setCancelable(false);
        builder.setTitle(R.string.touch_info_showcase_title);
        builder.setMessage(R.string.touch_info_showcase_message);
        builder.setPositiveButton("Los gehts!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        Dialog dialog = builder.create();
        dialog.show();

    }

    @Override
    public void onShowcaseViewHide(ShowcaseView showcaseView) {
        switch (Integer.valueOf((String) showcaseView.getTag())) {
            case 0:
                setupDrawerGuide();
                break;
            case 1:
                break;
            case 2:
                setupSwipeGuide();
                break;
            case 3:
                setupColorGuide();
                break;
            case 4:
                setupBemerkungsGuide();
                break;

        }
    }

    @Override
    public void onShowcaseViewDidHide(ShowcaseView showcaseView) {

    }

    @Override
    public void onShowcaseViewShow(ShowcaseView showcaseView) {

    }
}
