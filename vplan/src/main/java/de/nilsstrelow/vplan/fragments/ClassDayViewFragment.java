package de.nilsstrelow.vplan.fragments;

import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.nhaarman.listviewanimations.swinginadapters.prepared.SwingBottomInAnimationAdapter;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;

import de.nilsstrelow.vplan.R;
import de.nilsstrelow.vplan.activities.Settings;
import de.nilsstrelow.vplan.activities.VertretungsplanActivity;
import de.nilsstrelow.vplan.adapters.HourAdapter;
import de.nilsstrelow.vplan.helpers.SchoolDay;

/**
 * Fragment for ViewPager containing header and listview with day entries
 * Created by djnilse on 30.03.2014.
 */
public class ClassDayViewFragment extends Fragment {

    public static final String SCHOOLDAY_KEY = "schoolday_key";

    SchoolDay schoolDay;

    public static ClassDayViewFragment newInstance(SchoolDay schoolDay) {
        ClassDayViewFragment fragment = new ClassDayViewFragment();

        final Bundle args = new Bundle(1);
        args.putParcelable(SCHOOLDAY_KEY, schoolDay);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        schoolDay = getArguments().getParcelable(SCHOOLDAY_KEY);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // setup layout
        final LinearLayout rootView = (LinearLayout) inflater.inflate(R.layout.fragment_main, container, false);
        final ListView listView = (ListView) rootView.findViewById(R.id.list_vplan);
        final TextView genericTxt = (TextView) rootView.findViewById(R.id.generic_msg);
        final LinearLayout header = (LinearLayout) rootView.findViewById(R.id.headerRow);


        final int mCount = header.getChildCount();
        for (int i = 0; i < mCount; ++i) {
            final View mChild = header.getChildAt(i);
            if (mChild instanceof TextView) {
                ((TextView) mChild).setTypeface(VertretungsplanActivity.robotoBold);
            }
        }

        genericTxt.setTypeface(VertretungsplanActivity.robotoBold);

        final HourAdapter hourAdapter = new HourAdapter(getActivity(), schoolDay);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            SwingBottomInAnimationAdapter adapter = new SwingBottomInAnimationAdapter(hourAdapter);
            adapter.setAbsListView(listView);
            listView.setAdapter(adapter);
        } else {
            listView.setAdapter(hourAdapter);
        }

        if (schoolDay.getGenericMessage() != null && !schoolDay.getGenericMessage().equals("")) {
            genericTxt.setVisibility(TextView.VISIBLE);
            genericTxt.setText(schoolDay.getGenericMessage());
            if (listView.getAdapter().getCount() > 4) {
                // onClickListener to make genericTxt visible and hide
                header.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!VertretungsplanActivity.sharedPref.getBoolean(Settings.HIDE_COMMON_PREF, true)) {
                            if (genericTxt.getVisibility() == TextView.VISIBLE) {
                                genericTxt.setVisibility(TextView.GONE);
                            } else {
                                genericTxt.setVisibility(TextView.VISIBLE);
                            }
                        }
                    }
                });
            }

            // define onScrollListener for hiding genericTxt
            listView.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                    // only show/hide when not scrolling
                    if (scrollState == SCROLL_STATE_IDLE) {
                        // only hide when preference is true
                        if (VertretungsplanActivity.sharedPref.getBoolean(Settings.HIDE_COMMON_PREF, true)) {
                            if (view.getAdapter().getCount() > 4) {
                                final int height = genericTxt.getHeight();

                                if ((view.getFirstVisiblePosition() == 0)) {
                                    //genericTxt.setVisibility(View.VISIBLE);
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                        if (!rootView.getChildAt(0).equals(genericTxt))
                                            rootView.addView(genericTxt, 0);
                                    } else {
                                        if (ViewHelper.getTranslationY(genericTxt) != 0) {
                                            ObjectAnimator anim = ObjectAnimator.ofFloat(genericTxt, "translationY", 0.0f);
                                            anim.addListener(new Animator.AnimatorListener() {

                                                @Override
                                                public void onAnimationStart(Animator animation) {

                                                }

                                                @Override
                                                public void onAnimationEnd(Animator animation) {
                                                    //if (!rootView.getChildAt(0).equals(genericTxt))
                                                    //    rootView.addView(genericTxt, 0);
                                                }

                                                @Override
                                                public void onAnimationCancel(Animator animation) {

                                                }

                                                @Override
                                                public void onAnimationRepeat(Animator animation) {

                                                }
                                            });
                                            genericTxt.setVisibility(View.VISIBLE);
                                            anim.start();
                                        }
                                    }
                                } else {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                                        if (rootView.getChildAt(0).equals(genericTxt))
                                            rootView.removeView(genericTxt);
                                    } else {
                                        if (ViewHelper.getTranslationY(genericTxt) == 0) {
                                            ObjectAnimator anim = ObjectAnimator.ofFloat(genericTxt, "translationY", -height);
                                            anim.addListener(new Animator.AnimatorListener() {
                                                @Override
                                                public void onAnimationStart(Animator animation) {

                                                }

                                                @Override
                                                public void onAnimationEnd(Animator animation) {
                                                    genericTxt.setVisibility(View.GONE);
                                                }

                                                @Override
                                                public void onAnimationCancel(Animator animation) {

                                                }

                                                @Override
                                                public void onAnimationRepeat(Animator animation) {

                                                }
                                            });
                                            anim.start();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

                }
            });

        } else {
            genericTxt.setVisibility(TextView.GONE);
        }

        return rootView;
    }
}
