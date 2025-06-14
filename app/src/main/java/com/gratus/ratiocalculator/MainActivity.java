package com.gratus.ratiocalculator;

import static androidx.constraintlayout.widget.ConstraintProperties.WRAP_CONTENT;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText input01, input02, input_A1, input_A2;
    private TextView ratioFraction;
    private boolean isUpdating = false; // Flag to track updates
    private ImageButton clearButton, addRatio, subtractRatio; // Clear button

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Initialize views
        input01 = findViewById(R.id.input01);
        input02 = findViewById(R.id.input02);
        input_A1 = findViewById(R.id.input_A1);
        input_A2 = findViewById(R.id.input_A2);
        ratioFraction = findViewById(R.id.ratiofraction);
        clearButton = findViewById(R.id.clearButton); // Initialize clear but
        addRatio = findViewById(R.id.ratios_add);
        subtractRatio = findViewById(R.id.ratios_subtract);

        // Set padding for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        addOutputRows(3);

        // Set click listener for clear button
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearFields();
            }
        });
    }
    private void addOutputRows(int count) {
        ConstraintLayout container = findViewById(R.id.outputratio_const);
        ConstraintSet cs = new ConstraintSet();
        cs.clone(container);

        // anchor under the existing second‐input field of row#1
        int prevBottomId = R.id.input_A2;

        for (int i = 0; i < count; i++) {
            // 1) create a new ConstraintLayout “row”
            ConstraintLayout row = new ConstraintLayout(this);
            int rowId = View.generateViewId();
            row.setId(rowId);
            row.setLayoutParams(new ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.MATCH_CONSTRAINT,
                    ConstraintLayout.LayoutParams.WRAP_CONTENT
            ));

            // 2) colon TextView
            TextView colon = new TextView(this);
            int colonId = View.generateViewId();
            colon.setId(colonId);
            colon.setText(":");
            colon.setTextSize(18);
            colon.setTypeface(Typeface.DEFAULT_BOLD);
            ConstraintLayout.LayoutParams colonLp = new ConstraintLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
            colonLp.rightToRight = ConstraintLayout.LayoutParams.PARENT_ID;
            colonLp.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID;
            colonLp.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
            colonLp.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
            row.addView(colon, colonLp);

            // 3) left TextInputEditText
            TextInputEditText left = new TextInputEditText(this);
            int leftId = View.generateViewId();
            left.setId(leftId);
            left.setHint("");
            left.setGravity(Gravity.CENTER);
            left.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            ConstraintLayout.LayoutParams leftLp = new ConstraintLayout.LayoutParams(dpToPx(0), WRAP_CONTENT);
            leftLp.setMarginStart(dpToPx(16));  // Start margin
            leftLp.setMarginEnd(dpToPx(16));    // End margin
            leftLp.leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID;
            leftLp.rightToLeft = colonId;
            leftLp.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
            leftLp.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
            row.addView(left, leftLp);

            // 4) right TextInputEditText
            TextInputEditText right = new TextInputEditText(this);
            int rightId = View.generateViewId();
            right.setId(rightId);
            right.setHint("");
            right.setGravity(Gravity.CENTER);
            right.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            ConstraintLayout.LayoutParams rightLp = new ConstraintLayout.LayoutParams(dpToPx(0), WRAP_CONTENT);
            rightLp.setMarginStart(dpToPx(16));  // Start margin
            rightLp.setMarginEnd(dpToPx(16));    // End margin
            rightLp.leftToRight = colonId;
            rightLp.rightToRight = ConstraintLayout.LayoutParams.PARENT_ID;
            rightLp.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
            rightLp.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;
            row.addView(right, rightLp);

            // 5) add the row to the container and position it
            container.addView(row);
            cs.clone(container);
            cs.connect(rowId, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
            cs.connect(rowId, ConstraintSet.END,   ConstraintSet.PARENT_ID, ConstraintSet.END);
            cs.connect(rowId, ConstraintSet.TOP,   prevBottomId,           ConstraintSet.BOTTOM, dpToPx(8));
            cs.applyTo(container);

            prevBottomId = rowId;  // next row will go below this one
        }
    }

    // helper to convert dp → px
    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }


    // Clear all input and output fields
    private void clearFields() {
        isUpdating = true; // Prevent triggering TextWatcher during clear
        input01.setText("");
        input02.setText("");
        input_A1.setText("");
        input_A2.setText("");
        ratioFraction.setText("0.0");
        // Clear dynamically added rows
        ConstraintLayout container = findViewById(R.id.outputratio_const);
        for (int i = 0; i < container.getChildCount(); i++) {
            View child = container.getChildAt(i);
            if (child instanceof ConstraintLayout) { // Check if it's a row
                for (int j = 0; j < ((ConstraintLayout) child).getChildCount(); j++) {
                    View subChild = ((ConstraintLayout) child).getChildAt(j);
                    if (subChild instanceof TextInputEditText) {
                        ((TextInputEditText) subChild).setText(""); // Clear text fields
                    }
                }
            }
        }
        isUpdating = false;
    }
}