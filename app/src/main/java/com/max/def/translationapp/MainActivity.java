package com.max.def.translationapp;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.AppCompatTextView;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;
import com.google.gson.Gson;
import com.max.def.translationapp.Models.ApiResponse;
import com.max.def.translationapp.Utils.Languages;
import com.mvc.imagepicker.ImagePicker;

import org.angmarch.views.NiceSpinner;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;

import dmax.dialog.SpotsDialog;

public class MainActivity extends AppCompatActivity
{
    private NiceSpinner sourceLanguagesSpinner;
    private NiceSpinner targetLanguagesSpinner;

    private AppCompatEditText inputEditText;

    private AppCompatTextView seeOutputTextView;
    private AppCompatTextView outputTextView;

    private ArrayList<String> languagesList = new ArrayList<>(Arrays.asList(Languages.getLanguages()));
    private ArrayList<String> languagesCodeList = new ArrayList<>(Arrays.asList(Languages.getLanguagesCode()));

    private AlertDialog alertDialog;

    private TextToSpeech textToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        alertDialog = new SpotsDialog.Builder().setContext(this).setCancelable(false).build();

        sourceLanguagesSpinner = findViewById(R.id.source_languages_spinner);
        targetLanguagesSpinner = findViewById(R.id.target_languages_spinner);
        AppCompatImageButton swapLanguageButton = findViewById(R.id.swap_language_button);

        AppCompatImageButton voiceRecognitionButton = findViewById(R.id.voice_recognition_button);
        AppCompatImageButton imageRecognitionButton = findViewById(R.id.image_recognition_button);

        inputEditText = findViewById(R.id.input_edit_text);
        AppCompatImageButton clearInputText = findViewById(R.id.clear_input_text);

        seeOutputTextView = findViewById(R.id.see_output_text_view);
        outputTextView = findViewById(R.id.output_text_view);
        AppCompatImageButton speakTextButton = findViewById(R.id.speak_text_button);
        AppCompatImageButton shareTextButton = findViewById(R.id.share_text_button);

        AppCompatTextView textTranslateButton = findViewById(R.id.text_translate_button);

        sourceLanguagesSpinner.attachDataSource(languagesList);
        targetLanguagesSpinner.attachDataSource(languagesList);

        int targetCurrentPosition = targetLanguagesSpinner.getSelectedIndex();

        final String targetLanguageCode =  languagesCodeList.get(targetCurrentPosition);

        textToSpeech = new TextToSpeech(MainActivity.this, new TextToSpeech.OnInitListener()
        {
            @Override
            public void onInit(int status)
            {
                if (status != TextToSpeech.ERROR)
                {
                    Locale locale = new Locale(targetLanguageCode);

                    textToSpeech.setLanguage(locale);
                }
            }
        });

        swapLanguageButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                int sourceIndexPosition = sourceLanguagesSpinner.getSelectedIndex();
                int targetIndexPosition = targetLanguagesSpinner.getSelectedIndex();

                targetLanguagesSpinner.setSelectedIndex(sourceIndexPosition);
                sourceLanguagesSpinner.setSelectedIndex(targetIndexPosition);

                String inputText = inputEditText.getText().toString();
                String outputText = outputTextView.getText().toString();

