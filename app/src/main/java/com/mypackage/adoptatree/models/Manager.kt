package com.mypackage.adoptatree.models

class Manager : Person {
    var isManager: Boolean = true

    constructor()
    constructor(name: String, email: String, uid: String) : super(name, email, uid) {}
}