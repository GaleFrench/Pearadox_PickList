package com.example.pearadox_picklist;

import android.Manifest;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private static final int MY_PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE = ;
    private static String TAG = "MainActivity";        // This CLASS name
    public int teamSelected = -1;
    public int listSize = 32;       // Minimum size of Pick List teams
    ImageView imageView_Pearadox;
    ListView lstView_Teams;
    Boolean save_list = false;
    Button btn_UP, btn_DOWN, btn_save;
    Spinner spinner_Event;
    SimpleAdapter adaptTeams;
    ArrayAdapter<String> adapter_Event;
    String tnum = "";
    String FRC_ChampDiv = "";
    TextView TeamData, BA, Stats, Stats2;
    static final ArrayList<HashMap<String, String>> draftList = new ArrayList<HashMap<String, String>>();
    int numTeams = 0;
    TextView txt_Teams, txt_Selected;
    String divider = new String(new char[30]).replace("\0", "▇");           // string of 'x' med blocks
    String divStat2 = new String(new char[38]).replace("\0", "▇");          // string of 'x' med blocks


    //@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i(TAG, "\n \n******* Starting Pearadox-5414 Pick-List  *******");
        ImageView imageView_Pearadox = (ImageView) findViewById(R.id.imageView_Pearadox);
        lstView_Teams = (ListView) findViewById(R.id.lstView_Teams);
