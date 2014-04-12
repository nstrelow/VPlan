package de.nilsstrelow.vplan.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.espian.showcaseview.OnShowcaseEventListener;
import com.espian.showcaseview.ShowcaseView;
import com.espian.showcaseview.ShowcaseViewBuilder;
import com.espian.showcaseview.targets.ActionItemTarget;
import com.espian.showcaseview.targets.ViewTarget;
import com.google.analytics.tracking.android.EasyTracker;
import com.viewpagerindicator.CirclePageIndicator;

import java.io.File;
import java.io.FilenameFilter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import de.nilsstrelow.vplan.R;
import de.nilsstrelow.vplan.adapters.ClassListViewAdapter;
import de.nilsstrelow.vplan.adapters.DaysPagerAdapter;
import de.nilsstrelow.vplan.constants.HandlerMsg;
import de.nilsstrelow.vplan.fragments.FeedbackDialogFragment;
import de.nilsstrelow.vplan.helpers.ErrorMessage;
import de.nilsstrelow.vplan.helpers.SchoolDay;
import de.nilsstrelow.vplan.constants.Device;
import de.nilsstrelow.vplan.constants.Server;
import de.nilsstrelow.vplan.receivers.CheckForPlanBroadcastReceiver;
import de.nilsstrelow.vplan.tasks.DownloadVPlanTask;
import de.nilsstrelow.vplan.tasks.DownloadVertretungsplanTask;
import de.nilsstrelow.vplan.tasks.LoadVPlanTask;
import de.nilsstrelow.vplan.utils.DateUtils;
import de.nilsstrelow.vplan.utils.SchoolClassUtils;

public class VertretungsplanActivity extends ActionBarActivity implements ListView.OnItemClickListener, OnShowcaseEventListener {

    public static final String UPDATE_NOTIFICATION = "showed_get_update_notifications";
    public static final String NEW_VERSION_MSG = "new_version_msg" + "205";
    // Define the handler that receives messages from the thread and update the progress
    // vertretungsplan files from .zs-vertretunsplan
    public static Handler handler;
    public static SharedPreferences sharedPref;
    public static Typeface robotoBold;
    public static Typeface robotoBlack;
    public static String currentSchoolClassName;
    // UI items for ViewPager
    public static int NUM_PAGES;
    // more efficient than HashMap for mapping integers to objects
    public static SparseArray<SchoolDay> schoolDays = new SparseArray<SchoolDay>();
    public static Date[] dates;
    // item position of list
    public static int currentSchoolDay;
    public static int currentSchoolClass;
    public static String[] schoolClasses;
    // Duration of Crouton in milliseconds
    final int croutonDuration = 2000;
    // counter to see if every day was already checked
    int dayCounter = 0;
    int settingsRequestCode = 2433;
    int counter = 0;
    int randomEasterEggNumber = 0;
    // UI items for Actionbar
    private CharSequence mTitle;
    private CharSequence mDrawerTitle;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private DaysPagerAdapter mPagerAdapter;
    private ActionBarDrawerToggle mDrawerToggle;
    private ViewPager mPager;
    // MenuItem to refresh
    private Menu optionsMenu;
    // false if not loading
    private boolean isProgressLoading = false;
    private boolean isTutorialMode = true;
    private LoadVPlanTask loadVPlanTask;
    private ShowcaseView homeShowcaseView;
    private DownloadVPlanTask downloadVertretungsplanTask;
    private boolean isPlanLoaded = false;

