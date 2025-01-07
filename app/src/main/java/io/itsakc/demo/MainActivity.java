package io.itsakc.demo;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.text.Editable;
import android.text.Spannable;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import io.itsakc.demo.databinding.ActivityMainBinding;

import io.itsakc.piecetable.core.PieceTable;
import io.itsakc.piecetable.text.Buffer;
import io.itsakc.piecetable.util.DynamicList;
import io.itsakc.piecetable.util.SearchResult;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int MANAGE_EXTERNAL_STORAGE_REQUEST_CODE = 101;
    private PieceTable pieceTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Inflate and get instance of binding
        binding = ActivityMainBinding.inflate(getLayoutInflater());

        // set content view to binding's root
        setContentView(binding.getRoot());

        Buffer buffer = new Buffer();
        buffer.append("Hello World!");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivity(intent);
            }
        }

        String initialContent = "Hello, 123 World 765";
        
        pieceTable = new PieceTable(false);
        pieceTable.enableThrowException(true);
//                StringBuilder builder = new StringBuilder();
//                builder.append(System.currentTimeMillis())
//                    .append("\n");
//        //        pieceTable.loadInitialContent(new File("/sdcard/idelog.txt"));
//        // Check for permissions
//        if (checkPermissions()) {
//            loadContent();
//            builder.append(System.currentTimeMillis());
//        } else {
//            requestPermissions();
//        }
//        pieceTable.loadInitialContent(initialContent);
//        pieceTable.insert(5, "H");
//        pieceTable.append("This string is appended!");
//        pieceTable.insert(pieceTable.length(), "\n");
//        pieceTable.insert(pieceTable.length(), "This string is inserted!");

//        List<SearchResult> results = pieceTable.performMultiSearch("\\d+", false);
//        StringBuilder string = new StringBuilder();
//
//        for (SearchResult result : results) {
//            string.append(result.toString());
//            string.append(" ");
//        }
        long startTime = System.currentTimeMillis();
        if (checkPermissions()) {
            loadContent();
        } else {
            requestPermissions();
        }
        long endTime = System.currentTimeMillis();
        binding.textview.setText("Time taken to open in multiple buffer mode: " + (endTime - startTime) / 1000.0 + " seconds");
        CharSequence content = pieceTable.text();
        CharSequence text = "являться/24,35,24,52,87,106,128,53,88,107,129,25,36,25,54,89,108,130";
        binding.textview.setText(binding.textview.getText() + "\n\n\n\n" + content.subSequence(content.length() - text.length() - 1, content.length() - 1));
        

        DynamicList<String> list = new DynamicList<>();
//        StringBuilder results = new StringBuilder();
//        pieceTable.performMultiSearch(false, "\nThis", false, new PieceTable.MultiSearchListener() {
//            @Override
//            public void onMultiSearch(SearchResult result, int caret) {
//            	results.append(result.toString()).append(" ");
//            }
//        });
//                binding.textview.setText(
//                        pieceTable.text()
//                                + " "
//                                + pieceTable.performSingleSearch(true, "orl", false).toString()
//                                + " "
//                                + pieceTable.performSingleSearchAutomated("This.*?inserted!", true).toString()
//                                + " "
//                                + results.toString()
//                                + " lineCount: "
//                                + pieceTable.lineCount()
//                                + " lineOffset: "
//                                + pieceTable.lineOffset(200)
//                                + " lineRange1: "
//                                + pieceTable.lineRange(0).toString());

        long deleteStartTime = System.currentTimeMillis();
        pieceTable.delete(0, pieceTable.length() - 1);
        long deleteEndTime = System.currentTimeMillis();
        binding.textview.setText(binding.textview.getText() + "\nTime taken to delete in multiple buffer mode: " + (deleteEndTime - deleteStartTime) / 1000.0 + " seconds");

//        if (true) throw new Error("After: " + (System.currentTimeMillis() - endTime) / 1000.0);
        //
        //        binding.textview.setText(
        //                binding.textview.getText() + " After delete: " + pieceTable.text());

        binding.edittext.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(
                            CharSequence arg0, int arg1, int arg2, int arg3) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        // Update only the changed part of the text to minimize memory usage
                        if (count > 0) pieceTable.insert(start, s);
                        else pieceTable.delete(start - 1, start);
                        
                        // Efficiently update the TextView content
                        binding.textview.setText(pieceTable.text());
                    }

                    @Override
                    public void afterTextChanged(Editable arg0) {}
                });
    }

    private void loadContent() {
        pieceTable = new PieceTable();
        File file = new File(Environment.getExternalStorageDirectory().getPath() + "/test.txt");

        if (file.exists() && file.canRead()) {
            pieceTable.loadInitialContent(file);
            Toast.makeText(this, "Content loaded successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "File not found or cannot be read", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            int result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            return result == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, MANAGE_EXTERNAL_STORAGE_REQUEST_CODE);
            } catch (Exception e) {
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                startActivityForResult(intent, MANAGE_EXTERNAL_STORAGE_REQUEST_CODE);
            }
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadContent();
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MANAGE_EXTERNAL_STORAGE_REQUEST_CODE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    loadContent();
                } else {
                    Toast.makeText(this, "Manage External Storage Permission Denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.binding = null;
    }
}
