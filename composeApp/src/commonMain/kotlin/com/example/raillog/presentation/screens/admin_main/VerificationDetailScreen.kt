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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.raillog.presentation.theme.RailLogColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerificationDetailScreen(
    requisitionId: Long,
    onNavigateBack: () -> Unit
) {
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
                    IconButton(onClick = onNavigateBack) {
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
                            "Requisition #$requisitionId",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.height(12.dp))

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
                                "AI Confidence 94%",
                                color = RailLogColors.Success,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    "AI Extracted Information",
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
                            value = "LRT-JABO-24A",
                            valid = true
                        )

                        VerificationField(
                            label = "Destination",
                            value = "Depok Workshop",
                            valid = true
                        )

                        VerificationField(
                            label = "Material Count",
                            value = "12 Items",
                            valid = true
                        )

                        VerificationField(
                            label = "Requested Quantity",
                            value = "84 Units",
                            valid = false
                        )
                    }
                }
            }

            item {
                Text(
                    "AI Findings",
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
                            text = "Material quantity mismatch",
                            success = false
                        )

                        FindingRow(
                            text = "Project code validated",
                            success = true
                        )

                        FindingRow(
                            text = "Destination validated",
                            success = true
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
                        onClick = { }
                    ) {
                        Text("Request Revision")
                    }

                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = { }
                    ) {
                        Text("Approve")
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
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

        Column {

            Text(
                label,
                style = MaterialTheme.typography.labelMedium
            )

            Text(
                value,
                fontWeight = FontWeight.SemiBold
            )
        }

        Icon(
            imageVector = if (valid)
                Icons.Default.CheckCircle
            else
                Icons.Default.Warning,
            contentDescription = null,
            tint = if (valid)
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
            imageVector = if (success)
                Icons.Default.CheckCircle
            else
                Icons.Default.Warning,
            contentDescription = null,
            tint = if (success)
                RailLogColors.Success
            else
                RailLogColors.Error
        )

        Spacer(Modifier.width(8.dp))

        Text(text)
    }
}