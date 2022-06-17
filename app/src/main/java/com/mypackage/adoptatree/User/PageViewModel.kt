package com.mypackage.adoptatree.User

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mypackage.adoptatree.models.Question

class PageViewModel : ViewModel() {

    private val ques = MutableLiveData<Question>()
    val question: LiveData<Question> get() = ques
    fun setData(que: Question) {
        ques.value = que
    }

}