package edu.obya.c4.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun <T> Class<T>.logger(): Logger = LoggerFactory.getLogger(this)

fun String.logger(): Logger = LoggerFactory.getLogger(this)
