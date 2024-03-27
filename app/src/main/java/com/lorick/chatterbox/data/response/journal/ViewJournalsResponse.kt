package com.lorick.chatterbox.data.response.journal

import com.lorick.chatterbox.base.BaseResponse

data class ViewJournalsResponse(
    val `data`: ViewJournalsDataResponse,
) : BaseResponse()

data class ViewJournalsDataResponse(
    val createdOn: String,
    val journalDate: String,
    val journalId: Int,
    val notes: String,
    val subject: String,
    val type: Type,
    val typeId: Int,
    val user: Any,
    val userId: Int,
    val weightJournalNotes: ArrayList<WeightJournalNotes>?,
    val weightJournalNotesAs: ArrayList<WeightJournalNotesA>?,
    val weightJournalNotesBs: ArrayList<WeightJournalNotesB>?
)
data class Type(
    val journalCategoryType: String,
    val journals: List<Any>,
    val masterJournalCatId: Int
)

data class WeightJournalNotesA(
    val createdon: String,
    val journalId: Int,
    val notesPros: String,
    val notesTitle: String?,
    val totalPoints: Int?,
    val weightItemsAs: ArrayList<WeightItemsA>?,
    val weightJournalNotesId: Int,
    val wt: Double
)

data class WeightItemsA(
    var itemDesc: String?,
    val typeId: Int?,
)
data class WeightJournalNotesB(
    val createdon: String,
    val journalId: Int,
    val notesPros: String,
    val notesTitle: String?,
    val totalPoints: Int?,
    val weightItemsBs: ArrayList<WeightItemsB>?,
    val weightJournalNotesId: Int,
    val wt: Double
)

data class WeightItemsB(
    var itemDesc: String?,
    val typeId: Int?,
)

data class WeightJournalNotes(
    var weightJournalNotesId: Int,
    val notesTitle: String,
    val notesPros: String,
    val createdon: String,
    val journalId: String,
    val totalPoints: String,
)

