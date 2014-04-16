package de.nilsstrelow.vplan.activities;

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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
import de.nilsstrelow.vplan.R;
import de.nilsstrelow.vplan.adapters.ClassListViewAdapter;
import de.nilsstrelow.vplan.adapters.DaysPagerAdapter;
import de.nilsstrelow.vplan.constants.Device;
import de.nilsstrelow.vplan.constants.HandlerMsg;
import de.nilsstrelow.vplan.constants.Server;
import de.nilsstrelow.vplan.fragments.FeedbackDialogFragment;
import de.nilsstrelow.vplan.helpers.ErrorMessage;
import de.nilsstrelow.vplan.helpers.SchoolClass;
import de.nilsstrelow.vplan.receivers.CheckForUpdateBroadcastReceiver;
import de.nilsstrelow.vplan.tasks.DownloadVPlanTask;
import de.nilsstrelow.vplan.tasks.LoadVPlanTask;
import de.nilsstrelow.vplan.utils.CroutonUtils;
import de.nilsstrelow.vplan.utils.DateUtils;
import de.nilsstrelow.vplan.utils.SchoolClassUtils;
import de.nilsstrelow.vplan.utils.Startup;

public class VertretungsplanActivity extends ActionBarActivity implements ListView.OnItemClickListener {

    // Define the handler that receives messages from the thread and update the progress
    // vertretungsplan files from .zs-vertretunsplan
    public static Handler handler;
    public static SharedPreferences sharedPref;
    public static Typeface robotoBold;
    public static Typeface robotoBlack;
    // UI items for ViewPager
    public static int NUM_PAGES;
    public static String[] schoolClasses;
    // counter to see if every day was already checked
    int settingsRequestCode = 2433;
    int counter = 0;
    int randomEasterEggNumber = 0;
    private String currentSchoolClassName;
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
    private boolean isLoading = false;
    private boolean isDownloading = false;
    private LoadVPlanTask loadVPlanTask;
    private DownloadVPlanTask downloadVertretungsplanTask;

    private Startup startup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);

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

        handler = new Handler() {

            // Create handleMessage function
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case HandlerMsg.STARTING_LOADING_PLAN:
                        supportInvalidateOptionsMenu();
                        isLoading = true;
                        setRefreshActionButtonState(isLoading());
                        break;
                    case HandlerMsg.FINISHED_LOADING_PLAN:
                        isLoading = false;
                        setRefreshActionButtonState(isLoading());

                        SchoolClass schoolClass = ((SchoolClass) msg.obj);
                        currentSchoolClassName = sharedPref.getString(Settings.MY_SCHOOL_CLASS_PREF, "5a");

                        setupTitle(currentSchoolClassName, schoolClass);
                        setSubtitle(null);
                        setupViewPager(schoolClass);
                        break;
                    case HandlerMsg.STARTING_DOWNLOADING_PLAN:
                        supportInvalidateOptionsMenu();
                        setSubtitle((String) msg.obj);

                        isDownloading = true;
                        setRefreshActionButtonState(isLoading());
                        break;
                    case HandlerMsg.FINISHED_DOWNLOADING_PLAN:
                        setSubtitle(null);

                        isDownloading = false;
                        setRefreshActionButtonState(isLoading());
                        break;
                    case HandlerMsg.UPDATING:
                        String updatingSchoolClass = (String) msg.obj;
                        setSubtitle("Updating " + updatingSchoolClass + "...");
                    case HandlerMsg.UPDATED:
                        loadPlan();
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
                    case HandlerMsg.CROUTON_CONFIRM:
                        CroutonUtils.makeCrouton(VertretungsplanActivity.this, (String) msg.obj, CroutonUtils.CROUTON_CONFIRM);
                        break;
                    case HandlerMsg.CROUTON_INFO:
                        CroutonUtils.makeCrouton(VertretungsplanActivity.this, (String) msg.obj, CroutonUtils.CROUTON_INFO);
                        break;
                    case HandlerMsg.CROUTON_ALERT:
                        CroutonUtils.makeCrouton(VertretungsplanActivity.this, (String) msg.obj, CroutonUtils.CROUTON_ALERT);
                        break;
                }
            }
        };

        //deleteOldVPlans();

        // Set the list's click listener
        mTitle = mDrawerTitle = getTitle();

        initActionBar();

        initDrawer();

        startup = new Startup(this, sharedPref);
        startup.start();
    }

    private boolean isLoading() {
        return (isLoading || isDownloading);
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
                if (startup.isTutorialMode())
                    startup.setupSwipeGuide();
            }

            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle(mDrawerTitle);
                supportInvalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()

                // hide overlay, when user clicked the navigation drawer
                if (startup.isTutorialMode())
                    startup.hideShowcaseView();
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

    private void loadPlan() {
        String mySchoolClass = sharedPref.getString(Settings.MY_SCHOOL_CLASS_PREF, "5a");
        LoadVPlanTask loadVPlanTask = new LoadVPlanTask(this);
        loadVPlanTask.execute(mySchoolClass);
        if (!isRunning()) {
            Toast.makeText(this, R.string.no_plan_msg, Toast.LENGTH_LONG).show();
        }
        if (startup.isNewVersion())
            startup.setupNewVersionGuide();
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
                    Date planDate = DateUtils.parseString(plan.getName());
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
        } catch (Exception ignored) {

        }
    }

    public void loadVPlan() {
        loadClass();
    }

    @Override
    protected void onStart() {
        EasyTracker.getInstance(this).activityStart(this);
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
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
        Intent intent = new Intent(this, CheckForUpdateBroadcastReceiver.class);
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

    private void setupViewPager(final SchoolClass schoolClass) {
        NUM_PAGES = schoolClass.getSize();
        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setVisibility(ViewPager.VISIBLE);
        FragmentManager fragmentManager = getSupportFragmentManager();
        mPagerAdapter = new DaysPagerAdapter(fragmentManager, schoolClass);
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
                setupTitle(currentSchoolClassName, schoolClass, position);
            }

        });
        mPager.setCurrentItem(0);

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mDrawerList.setItemChecked(position, true);
        mDrawerList.setSelection(position);
        currentSchoolClassName = schoolClasses[position];
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(Settings.MY_SCHOOL_CLASS_PREF, currentSchoolClassName);
        editor.commit();
        loadClass();
    }

    private void loadClass() {
        currentSchoolClassName = sharedPref.getString(Settings.MY_SCHOOL_CLASS_PREF, "5a");
        int position = SchoolClassUtils.getClassIndex(schoolClasses, currentSchoolClassName);
        mDrawerList.setItemChecked(position, true);
        mDrawerList.setSelection(position);
        mDrawerLayout.closeDrawer(mDrawerList);
        loadVPlanTask = new LoadVPlanTask(this);
        loadVPlanTask.execute(currentSchoolClassName);
    }

    @Override
    public void setTitle(CharSequence title) {
        mTitle = title;
        getSupportActionBar().setTitle(mTitle);
    }

    public void setupTitle(String schoolClassName, SchoolClass schoolClass) {
        setTitle("KL: " + schoolClassName + "   " + DateUtils.parseDate(schoolClass.getDay(0).day).replaceFirst("\\.", " "));
    }

    public void setupTitle(String schoolClassName, SchoolClass schoolClass, int position) {
        setTitle("KL: " + schoolClassName + "   " + DateUtils.parseDate(schoolClass.getDay(position).day).replaceFirst("\\.", " "));
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
        setRefreshActionButtonState(isLoading());
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
