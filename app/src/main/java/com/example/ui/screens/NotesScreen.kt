package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NoteItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun NotesScreen(
    notes: List<NoteItem>,
    onTogglePin: (NoteItem) -> Unit,
    onDeleteNote: (NoteItem) -> Unit,
    onAddNoteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sdf = SimpleDateFormat("dd.MM.yyyy, HH:mm", Locale.getDefault())

    val notePastelColors = listOf(
        Color(0xFFFEF3C7) to Color(0xFF78350F), // Yellow (bg to text)
        Color(0xFFDBEAFE) to Color(0xFF1E3A8A), // Blue
        Color(0xFFD1FAE5) to Color(0xFF064E3B), // Mint
        Color(0xFFEDE9FE) to Color(0xFF581C87), // Lavender
        Color(0xFFFCE7F3) to Color(0xFF831843)  // Rose
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddNoteClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .padding(bottom = 80.dp)
                    .testTag("add_note_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Yangi eslatma yozish")
            }
        }
    ) { innerPadding ->
        if (notes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(bottom = 100.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(32.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Eslatmalar yo'q",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Fikrlar, g'oyalar va muhim eslatmalarni saqlab borish uchun '+' tugmasini bosing.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .testTag("notes_lazy_list"),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(notes, key = { it.id }) { note ->
                    val colorPair = notePastelColors.getOrElse(note.colorIndex % notePastelColors.size) {
                        notePastelColors[0]
                    }
                    val cardBg = colorPair.first
                    val textColor = colorPair.second

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("note_card_${note.id}")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = note.title,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = textColor
                                    ),
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Row {
                                    IconButton(
                                        onClick = { onTogglePin(note) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (note.isPinned) Icons.Filled.PushPin else Icons.Outlined.PushPin,
                                            contentDescription = if (note.isPinned) "Qadalgan" else "Qadash",
                                            tint = textColor,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { onDeleteNote(note) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "O'chirish",
                                            tint = textColor.copy(alpha = 0.7f),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = note.content,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = textColor.copy(alpha = 0.9f),
                                    lineHeight = 20.sp
                                )
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(textColor.copy(alpha = 0.12f))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = note.category,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = textColor
                                    )
                                }

                                Text(
                                    text = sdf.format(Date(note.updatedAt)),
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = textColor.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
