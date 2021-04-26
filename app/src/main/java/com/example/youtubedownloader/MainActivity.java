package com.example.youtubedownloader;

import android.Manifest;
import android.app.Dialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.os.health.SystemHealthManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // Progress Dialog
    private ProgressDialog pDialog;
    public static final int progress_bar_type = 0;

    EditText linkText;
    Button savebttn;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        linkText = ((EditText)findViewById(R.id.linkText));
        savebttn = ((Button)findViewById(R.id.saveBttn));
        textView = ((TextView)findViewById(R.id.textView));
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void download(View v) throws YoutubeException {
        YoutubeDownloader downloader = new YoutubeDownloader();

        downloader.addCipherFunctionPattern(2, "\\b([a-zA-Z0-9$]{2})\\s*=\\s*function\\(\\s*a\\s*\\)\\s*\\{\\s*a\\s*=\\s*a\\.split\\(\\s*\"\"\\s*\\)");
        // extractor features
        downloader.setParserRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.121 Safari/537.36");
        downloader.setParserRetryOnFailure(1);

        String videoID = linkText.getText().toString();
        videoID = videoID.replace("https://youtu.be/", "");
        YoutubeVideo video = downloader.getVideo(videoID);

        // get videos with audio
        List<AudioVideoFormat> videoWithAudioFormats = video.videoWithAudioFormats();
        String videoURL = videoWithAudioFormats.get(videoWithAudioFormats.size()-1).url();
        System.out.println("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"+videoURL);

        VideoDetails details = video.details();
        textView.setText(details.title());
        textView.setVisibility(View.VISIBLE);
        linkText.setText("");

        download2(Environment.DIRECTORY_DOWNLOADS, details.title(), videoURL);
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

    public void download2(String path, String title, String url) {
        haveStoragePermission();
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
    }

    public  boolean haveStoragePermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.e("Permission error","You have permission");
                return true;
            } else {

                Log.e("Permission error","You have asked for permission");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //you dont need to worry about these stuff below api level 23
            Log.e("Permission error","You already have the permission");
            return true;
        }
    }
//            Toast.makeText(this, "the button was disabled", Toast.LENGTH_LONG).show();





}