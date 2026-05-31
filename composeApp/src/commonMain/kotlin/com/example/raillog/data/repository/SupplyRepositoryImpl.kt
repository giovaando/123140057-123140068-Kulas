package com.example.raillog.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.example.raillog.data.local.RailLogDatabase
import com.example.raillog.domain.model.DraftItem
import com.example.raillog.domain.model.SupplyItem
import com.example.raillog.domain.model.PartCategory
import com.example.raillog.domain.model.Priority
import com.example.raillog.domain.model.SupplyStatus
import com.example.raillog.domain.repository.SupplyRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Instant // IMPORT INSTANT

class SupplyRepositoryImpl(
    db: RailLogDatabase
) : SupplyRepository {

    private val queries = db.supplyItemQueries
    private val draftQueries = db.draftRequisitionQueries

    override fun getAllItems(): Flow<List<SupplyItem>> {
        return queries.getAllItems()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities ->
                entities.map { entity ->
                    SupplyItem(
                        id = entity.id,
                        partCode = entity.part_code,
                        name = entity.name,
                        category = PartCategory.fromString(entity.category),
                        quantity = entity.quantity.toInt(),
                        unit = entity.unit ?: "",
                        supplier = entity.supplier ?: "",
                        status = SupplyStatus.fromString(entity.status),
                        priority = Priority.fromString(entity.priority),
                        documentRef = entity.document_ref,
                        notes = entity.notes ?: "",
                        createdAt = Instant.fromEpochMilliseconds(entity.created_at),
                        updatedAt = Instant.fromEpochMilliseconds(entity.updated_at)
                    )
                }
            }
    }

    override fun getItemById(id: Long): Flow<SupplyItem?> {
        return queries.getAllItems()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .map { entities ->
                val entity = entities.find { it.id == id }
                entity?.let {
                    SupplyItem(
                        id = it.id,
                        partCode = it.part_code,
                        name = it.name,
                        category = PartCategory.fromString(it.category),
                        quantity = it.quantity.toInt(),
                        unit = it.unit ?: "",
                        supplier = it.supplier ?: "",
                        status = SupplyStatus.fromString(it.status),
                        priority = Priority.fromString(it.priority),
                        documentRef = it.document_ref,
                        notes = it.notes ?: "",
                        createdAt = Instant.fromEpochMilliseconds(it.created_at),
                        updatedAt = Instant.fromEpochMilliseconds(it.updated_at)
                    )
                }
            }
    }

    override suspend fun insertItem(item: SupplyItem) {
        withContext(Dispatchers.IO) {
            queries.insertItem(
                name = item.name,
                part_code = item.partCode,
                category = item.category.name,
                priority = item.priority.name,
                status = item.status.name,
                quantity = item.quantity.toLong(),
                supplier = item.supplier,
                unit = item.unit,
                notes = item.notes,
                document_ref = item.documentRef ?: "",
                created_at = item.createdAt.toEpochMilliseconds(),
                updated_at = item.updatedAt.toEpochMilliseconds()
            )
        }
    }

    override suspend fun updateItem(item: SupplyItem) {
        withContext(Dispatchers.IO) {
            queries.updateItem(
                name = item.name,
                part_code = item.partCode,
                category = item.category.name,
                priority = item.priority.name,
                status = item.status.name,
                quantity = item.quantity.toLong(),
                supplier = item.supplier,
                unit = item.unit,
                notes = item.notes,
                document_ref = item.documentRef ?: "",
                updated_at = item.updatedAt.toEpochMilliseconds(),
                id = item.id
            )
        }
    }

    override suspend fun deleteItem(id: Long) {
        withContext(Dispatchers.IO) {
            queries.deleteItem(id)
        }
    }

    override suspend fun updateStatus(id: Long, status: SupplyStatus) {
        withContext(Dispatchers.IO) {
            val entities = queries.getAllItems().executeAsList()
            val entity = entities.find { it.id == id }

            if (entity != null) {
                queries.updateItem(
                    name = entity.name,
                    part_code = entity.part_code,
                    category = entity.category,
                    priority = entity.priority,
                    status = status.name,
                    quantity = entity.quantity,
                    supplier = entity.supplier,
                    unit = entity.unit,
                    notes = entity.notes ?: "",
                    document_ref = entity.document_ref ?: "",
                    updated_at = entity.updated_at,
                    id = id
                )
            }
        }
    }

    // ==========================================
    // FITUR DRAFT SQLDELIGHT
    // ==========================================
    override suspend fun saveDraft(draftId: String, projectTitle: String, currentStep: Int, lastUpdated: Long, formStateJson: String) {
        withContext(Dispatchers.IO) { draftQueries.insertOrReplaceDraft(draftId, projectTitle, currentStep.toLong(), lastUpdated, formStateJson) }
    }
    override fun getAllDrafts(): Flow<List<DraftItem>> {
        return draftQueries.getAllDrafts().asFlow().mapToList(Dispatchers.IO).map { entities ->
            entities.map { DraftItem(it.draftId, it.projectTitle, it.currentStep.toInt(), it.lastUpdated, it.formStateJson) }
        }
    }
    override suspend fun deleteDraft(draftId: String) {
        withContext(Dispatchers.IO) { draftQueries.deleteDraftById(draftId) }
    }
}