package com.mirado

case class Url (value: String)

case class Hash (value: String)

case class Entry (hash: Hash, url: Url)
