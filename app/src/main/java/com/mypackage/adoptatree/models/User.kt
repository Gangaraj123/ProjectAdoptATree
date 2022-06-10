package com.mypackage.adoptatree.models

class User : Person {
    var isManager: Boolean = false

    constructor()
    constructor(name: String, email: String, uid: String) : super(name, email, uid) {}
}
