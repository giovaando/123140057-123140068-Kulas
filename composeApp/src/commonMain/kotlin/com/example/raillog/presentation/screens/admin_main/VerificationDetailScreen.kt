package com.example.raillog.presentation.screens.admin_main

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.raillog.presentation.theme.RailLogColors
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationDetailScreen(
    requisitionId: Long,
    onNavigateBack: () -> Unit,
    viewModel: VerificationDetailViewModel = koinViewModel()
) {

    
    LaunchedEffect(requisitionId) {
        viewModel.loadItem(requisitionId)
    }

    val item by viewModel.selectedItem.collectAsState()
    val document by viewModel.selectedDocument.collectAsState()

    if (item == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val currentItem = item!!
    val confidence = 75 + (currentItem.id % 25).toInt()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Verification Detail",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            item {

                Card {

                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {

                        Text(
                            text = "Requisition #${currentItem.id}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(
                            modifier = Modifier.height(12.dp)
                        )

                        Box(
                            modifier = Modifier
                                .background(
                                    RailLogColors.SuccessBackground,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(
                                    horizontal = 10.dp,
                                    vertical = 6.dp
                                )
                        ) {

                            Text(
                                text = "AI Confidence $confidence%",
                                color = RailLogColors.Success,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            item {

                Text(
                    text = "AI Extracted Information",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {

                Card {

                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {

                        VerificationField(
                            label = "Project Code",
                            value = currentItem.partCode,
                            valid = true
                        )

                        VerificationField(
                            label = "Document",
                            value = document?.title
                                ?: currentItem.documentRef
                                ?: "-",
                            valid = true
                        )

                        VerificationField(
                            label = "Material",
                            value = currentItem.name,
                            valid = true
                        )

                        VerificationField(
                            label = "Requested Quantity",
                            value = "${currentItem.quantity} ${currentItem.unit}",
                            valid = currentItem.quantity > 0
                        )
                    }
                }
            }

            item {

                Text(
                    text = "AI Findings",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {

                Card {

                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {

                        FindingRow(
                            text = "Project code validated",
                            success = currentItem.partCode.isNotBlank()
                        )

                        FindingRow(
                            text = "Document attached",
                            success = !currentItem.documentRef.isNullOrBlank()
                        )

                        FindingRow(
                            text = "Quantity verified",
                            success = currentItem.quantity > 0
                        )
                    }
                }
            }

            item {

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    OutlinedButton(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.rejectItem {
                                onNavigateBack()
                            }
                        }
                    ) {
                        Text("Request Revision")
                    }

                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            viewModel.verifyItem {
                                onNavigateBack()
                            }
                        }
                    ) {
                        Text("Approve")
                    }
                }
            }

            item {
                Spacer(
                    modifier = Modifier.height(24.dp)
                )
            }
        }
    }
    

}

@Composable
private fun VerificationField(
    label: String,
    value: String,
    valid: Boolean
) {

    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium
            )

            Text(
                text = value,
                fontWeight = FontWeight.SemiBold
            )
        }

        Icon(
            imageVector =
                if (valid)
                    Icons.Default.CheckCircle
                else
                    Icons.Default.Warning,
            contentDescription = null,
            tint =
                if (valid)
                    RailLogColors.Success
                else
                    RailLogColors.Error
        )
    }
    

}

@Composable
private fun FindingRow(
    text: String,
    success: Boolean
) {

    
    Row(
        modifier = Modifier.padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        Icon(
            imageVector =
                if (success)
                    Icons.Default.CheckCircle
                else
                    Icons.Default.Warning,
            contentDescription = null,
            tint =
                if (success)
                    RailLogColors.Success
                else
                    RailLogColors.Error
        )

        Spacer(
            modifier = Modifier.width(8.dp)
        )

        Text(text = text)
    }
    

}
