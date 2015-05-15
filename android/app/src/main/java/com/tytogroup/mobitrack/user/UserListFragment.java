package com.tytogroup.mobitrack.user;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.melnykov.fab.FloatingActionButton;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.tytogroup.mobitrack.ParseManager;
import com.tytogroup.mobitrack.R;
import com.tytogroup.mobitrack.SwipeDismissListViewTouchListener;

import java.util.List;

public class UserListFragment extends ListFragment {
    private UserAdapter adap;
    private AdapterView.OnItemClickListener listener;
    private FloatingActionButton action;
    private int swipedPos=0;

    public UserListFragment() {
        ParseManager manager=ParseManager.getInstance();

        manager.getFriendships(AllUsers.deviceUser.phone,new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if(e!=null){
                    e.printStackTrace();
                    return;
                }
                if (adap!=null) {
                    adap.reloadFriendships();
                    adap.notifyDataSetChanged();
                }
            }
        });
    }

    public void setListener(AdapterView.OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setAction(FloatingActionButton action) {
        this.action = action;
    }

   public void not(){
       if(adap!=null){
           adap.notifyDataSetChanged();
       }
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
        if(listener!=null)
            getListView().setOnItemClickListener(listener);
        action.attachToListView(getListView());

        final SwipeDismissListViewTouchListener dis=new SwipeDismissListViewTouchListener(getListView(),
                new SwipeDismissListViewTouchListener.DismissCallbacks() {
                    @Override
                    public boolean canDismiss(int position) {
                        swipedPos=position;
                        if(adap.isTitle(position))
                            return false;
                       return true;
                    }

                    @Override
                    public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                        adap.remove(swipedPos);
                    }
                }
        );
        /*getListView().setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                dis.onTouch(v,event);
                return false;
            }
        });
        getListView().setOnScrollListener(dis.makeScrollListener());*/
    }
}
