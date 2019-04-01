package com.example.myapplication;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class FriendList {

    public static class MainActivity extends AppCompatActivity {

        private RecyclerView myFriendList;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            myFriendList = (RecyclerView) findViewById(R.id.friend_list);
            myFriendList.setHasFixedSize(true);
            LinerLayoutManager linerLayoutManager = new LinerLayoutManager(this);
            linerLayoutManager.setReverseLayout(true);
            linerLayoutManager.setStackFromEnd(true);
            myFriendList.setLayoutManager(linerLayoutManager);

        }

        public static class FriendViewHolder extends RecyclerView.ViewHolder
        {
            View mView;

            public FriendViewHolder(@NonNull View itemView) {
                super(itemView);

                mView = itemView;
            }
        }
    }

}
