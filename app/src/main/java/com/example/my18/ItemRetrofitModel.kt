package com.example.my18

data class ItemRetrofitModel (
    var rnum: Long = 0,
    var productGb: String? = null,
    var prdlstNm: String? = null,
    var rawmtrl: String? = null,
    var capacity: String? = null,
    var imgurl1: String? = null
)

data class MyItem(
    val item:ItemRetrofitModel
)

data class MyItems(
    val items : MutableList<MyItem>
)

data class MyModel(
    val body: MyItems
)