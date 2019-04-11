package com.example.pearadox_picklist;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    String TAG = "MainActivity";        // This CLASS name
    public int teamSelected = -1;
    ImageView imageView_Pearadox;
    ListView lstView_Teams;
    Button btn_Match, btn_Picked;
    Spinner spinner_Event;
    ArrayAdapter<String> adapter_Event;
    String EventName = "";
    String FRC_ChampDiv = "";
    TextView TeamData, BA, Stats, Stats2;
    static final ArrayList<HashMap<String, String>> draftList = new ArrayList<HashMap<String, String>>();
    int numTeams = 0;
    TextView txt_Teams;


    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i(TAG, "******* Starting Pearadox-5414 Pick-List  *******");
        ImageView imageView_Pearadox = (ImageView) findViewById(R.id.imageView_Pearadox);
        lstView_Teams = (ListView) findViewById(R.id.lstView_Teams);
//        FRC_ChampDiv = "TXCHA";   // DEBUG


        txt_Teams = (TextView) findViewById(R.id.txt_Teams);
        txt_Teams.setText("");
        Spinner spinner_Event = (Spinner) findViewById(R.id.spinner_Event);
        String[] events = getResources().getStringArray(R.array.event_array);
        adapter_Event = new ArrayAdapter<String>(this, R.layout.dev_list_layout, events);
        adapter_Event.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_Event.setAdapter(adapter_Event);
        spinner_Event.setSelection(0, false);
        spinner_Event.setOnItemSelectedListener(new event_OnItemSelectedListener());

        // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
        lstView_Teams.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent,
                                    View view, int pos, long id) {
                Log.w(TAG, "*** lstView_Teams ***   Item Selected: " + pos);
                teamSelected = pos;
                lstView_Teams.setSelector(android.R.color.holo_blue_light);
                /* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */
//                tnum = draftList.get(teamSelected).substring(0,4);
            }

            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing.
            }
        });
    }


    /* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */
    private class event_OnItemSelectedListener implements android.widget.AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent,
                                   View view, int pos, long id) {
            String ev = parent.getItemAtPosition(pos).toString();
            EventName = ev;
            Log.w(TAG, ">>>>> Event '" + ev + "'  \n ");
            FRC_ChampDiv = EventName;
//           draftList.clear();
            loadFile();

        }
        public void onNothingSelected(AdapterView<?> parent) {
            // Do nothing.
        }
    }


    /* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */
    /* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */
    /* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */
    private void loadFile() {
        SimpleAdapter adaptTeams = new SimpleAdapter(
                this,
                draftList,
                R.layout.draft_list_layout,
                new String[]{"team", "BA", "Stats", "Stats2"},
                new int[]{R.id.TeamData, R.id.BA, R.id.Stats, R.id.Stats2}
        );

        draftList.clear();
        String file = "";

        try {
            file = FRC_ChampDiv + "_Pick-List" + ".txt";
            File picks = new File(Environment.getExternalStorageDirectory() + "/download/FRC5414/" + file);
            BufferedReader bufferedReader = null;
            bufferedReader = new BufferedReader(new FileReader(picks));
            String inputLine;
            String teamStuff = ""; String rankStuff = ""; String statStuff = ""; String stat2Stuff = "";
            while ((inputLine = bufferedReader.readLine()) != null) {
                System.out.println(inputLine);
                if (inputLine.length() > 1) {
//                    Log.w(TAG, "'" + inputLine + "' " + inputLine.length());
                    int x = inputLine.indexOf("team=");
                    teamStuff = inputLine.substring(x+5, inputLine.indexOf("}"));
                    x = inputLine.indexOf(", Stats2=");
                    rankStuff = inputLine.substring(4, x);
                    x = inputLine.indexOf("Stats=");
                    int y = inputLine.indexOf("team=") -2;
                    statStuff = inputLine.substring(x+6, y);
                    x = inputLine.indexOf("Stats2=");
                    y = inputLine.indexOf("Stats=") -2;
                    stat2Stuff = inputLine.substring(x+7, y);
                    HashMap<String, String> temp = new HashMap<String, String>();
                    temp.put("team", teamStuff);
                    temp.put("BA", rankStuff);
                    temp.put("Stats", statStuff);
                    temp.put("Stats2", stat2Stuff);
                    draftList.add(temp);
                    numTeams++;
                } else {
                    Log.w(TAG, "'" + inputLine + "' " + inputLine.length());
                }
            }
            Log.w(TAG, "### Teams ###  : " + draftList.size());
            txt_Teams = (TextView) findViewById(R.id.txt_Teams);
            txt_Teams.setText(String.valueOf(draftList.size()));
            lstView_Teams.setAdapter(adaptTeams);
            adaptTeams.notifyDataSetChanged();

        } catch (FileNotFoundException ex) {
            Toast toast = Toast.makeText(getBaseContext(), "*** File NOT found: " + file, Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
            toast.show();
            System.out.println(ex.getMessage() + " not found in the specified directory.");
//            System.exit(0);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }

    }


    // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    public void buttonCapt_Click(View view) {
        Log.i(TAG, ">>>>> buttonCapt_Click  " + teamSelected);
        if (teamSelected >= 0) {
            lstView_Teams.getChildAt(teamSelected).setBackgroundColor(Color.GREEN);
        }
    }

    // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    public void buttonPicked_Click (View view){
        Log.i(TAG, ">>>>> buttonPicked_Click  " + teamSelected);
        if (teamSelected >= 0) {
            lstView_Teams.getChildAt(teamSelected).setBackgroundColor(Color.DKGRAY);
        }
    }

    // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    public void buttonCookie_Click (View view){
        Log.i(TAG, ">>>>> buttonCookie_Click  " + teamSelected);
        if (teamSelected >= 0) {
            lstView_Teams.getChildAt(teamSelected).setBackgroundColor(Color.RED);
        }
    }


//###################################################################
//###################################################################
//###################################################################
    @Override
    public void onStart() {
        super.onStart();
        Log.v(TAG, "onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.v(TAG, "****> onResume <**** ");
    }
    @Override
    public void onPause() {
        super.onPause();
        Log.v(TAG, "onPause  ");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.v(TAG, "onStop");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "OnDestroy");
    }
}


