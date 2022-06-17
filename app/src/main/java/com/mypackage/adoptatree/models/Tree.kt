package com.mypackage.adoptatree.models

class Tree {
    var planted_on: Long = 0
    var tree_nick_name: String? = null
    var location: Tree_Location? = null
    var adopted_by: String? = null
    var adopted_on:Long=0
    var tree_id: String = ""

    constructor() {}
    constructor(uid: String, name: String? = null, owner: String? = null) {
        this.tree_id = uid
        this.tree_nick_name = name
        this.adopted_by = owner
        this.planted_on = System.currentTimeMillis()
    }
}