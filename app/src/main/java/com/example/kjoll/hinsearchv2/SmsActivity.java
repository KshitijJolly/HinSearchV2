package com.example.kjoll.hinsearchv2;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Locale;
import java.util.UUID;

public class SmsActivity extends Activity implements TextToSpeech.OnInitListener {

    private static SmsActivity inst;
    TextView text_view_result;
    Button speak_button;
    private TextToSpeech textToSpeech;
    private boolean ready;
  /*  ArrayList<String> smsMessagesList = new ArrayList<String>();
    ListView smsListView;
    ArrayAdapter arrayAdapter;*/

    public static SmsActivity instance() {
        return inst;
    }

    @Override
    public void onStart() {
        super.onStart();
        inst = this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms);
        text_view_result = (TextView)findViewById(R.id.text_view_result);
        speak_button = (Button)findViewById(R.id.button_speak);
        textToSpeech = new TextToSpeech(getApplicationContext(),this);
        speak_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speakOut();
            }
        });
      /*  smsListView = (ListView) findViewById(R.id.SMSList);
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, smsMessagesList);
        smsListView.setAdapter(arrayAdapter);
        smsListView.setOnItemClickListener(this);*/

    //    refreshSmsInbox();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.menu_sms, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_share) {
            Share();
        }else if(id==R.id.action_download){
            Download();
        }

        return super.onOptionsItemSelected(item);
    }

    public void Share(){

    }
    public void Download(){
        String s;
        try {
            File myFile = new File("mysdfile.txt");
            myFile.createNewFile();
            FileOutputStream fOut = new FileOutputStream(myFile);
            OutputStreamWriter myOutWriter =
                    new OutputStreamWriter(fOut);
            myOutWriter.append(text_view_result.getText());
            myOutWriter.close();
            fOut.close();
            Toast.makeText(getApplicationContext(),
                    "Done writing SD 'mysdfile.txt'",
                    Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }


    private void speakOut() {
        if (!ready) {
            Toast.makeText(getApplicationContext(), "Text to Speech not ready", Toast.LENGTH_LONG).show();
            return;
        }
        // Text to Speak
        String toSpeak = text_view_result.getText().toString();
        String toShow = "Speaking...";
        Toast.makeText(getApplicationContext(), toShow, Toast.LENGTH_SHORT).show();
        // A random String (Unique ID).
        String utteranceId = UUID.randomUUID().toString();
        textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
    }

   /* public void refreshSmsInbox() {
        ContentResolver contentResolver = getContentResolver();
        Cursor smsInboxCursor = contentResolver.query(Uri.parse("content://sms/inbox"), null, null, null, null);
        int indexBody = smsInboxCursor.getColumnIndex("body");
        int indexAddress = smsInboxCursor.getColumnIndex("address");
        if (indexBody < 0 || !smsInboxCursor.moveToFirst()) return;
        arrayAdapter.clear();
        do {
            String str = "SMS From: " + smsInboxCursor.getString(indexAddress) +
                    "\n" + smsInboxCursor.getString(indexBody) + "\n";
            arrayAdapter.add(str);
        } while (smsInboxCursor.moveToNext());
    }
*/
    public void updateList(final String smsMessage,String address) {
        //String textStr[] = smsMessage.split("\\r\\n|\\n|\\r");
        //String result_string ="";
        //for(int i=1;i<textStr.length;i++)
        //    result_string += textStr[i];
        text_view_result.setText(smsMessage);
        speak_button.setVisibility(View.VISIBLE);
      //  arrayAdapter.insert(smsMessage, 0);
      //  arrayAdapter.notifyDataSetChanged();

    }

    @Override
    public void onInit(int status) {
        //printOutSupportedLanguages();
        setTextToSpeechLanguage();

    }



    private void setTextToSpeechLanguage() {
        Locale language = new Locale("hi", "IN");
        if (language == null) {
            this.ready = false;
            Toast.makeText(getApplicationContext(), "Not language selected", Toast.LENGTH_SHORT).show();
            return;
        }

        //TODO: please review the two intents instances below.
        int result = textToSpeech.setLanguage(language);
        if (result == TextToSpeech.LANG_MISSING_DATA) {
            this.ready = false;
            Toast.makeText(getApplicationContext(), "Missing language data", Toast.LENGTH_SHORT).show();
            Intent installTTSIntent = new Intent();
            installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
            startActivity(installTTSIntent);
            return;
        } else if (result == TextToSpeech.LANG_NOT_SUPPORTED) {
            this.ready = false;
            Toast.makeText(getApplicationContext(), "Language not supported", Toast.LENGTH_SHORT).show();
            Intent installTTSIntent = new Intent();
            installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
            startActivity(installTTSIntent);
            return;
        } else {
            this.ready = true;
            Locale currentLanguage = textToSpeech.getVoice().getLocale();
            //   Toast.makeText(this, "Language " + currentLanguage, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPause() {
        if (textToSpeech != null) {
            textToSpeech.stop();
        }
        super.onPause();
    }


   /* public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
        try {
            String[] smsMessages = smsMessagesList.get(pos).split("\n");
            String address = smsMessages[0];
            String smsMessage = "";
            for (int i = 1; i < smsMessages.length; ++i) {
                smsMessage += smsMessages[i];
            }

            String smsMessageStr = address + "\n";
            smsMessageStr += smsMessage;
            Toast.makeText(this, smsMessageStr, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/
}
