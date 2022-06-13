package com.mypackage.adoptatree.Maintainance.Update

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class Image_upload_ViewModel : ViewModel() {

    private val _text = MutableLiveData<String>()
    val text: LiveData<String> get() = _text
    fun setData(str: String) {
        _text.value = str

    }
}