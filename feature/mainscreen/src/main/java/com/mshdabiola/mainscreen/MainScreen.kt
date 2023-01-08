package com.mshdabiola.mainscreen

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.speech.RecognizerIntent
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.ExperimentalLifecycleComposeApi
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mshdabiola.designsystem.component.ColorDialog
import com.mshdabiola.designsystem.component.NoteCard
import com.mshdabiola.designsystem.component.NotificationDialog
import com.mshdabiola.designsystem.component.state.LabelUiState
import com.mshdabiola.designsystem.component.state.NotePadUiState
import com.mshdabiola.designsystem.component.state.NoteType
import com.mshdabiola.designsystem.component.state.NoteUiState
import com.mshdabiola.designsystem.icon.NoteIcon
import com.mshdabiola.designsystem.theme.NotePadAppTheme
import com.mshdabiola.mainscreen.component.ImageDialog
import com.mshdabiola.mainscreen.component.MainNavigation
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch

@OptIn(ExperimentalLifecycleComposeApi::class)
@Composable
fun MainScreen(
    mainViewModel: MainViewModel = hiltViewModel(),
    navigateToEdit: (Long, String, Long) -> Unit = { _, _, _ -> },
    navigateToLevel: (Boolean) -> Unit,
    navigateToSearch: () -> Unit,
    navigateToSelectLevel: (IntArray) -> Unit
) {

    val mainState = mainViewModel.mainState.collectAsStateWithLifecycle()

    var showDialog by remember {
        mutableStateOf(false)
    }
    var showColor by remember {
        mutableStateOf(false)
    }
    val selectId = remember(mainState.value.notePads) {
        mainState.value.notePads.filter { it.note.selected }.mapNotNull { it.note.id?.toInt() }
            .toIntArray()
    }
    MainScreen(
        notePads = mainState.value.notePads,
        labels = mainState.value.labels,
        navigateToEdit = navigateToEdit,
        navigateToLevel = navigateToLevel,
        navigateToSearch = navigateToSearch,
        saveImage = mainViewModel::savePhoto,
        saveVoice = mainViewModel::saveVoice,
        photoUri = mainViewModel::getPhotoUri,
        currentNoteType = mainState.value.noteType,
        onNavigationNoteType = mainViewModel::setNoteType,
        onSelectedCard = mainViewModel::onSelectCard,
        onClearSelected = mainViewModel::clearSelected,
        setAllPin = mainViewModel::setPin,
        setAllAlarm = { showDialog = true },
        setAllColor = { showColor = true },
        setAllLabel = { navigateToSelectLevel(selectId) }

    )

    val note = remember(mainState.value.notePads) {
        val noOfSelected = mainState.value.notePads.count { it.note.selected }
        if (noOfSelected == 1) {
            mainState.value.notePads.singleOrNull { it.note.selected }?.note
        } else {
            null
        }
    }

    val colorIndex = remember(mainState.value.notePads) {
        val noOfSelected = mainState.value.notePads.count { it.note.selected }
        if (noOfSelected == 1) {
            mainState.value.notePads.singleOrNull { it.note.selected }?.note?.color
        } else {
            null
        }
    }

    NotificationDialog(
        showDialog,
        onDismissRequest = { showDialog = false },
        remainder = note?.reminder ?: -1,
        interval = if (note?.interval == (-1L)) null else note?.interval,
        onSetAlarm = mainViewModel::setAlarm,
        onDeleteAlarm = mainViewModel::deleteAlarm
    )

    ColorDialog(
        show = showColor,
        onDismissRequest = { showColor = false },
        onColorClick = mainViewModel::setAllColor,
        currentColor = colorIndex ?: -1
    )

}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MainScreen(
    notePads: ImmutableList<NotePadUiState>,
    labels: ImmutableList<LabelUiState>,
    navigateToEdit: (Long, String, Long) -> Unit = { _, _, _ -> },
    navigateToLevel: (Boolean) -> Unit = {},
    saveImage: (Uri, Long) -> Unit = { _, _ -> },
    saveVoice: (Uri, Long) -> Unit = { _, _ -> },
    photoUri: (Long) -> Uri = { Uri.EMPTY },
    currentNoteType: NoteType = NoteType.NOTE,
    onNavigationNoteType: (NoteType) -> Unit = {},
    navigateToSearch: () -> Unit = {},
    onSelectedCard: (Long) -> Unit = {},
    onClearSelected: () -> Unit = {},
    setAllPin: () -> Unit = {},
    setAllAlarm: () -> Unit = {},
    setAllColor: () -> Unit = {},
    setAllLabel: () -> Unit = {}
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    val pinScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    var showImageDialog by remember {
        mutableStateOf(false)
    }
    var photoId by remember {
        mutableStateOf(0L)
    }

    val context = LocalContext.current
    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = {
            it?.let {
                Log.e("imageUir", "$it")
                showImageDialog = false
                val time = System.currentTimeMillis()
                saveImage(it, time)
                navigateToEdit(-3, "image text", time)
            }

        })

    val snapPictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = {
            if (it) {
                navigateToEdit(-3, "image text", photoId)
            }
        })


    val voiceLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = {
            it.data?.let { intent ->
                val strArr = intent.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                val audiouri = intent.data


                if (audiouri != null) {
                    val time = System.currentTimeMillis()
                    saveVoice(audiouri, time)
                    Log.e("voice ", "uri $audiouri ${strArr?.joinToString()}")
                    navigateToEdit(-4, strArr?.joinToString() ?: "", time)
                }


            }
        }
    )

    val audioPermission =
        rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission(),
            onResult = {
                if (it) {
                    voiceLauncher.launch(
                        Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(
                                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                            )
                            putExtra(RecognizerIntent.EXTRA_PROMPT, "Speck Now Now")
                            putExtra("android.speech.extra.GET_AUDIO_FORMAT", "audio/AMR")
                            putExtra("android.speech.extra.GET_AUDIO", true)

                        })


                }

            }
        )

    val pinNotePad by remember(notePads) {
        derivedStateOf {
            notePads.partition { it.note.isPin }
        }
    }
