package com.example.raillog.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.example.raillog.data.local.RailLogDatabase
import com.example.raillog.domain.model.DocumentType
import com.example.raillog.domain.model.TechnicalDocument
import com.example.raillog.domain.model.VerificationStatus
import com.example.raillog.domain.repository.TechnicalDocumentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant

class TechnicalDocumentRepositoryImpl(
    db: RailLogDatabase
) : TechnicalDocumentRepository {

    private val queries = db.technicalDocumentEntityQueries

    override fun getAllDocuments(): Flow<List<TechnicalDocument>> {
        return queries.getAllDocuments()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities ->
                entities.map { entity ->
                    TechnicalDocument(
                        id = entity.id,
                        title = entity.title,
                        documentType = DocumentType.fromString(entity.document_type),
                        content = entity.content,
                        linkedItemId = entity.linked_item_id,
                        verificationStatus = VerificationStatus.fromString(entity.verification_status),
                        aiSummary = entity.ai_summary,
                        createdAt = Instant.fromEpochMilliseconds(entity.created_at)
                    )
                }
            }
    }

    override fun getDocumentById(id: Long): Flow<TechnicalDocument?> {
        return queries.getDocumentById(id)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { list ->
                list.firstOrNull()?.let { entity ->
                    TechnicalDocument(
                        id = entity.id,
                        title = entity.title,
                        documentType = DocumentType.fromString(entity.document_type),
                        content = entity.content,
                        linkedItemId = entity.linked_item_id,
                        verificationStatus = VerificationStatus.fromString(entity.verification_status),
                        aiSummary = entity.ai_summary,
                        createdAt = Instant.fromEpochMilliseconds(entity.created_at)
                    )
                }
            }
    }

    override fun getDocumentsByItem(itemId: Long): Flow<List<TechnicalDocument>> {
        return queries.getDocumentsByItem(itemId)
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities ->
                entities.map { entity ->
                    TechnicalDocument(
                        id = entity.id,
                        title = entity.title,
                        documentType = DocumentType.fromString(entity.document_type),
                        content = entity.content,
                        linkedItemId = entity.linked_item_id,
                        verificationStatus = VerificationStatus.fromString(entity.verification_status),
                        aiSummary = entity.ai_summary,
                        createdAt = Instant.fromEpochMilliseconds(entity.created_at)
                    )
                }
            }
    }

    override fun getUnverifiedDocuments(): Flow<List<TechnicalDocument>> {
        return queries.getUnverifiedDocuments()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities ->
                entities.map { entity ->
                    TechnicalDocument(
                        id = entity.id,
                        title = entity.title,
                        documentType = DocumentType.fromString(entity.document_type),
                        content = entity.content,
                        linkedItemId = entity.linked_item_id,
                        verificationStatus = VerificationStatus.fromString(entity.verification_status),
                        aiSummary = entity.ai_summary,
                        createdAt = Instant.fromEpochMilliseconds(entity.created_at)
                    )
                }
            }
    }

    override suspend fun insertDocument(document: TechnicalDocument) {
        withContext(Dispatchers.IO) {
            queries.insertDocument(
                title = document.title,
                document_type = document.documentType.name,
                content = document.content,
                linked_item_id = document.linkedItemId,
                verification_status = document.verificationStatus.name,
                ai_summary = document.aiSummary,
                created_at = document.createdAt.toEpochMilliseconds()
            )
        }
    }

    override suspend fun updateVerification(
        id: Long,
        status: VerificationStatus,
        aiSummary: String?
    ) {
        withContext(Dispatchers.IO) {
            queries.updateVerification(
                verification_status = status.name,
                ai_summary = aiSummary,
                id = id
            )
        }
    }

    override suspend fun deleteDocument(id: Long) {
        withContext(Dispatchers.IO) {
            queries.deleteDocument(id)
        }
    }
}