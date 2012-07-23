package com.echoed.chamber.controllers

case class ErrorResult(error: String) {

}

object ErrorResult {
    def timeout = ErrorResult("timeout")
}
