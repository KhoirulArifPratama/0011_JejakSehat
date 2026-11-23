package com.example.jejaksehat

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidationTest {
    @Test fun validMemberFields() { assertTrue(Validation.name("Budi")); assertTrue(Validation.height("170")); assertTrue(Validation.weight("65")) }
    @Test fun invalidMemberRanges() { assertFalse(Validation.height("20")); assertFalse(Validation.height("300")); assertFalse(Validation.weight("0")) }
    @Test fun futureDateRejected() { assertFalse(Validation.dateNotFuture("2999-01-01")); assertTrue(Validation.dateNotFuture("2020-01-01")); assertFalse(Validation.dateNotFuture("tanggal")) }
    @Test fun categoryIsCaseInsensitive() { assertTrue(Validation.recordCategory(" penyakit ")); assertFalse(Validation.recordCategory("demam")) }
    @Test fun medicalDocumentRule() { assertTrue(Validation.document("image/jpeg", 1024)); assertFalse(Validation.document("application/pdf", 1024)); assertFalse(Validation.document("image/png", 6L * 1024 * 1024)) }
    @Test fun visitNeedsComplaintOrDiagnosis() { assertTrue(Validation.visitHasComplaintOrDiagnosis("Demam", "")); assertFalse(Validation.visitHasComplaintOrDiagnosis("", "")) }
}
