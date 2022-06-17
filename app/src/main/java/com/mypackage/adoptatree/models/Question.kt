package com.mypackage.adoptatree.models

class Question {
    var question: String? = null
    var answer: String? = null
    var answeredBy: String? = null
    var answeredOn: Long = 0 // timestamps
    var askedOn: Long = 0
    var isAnswered:Boolean=false
    constructor() {}
    constructor(question: String, answer: String? = null, answeredBy: String? = null,askedon:Long?=null) {
        this.question = question
        this.answer = answer
        this.answeredBy = answeredBy
        if(askedon!=null)
        this.askedOn =askedon
        else this.askedOn=System.currentTimeMillis()
    }
}