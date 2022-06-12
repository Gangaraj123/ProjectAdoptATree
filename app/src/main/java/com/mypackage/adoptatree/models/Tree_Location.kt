package com.mypackage.adoptatree.models

import android.location.Address

class Tree_Location {
    var latitude:Double= 0.0
    var longitude:Double=0.0
    var country:String="India"
    var state:String=""
    constructor(){}
    constructor(location:Address){

    }
}