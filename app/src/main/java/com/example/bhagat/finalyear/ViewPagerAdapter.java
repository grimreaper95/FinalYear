package com.example.bhagat.finalyear;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import layout.ActiveRequests;
import layout.PendingRequests;

/**
 * Created by bhagat on 10/9/16.
 */
public class ViewPagerAdapter extends FragmentPagerAdapter {

    int numberOfTabs = 2;
    String [] title = {"Active", "Pending"};

    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return position==0 ? new ActiveRequests() : new PendingRequests();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        //return super.getPageTitle(position);
        return title[position];
    }

    @Override
    public int getCount() {
        return numberOfTabs;
    }
}
