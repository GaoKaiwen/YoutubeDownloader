package com.example.youtubedownloader;

import android.Manifest;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.github.kiulian.downloader.YoutubeDownloader;
import com.github.kiulian.downloader.YoutubeException;
import com.github.kiulian.downloader.model.VideoDetails;
import com.github.kiulian.downloader.model.YoutubeVideo;
import com.github.kiulian.downloader.model.formats.AudioVideoFormat;
import com.github.kiulian.downloader.model.playlist.YoutubePlaylist;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // Progress Dialog
    private ProgressDialog pDialog;
    public static final int progress_bar_type = 0;

    EditText linkText;
    Button findBttn;
    Button saveButton;
    TextView textView;
    Spinner videosResolution;
    ImageButton searchDirBttn;
    TextView directoryText;

    private VideoDetails details;
    private String videoURL;
    private String pathToDownload;
    private AudioVideoFormat audioVideoFormat;
    private List<AudioVideoFormat> videoWithAudioFormats;


    final int[] qualityPosition = new int[1];

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        linkText = ((EditText)findViewById(R.id.linkText));
        findBttn = ((Button)findViewById(R.id.findBttn));
        textView = ((TextView)findViewById(R.id.textView));
        videosResolution = ((Spinner)findViewById(R.id.videosResolution));
        saveButton = ((Button)findViewById(R.id.saveBttn));
        searchDirBttn = ((ImageButton)findViewById(R.id.searchDirBttn));
        directoryText = ((TextView)findViewById(R.id.directoryText));

        Intent intent = getIntent();
        String link = intent.getStringExtra(Intent.EXTRA_TEXT);
        if(link != null) {
            linkText.setText(link.toString());
            Toast.makeText(this,link.toString(),Toast.LENGTH_LONG).show();
            findButton(findBttn);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void findButton(View v) {
        System.out.println(Environment.DIRECTORY_DOWNLOADS);
        if(!linkText.getText().toString().contains("playlist")) {
            getVideo();
        }
        else {
            getPlayist();
        }

    }

    public void getVideo() {
        YoutubeDownloader downloader = new YoutubeDownloader();

        downloader.addCipherFunctionPattern(2, "\\b([a-zA-Z0-9$]{2})\\s*=\\s*function\\(\\s*a\\s*\\)\\s*\\{\\s*a\\s*=\\s*a\\.split\\(\\s*\"\"\\s*\\)");
        // extractor features
        downloader.setParserRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.121 Safari/537.36");
        downloader.setParserRetryOnFailure(1);

        String videoID = linkText.getText().toString();
        videoID = videoID.replace("https://youtu.be/", "");
        YoutubeVideo video = null;
        try {
            video = downloader.getVideo(videoID);

            // get videos with audio
            videoWithAudioFormats = video.videoWithAudioFormats();
            List<String> videoWithAudioFormatsString = new LinkedList<String>();
            for(AudioVideoFormat videoFormat : videoWithAudioFormats) {
                videoWithAudioFormatsString.add(videoFormat.qualityLabel());
            }
            // Create an ArrayAdapter using the string array and a default spinner layout
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, videoWithAudioFormatsString);
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            // Apply the adapter to the spinner
            videosResolution.setAdapter(adapter);

            videosResolution.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    qualityPosition[0] = (int) parent.getSelectedItemPosition();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            details = video.details();

            textView.setText(details.title());
            textView.setVisibility(View.VISIBLE);
            saveButton.setVisibility(View.VISIBLE);
            videosResolution.setVisibility(View.VISIBLE);
            searchDirBttn.setVisibility(View.VISIBLE);
            directoryText.setVisibility(View.VISIBLE);
            linkText.setText("");


        } catch (YoutubeException e) {
            textView.setText("Link corrompido");
            textView.setVisibility(View.VISIBLE);
        }
    }

    public void getPlayist() {
        YoutubeDownloader downloader = new YoutubeDownloader();

        downloader.addCipherFunctionPattern(2, "\\b([a-zA-Z0-9$]{2})\\s*=\\s*function\\(\\s*a\\s*\\)\\s*\\{\\s*a\\s*=\\s*a\\.split\\(\\s*\"\"\\s*\\)");
        // extractor features
        downloader.setParserRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.121 Safari/537.36");
        downloader.setParserRetryOnFailure(1);

        String playlistID = linkText.getText().toString();
        playlistID = playlistID.replace("https://youtube.com/playlist?list=", "");
        try {
            YoutubePlaylist playlist = downloader.getPlaylist(playlistID);
        } catch (YoutubeException e) {
            e.printStackTrace();
        }

    }

    public boolean downloadVideo(String title, String url) {

        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                String mBaseFolderPath = Environment
                        .getExternalStorageDirectory()
                        + File.separator
                        + "FolderName" + File.separator;
                if (!new File(mBaseFolderPath).exists()) {
                    new File(mBaseFolderPath).mkdir();
                }

                String mFilePath = "file://" + mBaseFolderPath + "/" + "teste";

                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url))
                        .setTitle(title)
                        .setDescription("Downloading")
                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                        .setDestinationInExternalPublicDir(pathToDownload, title)
                        .setAllowedOverMetered(true)
                        .setAllowedOverRoaming(true);

                DownloadManager mDownloadManager = (DownloadManager) this.getSystemService(Context.DOWNLOAD_SERVICE);
                long downloadId = mDownloadManager.enqueue(request);
                Toast.makeText(this,"Iniciando donwload",Toast.LENGTH_LONG).show();
                return true;
            } else {

                Log.e("Permission error","You have asked for permission");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return downloadVideo(title, url);
            }
        }
        else { //you dont need to worry about these stuff below api level 23
            Log.e("Permission error","You already have the permission");
            return downloadVideo(title, url);
        }


    }

    public void downloadButton(View v) {
        videoURL = videoWithAudioFormats.get(qualityPosition[0]).url();
        downloadVideo(details.title(), videoURL);
    }

    public void openDirectory(View v) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP){
            Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            i.addCategory(Intent.CATEGORY_DEFAULT);
            startActivityForResult(Intent.createChooser(i, "Choose directory"), 9999);
        }


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case 9999:
                Uri uri = data.getData();
                File file = new File(uri.getPath());
                final String[] split = file.getPath().split(":");//split the path.
                pathToDownload = split[1];//assign it to a string(your choice).
                directoryText.setText(pathToDownload);
                break;
        }
    }

    public String parsePath(String path) {
        String[] pathAux = path.split("%3A");
//        if(pathAux[1].contains("%2F")) {
//            pathAux[1]
//        }
        return "";

    }

}