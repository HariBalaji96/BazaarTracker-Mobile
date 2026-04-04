package com.example.bazaartracker

data class Vendor(
    val id: String,
    val name: String,
    val phone: String,
    val address: String
)

data class CreateVendorRequest(
    val name: String,
    val phone: String,
    val address: String
)

data class VendorResponse(
    val message: String?,
    val vendor: Vendor?
)
