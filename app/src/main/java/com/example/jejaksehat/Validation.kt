package com.example.jejaksehat

import java.time.LocalDate

object Validation {
    private val emailPattern = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
    fun email(value: String) = emailPattern.matches(value.trim())
    fun name(value: String) = value.trim().length >= 2
    fun relation(value: String) = value.trim().isNotEmpty()
    fun height(value: String): Boolean = value.toIntOrNull()?.let { it in 30..250 } ?: false
    fun weight(value: String): Boolean = value.toDoubleOrNull()?.let { it in 1.0..300.0 } ?: false
    fun dateNotFuture(value: String): Boolean = runCatching { !LocalDate.parse(value.trim()).isAfter(LocalDate.now()) }.getOrDefault(false)
    fun recordCategory(value: String) = value.trim().uppercase() in setOf("PENYAKIT", "ALERGI")
    fun visitHasComplaintOrDiagnosis(complaint: String, diagnosis: String) = complaint.isNotBlank() || diagnosis.isNotBlank()
    fun document(mime: String, sizeBytes: Long) = mime in setOf("image/jpeg", "image/png") && sizeBytes <= 5L * 1024 * 1024
}
