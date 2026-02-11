package com.gratus.ratiocalculator

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.widget.EditText
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.Group
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText
import java.text.DecimalFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var ip_colA: TextInputEditText
    private lateinit var ip_colB: TextInputEditText
    private lateinit var ip_colB1: TextInputEditText
    private lateinit var ip_colB2: TextInputEditText
    private lateinit var op_rowA: TextInputEditText
    private lateinit var op_rowB: TextInputEditText
    private lateinit var op_rowB1: TextInputEditText
    private lateinit var op_rowB2: TextInputEditText
    
    private lateinit var frac1: TextView
    private lateinit var frac2: TextView
    private lateinit var frac3: TextView
    
    private lateinit var clearButton: ImageButton
    private lateinit var addRatio: ImageButton
    private lateinit var subRatio: ImageButton
    private lateinit var rowsContainer: LinearLayout

    private var isUpdating = false // Flag to track updates
    
    /* ----------  F I E L D S  ---------- */
    private var visibleParts = MIN_PARTS     // start with 2

    // quick holders for the extra-column groups in the top card
    private lateinit var topGroups: Array<Group>    // { CR_g_1, CR_g_2 }

    // list of rows, each carries its own RR_g_1 & RR_g_2
    private val rowGroups = ArrayList<Array<Group>>()

    private var doubleBackToExitPressedOnce = false
    private val backPressHandler = Handler(Looper.getMainLooper())

    fun View.clearFocusOnKeyboardHideAllInputs() {
        // Collect all EditText children recursively
        val editTexts = mutableListOf<TextInputEditText>()
        fun collect(view: View) {
            if (view is TextInputEditText) {
                editTexts.add(view)
            } else if (view is ViewGroup) {
                for (i in 0 until view.childCount) {
                    collect(view.getChildAt(i))
                }
            }
        }
        collect(this)

        // Apply listener once to root
        ViewCompat.setOnApplyWindowInsetsListener(rootView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)

            if (!insets.isVisible(WindowInsetsCompat.Type.ime())) {
                editTexts.forEach { it.clearFocus() }
            }
            insets
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Initialize views
        ip_colA = findViewById(R.id.colA)
        ip_colB = findViewById(R.id.colB)
        ip_colB1 = findViewById(R.id.colB1)
        ip_colB2 = findViewById(R.id.colB2)
        
        op_rowA = findViewById(R.id.fieldA)
        op_rowB = findViewById(R.id.fieldB)
        op_rowB1 = findViewById(R.id.fieldB1)
        op_rowB2 = findViewById(R.id.fieldB2)
        
        frac1 = findViewById(R.id.ratiofraction)
        frac2 = findViewById(R.id.ratiofraction1)
        frac3 = findViewById(R.id.ratiofraction2)
        
        clearButton = findViewById(R.id.clearButton)
        addRatio = findViewById(R.id.ratios_add)
        subRatio = findViewById(R.id.ratios_subtract)
        rowsContainer = findViewById(R.id.rows_container)

        val icon = findViewById<ImageView>(R.id.github_icon)
        icon.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.github.com/spewedprojects/RatioCalculator"))
            it.context.startActivity(intent)
        }

        /*  top-card groups  */
        topGroups = arrayOf(
            findViewById(R.id.CR_g_1),
            findViewById(R.id.CR_g_2)
        )
        // make sure they start hidden
        for (g in topGroups) g.visibility = View.GONE

        setupDynamicRowLogic()
        setupRatioListeners()

        /*  hook buttons  */
        addRatio.setOnClickListener { addPart() }
        subRatio.setOnClickListener { removePart() }

        // Set click listener for clear button
        clearButton.setOnClickListener { clearAllFields() }

        val rootView = findViewById<View>(R.id.main)
        rootView.clearFocusOnKeyboardHideAllInputs()
    }


    private fun setupDynamicRowLogic(){
        /*  row-level groups – collect once for every row you inflate  */
        for (i in 0 until rowsContainer.childCount) {
            val row = rowsContainer.getChildAt(i)

            // Get the 3rd and 4th column Group blocks inside this row
            val g1 = row.findViewById<Group>(R.id.RR_g_1)
            val g2 = row.findViewById<Group>(R.id.RR_g_2)

            // Store these in your list to toggle later with +/- buttons
            rowGroups.add(arrayOf(g1, g2))

            // Set their initial visibility based on how many parts are visible
            g1.visibility = if (visibleParts > 2) View.VISIBLE else View.GONE
            g2.visibility = if (visibleParts > 3) View.VISIBLE else View.GONE
        }

        // rowsContainer already found earlier - textwatcher
        for (r in 0 until rowsContainer.childCount) {
            val row = rowsContainer.getChildAt(r)

            for (col in 0 until MAX_PARTS) {
                val field = row.findViewById<TextInputEditText>(FIELD_IDS[col]) ?: continue // hidden in 2-part mode

                val typedCol = col // capture index
                field.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
                    override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
                    override fun afterTextChanged(s: Editable?) {
                        if (isUpdating) return          // only guard against re-entrant calls

                        isUpdating = true

                        if (s.isNullOrEmpty()) {
                            clearRow(row)                       // NEW  ➜ blank all columns
                        } else {
                            propagateRowFromField(row, typedCol) // existing math
                        }
                        isUpdating = false
                    }
                })
            }
        }
    }

    private fun setupRatioListeners(){
        val ratioIds = intArrayOf(R.id.colA, R.id.colB, R.id.colB1, R.id.colB2)

        for (i in ratioIds.indices) {
            val ratioField = findViewById<TextInputEditText>(ratioIds[i])

            ratioField.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    updateRatioPreviews()
                    updateAllRows()  // optional: recalc if ratio changes
                }
            })
        }
    }

    /* ----------  +  ------------ */
    private fun addPart() {
        if (visibleParts >= MAX_PARTS) return           // already at 4

        // index 0 = turning on 3rd part, index 1 = turning on 4th part
        val block = visibleParts - 2
        topGroups[block].visibility = View.VISIBLE
        for (gs in rowGroups) gs[block].visibility = View.VISIBLE

        visibleParts++
    }

    /* ----------  –  ------------ */
    private fun removePart() {
        if (visibleParts <= MIN_PARTS) return           // already at 2

        visibleParts--
        val block = visibleParts - 2                    // same mapping
        topGroups[block].visibility = View.GONE
        for (gs in rowGroups) gs[block].visibility = View.GONE
    }

    private fun getAllRows(): List<View> {
        val rows = ArrayList<View>()
        val rowsContainer = findViewById<LinearLayout>(R.id.rows_container)
        for (i in 0 until rowsContainer.childCount) {
            rows.add(rowsContainer.getChildAt(i))
        }
        return rows
    }

    /* -------- parse the current top-ratio (only visible parts) -------- */
    private fun getCurrentRatio(): FloatArray {
        val parts = FloatArray(visibleParts)
        for (i in 0 until visibleParts) {
            val e = findViewById<TextInputEditText>(RATIO_IDS[i])
            parts[i] = try {
                e.text.toString().toFloat()
            } catch (ex: Exception) {
                0f
            }
        }
        return parts
    }

    /* -------- update the small A÷B, B÷C, C÷D previews -------- */
    private fun updateRatioPreviews() {
        val r = getCurrentRatio()
        for (i in 0 until visibleParts - 1) {
            val pv = findViewById<TextView>(PREVIEW_IDS[i])
            if (r[i] > 0 && r[i + 1] > 0) {
                pv.text = fmt(r[i] / r[i + 1])        // << use fmt()
            } else {
                pv.text = "–"
            }
        }
    }

    /* -------- recalc one row from whichever column was typed -------- */
    private fun propagateRowFromField(row: View, typedIdx: Int) {
        val ratio = getCurrentRatio()
        if (typedIdx >= visibleParts || ratio[typedIdx] == 0f) return

        val typedField = row.findViewById<TextInputEditText>(FIELD_IDS[typedIdx])
        val typedVal: Float
        try {
            typedVal = typedField.text.toString().toFloat()
        } catch (e: Exception) {
            return
        }

        val scale = typedVal / ratio[typedIdx]

        for (i in 0 until visibleParts) {
            val f = row.findViewById<TextInputEditText>(FIELD_IDS[i])
            if (i == typedIdx || f == null) continue
            f.setText(fmt(ratio[i] * scale))        // << use fmt()
        }
    }

    /* -------- OPTIONAL: when ratio changes, refresh all rows -------- */
    private fun updateAllRows() {
        for (i in 0 until rowsContainer.childCount) {
            val row = rowsContainer.getChildAt(i)
            // find first non-empty field, use it as anchor
            for (col in 0 until visibleParts) {
                val f = row.findViewById<TextInputEditText>(FIELD_IDS[col])
                if (f != null && f.length() > 0) {
                    propagateRowFromField(row, col)
                    break
                }
            }
        }
    }

    private fun clearRow(row: View) {
        for (i in 0 until visibleParts) {
            val f = row.findViewById<TextInputEditText>(FIELD_IDS[i])
            f?.setText("")
        }
    }

    // Clear all input and output fields
    private fun clearAllFields() {
        // --- clear top ratio fields ---
        val ratioIds = intArrayOf(R.id.colA, R.id.colB, R.id.colB1, R.id.colB2)
        for (id in ratioIds) {
            val field = findViewById<TextInputEditText>(id)
            field.setText("")
        }

        // --- clear top preview texts ---
        val previewIds = intArrayOf(R.id.ratiofraction, R.id.ratiofraction1, R.id.ratiofraction2)
        for (id in previewIds) {
            val preview = findViewById<TextView>(id)
            preview.text = "0.0"
        }

        // --- clear each row ---
        for (row in getAllRows()) {
            val rowFieldIds = intArrayOf(
                R.id.fieldA, R.id.fieldB, R.id.fieldB1, R.id.fieldB2
            )
            for (id in rowFieldIds) {
                val field = row.findViewById<TextInputEditText>(id)
                field.setText("")
            }
        }
    }

    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            super.onBackPressed()
            return
        }
        doubleBackToExitPressedOnce = true
        Toast.makeText(this, "Press BACK again to exit", Toast.LENGTH_SHORT).show()
        backPressHandler.postDelayed({ doubleBackToExitPressedOnce = false }, 2000)
    }

    override fun onDestroy() {
        super.onDestroy()
        backPressHandler.removeCallbacksAndMessages(null)
    }

    companion object {
        private val RATIO_IDS = intArrayOf(R.id.colA, R.id.colB, R.id.colB1, R.id.colB2)
        private val PREVIEW_IDS = intArrayOf(R.id.ratiofraction, R.id.ratiofraction1, R.id.ratiofraction2)
        private val FIELD_IDS = intArrayOf(R.id.fieldA, R.id.fieldB, R.id.fieldB1, R.id.fieldB2)
        private const val MIN_PARTS = 2   // always keep at least A:B
        private const val MAX_PARTS = 4

        private fun fmt(v: Float): String {
            // show "48"   instead of "48.00"
            // show "32.5" instead of "32.50"
            return if (v == v.toLong().toFloat()) {                     // integer test
                String.format(Locale.US, "%d", v.toLong())
            } else {
                DecimalFormat("0.##").format(v.toDouble())     // up to 2 dec places, no trailing zeros
            }
        }
    }
}