                inputEditText.setText(outputText);
                outputTextView.setText(inputText);
            }
        });

        sourceLanguagesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                String currentLanguage = languagesList.get(position);

                inputEditText.setHint("Enter input in " + currentLanguage);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {

            }
        });

        targetLanguagesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                String currentLanguage = languagesList.get(position);

                seeOutputTextView.setText("See output in " + currentLanguage);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {

            }
        });

        clearInputText.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                inputEditText.setText("");
            }
        });

        voiceRecognitionButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                int sourceCurrentPosition = sourceLanguagesSpinner.getSelectedIndex();
                int targetCurrentPosition = targetLanguagesSpinner.getSelectedIndex();

                if (sourceCurrentPosition == targetCurrentPosition)
                {
                    Toast.makeText(MainActivity.this, "Choose other target language to translate", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    String sourceLanguage = languagesList.get(sourceCurrentPosition);
                    String sourceLanguageCode =  languagesCodeList.get(sourceCurrentPosition);

                    Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                    intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,sourceLanguageCode);
                    intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Speak in " + sourceLanguage);

                    try
                    {
                        startActivityForResult(intent,2);
                    }
                    catch (ActivityNotFoundException e)
                    {
                        Toast.makeText(MainActivity.this, "Could not open your speech recognizer..", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        imageRecognitionButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                int sourceCurrentPosition = sourceLanguagesSpinner.getSelectedIndex();
                int targetCurrentPosition = targetLanguagesSpinner.getSelectedIndex();

                if (sourceCurrentPosition == targetCurrentPosition)
                {
                    Toast.makeText(MainActivity.this, "Choose other target language to translate", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    ImagePicker.pickImage(MainActivity.this,"Select Image From");
                }
            }
        });

        speakTextButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String outputText = outputTextView.getText().toString();

                if (!outputText.equals(""))
                {
                    textToSpeech.speak(outputText, TextToSpeech.QUEUE_FLUSH, null,null);
                }
                else
                {
                    Toast.makeText(MainActivity.this, "Output is empty..", Toast.LENGTH_SHORT).show();
                }
            }
        });

        shareTextButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String outputText = outputTextView.getText().toString();

                if (!outputText.equals(""))
                {
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_TEXT,outputText);
                    startActivity(Intent.createChooser(intent,"Share Text With"));
                }
                else
                {
                    Toast.makeText(MainActivity.this, "Output is empty..", Toast.LENGTH_SHORT).show();
                }
            }
        });

        textTranslateButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                int sourceCurrentPosition = sourceLanguagesSpinner.getSelectedIndex();
                int targetCurrentPosition = targetLanguagesSpinner.getSelectedIndex();

                String sourceLanguage = languagesList.get(sourceCurrentPosition);

                String inputText = inputEditText.getText().toString();

                if (TextUtils.isEmpty(inputText) || inputText.equals(""))
                {
                    Toast.makeText(MainActivity.this, "Please enter input text " + sourceLanguage, Toast.LENGTH_SHORT).show();
                }
                else
                {
                    if (sourceCurrentPosition == targetCurrentPosition)
                    {
                        Toast.makeText(MainActivity.this, "Choose other target language to translate", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        alertDialog.setMessage("Translating...");
                        alertDialog.show();

                        String sourceLanguageCode =  languagesCodeList.get(sourceCurrentPosition);
                        String targetLanguageCode =  languagesCodeList.get(targetCurrentPosition);

                        new TranslateText(MainActivity.this).execute(inputText,sourceLanguageCode,targetLanguageCode);
                    }
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK)
        {
            if (requestCode == 1)
            {
                Bitmap bitmap = ImagePicker.getImageFromResult(MainActivity.this,requestCode,resultCode,data);

                //add spots dialog

                alertDialog.setMessage("Translating...");
                alertDialog.show();

                if (bitmap != null)
                {
                    FirebaseVisionImage visionImage = FirebaseVisionImage.fromBitmap(bitmap);

                    FirebaseVisionTextRecognizer textRecognizer = FirebaseVision.getInstance().getCloudTextRecognizer();

                    textRecognizer.processImage(visionImage).addOnCompleteListener(new OnCompleteListener<FirebaseVisionText>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<FirebaseVisionText> task)
                        {
                            if (task.isSuccessful() && task.getResult() != null)
                            {
                                String inputText = task.getResult().getText();

                                if (inputText != null && !inputText.equals(""))
                                {
                                    inputEditText.setText(inputText);

                                    // send text to api gateway

                                    int sourceCurrentPosition = sourceLanguagesSpinner.getSelectedIndex();
                                    int targetCurrentPosition = targetLanguagesSpinner.getSelectedIndex();

                                    String sourceLanguageCode =  languagesCodeList.get(sourceCurrentPosition);
                                    String targetLanguageCode =  languagesCodeList.get(targetCurrentPosition);

                                    new TranslateText(MainActivity.this).execute(inputText,sourceLanguageCode,targetLanguageCode);
                                }
                                else
                                {
                                    Toast.makeText(MainActivity.this, "Any text not found in your image.", Toast.LENGTH_SHORT).show();
                                }
                            }
                            else
                            {
                                Toast.makeText(MainActivity.this, "Could recognize text from image.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                else
                {
                    if (alertDialog.isShowing())
                    {
                        alertDialog.dismiss();
                    }

                    Toast.makeText(MainActivity.this, "Any text not found in your image.", Toast.LENGTH_SHORT).show();
                }
            }
            else if (requestCode == 2)
            {
                if (data != null)
                {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);

                    String inputText = result.get(0);

                    alertDialog.setMessage("Translating...");
                    alertDialog.show();

                    inputEditText.setText(inputText);

                    // send text to api gateway

                    int sourceCurrentPosition = sourceLanguagesSpinner.getSelectedIndex();
                    int targetCurrentPosition = targetLanguagesSpinner.getSelectedIndex();

                    String sourceLanguageCode =  languagesCodeList.get(sourceCurrentPosition);
                    String targetLanguageCode =  languagesCodeList.get(targetCurrentPosition);

                    new TranslateText(MainActivity.this).execute(inputText,sourceLanguageCode,targetLanguageCode);
                }
                else
                {
                    Toast.makeText(this, "Could recognize your voice..", Toast.LENGTH_SHORT).show();
                }
            }
            else
            {
                Toast.makeText(this, "Action Canceled", Toast.LENGTH_SHORT).show();
            }
        }
        else if (resultCode == RESULT_CANCELED)
        {
            Toast.makeText(this, "Action Canceled", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Toast.makeText(this, "Action Canceled", Toast.LENGTH_SHORT).show();
        }
    }

    private static class TranslateText extends AsyncTask<String,String,String>
    {
        private WeakReference<MainActivity> activityWeakReference;

        TranslateText(MainActivity mainActivity)
        {
            this.activityWeakReference = new WeakReference<>(mainActivity);
        }

        @Override
        protected String doInBackground(String... params)
        {
            try
            {
                URL url = new URL("https://s3vy2xe9x5.execute-api.us-east-1.amazonaws.com/alpha/textTranslateFunction?inputText=" + params[0] + "&sourceLanguage=" + params[1] + "&targetLanguage=" + params[2]);

                HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type","application/json");
                connection.setRequestMethod("POST");

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                StringBuilder stringBuilder = new StringBuilder();

                String jsonOutput;

                while ((jsonOutput = bufferedReader.readLine()) != null)
                {
                    stringBuilder.append(jsonOutput);
                }

                return stringBuilder.toString();
            }
            catch (IOException e)
            {
                return null;
            }
        }

        @Override
        protected void onPostExecute(String translatedText)
        {
            super.onPostExecute(translatedText);

            if (translatedText != null)
            {
                if (activityWeakReference.get().alertDialog.isShowing())
                {
                    activityWeakReference.get().alertDialog.dismiss();
                }

                ApiResponse apiResponse = new Gson().fromJson(translatedText,ApiResponse.class);

                if (apiResponse != null)
                {
                    if (apiResponse.getTranslatedText().equals("null"))
                    {
                        Toast.makeText(activityWeakReference.get(),"Could not convert the text",Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        activityWeakReference.get().outputTextView.setText(apiResponse.getTranslatedText());
                    }
                }
                else
                {
                    Toast.makeText(activityWeakReference.get(),"Could not convert the text",Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
























