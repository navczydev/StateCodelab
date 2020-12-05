/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.codelabs.state.todo

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumnFor
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.ui.tooling.preview.Devices
import androidx.ui.tooling.preview.Preview
import androidx.ui.tooling.preview.UiMode
import com.codelabs.state.util.generateRandomTodoItem
import kotlin.random.Random

/**
 * Stateless component that is responsible for the entire todo screen.
 *
 * @param items (state) list of [TodoItem] to display
 * @param onAddItem (event) request an item be added
 * @param onRemoveItem (event) request an item be removed
 */
@Composable
fun TodoScreen(
    items: List<TodoItem>,
    currentlyEditing: TodoItem?,
    onAddItem: (TodoItem) -> Unit,
    onRemoveItem: (TodoItem) -> Unit,
    onStartEdit: (TodoItem) -> Unit,
    onEditItemChange: (TodoItem) -> Unit,
    onEditDone: () -> Unit
) {
    Column {
        val enableTopSection = currentlyEditing == null
        TodoItemInputBackground(elevate = enableTopSection, modifier = Modifier.fillMaxWidth()) {
            if (enableTopSection) {
                TodoItemEntryInput(onItemComplete = onAddItem)
            } else {
                Text(
                    "Editing item",
                    style = MaterialTheme.typography.h6,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .align(Alignment.CenterVertically)
                        .padding(16.dp)
                        .fillMaxWidth()
                )
            }
        }
        LazyColumnFor(
            items = items,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(top = 8.dp)
        ) { todo ->
            if (currentlyEditing?.id == todo.id) {
                TodoItemInlineEditor(
                    item = currentlyEditing,
                    onEditItemChange = onEditItemChange,
                    onEditDone = onEditDone,
                    onRemoveItem = { onRemoveItem(todo) }
                )
            } else {
                TodoRow(
                    todo = todo,
                    onItemClicked = { onStartEdit(it) },
                    modifier = Modifier.fillParentMaxWidth()
                )
            }
        }

        // For quick testing, a random item generator button
        Button(
            onClick = { onAddItem(generateRandomTodoItem()) },
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
        ) {
            Text("Add random item")
        }
    }
}

/**
 * Stateless composable that displays a full-width [TodoItem].
 *
 * @param todo item to show
 * @param onItemClicked (event) notify caller that the row was clicked
 * @param modifier modifier for this element
 */
@Composable
fun TodoRow(
    todo: TodoItem,
    onItemClicked: (TodoItem) -> Unit,
    modifier: Modifier = Modifier,
    iconAlpha: Float = remember(todo.id) { randomTint() }
) {
    Row(
        modifier = modifier
            .clickable { onItemClicked(todo) }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(todo.task)
        Icon(
            todo.icon.vectorAsset,
            tint = AmbientContentColor.current.copy(alpha = iconAlpha)
        )
    }
}

@Composable
fun TodoItemEntryInput(onItemComplete: (TodoItem) -> Unit) {
    val (text, setText) = remember { mutableStateOf("") }
    val (icon, setIcon) = remember { mutableStateOf(TodoIcon.Default) }
    val iconVisible = text.isNotBlank()
    val submit = {
        if (text.isNotBlank()) {
            onItemComplete(TodoItem(text, icon))
            setIcon(TodoIcon.Default)
            setText("")
        }
    }
    // onItemComplete is an event will fire when an item is completed by the user
    TodoItemInput(
        text = text,
        onTextChange = setText,
        submit = submit,
        iconVisible = iconVisible,
        icon = icon,
        onIconChange = setIcon,
    ) {
        TodoEditButton(onClick = submit, text = "Add", enabled = text.isNotBlank())
    }
}

@Composable
fun TodoItemInlineEditor(
    item: TodoItem,
    onEditItemChange: (TodoItem) -> Unit,
    onEditDone: () -> Unit,
    onRemoveItem: () -> Unit
) = TodoItemInput(
    text = item.task,
    onTextChange = { onEditItemChange(item.copy(task = it)) },
    icon = item.icon,
    onIconChange = { onEditItemChange(item.copy(icon = it)) },
    submit = onEditDone,
    iconVisible = true,
    buttonSlot = {
        Row {
            val shrinkButtons = Modifier.widthIn(20.dp)
            TextButton(onClick = onEditDone, modifier = shrinkButtons) {
                Text(
                    text = "\uD83D\uDCBE", // floppy disk
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(30.dp)
                )
            }
            TextButton(onClick = onRemoveItem, modifier = shrinkButtons) {
                Text(
                    text = "❌",
                    textAlign = TextAlign.End,
                    modifier = Modifier.width(30.dp)
                )
            }
        }
    }
)

@Composable
fun TodoItemInput(
    text: String,
    onTextChange: (String) -> Unit,
    submit: () -> Unit,
    iconVisible: Boolean,
    icon: TodoIcon,
    onIconChange: (TodoIcon) -> Unit,
    buttonSlot: @Composable () -> Unit
) {
    Column {
        Row(
            Modifier
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp)
        ) {
            TodoInputText(
                text = text, onTextChange = onTextChange, Modifier
                    .weight(1f)
                    .padding(end = 8.dp), submit
            )
            Spacer(modifier = Modifier.width(8.dp))
            Box(Modifier.align(Alignment.CenterVertically)) { buttonSlot() }
        }
        when {
            iconVisible -> {
                AnimatedIconRow(
                    icon = icon,
                    onIconChange = onIconChange,
                    modifier = Modifier.padding(16.dp)
                )
            }
            else -> {
                Spacer(modifier = Modifier.padding(16.dp))
            }
        }
    }
}

private fun randomTint(): Float {
    return Random.nextFloat().coerceIn(0.3f, 0.9f)
}

@Preview(
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_NO,
    widthDp = 800,
    heightDp = 400,
    device = Devices.PIXEL_4_XL
)
@Composable
fun PreviewTodoScreen() {
    val items = listOf(
        TodoItem("Learn compose", TodoIcon.Event),
        TodoItem("Take the codelab"),
        TodoItem("Apply state", TodoIcon.Done),
        TodoItem("Build dynamic UIs", TodoIcon.Square)
    )
    TodoScreen(items, null, {}, {}, {}, {}, {})
}

@Preview(showBackground = true)
@Composable
fun PreviewTodoScreenEditing() {
    TodoScreen(listOf(
        TodoItem("Learn compose", TodoIcon.Event),
    ), TodoItem("Learn compose", TodoIcon.Event), {}, {}, {}, {}, {})
}

@Preview(showBackground = true)
@Composable
fun PreviewTodoItemInout() = TodoItemEntryInput(onItemComplete = {})

@Preview(showBackground = true)
@Composable
fun PreviewTodoRow() {
    val todo = remember { generateRandomTodoItem() }
    TodoRow(todo = todo, onItemClicked = {}, modifier = Modifier.fillMaxWidth())
}
