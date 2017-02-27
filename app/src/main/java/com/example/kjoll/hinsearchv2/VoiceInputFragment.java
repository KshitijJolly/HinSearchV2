package com.example.kjoll.hinsearchv2;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link VoiceInputFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 */
public class VoiceInputFragment extends Fragment implements View.OnClickListener {

    private ImageButton btnSpeak;
    private EditText search_input_et;
    private ImageButton text_input_button;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private boolean send_message = false;
    private OnFragmentInteractionListener mListener;

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String NUMBER_SERVER = "+917905269916";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public VoiceInputFragment() {
        // Required empty public constructor
    }

    public static VoiceInputFragment newInstance(String param1, String param2) {
        VoiceInputFragment fragment = new VoiceInputFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        View v = getView();
        search_input_et = (EditText) v.findViewById(R.id.search_input_et);
        text_input_button = (ImageButton) v.findViewById(R.id.text_input_btn);
        text_input_button.setOnClickListener(this);
        btnSpeak = (ImageButton) v.findViewById(R.id.btnSpeak);
        ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.SEND_SMS},1);
        btnSpeak.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                promptSpeechInput();
            }
        });

    }

    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getActivity().getApplicationContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_voice_input, container, false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.mode, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {

                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    search_input_et.setText(result.get(0));
                    SharedPreferences prefs = getActivity().getSharedPreferences("HinPref", Context.MODE_PRIVATE);
                    boolean online = prefs.getBoolean("online",false);

                    if (!online) {
                        String keyword=null,send_message_url;
                        try {
                            keyword = URLEncoder.encode(result.get(0),"UTF-8");
                            ///send_message_url = "http://10.8.0.158/sendquerysms.php?to="+NUMBER_SERVER+"&message="+keyword;
                           // sendMessage(send_message_url);
                        } catch (UnsupportedEncodingException e) {
                            e.printStackTrace();
                        }

                        SmsManager smsManager = SmsManager.getDefault();
                        smsManager.sendTextMessage(NUMBER_SERVER, null, "query "+result.get(0), null, null);
                        SmsFragment smsFragment = new SmsFragment();
                        Bundle args = new Bundle();
                        args.putString("text", result.get(0));
                        smsFragment.setArguments(args);
                        getFragmentManager().beginTransaction().replace(R.id.fragment_container, smsFragment)
                                .addToBackStack("frag").commit();

                    } else {
                        if (!isNetworkAvailable()) {
                            Toast.makeText(getActivity(), "Please check your internet connection", Toast.LENGTH_SHORT).show();

                        } else {
                            SearchResultsFragment searchResultsFragment = new SearchResultsFragment();
                            Bundle args = new Bundle();
                            args.putString("text", result.get(0));
                            searchResultsFragment.setArguments(args);
                            getFragmentManager().beginTransaction().replace(R.id.fragment_container, searchResultsFragment)
                                    .addToBackStack("fragback").commit();
                        }
                    }
                }
                break;
            }


        }
    }

    private void sendMessage(String url){

        StringRequest stringRequest = new StringRequest(url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(getActivity().getApplicationContext(),"Response : "+response,Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getActivity().getApplicationContext(),error.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        requestQueue.add(stringRequest);
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
    public void onClick(View v) {
        String search_text = search_input_et.getText().toString();
        InputMethodManager inputMethodManager = (InputMethodManager)
                getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),
                InputMethodManager.HIDE_NOT_ALWAYS);
        if(search_text.equals(""))
            Toast.makeText(getActivity(),"Please provide some input",Toast.LENGTH_SHORT).show();
        else {
            SharedPreferences prefs = getActivity().getSharedPreferences("HinPref", Context.MODE_PRIVATE);
            boolean online = prefs.getBoolean("online",false);
            String keyword;
            if (!online) {
                try {
                    keyword = URLEncoder.encode(search_text,"UTF-8");
                    //send_message_url = "http://10.8.0.158/sendquerysms.php?to="+NUMBER_SERVER+"&message="+keyword;
                    //sendMessage(send_message_url);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(NUMBER_SERVER, null, "query "+search_text, null, null);
                SmsFragment smsFragment = new SmsFragment();
                Bundle args = new Bundle();
                args.putString("text", search_text);
                smsFragment.setArguments(args);
                getFragmentManager().beginTransaction().replace(R.id.fragment_container, smsFragment)
                        .addToBackStack("frag").commit();

            } else {
                if (!isNetworkAvailable())
                    Toast.makeText(getActivity(), "Please check internet connection", Toast.LENGTH_SHORT).show();
                else {
                    SearchResultsFragment searchResultsFragment = new SearchResultsFragment();
                    Bundle args = new Bundle();
                    args.putString("text", search_text);
                    searchResultsFragment.setArguments(args);
                    getFragmentManager().beginTransaction().replace(R.id.fragment_container, searchResultsFragment)
                            .addToBackStack("fragback").commit();
                }
            }
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

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
