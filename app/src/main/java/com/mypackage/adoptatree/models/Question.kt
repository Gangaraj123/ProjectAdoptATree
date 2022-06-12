package com.mypackage.adoptatree.models

class Question {
    var question: String? = null
    var answer: String? = null
    var answeredBy: String? = null
    var answeredOn: Long = 0 // timestamps
    var askedOn: Long = 0

    constructor() {}
    constructor(question: String, answer: String? = null, answeredBy: String? = null) {
        this.question = question
        this.answer = answer
        this.answeredBy = answeredBy
        this.askedOn = System.currentTimeMillis()
    }
}