//        FRC_ChampDiv = "TXCHA";   // DEBUG


        txt_Teams = (TextView) findViewById(R.id.txt_Teams);
        txt_Teams.setText("");
        txt_Selected = (TextView) findViewById(R.id.txt_Selected);
        txt_Selected.setText("");
        Spinner spinner_Event = (Spinner) findViewById(R.id.spinner_Event);
        String[] events = getResources().getStringArray(R.array.event_array);
        adapter_Event = new ArrayAdapter<String>(this, R.layout.dev_list_layout, events);
        adapter_Event.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner_Event.setAdapter(adapter_Event);
        spinner_Event.setSelection(0, false);
        spinner_Event.setOnItemSelectedListener(new event_OnItemSelectedListener());
        lstView_Teams.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        btn_save=(Button) findViewById(R.id.btn_save);
        btn_save.setEnabled(false);     // not until event selected
        btn_save.setVisibility(View.INVISIBLE);

        // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
        lstView_Teams.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent,
                                    View view, int pos, long id) {
                Log.w(TAG, "*** lstView_Teams ***   Item Selected: " + pos);
                teamSelected = pos;
                lstView_Teams.setSelector(android.R.color.holo_blue_light);
                /* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */
                txt_Selected.setText(String.valueOf(pos+1));
//                Toast toast = Toast.makeText(getBaseContext(), "POS= " + teamSelected, Toast.LENGTH_LONG);
//                toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
//                toast.show();
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
            FRC_ChampDiv = ev;
            Log.w(TAG, ">>>>> Event '" + ev + "'  \n ");
            draftList.clear();
            save_list = false;
            loadFile();
            btn_save.setVisibility(View.VISIBLE);
            btn_save.setEnabled(true);     // now event selected
        }
        public void onNothingSelected(AdapterView<?> parent) {
            // Do nothing.
        }
    }


    /* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */
    /* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */
    /* @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@ */
    private void loadFile() {
        Log.w(TAG, "*** loadFile ***   Saved: " + save_list);
        adaptTeams = new SimpleAdapter(
                this,
                draftList,
                R.layout.draft_list_layout,
                new String[]{"team", "BA", "Stats", "Stats2", "Stats3"},
                new int[]{R.id.TeamData, R.id.BA, R.id.Stats, R.id.Stats2, R.id.Stats3}
        );

        draftList.clear();
        String file = "";
        numTeams = 0;
        try {
            file = FRC_ChampDiv + "_Save-List" + ".txt";
            File picks = new File(Environment.getExternalStorageDirectory() + "/download/FRC5414/" + file);
            if(!picks.exists()) {
                file = FRC_ChampDiv + "_Pick-List" + ".txt";
                picks = new File(Environment.getExternalStorageDirectory() + "/download/FRC5414/" + file);
                save_list = false;
            } else {
                final ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
                tg.startTone(ToneGenerator.TONE_CDMA_ABBR_ALERT);
                save_list = true;
                Toast toast = Toast.makeText(getBaseContext(), " \n★★★   Using the SAVED Pick List   ★★★\n  ", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                toast.show();
            }
            Log.w(TAG, "@@@@@@  Saved: " + save_list);
            BufferedReader bufferedReader = null;
            bufferedReader = new BufferedReader(new FileReader(picks));
                String inputLine;
                String teamStuff = ""; String rankStuff = ""; String statStuff = ""; String stat2Stuff = ""; String stat3Stuff = "";
                while ((inputLine = bufferedReader.readLine()) != null) {
//                    System.out.println(inputLine);      // ** DEBUG **
                    if ( (inputLine.length() > 1) && (!inputLine.contains("▇")) ) {      // not empty & not the divider
                        Log.w(TAG, "'" + inputLine + "' " + inputLine.length());
                        int x = inputLine.indexOf("team=");
                        int y = inputLine.indexOf("]");
                        teamStuff = inputLine.substring(x+5, y+1);
                        x = inputLine.indexOf("BA=");
                        y = inputLine.indexOf(", Stats3=");
                        rankStuff = inputLine.substring(x+3, y);
                        x = inputLine.indexOf("Stats=");
                        y = inputLine.indexOf("}");
                        statStuff = inputLine.substring(x+6, y);
                        x = inputLine.indexOf("Stats2=");
                        y = inputLine.indexOf("team=") -2;
                        stat2Stuff = inputLine.substring(x+7, y);
                        x = inputLine.indexOf("Stats3=");
                        y = inputLine.indexOf("Stats2=")-2;
                        stat3Stuff = inputLine.substring(x+7, y);
                        HashMap<String, String> temp = new HashMap<String, String>();
                        temp.put("Stats3", stat3Stuff);
                        temp.put("Stats2", stat2Stuff);
                        temp.put("Stats", statStuff);
                        temp.put("team", teamStuff);
                        temp.put("BA", rankStuff);
                        draftList.add(temp);
                        numTeams++;
                        if (numTeams == listSize)  {     // Add "break" entry if NOT Saved list
                            Log.w(TAG, "At divide point " + save_list);
                            if (!save_list) {
                                Log.w(TAG, " Generating DIVIDER  -  #Teams:" + numTeams);
                                HashMap<String, String> div = new HashMap<String, String>();
                                div.put("team", divider);
                                div.put("BA", " ⇧    Top " + listSize + " teams     ⬆");
                                div.put("Stats", " ");
                                div.put("Stats2", "  ");
                                div.put("Stats3", "  " + divStat2);
                                draftList.add(div);
                            } else {
                                Log.w(TAG, " Already in the Saved List");
                            }
                        }
                    } else {
                        if ( inputLine.contains("▇") ) {      // _IS_ the divider
                        Log.w(TAG, "\n  DIV'" + inputLine + "' " + inputLine.length());
                        }
                    }
                }
            Log.w(TAG, "**** Teams **** : " + (draftList.size()-0));
            txt_Teams = (TextView) findViewById(R.id.txt_Teams);
            txt_Teams.setText(String.valueOf((draftList.size()-0) + " teams"));
            lstView_Teams.setAdapter(adaptTeams);
            adaptTeams.notifyDataSetChanged();

        } catch (FileNotFoundException ex) {
            Toast toast = Toast.makeText(getBaseContext(), "*** File NOT found: " + file + " \n" + ex.getMessage(), Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
            toast.show();
            System.out.println(ex.getMessage() + " not found in the specified directory.");
//            System.exit(0);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }


    // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    public void buttonUP_Click(View view) {
        Log.d(TAG, ">>>>> buttonUP_Click  " + teamSelected);
        if (teamSelected >= 0) {
            if (teamSelected == 0) {
                final ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
                tg.startTone(ToneGenerator.TONE_PROP_BEEP2);
                Toast toast = Toast.makeText(getBaseContext(), "*** First Team cannot move Up! ***  ", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                toast.show();
            } else {
                Log.e(TAG, "Swapping  " + teamSelected + " and " + (teamSelected-1));
                HashMap<String, String> top = draftList.get(teamSelected);
                HashMap<String, String> bot = draftList.get(teamSelected-1);
                draftList.set(teamSelected, bot);       // Swap the
                draftList.set(teamSelected-1, top);     //   order of teams
                lstView_Teams.clearChoices();
                adaptTeams.notifyDataSetInvalidated();
                adaptTeams.notifyDataSetChanged();
                lstView_Teams.setSelection(teamSelected-1);     // Highlight the team moved
                lstView_Teams.performItemClick(lstView_Teams.findViewWithTag(lstView_Teams.getAdapter().getItem(teamSelected-1)), teamSelected-1, lstView_Teams.getAdapter().getItemId(teamSelected-1));
                adaptTeams.notifyDataSetChanged();
            }
        }
    }

    // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    public void buttonDOWN_Click (View view){
        Log.d(TAG, ">>>>> buttonDOWN_Click  " + teamSelected);
        if (teamSelected >= 0) {
            if (teamSelected == (numTeams -1)) {
                final ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
                tg.startTone(ToneGenerator.TONE_PROP_BEEP2);
                Toast toast = Toast.makeText(getBaseContext(), "*** Last Team cannot move Down! ***  ", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                toast.show();
            } else {
                Log.e(TAG, "Swapping  " + teamSelected + " and " + (teamSelected+1));
                HashMap<String, String> top = draftList.get(teamSelected);
                HashMap<String, String> bot = draftList.get(teamSelected+1);
                draftList.set(teamSelected, bot);       // Swap the
                draftList.set(teamSelected+1, top);     //   order of team
                lstView_Teams.clearChoices();
                adaptTeams.notifyDataSetInvalidated();
                adaptTeams.notifyDataSetChanged();
                lstView_Teams.setSelection(teamSelected+1);     // Highlight the team moved
                lstView_Teams.performItemClick(lstView_Teams.findViewWithTag(lstView_Teams.getAdapter().getItem(teamSelected+1)), teamSelected+1, lstView_Teams.getAdapter().getItemId(teamSelected+1));
                adaptTeams.notifyDataSetChanged();
            }
        }
    }
    // @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
    public void buttonSAVE_Click (View view) {
        Log.d(TAG, ">>>>> buttonSAVE_Click  " + FRC_ChampDiv);

        if (!FRC_ChampDiv.isEmpty()) {
            HashMap<String, String> temp = new HashMap<String, String>();
            try {
                String destFile = FRC_ChampDiv + "_Save-List" + ".txt";
                File prt = new File(Environment.getExternalStorageDirectory() + "/download/FRC5414/" + destFile);
                BufferedWriter bW = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(prt), "UTF-8"
                ));

                for (int i = 0; i < draftList.size(); i++) {    // load by sorted scores
                    temp = draftList.get(i);
                    bW.write(temp + "\n");

                } //end FOR
                //**********************************************
                bW.write(" \n" + "\n");        // NL
                bW.flush();
                bW.close();
                Toast toast = Toast.makeText(getBaseContext(), "***   Pick-List saved   ***  ", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
                toast.show();

            } catch (FileNotFoundException ex) {
                System.out.println(ex.getMessage() + " not found in the specified directory.");
                System.exit(0);
            } catch (IOException e) {
                System.out.println(e.getMessage());
            }
        } else {
            final ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100);
            tg.startTone(ToneGenerator.TONE_PROP_BEEP2);
            Toast toast = Toast.makeText(getBaseContext(), "★★★   Event MUST be selected first!   ★★★  ", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
            toast.show();
        }
    }



//###################################################################
//###################################################################
//###################################################################
    @Override
    public void onStart() {
        super.onStart();
        Log.v(TAG, "onStart");
        ActivityCompat.requestPermissions((MainActivity)mContext,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},MY_PERMISSION_REQUEST_WRITE_EXTERNAL_STORAGE);
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

//GLF

