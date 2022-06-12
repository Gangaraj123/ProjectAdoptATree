package com.mypackage.adoptatree.models

class Image {
    var image_url:String?=null
    var image_timestamp:Long=0
    constructor(){}
    constructor(imageurl:String?=null)
    {
        this.image_url=imageurl
        image_timestamp=System.currentTimeMillis()
    }
}