package com.echoed.chamber.domain.public

trait Content {

    def _type:          String
    def _id:            String
    def _updatedOn:     Long
    def _createdOn:     Long
    def _views:         Int
    def _votes:         Int
    def _comments:      Int


}
