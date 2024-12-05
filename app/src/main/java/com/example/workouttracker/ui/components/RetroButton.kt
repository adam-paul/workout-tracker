package com.example.workouttracker.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

@Composable
fun RetroButton(
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier,
    onEdit: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    keepActionsVisible: Boolean = false
) {
    var isPressed by remember { mutableStateOf(false) }
    var showActions by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }

    LaunchedEffect(keepActionsVisible) {
        if (!keepActionsVisible) {
            showActions = false
            isDeleting = false
        }
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .background(Color(0xFFD0D0D0))
                .border(1.dp, Color.Black)
                .padding(1.dp)
        ) {
            // Top and left highlight
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        width = 4.dp,
                        color = Color.White,
                        shape = RectangleShape
                    )
            )

            // Bottom and right shadow
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(
                        width = 4.dp,
                        color = Color.Gray,
                        shape = RectangleShape
                    )
                    .padding(top = 2.dp, start = 2.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                isPressed = true
                                tryAwaitRelease()
                                isPressed = false
                            },
                            onTap = {
                                if (showActions) {
                                    showActions = false
                                } else {
                                    onClick()
                                }
                            },
                            onLongPress = {
                                if (onEdit != null && onDelete != null && !showActions) {
                                    showActions = true
                                }
                                else if (showActions) {
                                    showActions = false
                                }
                            }
                        )
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = text,
                            modifier = Modifier
                                .offset {
                                    IntOffset(
                                        x = if (isPressed) 1.dp.roundToPx() else 0,
                                        y = if (isPressed) 1.dp.roundToPx() else 0
                                    )
                                },
                            color = Color.Black,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (trailingIcon != null) {
                        CompositionLocalProvider(LocalContentColor provides Color.Black) {  // Force black color for icons
                            trailingIcon()
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = (showActions && onEdit != null && onDelete != null) || keepActionsVisible,
            enter = slideInHorizontally(initialOffsetX = { it }),
            // Use immediate exit if deleting, otherwise animate
            exit = if (isDeleting) ExitTransition.None else slideOutHorizontally(targetOffsetX = { it })
        ) {
            Row(
                modifier = Modifier.padding(start = 8.dp)
            ) {
                IconButton(onClick = {
                    onEdit?.invoke()
                    if (!keepActionsVisible) showActions = false
                }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = {
                    isDeleting = true
                    onDelete?.invoke()
                    if (!keepActionsVisible) showActions = false
                }) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                }
            }
        }
    }
}