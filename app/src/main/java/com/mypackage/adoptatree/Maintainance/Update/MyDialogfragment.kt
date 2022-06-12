package com.mypackage.adoptatree.Maintainance.Update

 import android.annotation.SuppressLint
 import android.content.DialogInterface
 import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
 import com.mypackage.adoptatree.R

class MyDialogfragment : DialogFragment() {
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		super.onCreateView(inflater, container, savedInstanceState)
		return inflater.inflate(R.layout.verifying_dialog_fragment, container, false)
	}

	override fun setCancelable(cancelable: Boolean) {
		super.setCancelable(false)
	}
	override fun onCancel(dialog: DialogInterface) {

	}
}