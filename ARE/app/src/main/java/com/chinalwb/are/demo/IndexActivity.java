package com.chinalwb.are.demo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class IndexActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_index);

        initViews();
    }

    private void initViews() {
        Button defaultToolbarButton = this.findViewById(R.id.defaultToolbar);
        openPage(defaultToolbarButton, ARE_DefaultToolbarActivity.class);
    }

    private void openPage(Button button, final Class activity) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(IndexActivity.this, activity);
                startActivity(intent);
            }
        });
    }
}
