package de.michael.tolleapp.presentation.components

import android.content.Context
import android.util.AttributeSet
import android.widget.Button
import android.widget.GridLayout
import androidx.core.view.setMargins

class CustomDartKeyboard @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : GridLayout(context, attrs, defStyleAttr) {

    var onKeyPress: ((String) -> Unit)? = null

    init {
        rowCount = 6
        columnCount = 5
        setupNumberButtons()
        setupSpecialButtons()
    }

    private fun setupNumberButtons() {
        val numbers = (0..20).toList() + listOf(25)
        numbers.forEach { number ->
            val button = Button(context).apply {
                text = number.toString()
                setOnClickListener { onKeyPress?.invoke(number.toString()) }
            }
            val params = LayoutParams().apply {
                width = 0
                height = LayoutParams.WRAP_CONTENT
                columnSpec = spec(UNDEFINED, 1f)
                setMargins(8)
            }
            addView(button, params)
        }
    }

    private fun setupSpecialButtons() {
        val doubleButton = Button(context).apply {
            text = "Double"
            setOnClickListener { onKeyPress?.invoke("D") }
        }
        val tripleButton = Button(context).apply {
            text = "Triple"
            setOnClickListener { onKeyPress?.invoke("T") }
        }

        val doubleParams = LayoutParams().apply {
            width = 0
            height = LayoutParams.WRAP_CONTENT
            columnSpec = spec(0, 2, 1f)
            setMargins(8)
        }

        val tripleParams = LayoutParams().apply {
            width = 0
            height = LayoutParams.WRAP_CONTENT
            columnSpec = spec(2, 2, 1f)
            setMargins(8)
        }

        addView(doubleButton, doubleParams)
        addView(tripleButton, tripleParams)
    }
}
