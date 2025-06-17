package com.gratus.ratiocalculator;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Group;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText ip_colA, ip_colB, ip_colB1, ip_colB2, op_rowA, op_rowB, op_rowB1, op_rowB2;
    private LinearLayout rowsContainer;
    private static final int[] RATIO_IDS   = {R.id.colA, R.id.colB, R.id.colB1, R.id.colB2};
    private static final int[] PREVIEW_IDS = {R.id.ratiofraction, R.id.ratiofraction1, R.id.ratiofraction2};
    private static final int[] FIELD_IDS   = {R.id.fieldA, R.id.fieldB, R.id.fieldB1, R.id.fieldB2};
    private TextView frac1, frac2, frac3;
    private boolean isUpdating = false; // Flag to track updates
    private ImageButton clearButton, addRatio, subRatio; // Clear button
    /* ----------  F I E L D S  ---------- */
    private static final int MIN_PARTS = 2;   // always keep at least A:B
    private static final int MAX_PARTS = 4;

    private int visibleParts = MIN_PARTS;     // start with 2

    // quick holders for the extra-column groups in the top card
    private Group[] topGroups;    // { CR_g_1, CR_g_2 }

    // list of rows, each carries its own RR_g_1 & RR_g_2
    private final List<Group[]> rowGroups = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Initialize views
        ip_colA = findViewById(R.id.colA);ip_colB = findViewById(R.id.colB);ip_colB1 = findViewById(R.id.colB1);ip_colB2 = findViewById(R.id.colB2);
        op_rowA = findViewById(R.id.fieldA);op_rowB = findViewById(R.id.fieldB);op_rowB1 = findViewById(R.id.fieldB1);op_rowB2 = findViewById(R.id.fieldB2);
        frac1 = findViewById(R.id.ratiofraction);frac2 = findViewById(R.id.ratiofraction1);frac3 = findViewById(R.id.ratiofraction2);
        clearButton = findViewById(R.id.clearButton); // Initialize clear but
        addRatio = findViewById(R.id.ratios_add);
        subRatio = findViewById(R.id.ratios_subtract);
        rowsContainer = findViewById(R.id.rows_container);
        int[] ratioFieldIds = { R.id.colA, R.id.colB, R.id.colB1, R.id.colB2 };
        int[] previewIds = { R.id.ratiofraction, R.id.ratiofraction1, R.id.ratiofraction2 };

        // Set padding for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ImageView icon = findViewById(R.id.github_icon);
        icon.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.github.com/spewedprojects/RatioCalculator"));
            v.getContext().startActivity(intent);
        });

        //addOutputRows(3);

        /*  top-card groups  */
        topGroups = new Group[] {
                findViewById(R.id.CR_g_1),
                findViewById(R.id.CR_g_2)
        };
        // make sure they start hidden
        for (Group g : topGroups) g.setVisibility(View.GONE);

        /*  row-level groups – collect once for every row you inflate  */
        for (int i = 0; i < rowsContainer.getChildCount(); i++) {
            View row = rowsContainer.getChildAt(i);

            // Get the 3rd and 4th column Group blocks inside this row
            Group g1 = row.findViewById(R.id.RR_g_1);
            Group g2 = row.findViewById(R.id.RR_g_2);

            // Store these in your list to toggle later with +/- buttons
            rowGroups.add(new Group[] { g1, g2 });

            // Set their initial visibility based on how many parts are visible
            g1.setVisibility(visibleParts > 2 ? View.VISIBLE : View.GONE);
            g2.setVisibility(visibleParts > 3 ? View.VISIBLE : View.GONE);
        }

        // rowsContainer already found earlier - textwatcher
        for (int r = 0; r < rowsContainer.getChildCount(); r++) {
            View row = rowsContainer.getChildAt(r);

            for (int col = 0; col < MAX_PARTS; col++) {
                TextInputEditText field = row.findViewById(FIELD_IDS[col]);
                if (field == null) continue;                   // hidden in 2-part mode

                final int typedCol = col;                      // capture index
                field.addTextChangedListener(new TextWatcher() {
                    @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
                    @Override public void onTextChanged(CharSequence s, int st, int b, int c) {}
                    @Override public void afterTextChanged(Editable s) {
                        if (isUpdating) return;          // only guard against re-entrant calls

                        isUpdating = true;

                        if (s.length() == 0) {
                            clearRow(row);                       // NEW  ➜ blank all columns
                        } else {
                            propagateRowFromField(row, typedCol); // existing math
                        }
                        isUpdating = false;
                    }
                });
            }
        }


        int[] ratioIds = { R.id.colA, R.id.colB, R.id.colB1, R.id.colB2 };

        for (int i = 0; i < ratioIds.length; i++) {
            TextInputEditText ratioField = findViewById(ratioIds[i]);
            int finalI = i;

            ratioField.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override public void afterTextChanged(Editable s) {
                    updateRatioPreviews();
                    updateAllRows();  // optional: recalc if ratio changes
                }
            });
        }

        /*  hook buttons  */
        addRatio.setOnClickListener(v -> addPart());
        subRatio.setOnClickListener(v -> removePart());

        // Set click listener for clear button
        clearButton.setOnClickListener(v -> clearAllFields());
    }

    /* ----------  +  ------------ */
    private void addPart() {
        if (visibleParts >= MAX_PARTS) return;           // already at 4

        // index 0 = turning on 3rd part, index 1 = turning on 4th part
        int block = visibleParts - 2;
        topGroups[block].setVisibility(View.VISIBLE);
        for (Group[] gs : rowGroups) gs[block].setVisibility(View.VISIBLE);

        visibleParts++;
    }

    /* ----------  –  ------------ */
    private void removePart() {
        if (visibleParts <= MIN_PARTS) return;           // already at 2

        visibleParts--;
        int block = visibleParts - 2;                    // same mapping
        topGroups[block].setVisibility(View.GONE);
        for (Group[] gs : rowGroups) gs[block].setVisibility(View.GONE);
    }

    private List<View> getAllRows() {
        List<View> rows = new ArrayList<>();
        LinearLayout rowsContainer = findViewById(R.id.rows_container);
        for (int i = 0; i < rowsContainer.getChildCount(); i++) {
            rows.add(rowsContainer.getChildAt(i));
        }
        return rows;
    }

    /* -------- parse the current top-ratio (only visible parts) -------- */
    private float[] getCurrentRatio() {
        float[] parts = new float[visibleParts];
        for (int i = 0; i < visibleParts; i++) {
            EditText e = findViewById(RATIO_IDS[i]);
            try { parts[i] = Float.parseFloat(e.getText().toString()); }
            catch (Exception ex) { parts[i] = 0f; }
        }
        return parts;
    }

    /* -------- update the small A÷B, B÷C, C÷D previews -------- */
    private void updateRatioPreviews() {
        float[] r = getCurrentRatio();
        for (int i = 0; i < visibleParts - 1; i++) {
            TextView pv = findViewById(PREVIEW_IDS[i]);
            if (r[i] > 0 && r[i + 1] > 0) {
                pv.setText(fmt(r[i] / r[i + 1]));        // << use fmt()
            } else {
                pv.setText("–");
            }
        }
    }

    /* -------- recalc one row from whichever column was typed -------- */
    private void propagateRowFromField(View row, int typedIdx) {
        float[] ratio = getCurrentRatio();
        if (typedIdx >= visibleParts || ratio[typedIdx] == 0f) return;

        TextInputEditText typedField = row.findViewById(FIELD_IDS[typedIdx]);
        float typedVal;
        try { typedVal = Float.parseFloat(typedField.getText().toString()); }
        catch (Exception e) { return; }

        float scale = typedVal / ratio[typedIdx];

        for (int i = 0; i < visibleParts; i++) {
            TextInputEditText f = row.findViewById(FIELD_IDS[i]);
            if (i == typedIdx || f == null) continue;
            f.setText(fmt(ratio[i] * scale));        // << use fmt()
        }
    }

    /* -------- OPTIONAL: when ratio changes, refresh all rows -------- */
    private void updateAllRows() {
        for (int i = 0; i < rowsContainer.getChildCount(); i++) {
            View row = rowsContainer.getChildAt(i);
            // find first non-empty field, use it as anchor
            for (int col = 0; col < visibleParts; col++) {
                TextInputEditText f = row.findViewById(FIELD_IDS[col]);
                if (f != null && f.length() > 0) {
                    propagateRowFromField(row, col);
                    break;
                }
            }
        }
    }

    private static String fmt(float v) {
        // show "48"   instead of "48.00"
        // show "32.5" instead of "32.50"
        if (v == (long) v) {                     // integer test
            return String.format(Locale.US, "%d", (long) v);
        } else {
            return new DecimalFormat("0.##")     // up to 2 dec places, no trailing zeros
                    .format(v);
        }
    }

    private void clearRow(View row) {
        for (int i = 0; i < visibleParts; i++) {
            TextInputEditText f = row.findViewById(FIELD_IDS[i]);
            if (f != null) f.setText("");
        }
    }

    // Clear all input and output fields
    private void clearAllFields() {
        // --- clear top ratio fields ---
        int[] ratioIds = { R.id.colA, R.id.colB, R.id.colB1, R.id.colB2 };
        for (int id : ratioIds) {
            TextInputEditText field = findViewById(id);
            field.setText("");
        }

        // --- clear top preview texts ---
        int[] previewIds = { R.id.ratiofraction, R.id.ratiofraction1, R.id.ratiofraction2 };
        for (int id : previewIds) {
            TextView preview = findViewById(id);
            preview.setText("0.0");
        }

        // --- clear each row ---
        for (View row : getAllRows()) {
            int[] rowFieldIds = {
                    R.id.fieldA, R.id.fieldB, R.id.fieldB1, R.id.fieldB2
            };
            for (int id : rowFieldIds) {
                TextInputEditText field = row.findViewById(id);
                field.setText("");
            }
        }
    }

}