    public static String getCurrentSchoolClassName() {
        return currentSchoolClassName;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

        isTutorialMode = sharedPref.getBoolean(Settings.SHOW_TUTORIAL_PREF, true);

        initTheme();

        setContentView(R.layout.activity_vertretungsplan);

        robotoBold = Typeface.createFromAsset(getAssets(),
                "Roboto-Bold.ttf");
        robotoBlack = Typeface.createFromAsset(getAssets(),
                "Roboto-Black.ttf");


        // set up random easteregg click counter
        Random rand = new Random();
        randomEasterEggNumber = rand.nextInt(6) + 4;

        schoolClasses = getResources().getStringArray(R.array.zs_classes);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerList.setOnItemClickListener(this);

        ListAdapter listAdapter = new ClassListViewAdapter(this, getResources().getStringArray(R.array.zs_classes));
        mDrawerList.setAdapter(listAdapter);

        final de.keyboardsurfer.android.widget.crouton.Configuration config
                = new de.keyboardsurfer.android.widget.crouton.Configuration.Builder()
                .setDuration(croutonDuration).build();

        handler = new Handler() {

            @SuppressLint("ResourceAsColor")
            // Create handleMessage function
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case HandlerMsg.LOADING:
                        supportInvalidateOptionsMenu();
                        isProgressLoading = true;
                        setRefreshActionButtonState(true);
                        break;
                    case HandlerMsg.DOWNLOADING:
                        supportInvalidateOptionsMenu();
                        isProgressLoading = true;
                        setSubtitle((String) msg.obj);
                        setRefreshActionButtonState(true);
                        break;
                    case HandlerMsg.FINISHED_LOADING:
                        // remove subtitle only if both are false
                        setSubtitle(null);
                        setRefreshActionButtonState(false);
                        isProgressLoading = false;
                        break;
                    case HandlerMsg.LOAD_VPLAN_MSG:
                        loadVPlan();
                        break;
                    case HandlerMsg.CROUTON_CONFIRM:

                        Style mCONFIRM = new Style.Builder()
                                .setBackgroundColor(R.color.holo_green_crouton)
                                .setConfiguration(config).build();
                        Crouton.makeText(VertretungsplanActivity.this, (String) msg.obj, mCONFIRM).show();
                        break;
                    case HandlerMsg.CROUTON_ALERT:
                        Style mAlert = new Style.Builder()
                                .setBackgroundColor(R.color.holo_red_crouton)
                                .setConfiguration(config).build();
                        Crouton.makeText(VertretungsplanActivity.this, (String) msg.obj, mAlert).show();
                        break;
                    case HandlerMsg.CROUTON_INFO:
                        Style mInfo = new Style.Builder()
                                .setBackgroundColor(R.color.holo_blue_crouton)
                                .setConfiguration(config).build();
                        Crouton.makeText(VertretungsplanActivity.this, (String) msg.obj, mInfo).show();
                        break;
                    case HandlerMsg.LOAD_STARTPAGE_MSG:
                        loadMyPlan();
                        setupViewPager();
                        break;
                    case HandlerMsg.UPDATE:
                        downloadVertretungsplanTask = new DownloadVPlanTask(VertretungsplanActivity.this, Device.VPLAN_PATH);
                        downloadVertretungsplanTask.execute();
                        break;
                    case HandlerMsg.ERROR:
                        ErrorMessage errorMessage = (ErrorMessage) msg.obj;
                        AlertDialog.Builder builder = new AlertDialog.Builder(VertretungsplanActivity.this)
                                .setTitle(errorMessage.getErrorTitle())
                                .setMessage(errorMessage.getErrorMessage())
                                .setCancelable(false)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                        if (errorMessage.showLinkToPlan()) {
                            builder.setNegativeButton("Zeige Vertretungsplanlinks", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Server.ZS_WEBSITE_URL));
                                    startActivity(browserIntent);
                                    dialog.dismiss();
                                }
                            });
                        }
                        AlertDialog errorDialog = builder.create();
                        errorDialog.show();
                        break;
                    case HandlerMsg.NOT_UPDATED:
                        // loadPlan if not yet loaded
                        if (!isPlanLoaded) {
                            loadVPlan();
                        }
                        break;
                }
            }
        };

        //deleteOldVPlans();

        // Set the list's click listener
        mTitle = mDrawerTitle = getTitle();

        initActionBar();

        initDrawer();

        if (isTutorialMode)
            setupRefreshGuide();
    }

    private void initDrawer() {
        int icon;
        if (useLightIcons()) {
            icon = R.drawable.ic_drawer_holo_dark;
        } else {
            icon = R.drawable.ic_drawer_holo_light;
        }

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
                mDrawerLayout, /* DrawerLayout object */
                icon, /* nav drawer image to replace 'Up' caret */
                R.string.navigation_drawer_open, /* "open drawer" description for accessibility */
                R.string.navigation_drawer_close /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle(mTitle);
                supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
                // fire swipeGuide when navigation drawer is closed
                if (isTutorialMode)
                    setupSwipeGuide();
            }

            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle(mDrawerTitle);
                supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()

                // hide overlay, when user clicked the navigation drawer
                if (isTutorialMode)
                    homeShowcaseView.hide();
            }
        };

        // Set the drawer toggle as the DrawerListener
        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    private void initActionBar() {
        // Set up the action bar.
        ActionBar actionBar = getSupportActionBar();
        int color = sharedPref.getInt(Settings.ACTIONBAR_COLOR_PREF, 0xffffff);
        actionBar.setBackgroundDrawable(new ColorDrawable(color));
        int titleId = Resources.getSystem().getIdentifier("action_bar_title", "id", "android");
        int subTitleId = Resources.getSystem().getIdentifier("action_bar_subtitle", "id", "android");
        TextView actionBarTitle = null;
        TextView actionBarSubTitle = null;
        if (titleId != 0) {
            actionBarTitle = (TextView) findViewById(titleId);
        }
        if (subTitleId != 0) {
            actionBarSubTitle = (TextView) findViewById(subTitleId);
        }
        if (useLightIcons()) {
            actionBar.setIcon(getResources().getDrawable(R.drawable.ic_zs_drawer_white));
            if (actionBarTitle != null)
                actionBarTitle.setTextColor(getResources().getColor(R.color.holo_white));
            if (actionBarSubTitle != null)
                actionBarSubTitle.setTextColor(getResources().getColor(R.color.holo_white));
        } else {
            actionBar.setIcon(getResources().getDrawable(R.drawable.ic_zs_drawer));
            if (actionBarTitle != null)
                actionBarTitle.setTextColor(getResources().getColor(R.color.holo_gray_dark));
            if (actionBarSubTitle != null)
                actionBarSubTitle.setTextColor(getResources().getColor(R.color.holo_gray_dark));
        }
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    private void initTheme() {
        if (useLightIcons()) {
            setTheme(R.style.LightTheme);
        } else {
            setTheme(R.style.DarkTheme);
        }
    }

    public boolean useLightIcons() {
        return sharedPref.getBoolean(Settings.ACTIONBAR_ICON_STYLE_PREF, false);
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
                setupColorGuide();
                break;
            case 3:
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

    private void setupRefreshGuide() {

        ShowcaseView.ConfigOptions co = new ShowcaseView.ConfigOptions();
        co.hideOnClickOutside = false;
        co.block = true;
        co.shotType = ShowcaseView.TYPE_ONE_SHOT;
        ShowcaseViewBuilder builder = new ShowcaseViewBuilder(this);
        builder.setConfigOptions(co);
        builder.setText(R.string.refresh_button_showcase_title, R.string.refresh_button_showcase_message);
        ActionItemTarget refreshItemTarget = new ActionItemTarget(this, R.id.action_refresh);
        ShowcaseView showcaseView = builder.build();
        showcaseView.setShowcase(refreshItemTarget, true);
        showcaseView.setOnShowcaseEventListener(this);
        showcaseView.setScaleMultiplier(0.5f);
        showcaseView.setTag("0");
        ((ViewGroup) this.getWindow().getDecorView()).addView(showcaseView);
    }

    private void setupDrawerGuide() {

        ShowcaseView.ConfigOptions co = new ShowcaseView.ConfigOptions();
        co.hideOnClickOutside = false;
        co.shotType = ShowcaseView.TYPE_ONE_SHOT;
        co.noButton = false;
        ShowcaseViewBuilder builder = new ShowcaseViewBuilder(this);
        builder.setConfigOptions(co);
        builder.setText(R.string.home_button_showcase_title, R.string.home_button_showcase_message);

        Display display = getWindowManager().getDefaultDisplay();
        int width = display.getWidth() / 2;  // deprecated
        int height = display.getHeight() / 2;  // deprecated

        homeShowcaseView = builder.build();
        homeShowcaseView.setShowcaseItem(ShowcaseView.ITEM_ACTION_HOME, android.R.id.home, this);
        homeShowcaseView.setOnShowcaseEventListener(this);
        homeShowcaseView.setScaleMultiplier(0.7f);
        homeShowcaseView.animateGesture(0, height / 2, width / 2, height / 2);
        homeShowcaseView.setTag("1");
        ((ViewGroup) this.getWindow().getDecorView()).addView(homeShowcaseView);

    }

    private void setupSwipeGuide() {
        ShowcaseView.ConfigOptions co = new ShowcaseView.ConfigOptions();
        co.shotType = ShowcaseView.TYPE_ONE_SHOT;
        ShowcaseViewBuilder builder = new ShowcaseViewBuilder(this);
        builder.setConfigOptions(co);
        builder.setText(R.string.swipe_gesture_showcase_title, R.string.swipe_gesture_showcase_message);

        Display display = getWindowManager().getDefaultDisplay();
        int width = display.getWidth() / 2;  // deprecated
        int height = display.getHeight() / 2;  // deprecated

        ViewTarget swipeTarget = new ViewTarget(R.id.pager, this);
        ShowcaseView showcaseView = builder.build();
        showcaseView.setShowcase(swipeTarget, true);
        showcaseView.setOnShowcaseEventListener(this);
        showcaseView.animateGesture(width + width / 2.7f, height, width - width / 2, height);
        showcaseView.setTag("2");
        ((ViewGroup) this.getWindow().getDecorView()).addView(showcaseView);

    }

    private void setupColorGuide() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(Settings.SHOW_TUTORIAL_PREF, false);
        editor.commit();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
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

    private void setupNewVersionGuide() {

        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(NEW_VERSION_MSG, false);
        editor.commit();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle("Version 2.5 : Heard you like colors");
        builder.setMessage("ÜBER 500 DOWNLOADS :D\n\nÄnderungen\n\n"
                        + "1. Statusbarfarbe: Wähle zwischen Standardfarben oder erstelle dir deine Eigene! \n"
                        + "1,5. Wähle den Iconstyle (hell oder dunkel), damit die Icons zu deiner Farbe passen\n"
                        + "2. Bemerkung wird jetzt unter dem Eintrag angezeigt\n"
                        + "3. Allg. Bemerkungen werden jetzt beim Scrollen ausgeblendet, Option dazu in den Einstellungen\n"
                        + "4. Menueintrag um den Vertretungsplan im Browser anzuzeigen, sollte es Probleme geben oder spezielle Pläne\n"
                        + "5. Überarbeitete Einstellungen\n"
                        + "6. Bugfixes und Optimierungen (Dienstag und so)\n\n"
                        + "NEWS:\n\nSchriftliches Abitur ist endlich vorbei und ich hoffe alle Abiturienten habts gut überstanden.\n"
                        + "Der Plan: (ZS) Plan an weitere Schule bringen, ich denke als erstes werde ich mit der ERS anfangen, mal sehen wie das läuft.\n"
                        + "Ich werde ein paar Mechanismen in der App ändern, welche mir möglichen machen werden, euch nur zu benachrichtigen, wenn sich etwas in EURER Klasse/Stufe ändert!\n"
                        + "Außerdem möchte ich die App stabiler machen, besonders für Geräte ab Android 4.0. Schreibt mir einfach ein Feedback, wenn ihr irgendwo Verbesserungsmöglichkeiten oder einfach nur nervige Sachen seht oder sendet mir einen Fehlerbericht mit einer kleinen Naricht was ihr gerade vor dem Absturz gemacht habt.\n"
                        + "Nicht zu vergessen: ZS PLAN HAT JETZT ÜBER 500 DOWNLOADS :D :D :D\n"
                        + "Ich danke allen, die dabei sind und in ZS Plan eine nützliche App gefunden haben.\n"
        );

        builder.setPositiveButton("Abitur vorbei ...", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(getApplication(), "Dobby ist jetzt FREI! :D", Toast.LENGTH_LONG).show();
            }
        });


        Dialog dialog = builder.create();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (sharedPref.getBoolean(UPDATE_NOTIFICATION, true)) {
                    AlertDialog switchUpdateNotification = new AlertDialog.Builder(VertretungsplanActivity.this)
                            .setTitle(getResources().getString(R.string.get_update_notifications_title))
                            .setMessage(getResources().getString(R.string.get_update_notifications_msg))
                            .setCancelable(false)
                            .setPositiveButton("Einschalten", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    SharedPreferences.Editor editor = sharedPref.edit();
                                    editor.putBoolean(Settings.CHECK_FOR_UPDATE, true);
                                    editor.commit();
                                }
                            })
                            .setNegativeButton("Ausschalten", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    SharedPreferences.Editor editor = sharedPref.edit();
                                    editor.putBoolean(Settings.CHECK_FOR_UPDATE, false);
                                    editor.commit();

                                    Toast.makeText(getApplication(), "Solltest du dich umentscheiden, die Benachrichtigungen lassen sich auch in den Einstellungen aktivieren.", Toast.LENGTH_LONG).show();
                                }
                            })
                            .create();
                    switchUpdateNotification.show();
                    //  Remember that this was now shown
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putBoolean(UPDATE_NOTIFICATION, false);
                    editor.commit();
                }
            }
        });
        dialog.show();

    }

    private void loadMyPlan() {
        try {
            String mySchoolClass = sharedPref.getString(Settings.MY_SCHOOL_CLASS_PREF, "0");
            if (!mySchoolClass.equals("0")) {
                currentSchoolClassName = mySchoolClass;
                int child;
                Calendar calendar = Calendar.getInstance();
                SchoolDay schoolToday = searchNextDay(calendar);
                int size = schoolToday.schoolClasses.size();
                for (int i = 0; i < size; i++) {
                    if (schoolToday.schoolClasses.get(i).equals(mySchoolClass)) {
                        // in that case i is the right group element
                        child = i;
                        selectClass(child);
                        break;
                    }
                }
                mDrawerList.setItemChecked(SchoolClassUtils.getClassIndex(schoolClasses, currentSchoolClassName), true);
                mDrawerList.setSelection(SchoolClassUtils.getClassIndex(schoolClasses, currentSchoolClassName));
                isPlanLoaded = true;
            }
        } catch (NullPointerException e) {
            if (!isRunning()) {
                Toast.makeText(this, R.string.no_plan_msg, Toast.LENGTH_LONG).show();
            }
        }
        if (sharedPref.getBoolean(NEW_VERSION_MSG, true))
            setupNewVersionGuide();
    }

    public SchoolDay searchNextDay(Calendar today) {
        if (dates.length != 0) {


            DateFormat df = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN);
            final String t = df.format(today.getTime());
            int length = dates.length;
            for (int i = 0; i < length; i++) {
                final String d = df.format(dates[i]);
                if (t.equals(d)) {
                    // in that case i is the right group element
                    return schoolDays.get(i);
                }
            }
            today.add(Calendar.DATE, 1);
        /*
        * exit with first date, if every day was already checked
        * also if it already surpassed dates.length, could prevent future bugs
        */
            dayCounter++;
            if (dayCounter >= dates.length) {
                return schoolDays.get(0);
            }

            return searchNextDay(today);
        } else {
            return schoolDays.get(0);
        }
    }

    private void deleteOldVPlans() {
        try {
            File vplanDir = new File(Device.VPLAN_PATH);
            File[] plans = vplanDir.listFiles(new FilenameFilter() {
                public boolean accept(File dir, String name) {
                    return name.toLowerCase(Locale.GERMANY).endsWith(".txt");
                }
            });
            DateFormat df = new SimpleDateFormat("dd.MM.yyyy", Locale.GERMAN);
            Calendar today = Calendar.getInstance();
            final String format = df.format(today.getTime());
            Date date = null;
            try {
                date = df.parse(format);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (vplanDir.exists()) {
                for (final File plan : plans) {
                    //long diff = timestamp.lastModified() - plan.lastModified();
                    //if (diff > 6 * 60 * 60 * 1000) {
                    //   plan.delete();
                    //}
                    Date planDate = DateUtils.parseDate(SchoolClassUtils.parseSchoolDay(plan.getName()));
                    // if date of today is "bigger" than date of plan
                    if (date != null && planDate != null) {
                        if (date.compareTo(planDate) == 1) {
                            plan.delete();
                        }
                        // getTime() are milliseconds, so make them to hours and look up if its already at 18
                        if ((System.currentTimeMillis() - planDate.getTime()) / 1000 / 60 / 60 >= 18) {
                            plan.delete();
                        }
                    }

                    if (!plan.getName().matches("(Mo|Di|Mi|Do|Fr)[0-9]{6}.txt")) {
                        //Toast.makeText(this, "It's not a plan: " + plan.getName(), Toast.LENGTH_SHORT).show();
                        plan.delete();
                    }
                }
            }
        } catch (Exception e) {
            //ErrorMessage errorMessage = new ErrorMessage(false, "Fehler ")
            // show no error message here
        }
    }

    public void loadVPlan() {
        File vplanDir = new File(Device.VPLAN_PATH);
        if (vplanDir.exists() && vplanDir.list().length > 0) {
            loadVPlanTask = new LoadVPlanTask(this);
            loadVPlanTask.execute(loadSchoolDayFiles());
        } // if dir or files are not found, than onResume() will take care of updating
    }

    @Override
    protected void onStart() {
        EasyTracker.getInstance(this).activityStart(this);
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.w("Mehtod", "onResume()");
        initActionBar();
        initDrawer();
        loadVPlan();
        downloadVertretungsplanTask = new DownloadVPlanTask(this, Device.VPLAN_PATH);
        downloadVertretungsplanTask.execute();
        startCheckForUpdate();
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
    }

    @Override
    protected void onDestroy() {
        Crouton.cancelAllCroutons();
        super.onDestroy();
    }

    public void startCheckForUpdate() {
        Intent intent = new Intent(this, CheckForPlanBroadcastReceiver.class);
        sendBroadcast(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        EasyTracker.getInstance(this).activityStop(this);
        loadVPlanTask.cancel(true);
        super.onStop();
    }

    private void setupViewPager() {
        // one page one day !
        NUM_PAGES = new File(Device.VPLAN_PATH).listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase(Locale.GERMANY).endsWith(".txt");
            }
        }).length;
        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setVisibility(ViewPager.VISIBLE);
        FragmentManager fragmentManager = getSupportFragmentManager();
        mPagerAdapter = new DaysPagerAdapter(fragmentManager);
        mPager.setAdapter(mPagerAdapter);
        mPager.setOffscreenPageLimit(NUM_PAGES - 1);

        //Bind the title indicator to the adapter
        CirclePageIndicator circlePageIndicator = (CirclePageIndicator) findViewById(R.id.indicator);
        circlePageIndicator.setViewPager(mPager);
        circlePageIndicator.setSnap(false);

        circlePageIndicator.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrollStateChanged(int state) {

            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {
            }

            @Override
            public void onPageSelected(int position) {
                final String replace = schoolDays.get(position).string.replace(".20", ".");
                setTitle("KL: " + currentSchoolClassName + "   " + replace);
                currentSchoolDay = position;
            }

        });
        mPager.setCurrentItem(currentSchoolDay);

    }

    public File[] loadSchoolDayFiles() {
        File vertretungsplanDir = new File(Device.VPLAN_PATH);
        return vertretungsplanDir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.toLowerCase(Locale.GERMANY).endsWith(".txt");
            }
        });

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mDrawerList.setItemChecked(position, true);
        mDrawerList.setSelection(position);
        currentSchoolClass = position;
        currentSchoolClassName = schoolClasses[position];
        selectClass(currentSchoolClass);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(Settings.MY_SCHOOL_CLASS_PREF, currentSchoolClassName);
        editor.commit();
    }

    /**
     * Swaps fragments in the main content view
     */
    private void selectClass(int schoolClass) {
        currentSchoolClass = schoolClass;
        setupViewPager();
        mDrawerLayout.closeDrawer(mDrawerList);
        mPager.setCurrentItem(0);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }

    public void setSubtitle(CharSequence subTitle) {
        getSupportActionBar().setSubtitle(subTitle);
    }

    void showFeedbackDialog() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment prev = getSupportFragmentManager().findFragmentByTag("dialog");
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        // Create and show the dialog.
        DialogFragment newFragment = FeedbackDialogFragment.newInstance();
        newFragment.show(ft, "dialog");
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (this.optionsMenu == null)
            this.optionsMenu = menu;
        setRefreshActionButtonState(isProgressLoading);
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //this.optionsMenu = menu;
        if (!mDrawerLayout.isDrawerOpen(R.id.left_drawer)) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.vertretungsplan, menu);
            colorRefresh(menu);
            restoreActionBar();
            return true;
        }

        return super.onCreateOptionsMenu(menu);
    }

    public void colorRefresh(Menu menu) {
        MenuItem refresh = menu.findItem(R.id.action_refresh);
        int icon;
        if (useLightIcons()) {
            icon = R.drawable.ic_action_refresh_holo_dark;
        } else {
            icon = R.drawable.ic_action_refresh_holo_light;
        }
        if (refresh != null) {
            refresh.setIcon(getResources().getDrawable(icon));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                startActivityForResult(new Intent(this, Settings.class), settingsRequestCode);
                return true;
            case R.id.action_refresh:
                downloadVertretungsplanTask = new DownloadVPlanTask(this, Device.VPLAN_PATH);
                downloadVertretungsplanTask.execute();
                return true;
            case R.id.action_feedback:
                showFeedbackDialog();
                break;
            case R.id.action_show_links:
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(Server.ZS_WEBSITE_URL));
                startActivity(i);
                break;
            case android.R.id.home:
                if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
                    mDrawerLayout.closeDrawer(mDrawerList);
                    rainbowEasterEgg();
                } else {
                    mDrawerLayout.openDrawer(mDrawerList);
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void rainbowEasterEgg() {
        counter++;
        if (counter == randomEasterEggNumber - 2) {
            Toast.makeText(this, "You've got tha clicks", Toast.LENGTH_SHORT).show();
        }
        if (counter == randomEasterEggNumber) {
            Toast.makeText(this, "You mindless clicker, yoouuu ^^", Toast.LENGTH_LONG).show();
            Dialog easterEgg = new Dialog(this, android.R.style.Theme_Translucent_NoTitleBar);
            easterEgg.setContentView(R.layout.rainbow_dialog);
            WindowManager.LayoutParams lp = easterEgg.getWindow().getAttributes();
            lp.windowAnimations = R.style.EasterEggDialogAnimation;
            easterEgg.setCancelable(true);
            easterEgg.setCanceledOnTouchOutside(true);
            easterEgg.show();
            counter = 0;
            Random rand = new Random();
            randomEasterEggNumber = rand.nextInt(6) + 4;
        }
    }

    public void setRefreshActionButtonState(final boolean refreshing) {
        if (optionsMenu != null) {
            final MenuItem refreshItem = optionsMenu.findItem(R.id.action_refresh);
            if (refreshItem != null) {
                if (refreshing) {
                    if (MenuItemCompat.getActionView(refreshItem) == null) {
                        MenuItemCompat.setActionView(refreshItem, R.layout.actionbar_indeterminate_progress);
                    }
                } else {
                    MenuItemCompat.setActionView(refreshItem, null);
                }
            }
        }
    }

    /**
     * When using the ActionBarDrawerToggle, you must call it during
     * onPostCreate() and onConfigurationChanged()...
     */
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == settingsRequestCode && resultCode == Activity.RESULT_OK) {
            restartInterface();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void restartInterface() {

        Intent intent = getIntent();
        overridePendingTransition(0, 0);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        finish();

        overridePendingTransition(0, 0);
        startActivity(intent);
    }

    private boolean isRunning() {
        if (downloadVertretungsplanTask != null) {
            AsyncTask.Status status = downloadVertretungsplanTask.getStatus();
            return (AsyncTask.Status.RUNNING == status);
        } else {
            return false;
        }
    }

}
