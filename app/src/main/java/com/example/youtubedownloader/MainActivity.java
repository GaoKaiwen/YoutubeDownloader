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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
    Button savebttn;
    Button findButton;
    TextView textView;
    private VideoDetails details;
    private String videoURL;
    private String pathToDownload;

    Spinner videosResolution;
    final AudioVideoFormat[] qualityPosition = new AudioVideoFormat[1];

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        linkText = ((EditText)findViewById(R.id.linkText));
        savebttn = ((Button)findViewById(R.id.saveBttn));
        textView = ((TextView)findViewById(R.id.textView));
        videosResolution = ((Spinner)findViewById(R.id.videosResolution));
        findButton = ((Button)findViewById(R.id.findButton));

        Intent intent = getIntent();
        String link = intent.getStringExtra(Intent.EXTRA_TEXT);
        if(link != null) {
            linkText.setText(link.toString());
            Toast.makeText(this,link.toString(),Toast.LENGTH_LONG).show();
            download(savebttn);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void download(View v) {
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
            List<AudioVideoFormat> videoWithAudioFormats = video.videoWithAudioFormats();
//            List<String> videoWithAudioFormatsString = new LinkedList<String>();
//            for(AudioVideoFormat videoFormat : videoWithAudioFormats) {
//                videoWithAudioFormatsString.add(videoFormat.qualityLabel());
//            }
            // Create an ArrayAdapter using the string array and a default spinner layout
            ArrayAdapter<AudioVideoFormat> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, videoWithAudioFormats);
            // Specify the layout to use when the list of choices appears
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            // Apply the adapter to the spinner
            videosResolution.setAdapter(adapter);
            videosResolution.setVisibility(View.VISIBLE);

            videosResolution.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    qualityPosition[0] = (AudioVideoFormat) parent.getSelectedItem();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            String videoQuality = videosResolution.getSelectedItem().toString();
            System.out.println("Qualidade do vídeo: " + videoQuality);

            videoURL = videoWithAudioFormats.get(videoWithAudioFormats.size()-1).url();
            System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA "+videoURL);

            details = video.details();
            textView.setText(details.title());
            textView.setVisibility(View.VISIBLE);
            linkText.setText("");


        } catch (YoutubeException e) {
            textView.setText("Link corrompido");
            textView.setVisibility(View.VISIBLE);
        }

    }

    public void getPlayist(View v) throws YoutubeException {
        YoutubeDownloader downloader = new YoutubeDownloader();

        downloader.addCipherFunctionPattern(2, "\\b([a-zA-Z0-9$]{2})\\s*=\\s*function\\(\\s*a\\s*\\)\\s*\\{\\s*a\\s*=\\s*a\\.split\\(\\s*\"\"\\s*\\)");
        // extractor features
        downloader.setParserRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.121 Safari/537.36");
        downloader.setParserRetryOnFailure(1);

        String playlistID = linkText.getText().toString();
        playlistID = playlistID.replace("https://youtube.com/playlist?list=", "");
        YoutubePlaylist playlist = downloader.getPlaylist(playlistID);

    }

    public boolean download2(String path, String title, String url) {

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
                        .setDestinationInExternalPublicDir(path, title)
                        .setAllowedOverMetered(true)
                        .setAllowedOverRoaming(true);

                DownloadManager mDownloadManager = (DownloadManager) this.getSystemService(Context.DOWNLOAD_SERVICE);
                long downloadId = mDownloadManager.enqueue(request);
                Toast.makeText(this,"Iniciando donwload",Toast.LENGTH_LONG).show();
                return true;
            } else {

                Log.e("Permission error","You have asked for permission");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return download2(path, title, url);
            }
        }
        else { //you dont need to worry about these stuff below api level 23
            Log.e("Permission error","You already have the permission");
            return download2(path, title, url);
        }


    }

    public void downloadButtonDownload(View v) {
        download2(Environment.DIRECTORY_DOWNLOADS, details.title(), videoURL);
    }

}