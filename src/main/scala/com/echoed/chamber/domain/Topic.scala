package com.echoed.chamber.domain

case class Topic(
    id: String,
    createdOn: Long,
    updatedOn: Long,
    refType: String,
    refId: String,
    title: String,
    description: String,
    beginOn: Long,
    endOn: Long) {

}
