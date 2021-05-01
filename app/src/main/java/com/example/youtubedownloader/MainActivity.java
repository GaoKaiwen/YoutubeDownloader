package com.example.youtubedownloader;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.github.kiulian.downloader.YoutubeDownloader;
import com.github.kiulian.downloader.YoutubeException;
import com.github.kiulian.downloader.model.VideoDetails;
import com.github.kiulian.downloader.model.YoutubeVideo;
import com.github.kiulian.downloader.model.formats.AudioVideoFormat;
import com.github.kiulian.downloader.model.playlist.YoutubePlaylist;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // Progress Dialog
    private ProgressDialog pDialog;
    private int WIRTE_STORAGE_PERMISSION_CODE = 1;
    private int READ_STORAGE_PERMISSION_CODE = 2;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
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
    private String path;
    private long downloadId;



    final int[] qualityPosition = new int[1];

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
//        registerReceiver(onComplete, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

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
        BroadcastReceiver onComplete=new BroadcastReceiver() {
            public void onReceive(Context ctxt, Intent intent) {
                String action = intent.getAction();
                if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                    if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                        moveFile(new File(Environment.getExternalStorageDirectory().toString() + File.separator + pathToDownload + File.separator + details.title()),  Environment.getExternalStorageDirectory().toString() + File.separator + File.separator + path);
                        Log.e("File Path", new File(pathToDownload + File.separator + details.title()).getPath());
                        Log.e("Path", path);
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(MainActivity.this, "Movendo vídeo", Toast.LENGTH_LONG).show();
                            }
                        });
                    }else {
                        requestReadStoragePermission();
                        moveFile(new File(Environment.getExternalStorageDirectory().toString() + File.separator + pathToDownload + File.separator + details.title()),  Environment.getExternalStorageDirectory().toString() + File.separator + File.separator + path);
                        runOnUiThread(new Runnable() {
                            public void run() {
                                Toast.makeText(MainActivity.this, "Movendo vídeo", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
            }
        };

        registerReceiver(onComplete, new IntentFilter(
                DownloadManager.ACTION_DOWNLOAD_COMPLETE));
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

    private void getVideo() {
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

    private void getPlayist() {
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

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void downloadVideo(String title, String url) {
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
//                String mBaseFolderPath = Environment
//                        .getExternalStorageDirectory()
//                        + File.separator
//                        + "FolderName" + File.separator;
//                if (!new File(mBaseFolderPath).exists()) {
//                    new File(mBaseFolderPath).mkdir();
//                }
//
//                String mFilePath = "file://" + mBaseFolderPath + "/" + "teste";

            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url))
                    .setTitle(title)
                    .setDescription("Downloading")
                    .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                    .setDestinationInExternalPublicDir(pathToDownload, title)
                    .setAllowedOverMetered(true)
                    .setAllowedOverRoaming(true);

            DownloadManager mDownloadManager = (DownloadManager) this.getSystemService(Context.DOWNLOAD_SERVICE);
            downloadId = mDownloadManager.enqueue(request);
            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(MainActivity.this, "Iniciando donwload", Toast.LENGTH_LONG).show();
                }
            });
        } else {
            requestWriteStoragePermission();
        }
    }

    private void requestWriteStoragePermission() {
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            new AlertDialog.Builder(this)
                    .setTitle("Permissão necessária")
                    .setMessage("Esta permissão é necessária pois o aplicativo irá fazer transfêrencia na memória interna")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, WIRTE_STORAGE_PERMISSION_CODE);
                        }
                    })
                    .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();
        } else {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, WIRTE_STORAGE_PERMISSION_CODE);
            moveFile(new File(Environment.getExternalStorageDirectory().toString() + File.separator + pathToDownload + File.separator + details.title() + ".mp4"), path);
            runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(MainActivity.this, "Movendo vídeo", Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    private void requestReadStoragePermission() {
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            new AlertDialog.Builder(this)
                    .setTitle("Permissão necessária")
                    .setMessage("Esta permissão é necessária pois o aplicativo irá ler na memória interna")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, READ_STORAGE_PERMISSION_CODE);
                        }
                    })
                    .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();
        } else {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, READ_STORAGE_PERMISSION_CODE);

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == WIRTE_STORAGE_PERMISSION_CODE) {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permissão concedida", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Permissão negada", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void downloadButton(View v) {
        videoURL = videoWithAudioFormats.get(qualityPosition[0]).url();
        new DownloadAsync().execute();

    }

    public void openDirectory(View v) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP){
            Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
            i.addCategory(Intent.CATEGORY_DEFAULT);
            startActivityForResult(Intent.createChooser(i, "Escolha o diretório"), 9999);
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
                path = split[1];//assign it to a string(your choice).
                if(path.split("0/").length > 1) {
                    pathToDownload = path.split("0/")[1].split("/")[0];
                } else {
                    pathToDownload = path.split("/")[0];
                }
                directoryText.setText(path);
                break;
        }
    }

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    private static boolean moveFile(File source, String destPath){
//        verifyStoragePermissions(MainActivity.this);
        Log.e("moveFile", source.exists()+source.getName()+source.getTotalSpace());
        if(source.exists()){
            Log.e("moveFile", destPath);
            File dest = new File(destPath + File.separator + source.getName());
            checkMakeDirs(dest.getParent());
            try (FileInputStream fis = new FileInputStream(source);
                 FileOutputStream fos = new FileOutputStream(dest)){
                if(!dest.exists()){
                    dest.createNewFile();
                }
                writeToOutputStream(fis, fos);
                source.delete();
                return true;
            } catch (IOException ioE){
                Log.e("TAG", ioE.getMessage());
            }
        }
        return false;
    }

    private static void writeToOutputStream(InputStream is, OutputStream os) throws IOException {
        byte[] buffer = new byte[1024];
        int length;
        if (is != null) {
            while ((length = is.read(buffer)) > 0x0) {
                os.write(buffer, 0x0, length);
            }
        }
        os.flush();
    }

    private static boolean checkMakeDirs(String dirPath){
        try {
            File dir = new File(dirPath);
            return dir.exists() || dir.mkdirs();
        } catch (Exception e) {
            Log.e("TAG", e.getMessage());
        }
        return false;
    }

    private class DownloadAsync extends AsyncTask<Void, Void, Void>
    {
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        protected Void doInBackground(Void... params) {
            downloadVideo(details.title(), videoURL);
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {

//            moveFile(new File(Environment.DIRECTORY_DCIM+File.separator+"Camera"+"20201108_152137.jpg"), Environment.DIRECTORY_DOWNLOADS);
        }
    }


}