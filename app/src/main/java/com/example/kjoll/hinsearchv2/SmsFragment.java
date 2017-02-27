package com.example.kjoll.hinsearchv2;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Locale;
import java.util.UUID;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SmsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link SmsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SmsFragment extends Fragment implements TextToSpeech.OnInitListener {

    TextView heading;
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    String received_string;
    Button speak_button;
    private static SmsFragment inst;
    private TextToSpeech textToSpeech;
    private boolean ready;
    TextView text_view_result;

    private OnFragmentInteractionListener mListener;

    public SmsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *

     * @return A new instance of fragment SmsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SmsFragment newInstance() {

        return inst;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        text_view_result = (TextView)getActivity().findViewById(R.id.text_view_result);
        heading = (TextView)getActivity().findViewById(R.id.heading);
        speak_button = (Button)getActivity().findViewById(R.id.button_speak);
        textToSpeech = new TextToSpeech(getActivity(),this);
        heading.setText(received_string);
        speak_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speakOut();
            }
        });
        inst = this;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.menu_sms, menu);
        super.onCreateOptionsMenu(menu, inflater);
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
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, "I searched for " + received_string +
                                    " in HinSearch and here is what I got:\n" + text_view_result.getText().toString());
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, received_string);
        startActivity(Intent.createChooser(sharingIntent, "Share using"));
    }
    public void Download(){
        try {
            String root = Environment.getExternalStorageDirectory().toString();
            File myDir = new File(root + "/HinSearch");
            myDir.mkdirs();
            File file = new File (myDir, received_string+".txt");
            if (file.exists ()) file.delete ();
                FileOutputStream out = new FileOutputStream(file);
                PrintWriter pw = new PrintWriter(out);
                pw.println(text_view_result.getText().toString());
                pw.flush();
                pw.close();
                out.flush();
                out.close();
            Toast.makeText(getActivity(),"Search Result saved in /HinSearch/" + received_string + ".txt",Toast.LENGTH_LONG).show();
          /*  Toast.makeText(getActivity(), Environment.getExternalStorageDirectory().toString(),Toast.LENGTH_LONG).show();
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(getActivity().openFileOutput("a.txt", Context.MODE_PRIVATE));
            outputStreamWriter.write(text_view_result.getText().toString());
            outputStreamWriter.close();*/
       /*     File myFile = new File("/data/data/mysdfile.txt");
            myFile.createNewFile();
            FileOutputStream fOut = new FileOutputStream(myFile);
            OutputStreamWriter myOutWriter =
                    new OutputStreamWriter(fOut);
            myOutWriter.append(sms_received_tv.getText());
            myOutWriter.close();
            fOut.close();
            Toast.makeText(getActivity(),
                    "Done writing SD 'mysdfile.txt'",
                    Toast.LENGTH_SHORT).show();*/
        } catch (Exception e) {
            Toast.makeText(getActivity(), e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if(getArguments()!=null)
            received_string = getArguments().getString("text");
        return inflater.inflate(R.layout.fragment_sms, container, false);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */

    private void speakOut() {
        if (!ready) {
            Toast.makeText(getActivity(), "Text to Speech not ready", Toast.LENGTH_LONG).show();
            return;
        }
        // Text to Speak
        String toSpeak = text_view_result.getText().toString();
        String toShow = "Speaking...";
        Toast.makeText(getActivity(), toShow, Toast.LENGTH_SHORT).show();
        // A random String (Unique ID).
        String utteranceId = UUID.randomUUID().toString();
        textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
    }


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
            Toast.makeText(getActivity(), "Not language selected", Toast.LENGTH_SHORT).show();
            return;
        }

        //TODO: please review the two intents instances below.
        int result = textToSpeech.setLanguage(language);
        if (result == TextToSpeech.LANG_MISSING_DATA) {
            this.ready = false;
            Toast.makeText(getActivity(), "Missing language data", Toast.LENGTH_SHORT).show();
            Intent installTTSIntent = new Intent();
            installTTSIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
            startActivity(installTTSIntent);
            return;
        } else if (result == TextToSpeech.LANG_NOT_SUPPORTED) {
            this.ready = false;
            Toast.makeText(getActivity(), "Language not supported", Toast.LENGTH_SHORT).show();
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


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
