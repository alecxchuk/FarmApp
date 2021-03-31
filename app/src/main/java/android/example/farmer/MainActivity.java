package android.example.farmer;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    // private variable to store
    private TextView poultrySection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // find textView by id and store in text view variable
        poultrySection = findViewById(R.id.poultry_main);

        // set click listener on text view to know when clicked
        poultrySection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // set up intent to open pens activity
                Intent pensIntent = new Intent(MainActivity.this, Pens.class);
                startActivity(pensIntent);

            }
        });
    }
}