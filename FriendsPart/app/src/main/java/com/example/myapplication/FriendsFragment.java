package com.example.myapplication;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

public class FriendsFragment extends Fragment
{
    private RecyclerView myFriendList;

    private DatabaseReference FriendsReference;
    private DatabaseAuth mAuth;

    String online_user_id;

    private View myMainView;


    public FriendsFragment(){

    }

    public View onCreateView(layoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        myMainView = inflater.inflate(R.layout.fragment_friends, container, false);

        myFriendList = (RecyclerView) myMainView.findViewById(R.id.friend_list);

        mAuth = DatabaseAuth.getInstance();
        online_user_id = mAuth.getCurrentUser().getUid();

        FriendReference = Database.getInstance().getReference().child("Friends").child(online_user_id);

        myFriendList.setLayoutManager(new LinerLayoutManager(getContext()));


        return myMainView;

    }


    public void onStart(){

        super.onStart();

        DatabaseRecylerAdapter<Friends,FriendsViewHolder> databaseRecylerAdapter = new DatabaseRecyleAdapter<Friends, FriendsViewHolder>
                (

                Friends.class,
                R,layout.all_users_display_layout,
                FriendsViewHolder.class,
                FriendsReference
                )
        {

            protected void populateViewHolder(FriendsViewHolder viewHolder, Friends model, int position)
            {



            }
        };
    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder
    {

        View mView;

        public FriendsViewHolder(@NonNull View itemView) {
            super(itemView);

            mView = itemView;
        }
    }
}
