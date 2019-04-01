package com.example.myapplication;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class ListDataActivity extends AppCompatActivity {
    private static final String TAG = "ListDataActivity";
    DatabaseHelper mDatabaseHelper;
    private ListView mListView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState );
        setContentView(R.layout.list_layout);
        mListView = (ListView) findViewById(R.id.listView);
        mDatabaseHelper = new DatabaseHelper(this);

        populateListView();

    }
    private void populateListView(){
        Log.d(TAG, "populateListView: Displaying data in the listView");
        //get the data and append to a list
        Cursor data = mDatabaseHelper.getData();
        ArrayList<String> listData = new ArrayList<>();
        while(data.moveToNext()){
            listData.add (data.getString(1));
        }
        ListAdapter adapter = new ArrayAdapter<>(this , android.R.layout.simple_list_item_1, listData);
        mListView.setAdapter(adapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String name = adapterView.getItemAtPosition(i).toString();
                Log.d(TAG, "onItemClick: you Clicked on" + name);

                Cursor data = mDatabaseHelper.getitemID(name);//get the id with that name
                int itemID = -1;
                while(data.moveToNext()){
                    itemID = data.getInt(0);

                }
                if(itemID > -1){
                    Log.d(TAG,"onItemClick: the ID is: " + itemID);

                }else{
                    toastMessage("no ID associated with the name");
                    Intent editScreenIntent = new Intent(ListDataActivity.this, EditDataActivity.class);
                    editScreenIntent.PutExtra("id",itemID);
                    editScreenIntent.putExtra("name",name);
                }


            }



        });

    }

    private void toastMessage(String message){
        Toast.makeText(this,message, Toast.LENGTH_SHORT).show();

    }
}