//    val notPinNotePad by remember(notePads) {
//        derivedStateOf { notePads.filter { !it.note.isPin } }
//    }

    val coroutineScope = rememberCoroutineScope()

    val noOfSelected = remember(notePads) {
        notePads.count { it.note.selected }
    }
    val isAllPin = remember(notePads) {
        notePads.filter { it.note.selected }
            .all { it.note.isPin }
    }

    ModalNavigationDrawer(
        drawerContent = {
            MainNavigation(
                labels = labels,
                currentType = currentNoteType,
                onNavigation = {
                    onNavigationNoteType(it)
                    coroutineScope.launch { drawerState.close() }
                },
                navigateToLevel = navigateToLevel

            )
        },
        drawerState = drawerState,
        gesturesEnabled = true
    ) {
        Scaffold(
            modifier = Modifier.nestedScroll(if (noOfSelected > 0) pinScrollBehavior.nestedScrollConnection else scrollBehavior.nestedScrollConnection),
            topBar = {

                if (noOfSelected > 0) {
                    SelectTopBar(
                        selectNumber = noOfSelected,
                        isAllPin = isAllPin,
                        scrollBehavior = pinScrollBehavior,
                        onClear = onClearSelected,
                        onPin = setAllPin,
                        onNoti = setAllAlarm,
                        onColor = setAllColor,
                        onLabel = setAllLabel
                    )
                } else {
                    TopAppBar(
                        modifier = Modifier,
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clickable { navigateToSearch() }
                                    .fillMaxWidth()
                                    .padding(4.dp)
                                    .padding(end = 16.dp)
                                    .clip(
                                        RoundedCornerShape(
                                            topEnd = 50f,
                                            topStart = 50f,
                                            bottomEnd = 50f,
                                            bottomStart = 50f
                                        )
                                    )
                                    .background(MaterialTheme.colorScheme.secondaryContainer)
                            ) {
                                IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                                    Icon(
                                        imageVector = Icons.Default.Menu,
                                        contentDescription = "menu"
                                    )
                                }
                                Text(
                                    text = "Search your note",
                                    style = MaterialTheme.typography.titleMedium
                                )


                            }
                        },
                        scrollBehavior = scrollBehavior
                    )
                }


            },
            bottomBar = {
                BottomAppBar(
                    actions = {

                        IconButton(onClick = { navigateToEdit(-2, "", 0) }) {
                            Icon(
                                imageVector = ImageVector
                                    .vectorResource(id = NoteIcon.Check),
                                contentDescription = "note check"
                            )
                        }

                        IconButton(onClick = { /*TODO*/ }) {
                            Icon(
                                imageVector = ImageVector
                                    .vectorResource(id = NoteIcon.Brush),
                                contentDescription = "note check"
                            )
                        }


                        IconButton(onClick = {
                            //navigateToEdit(-4, "", Uri.EMPTY)
                            if (context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) ==
                                PackageManager.PERMISSION_GRANTED
                            ) {
                                voiceLauncher.launch(
                                    Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                        putExtra(
                                            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                                        )
                                        putExtra(RecognizerIntent.EXTRA_PROMPT, "Speck Now Now")
                                        putExtra(
                                            "android.speech.extra.GET_AUDIO_FORMAT",
                                            "audio/AMR"
                                        )
                                        putExtra("android.speech.extra.GET_AUDIO", true)

                                    })

                            } else {
                                audioPermission.launch(Manifest.permission.RECORD_AUDIO)
                            }


                        }) {
                            Icon(
                                imageVector = ImageVector
                                    .vectorResource(id = NoteIcon.Voice),
                                contentDescription = "note check"
                            )
                        }

                        IconButton(onClick = {//
                            showImageDialog = true
                        }) {
                            Icon(
                                imageVector = ImageVector
                                    .vectorResource(id = NoteIcon.Image),
                                contentDescription = "note check"
                            )
                        }

                    },
                    floatingActionButton = {
                        FloatingActionButton(onClick = { navigateToEdit(-1, "", 0) }) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "add note")
                        }
                    }
                )
            }
        ) { paddingValues ->

            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(8.dp)
            ) {
                if (pinNotePad.first.isNotEmpty()) {
                    Text(modifier = Modifier.fillMaxWidth(), text = "Pin")
                    LazyVerticalStaggeredGrid(

                        columns = StaggeredGridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)

                    ) {

                        items(pinNotePad.first) { notePadUiState ->
                            NoteCard(
                                notePad = notePadUiState,
                                onCardClick = {
                                    if (noOfSelected > 0)
                                        onSelectedCard(it)
                                    else
                                        navigateToEdit(it, "", 0)
                                },
                                onLongClick = onSelectedCard
                            )
                        }

                    }
                    if (pinNotePad.second.isNotEmpty()) {
                        Text(text = "Other")
                    }

                }
                LazyVerticalStaggeredGrid(

                    columns = StaggeredGridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)

                ) {

                    items(pinNotePad.second) { notePadUiState ->
                        NoteCard(
                            notePad = notePadUiState,
                            onCardClick = {
                                if (noOfSelected > 0)
                                    onSelectedCard(it)
                                else
                                    navigateToEdit(it, "", 0)
                            },
                            onLongClick = onSelectedCard
                        )
                    }

                }
            }

            ImageDialog(
                show = showImageDialog,
                onDismissRequest = { showImageDialog = false },
                onChooseImage = {
                    imageLauncher.launch(PickVisualMediaRequest(mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                onSnapImage = {
                    photoId = System.currentTimeMillis()
                    snapPictureLauncher.launch(photoUri(photoId))
                }
            )
        }
    }
}


