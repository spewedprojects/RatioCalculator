package com.gratus.ratiocalculator;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText inputF01, inputF02, inputF03, inputF04;
    private TextView ratioFraction;
    private boolean isUpdating = false; // Flag to track updates
    private ImageButton clearButton; // Clear button

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Initialize views
        inputF01 = findViewById(R.id.inputF_01);
        inputF02 = findViewById(R.id.inputF_02);
        inputF03 = findViewById(R.id.inputF_03);
        inputF04 = findViewById(R.id.inputF_04);
        ratioFraction = findViewById(R.id.ratiofraction);
        clearButton = findViewById(R.id.clearButton); // Initialize clear but

        // Set padding for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Text watcher for input fields to calculate ratio
        inputF01.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!isUpdating) {
                    calculateRatio();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        inputF02.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!isUpdating) {
                    calculateRatio();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Text watcher for output fields to apply ratio
        inputF03.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!isUpdating) {
                    applyRatio();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        inputF04.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!isUpdating) {
                    applyRatio();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Set click listener for clear button
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearFields();
            }
        });
    }

    // Calculate the ratio and update the display
    private void calculateRatio() {
        try {
            double a = Double.parseDouble(inputF01.getText().toString());
            double b = Double.parseDouble(inputF02.getText().toString());
            double ratio = a / b;
            ratioFraction.setText(String.format("%.3f", ratio));
        } catch (NumberFormatException e) {
            ratioFraction.setText("0.0");
        }
    }

    // Apply the ratio to the input value and update the corresponding field
    private void applyRatio() {
        try {
            double a = Double.parseDouble(inputF01.getText().toString());
            double b = Double.parseDouble(inputF02.getText().toString());
            double ratio = a / b;

            isUpdating = true; // Set flag before updating text

            if (!inputF03.getText().toString().isEmpty()) {
                double value = Double.parseDouble(inputF03.getText().toString());
                double result = value * ratio;
                inputF04.setText(String.format("%.2f", result));
            } else if (!inputF04.getText().toString().isEmpty()) {
                double value = Double.parseDouble(inputF04.getText().toString());
                double result = value / ratio;
                inputF03.setText(String.format("%.2f", result));
            }

            isUpdating = false; // Reset flag after update
        } catch (NumberFormatException e) {
            inputF03.setText("");
            inputF04.setText("");
            isUpdating = false;
        }
    }

    // Clear all input and output fields
    private void clearFields() {
        isUpdating = true; // Prevent triggering TextWatcher during clear
        inputF01.setText("");
        inputF02.setText("");
        inputF03.setText("");
        inputF04.setText("");
        ratioFraction.setText("x.xxx");
        isUpdating = false;
    }
}