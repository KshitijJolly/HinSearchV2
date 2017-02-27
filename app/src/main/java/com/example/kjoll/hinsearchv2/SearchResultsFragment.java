package com.example.kjoll.hinsearchv2;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.memetix.mst.language.Language;
import com.memetix.mst.translate.Translate;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link SearchResultsFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */



public class SearchResultsFragment extends Fragment implements TextToSpeech.OnInitListener{

    Button speak_button;
    ProgressBar progressBar;
    TextView search_result_tv,progress_tv,heading;
    private TextToSpeech textToSpeech;
    private OnFragmentInteractionListener mListener;
    public String received_string;
    public String result_string;

    public String JSON_URL="";
    public String JSON_URL_QUERY = "http://lookup.dbpedia.org/api/search.asmx/KeywordSearch?QueryString=";
    public String URL_END = "&MaxHits=1";
    public String CLIENT_ID = "Kshitij";
    public String CLIENT_SECRET = "2bhUBJtjmKg4GKKMaq1aJJsy9rgRAkuj4NLX2ROF5f0=";

    LinearLayout linearLayout;
    private boolean ready;
    public int request1_counter=0;
    public int request2_counter=0;
    public int request3_counter=0;
    String translatedText="default";

    public SearchResultsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if(getArguments()!=null)
            received_string = getArguments().getString("text");
        return inflater.inflate(R.layout.fragment_search_results, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        View v = getView();
        Translate.setClientId(CLIENT_ID);
        Translate.setClientSecret(CLIENT_SECRET);
        search_result_tv = (TextView) v.findViewById(R.id.search_result_tv);
        heading = (TextView) v.findViewById(R.id.heading);
        heading.setText(received_string);
        //search_result_tv.setText(received_string);
        speak_button = (Button) v.findViewById(R.id.speak_btn);
        textToSpeech = new TextToSpeech(getActivity().getApplicationContext(),this);

        progressBar = (ProgressBar)v.findViewById(R.id.progress_bar);
        progress_tv = (TextView)v.findViewById(R.id.progress_tv);
        linearLayout = (LinearLayout)v.findViewById(R.id.progress_lin_layout);
        GetResult();
        speak_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                speakOut();
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.main, menu);
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
        }else if(id==R.id.action_refresh){
            GetResult();
        }

        return super.onOptionsItemSelected(item);
    }

    public void Share(){
        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, "I searched for " + received_string +
                " in HinSearch and here is what I got:\n" + translatedText);
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, received_string);
        startActivity(Intent.createChooser(sharingIntent, "Share using"));
    }
    public void Download(){
        try {
            String root = Environment.getExternalStorageDirectory().toString();
            File myDir = new File(root + "/HinSearch");
            myDir.mkdirs();
            File file = new File (myDir, received_string +".txt");
            if (file.exists ()) file.delete ();
            FileOutputStream out = new FileOutputStream(file);
            PrintWriter pw = new PrintWriter(out);
            pw.println(search_result_tv.getText().toString());
            pw.flush();
            pw.close();
            out.flush();
            out.close();
            Toast.makeText(getActivity(),"Search Result saved in /HinSearch/" + received_string + ".txt",Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(getActivity(), e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void speakOut() {
        if (!ready) {
            Toast.makeText(getActivity(), "Text to Speech not ready", Toast.LENGTH_LONG).show();
            return;
        }
        // Text to Speak
        String toSpeak = search_result_tv.getText().toString();
        String toShow = "Speaking...";
        Toast.makeText(getActivity(), toShow, Toast.LENGTH_SHORT).show();
        // A random String (Unique ID).
        String utteranceId = UUID.randomUUID().toString();
        textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
    }



    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
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



    public void GetResult() {

        request1_counter=0;
        request2_counter=0;
        request3_counter=0;
        new AsyncTaskH2E().execute();
        progressBar.setVisibility(View.VISIBLE);
        progress_tv.setVisibility(View.VISIBLE);
        linearLayout.setVisibility(View.VISIBLE);
        linearLayout.setBackgroundColor(getResources().getColor(R.color.colorTranslucent));
        linearLayout.bringToFront();
        progress_tv.setText("Getting query ready ...");
    }

    private void sendRequest2(){

        StringRequest stringRequest = new StringRequest(JSON_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        result_string = response;
                        // search_result.setText(result_string);
                        XMLParser parser = new XMLParser();
                        Document doc = parser.getDomElement(result_string);
                        NodeList nl = doc.getElementsByTagName("Result");
                        Element e = (Element)nl.item(0);
                        result_string = parser.getValue(e,"Description");
                        progress_tv.setText("Translating the result ...");
                        new AsyncTaskE2H().execute();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        request2_counter++;
                        if(request2_counter<3)
                            sendRequest2();
                        else
                            Toast.makeText(getActivity(),error.getMessage()+"\n"+request2_counter, Toast.LENGTH_LONG).show();
                    }
                });
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        requestQueue.add(stringRequest);
    }



    class AsyncTaskH2E extends AsyncTask<Void, Integer, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            translatedText = "";
        }

        @Override
        protected Boolean doInBackground(Void... arg0) {

            try {
                translatedText = Translate.execute(received_string, Language.HINDI, Language.ENGLISH);
            } catch(Exception e) {
                request1_counter++;
                //translatedText = e.toString()+ "\n" + request1_counter;
            }
            return true;
        }
        @Override
        protected void onPostExecute(Boolean result) {
            //Toast.makeText(getActivity().getApplicationContext(),translatedText,Toast.LENGTH_SHORT).show();
            if(translatedText.equals(""))
            {
                if(request1_counter<3)
                    new AsyncTaskH2E().execute();
                else {
                    Toast.makeText(getActivity().getApplicationContext(), "Error occurred in THE", Toast.LENGTH_SHORT).show();
                    linearLayout.setVisibility(View.INVISIBLE);
                    return;
                }
            }
            else {
                //search_result_tv.setText(translatedText);
                Pattern stopWords = Pattern.compile("\\b(?:i|a|who|is|what|the)\\b\\s*", Pattern.CASE_INSENSITIVE);
                Matcher matcher = stopWords.matcher(translatedText);
                translatedText = matcher.replaceAll("").toString();
                //search_result_tv.setText(translatedText);
                try {
                    translatedText = URLEncoder.encode(translatedText, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                JSON_URL = JSON_URL_QUERY + translatedText + URL_END;
                progress_tv.setText("Fetching result ...");
                sendRequest2();
            }
        }
    }



    class AsyncTaskE2H extends AsyncTask<Void, Integer, Boolean> {
        private Exception e=null;
        @Override
        protected Boolean doInBackground(Void... arg0) {
            try {
                translatedText = Translate.execute(result_string, Language.ENGLISH, Language.HINDI);

            } catch(Exception e) {
                request3_counter++;
                this.e = e;
            }
            return true;
        }
        @Override
        protected void onPostExecute(Boolean result) {
            if(!(e==null))
            {
                if(request3_counter<3)
                    new AsyncTaskE2H().execute();
                else
                    Toast.makeText(getActivity().getApplicationContext(),"Error occurred in TEH",Toast.LENGTH_SHORT).show();
            }
            linearLayout.setVisibility(View.INVISIBLE);
            speak_button.setVisibility(View.VISIBLE);
            search_result_tv.setText(translatedText);

        }
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
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