@Preview
@Composable
fun MainScreenPreview() {
    NotePadAppTheme {
        MainScreen(
            notePads =
            listOf(
                NotePadUiState(
                    note = NoteUiState(title = "hammed", detail = "adiola")
                ),
                NotePadUiState(
                    note = NoteUiState(title = "hammed", detail = "adiola", selected = true)
                ),
                NotePadUiState(
                    note = NoteUiState(title = "hammed", detail = "adiola")
                )

            )
                .toImmutableList(),
            labels = emptyList<LabelUiState>().toImmutableList(),
        )
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectTopBar(
    selectNumber: Int = 0,
    isAllPin: Boolean = false,
    scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(),
    onClear: () -> Unit = {},
    onPin: () -> Unit = {},
    onNoti: () -> Unit = {},
    onColor: () -> Unit = {},
    onLabel: () -> Unit = {},

    ) {
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = onClear) {
                Icon(imageVector = Icons.Default.Clear, contentDescription = "clear")
            }
        },
        title = {
            Text(text = "$selectNumber")
        },
        actions = {
            IconButton(onClick = onPin) {
                Icon(
                    painter = painterResource(id = if (isAllPin) NoteIcon.Pin else NoteIcon.PinFill),
                    contentDescription = "pin"
                )
            }
            IconButton(onClick = onNoti) {
                Icon(
                    painter = painterResource(id = NoteIcon.Notification),
                    contentDescription = "notification"
                )
            }
            IconButton(onClick = onColor) {
                Icon(
                    painter = painterResource(id = NoteIcon.ColorLens),
                    contentDescription = "color"
                )
            }
            IconButton(onClick = onLabel) {
                Icon(painter = painterResource(id = NoteIcon.Label), contentDescription = "Label")
            }
            IconButton(onClick = {}) {
                Icon(Icons.Default.MoreVert, contentDescription = "more")
            }
        },
        scrollBehavior = scrollBehavior

    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
fun SelectTopAppBarPreview() {
    SelectTopBar()
}
