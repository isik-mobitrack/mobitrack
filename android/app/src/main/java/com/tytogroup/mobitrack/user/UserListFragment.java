package com.tytogroup.mobitrack.user;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.tytogroup.mobitrack.ParseManager;
import com.tytogroup.mobitrack.R;

import java.util.List;

public class UserListFragment extends ListFragment {
    private UserAdapter adap;

    public UserListFragment() {
        ParseManager manager=ParseManager.getInstance();

        manager.getFriendships(AllUsers.deviceUser.phone,new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if(e!=null){
                    e.printStackTrace();
                    return;
                }
                if (adap!=null)
                    adap.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        adap=new UserAdapter(getActivity());
        setListAdapter(adap);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setEmptyText(getResources().getString(R.string.no_friend));
        adap=new UserAdapter(getActivity());
        setListAdapter(adap);
    }
}
