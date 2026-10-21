package com.example.taskmanager

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

data class Task(
    val id: Int,
    val name: String,
    val category: String,
    val priority: Int,
    val isCompleted: Boolean = false
)

enum class TaskFilter(val label: String) {
    ALL("Összes"),
    ACTIVE("Aktív"),
    COMPLETED("Elkészült")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MaterialTheme {
                TaskManagerScreen()
            }
        }
    }
}

@Composable
fun TaskManagerScreen() {
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf("") }

    var nameError by remember { mutableStateOf(false) }
    var categoryError by remember { mutableStateOf(false) }
    var priorityError by remember { mutableStateOf(false) }

    var nextId by remember { mutableIntStateOf(1) }

    val tasks = remember { mutableStateListOf<Task>() }

    var selectedFilter by remember {
        mutableStateOf(TaskFilter.ALL)
    }

    val activeCount = tasks.count { !it.isCompleted }
    val completedCount = tasks.count { it.isCompleted }

    val filteredTasks = when (selectedFilter) {
        TaskFilter.ALL -> tasks.toList()
        TaskFilter.ACTIVE -> tasks.filter { !it.isCompleted }
        TaskFilter.COMPLETED -> tasks.filter { it.isCompleted }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(Modifier.height(4.dp))
        }

        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "Feladatkezelő",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Szervezd meg a napod <3",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        item {
            TaskInputFields(
                name = name,
                category = category,
                priority = priority,
                nameError = nameError,
                categoryError = categoryError,
                priorityError = priorityError,
                onNameChange = {
                    name = it
                    nameError = false
                },
                onCategoryChange = {
                    category = it
                    categoryError = false
                },
                onPriorityChange = {
                    priority = it
                    priorityError = false
                }
            )
        }

        item {
            Button(
                onClick = {
                    nameError = name.isBlank()
                    categoryError = category.isBlank()

                    val parsedPriority = priority.toIntOrNull()

                    priorityError =
                        parsedPriority == null || parsedPriority !in 1..3

                    if (nameError || categoryError || priorityError) {
                        Toast.makeText(
                            context,
                            "Ellenőrizd a megadott adatokat!",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        tasks.add(
                            Task(
                                id = nextId++,
                                name = name.trim(),
                                category = category.trim(),
                                priority = parsedPriority!!,
                                isCompleted = false
                            )
                        )

                        name = ""
                        category = ""
                        priority = ""

                        nameError = false
                        categoryError = false
                        priorityError = false

                        Toast.makeText(
                            context,
                            "Feladat sikeresen hozzáadva!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Feladat hozzáadása")
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TaskCounter(
                    label = "Összes",
                    count = tasks.size,
                    modifier = Modifier.weight(1f)
                )

                TaskCounter(
                    label = "Aktív",
                    count = activeCount,
                    modifier = Modifier.weight(1f)
                )

                TaskCounter(
                    label = "Elkészült",
                    count = completedCount,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            HorizontalDivider()
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TaskFilter.values().forEach { filter ->
                    if (selectedFilter == filter) {
                        Button(
                            onClick = {
                                selectedFilter = filter
                            },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(
                                horizontal = 4.dp,
                                vertical = 8.dp
                            )
                        ) {
                            Text(
                                text = filter.label,
                                maxLines = 1
                            )
                        }
                    } else {
                        OutlinedButton(
                            onClick = {
                                selectedFilter = filter
                            },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(
                                horizontal = 4.dp,
                                vertical = 8.dp
                            )
                        ) {
                            Text(
                                text = filter.label,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        if (filteredTasks.isEmpty()) {
            item {
                val emptyMessage = when {
                    tasks.isEmpty() -> {
                        "Még nincs feladatod."
                    }

                    selectedFilter == TaskFilter.ACTIVE -> {
                        "Nincs aktív feladat."
                    }

                    selectedFilter == TaskFilter.COMPLETED -> {
                        "Még nincs elkészült feladat."
                    }

                    else -> {
                        "Még nincs feladatod."
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 36.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = emptyMessage,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        } else {
            items(
                items = filteredTasks,
                key = { it.id }
            ) { task ->
                TaskCard(
                    task = task,
                    onToggleCompleted = {
                        val index = tasks.indexOfFirst {
                            it.id == task.id
                        }

                        if (index != -1) {
                            tasks[index] = tasks[index].copy(
                                isCompleted = !tasks[index].isCompleted
                            )
                        }
                    },
                    onDelete = {
                        tasks.removeAll {
                            it.id == task.id
                        }
                    },
                    onEdit = { editedTask ->
                        val index = tasks.indexOfFirst {
                            it.id == editedTask.id
                        }

                        if (index != -1) {
                            tasks[index] = editedTask

                            Toast.makeText(
                                context,
                                "Feladat sikeresen módosítva!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )
            }
        }

        item {
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
fun TaskInputFields(
    name: String,
    category: String,
    priority: String,
    nameError: Boolean,
    categoryError: Boolean,
    priorityError: Boolean,
    onNameChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onPriorityChange: (String) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = {
                Text("Feladat neve")
            },
            isError = nameError,
            supportingText = if (nameError) {
                {
                    Text("A feladat neve kötelező!")
                }
            } else {
                null
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = category,
            onValueChange = onCategoryChange,
            label = {
                Text("Kategória")
            },
            isError = categoryError,
            supportingText = if (categoryError) {
                {
                    Text("A kategória megadása kötelező!")
                }
            } else {
                null
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        OutlinedTextField(
            value = priority,
            onValueChange = onPriorityChange,
            label = {
                Text("Prioritás (1-3)")
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            isError = priorityError,
            supportingText = if (priorityError) {
                {
                    Text("A prioritás csak 1 és 3 közötti érték lehet!")
                }
            } else {
                null
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }
}

@Composable
fun TaskCounter(
    label: String,
    count: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge
            )

            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun TaskCard(
    task: Task,
    onToggleCompleted: () -> Unit,
    onDelete: () -> Unit,
    onEdit: (Task) -> Unit
) {
    val context = LocalContext.current

    var showDeleteDialog by remember {
        mutableStateOf(false)
    }

    var showEditDialog by remember {
        mutableStateOf(false)
    }

    var editName by remember {
        mutableStateOf(task.name)
    }

    var editCategory by remember {
        mutableStateOf(task.category)
    }

    var editPriority by remember {
        mutableStateOf(task.priority.toString())
    }

    var editNameError by remember {
        mutableStateOf(false)
    }

    var editCategoryError by remember {
        mutableStateOf(false)
    }

    var editPriorityError by remember {
        mutableStateOf(false)
    }

    val priorityText = when (task.priority) {
        1 -> "Alacsony prioritás"
        2 -> "Közepes prioritás"
        else -> "Magas prioritás"
    }

    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = task.priority.toString(),
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = task.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = task.category,
                    style = MaterialTheme.typography.bodyMedium
                )

                Text(
                    text = priorityText,
                    style = MaterialTheme.typography.bodySmall
                )

                Text(
                    text = if (task.isCompleted) {
                        "Elkészült"
                    } else {
                        "Aktív"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                Button(
                    onClick = onToggleCompleted,
                    contentPadding = PaddingValues(
                        horizontal = 12.dp,
                        vertical = 4.dp
                    )
                ) {
                    Text(
                        text = if (task.isCompleted) {
                            "Újra aktív"
                        } else {
                            "Elkészült"
                        }
                    )
                }
            }

            Column {
                IconButton(
                    onClick = {
                        editName = task.name
                        editCategory = task.category
                        editPriority = task.priority.toString()

                        editNameError = false
                        editCategoryError = false
                        editPriorityError = false

                        showEditDialog = true
                    }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_edit),
                        contentDescription = "Feladat szerkesztése"
                    )
                }

                IconButton(
                    onClick = {
                        showDeleteDialog = true
                    }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_delete),
                        contentDescription = "Feladat törlése"
                    )
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
            },
            title = {
                Text("Feladat törlése")
            },
            text = {
                Text(
                    "Biztosan törölni szeretnéd ezt a feladatot: ${task.name}?"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onDelete()
                    }
                ) {
                    Text("Törlés")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                    }
                ) {
                    Text("Mégse")
                }
            }
        )
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = {
                showEditDialog = false
            },
            title = {
                Text("Feladat szerkesztése")
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(
                        rememberScrollState()
                    )
                ) {
                    TaskInputFields(
                        name = editName,
                        category = editCategory,
                        priority = editPriority,
                        nameError = editNameError,
                        categoryError = editCategoryError,
                        priorityError = editPriorityError,
                        onNameChange = {
                            editName = it
                            editNameError = false
                        },
                        onCategoryChange = {
                            editCategory = it
                            editCategoryError = false
                        },
                        onPriorityChange = {
                            editPriority = it
                            editPriorityError = false
                        }
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        editNameError = editName.isBlank()
                        editCategoryError = editCategory.isBlank()

                        val parsedPriority = editPriority.toIntOrNull()

                        editPriorityError =
                            parsedPriority == null || parsedPriority !in 1..3

                        if (
                            editNameError ||
                            editCategoryError ||
                            editPriorityError
                        ) {
                            Toast.makeText(
                                context,
                                "Ellenőrizd a megadott adatokat!",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            onEdit(
                                task.copy(
                                    name = editName.trim(),
                                    category = editCategory.trim(),
                                    priority = parsedPriority!!
                                )
                            )

                            showEditDialog = false
                        }
                    }
                ) {
                    Text("Mentés")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showEditDialog = false
                    }
                ) {
                    Text("Mégse")
                }
            }
        )
    }
}