package org.robotics.blinkworld.stories.ui.editor

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.DialogFragment
import org.robotics.blinkworld.R

class TextEditorDialogFragment : DialogFragment() {
    private var mAddTextEditText: EditText? = null
    private var mAddTextDoneTextView: TextView? = null
    private var mInputMethodManager: InputMethodManager? = null
    private var mTextEditor: TextEditor? = null

    interface TextEditor {
        fun onDone(inputText: String?, position: Int)
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        mAddTextDoneTextView!!.requestFocus()

        //Make dialog full screen with transparent background
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.MATCH_PARENT
            dialog.window!!.setLayout(width, height)
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.add_text_dialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mAddTextEditText = view.findViewById(R.id.add_text_edit_text)
        mInputMethodManager =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        mAddTextDoneTextView = view.findViewById(R.id.add_text_done_tv)
        position = requireArguments().getInt(SELECTED_POSITION)
        mAddTextEditText?.setText(requireArguments().getString(EXTRA_INPUT_TEXT))
        mInputMethodManager?.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)

        //Make a callback on activity when user is done with text editing
        mAddTextDoneTextView?.setOnClickListener { mView ->
            mInputMethodManager!!.hideSoftInputFromWindow(mView.windowToken, 0)
            dismiss()
            val inputText = mAddTextEditText?.text.toString()
            if (!TextUtils.isEmpty(inputText) && mTextEditor != null) {
                mTextEditor?.onDone(inputText, position)
            }
        }
    }

    //Callback to listener if user is done with text editing
    fun setOnTextEditorListener(textEditor: TextEditor?) {
        mTextEditor = textEditor
    }

    companion object {
        val TAG = TextEditorDialogFragment::class.java.simpleName

        const val EXTRA_INPUT_TEXT = "extra_input_text"
        const val SELECTED_POSITION = "extra_selected_position"
        private var position = 0

        //Show dialog with provide text
        fun show(
            appCompatActivity: AppCompatActivity,
            inputText: String,
            position: Int
        ): TextEditorDialogFragment {
            val args = Bundle()
            args.putString(EXTRA_INPUT_TEXT, inputText)
            args.putInt(SELECTED_POSITION, position)
            val fragment = TextEditorDialogFragment()
            fragment.arguments = args
            fragment.show(appCompatActivity.supportFragmentManager, TAG)
            return fragment
        }

        fun show(appCompatActivity: AppCompatActivity, position: Int): TextEditorDialogFragment {
            return show(
                appCompatActivity,
                "", position
            )
        }
    }
}