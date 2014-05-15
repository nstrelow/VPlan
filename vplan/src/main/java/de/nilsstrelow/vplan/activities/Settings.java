package de.nilsstrelow.vplan.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.larswerkman.holocolorpicker.ColorPicker;
import com.larswerkman.holocolorpicker.OpacityBar;
import com.larswerkman.holocolorpicker.SVBar;

import java.io.File;

import de.nilsstrelow.vplan.R;
import de.nilsstrelow.vplan.constants.Device;
import de.nilsstrelow.vplan.utils.Startup;

/**
 * Created by djnilse on 11/19/13.
 * This is the Settings PreferenceActivity for ZS Plan
 */
public class Settings extends PreferenceActivity implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String MY_SCHOOL_CLASS_PREF = "pref_my_school_class";
    public static final String MY_SCHOOL_PREF = "pref_my_school";
    public static final String SHOW_TUTORIAL_PREF = "pref_show_tutorial2";
    public static final String CHECK_FOR_UPDATE = "pref_check_for_update";
    public static final String CHANGELOG_PREF = "pref_changelog";
    public static final String ACTIONBAR_COLOR_PREF = "pref_ab_color";
    public static final String ACTIONBAR_ICON_STYLE_PREF = "pref_ab_icon_style";
    public static final String DELETE_PLANS_PREF = "pref_delete_plans";
    public static final String HIDE_COMMON_PREF = "pref_hide_common";

    SharedPreferences sp;
    int counter = 0;
    private Preference mTutorialPreference;
    private Preference mUpdatePreference;
    private Preference mHideCommonPreference;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        sp = PreferenceManager.getDefaultSharedPreferences(this);
        initializeTheme();

        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.settings);

        initializeActionBar();

        PreferenceScreen prefSet = getPreferenceScreen();

        mTutorialPreference = prefSet.findPreference(SHOW_TUTORIAL_PREF);
        mUpdatePreference = prefSet.findPreference(CHECK_FOR_UPDATE);
        mHideCommonPreference = prefSet.findPreference(HIDE_COMMON_PREF);

        String tutorialSum = sp.getBoolean(SHOW_TUTORIAL_PREF, true) ? getString(R.string.sum_tutorial_is_shown) : getString(R.string.sum_tutorial_not_shown);
        mTutorialPreference.setSummary(tutorialSum);
        String hideCommonSum = sp.getBoolean(HIDE_COMMON_PREF, true) ? getString(R.string.sum_hide_common) : getString(R.string.sum_not_hide_common);
        mHideCommonPreference.setSummary(hideCommonSum);
        String updateSum = sp.getBoolean(CHECK_FOR_UPDATE, true) ? getString(R.string.sum_check_for_update) : getString(R.string.sum_not_check_for_update);
        mUpdatePreference.setSummary(updateSum);


        setResult(Activity.RESULT_CANCELED);

    }


    private void initializeActionBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ActionBar actionBar = getActionBar();
            int color = sp.getInt(ACTIONBAR_COLOR_PREF, 0xffffff);
            actionBar.setBackgroundDrawable(new ColorDrawable(color));
            actionBar.setDisplayHomeAsUpEnabled(true);
            int titleId = Resources.getSystem().getIdentifier("action_bar_title", "id", "android");
            TextView actionBarTitle = null;
            if (titleId != 0) {
                actionBarTitle = (TextView) findViewById(titleId);
            }
            if (useLightIcons()) {
                actionBar.setIcon(getResources().getDrawable(R.drawable.ic_vplan_logo_white));
                if (actionBarTitle != null)
                    actionBarTitle.setTextColor(getResources().getColor(R.color.holo_white));
            } else {
                actionBar.setIcon(getResources().getDrawable(R.drawable.ic_vplan_logo));
                if (actionBarTitle != null)
                    actionBarTitle.setTextColor(getResources().getColor(R.color.holo_gray_dark));
            }
            actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        }
    }

    private void initializeTheme() {
        if (useLightIcons()) {
            setTheme(R.style.LightTheme);
        } else {
            setTheme(R.style.DarkTheme);
        }
    }

    public boolean useLightIcons() {
        return sp.getBoolean(Settings.ACTIONBAR_ICON_STYLE_PREF, false);
    }

    protected void onResume() {
        super.onResume();
        initializeActionBar();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        Preference pref = findPreference(key);
        if (pref instanceof ListPreference) {
            ListPreference listPref = (ListPreference) pref;
            pref.setSummary(listPref.getEntry());
        }
        if (key.equals(CHECK_FOR_UPDATE)) {
            String updateSum = sp.getBoolean(CHECK_FOR_UPDATE, true) ? getString(R.string.sum_check_for_update) : getString(R.string.sum_not_check_for_update);
            mUpdatePreference.setSummary(updateSum);
        }

        if (key.equals(HIDE_COMMON_PREF)) {
            String hideCommonSum = sp.getBoolean(HIDE_COMMON_PREF, true) ? getString(R.string.sum_hide_common) : getString(R.string.sum_not_hide_common);
            mHideCommonPreference.setSummary(hideCommonSum);
        }

        if (key.equals(ACTIONBAR_ICON_STYLE_PREF)) {
            initializeTheme();
            initializeActionBar();
            setResult(Activity.RESULT_OK);
        }

        if (key.equals(ACTIONBAR_COLOR_PREF)) {
            initializeTheme();
            initializeActionBar();
            setResult(Activity.RESULT_OK);
        }

        if (key.equals(SHOW_TUTORIAL_PREF)) {
            String tutorialSum = sp.getBoolean(SHOW_TUTORIAL_PREF, true) ? getString(R.string.sum_tutorial_is_shown) : getString(R.string.sum_tutorial_not_shown);
            pref.setSummary(tutorialSum);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showColorPicker() {
        View colorDlgView = getLayoutInflater().inflate(R.layout.color_picker_dialog, null);
        final AlertDialog colorDialog = new AlertDialog.Builder(this)
                .setView(colorDlgView)
                .create();
        // Get every button and add listener --> dismiss and change color(save) onClick
        colorDlgView.findViewById(R.id.bt_white).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNewColor(Color.rgb(0xff, 0xff, 0xff));
                colorDialog.dismiss();
            }
        });
        colorDlgView.findViewById(R.id.bt_black).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNewColor(Color.rgb(0x00, 0x00, 0x00));
                colorDialog.dismiss();
            }
        });
        colorDlgView.findViewById(R.id.bt_gray).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNewColor(Color.rgb(0xdd, 0xdd, 0xdd));
                colorDialog.dismiss();
            }
        });
        colorDlgView.findViewById(R.id.bt_blue_dark).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNewColor(Color.rgb(0x00, 0x99, 0xCC));
                colorDialog.dismiss();
            }
        });
        colorDlgView.findViewById(R.id.bt_blue_light).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNewColor(Color.rgb(0x33, 0xb5, 0xe5));
                colorDialog.dismiss();
            }
        });
        colorDlgView.findViewById(R.id.bt_green_dark).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNewColor(Color.rgb(0x66, 0x99, 0x00));
                colorDialog.dismiss();
            }
        });
        colorDlgView.findViewById(R.id.bt_green_light).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNewColor(Color.rgb(0x99, 0xcc, 0x00));
                colorDialog.dismiss();
            }
        });
        colorDlgView.findViewById(R.id.bt_red_dark).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNewColor(Color.rgb(0xcc, 0x00, 0x00));
                colorDialog.dismiss();
            }
        });
        colorDlgView.findViewById(R.id.bt_red_light).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNewColor(Color.rgb(0xff, 0x44, 0x44));
                colorDialog.dismiss();
            }
        });
        colorDlgView.findViewById(R.id.bt_purple_dark).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNewColor(Color.rgb(0x99, 0x33, 0xcc));
                colorDialog.dismiss();
            }
        });
        colorDlgView.findViewById(R.id.bt_purple_light).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNewColor(Color.rgb(0xaa, 0x66, 0xcc));
                colorDialog.dismiss();
            }
        });
        colorDlgView.findViewById(R.id.bt_orange_dark).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNewColor(Color.rgb(0xff, 0x88, 0x00));
                colorDialog.dismiss();
            }
        });
        colorDlgView.findViewById(R.id.bt_orange_light).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveNewColor(Color.rgb(0xff, 0xbb, 0x33));
                colorDialog.dismiss();
            }
        });
        colorDlgView.findViewById(R.id.bt_custom_color).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupCustomColorDialog();
                colorDialog.dismiss();
            }
        });
        colorDialog.show();

    }

    private void setupCustomColorDialog() {
        View customColorDlgView = getLayoutInflater().inflate(R.layout.custom_color_picker_dialog, null, false);
        final ColorPicker picker = (ColorPicker) customColorDlgView.findViewById(R.id.picker);
        SVBar svBar = (SVBar) customColorDlgView.findViewById(R.id.svbar);
        OpacityBar opacityBar = (OpacityBar) customColorDlgView.findViewById(R.id.opacitybar);
        picker.addSVBar(svBar);
        picker.addOpacityBar(opacityBar);
        picker.setShowOldCenterColor(false);
        picker.setColor(sp.getInt(ACTIONBAR_COLOR_PREF, 0xffffff));

        final AlertDialog customColorDialog = new AlertDialog.Builder(this)
                .setView(customColorDlgView)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        saveNewColor(picker.getColor());
                    }
                })
                .create();
        customColorDialog.show();
    }

    private void saveNewColor(int colorCode) {
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt(ACTIONBAR_COLOR_PREF, colorCode);
        editor.commit();
    }

    private void showDeletePlansDialog() {
        AlertDialog deletePlansDialog = new AlertDialog.Builder(this)
                .setMessage("Alle heruntergeladenen Vertretungspläne löschen ?\nDie Pläne werden beim nächsten Aktualisieren neu heruntergeladen.")
                .setPositiveButton("Ja", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        File vplanDir = new File(Device.VPLAN_PATH);
                        if (vplanDir.exists())
                            deleteFolder(vplanDir);
                    }
                })
                .setNegativeButton("Nein", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).create();
        deletePlansDialog.show();
    }

    private void deleteFolder(File folder) {
        File[] files = folder.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    deleteFolder(f);
                } else {
                    f.delete();
                }
            }
        }
        folder.delete();
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (preference.getKey().equals(DELETE_PLANS_PREF)) {
            showDeletePlansDialog();
        }
        if (preference.getKey().equals(ACTIONBAR_COLOR_PREF)) {
            showColorPicker();
        }
        if (preference.getKey().equals(ACTIONBAR_ICON_STYLE_PREF)) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                AlertDialog iconStyleDialog = new AlertDialog.Builder(this)
                        .setTitle("Style der Icons")
                        .setMessage("Wähle den Style der Icons passend zu der Farbe deiner Statusleiste")
                        .setInverseBackgroundForced(true)
                        .setPositiveButton("Hell", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SharedPreferences.Editor editor = sp.edit();
                                editor.putBoolean(ACTIONBAR_ICON_STYLE_PREF, true);
                                editor.commit();
                            }
                        })
                        .setNegativeButton("Dunkel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                SharedPreferences.Editor editor = sp.edit();
                                editor.putBoolean(ACTIONBAR_ICON_STYLE_PREF, false);
                                editor.commit();
                            }
                        }).create();
                iconStyleDialog.show();
            } else {
                initializeActionBar();
            }
        }
        if (preference.getKey().equals(CHANGELOG_PREF)) {
            new Startup(this, preferenceScreen.getSharedPreferences()).setupNewVersionGuide();

        }

        counter++;
        if (counter == 10) {
            Toast.makeText(this, "Something is about to happen...", Toast.LENGTH_SHORT).show();
        }
        if (counter == 13) {
            Toast.makeText(this, "GREAT POWER LIES WITHIN YOU", Toast.LENGTH_LONG).show();
            Dialog easterEgg = new Dialog(this, android.R.style.Theme_Translucent_NoTitleBar);
            easterEgg.setContentView(R.layout.keep_calm_dialog);
            WindowManager.LayoutParams lp = easterEgg.getWindow().getAttributes();
            lp.windowAnimations = R.style.EasterEggDialogAnimation;
            easterEgg.setCancelable(true);
            easterEgg.setCanceledOnTouchOutside(true);
            easterEgg.show();
            counter = 0;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }
}
