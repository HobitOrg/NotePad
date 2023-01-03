package com.mshdabiola.editscreen.component

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.speech.RecognizerIntent
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import com.mshdabiola.bottomsheet.ModalBottomSheet
import com.mshdabiola.bottomsheet.ModalState
import com.mshdabiola.designsystem.icon.NoteIcon
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBottomSheet(
    modalState: ModalState,
    isNoteCheck: Boolean,
    saveImage: (Uri, Long) -> Unit = { _, _ -> },
    saveVoice: (Uri, String, Long) -> Unit = { _, _, _ -> },
    getPhotoUri: () -> Uri = { Uri.EMPTY },
    savePhoto: () -> Unit = {},
    changeToCheckBoxes: () -> Unit = {}
) {
    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = {
            it?.let {
                Log.e("imageUir", "$it")

                val time = System.currentTimeMillis()
                saveImage(it, time)

            }

        })
    val snapPictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture(),
        onResult = {
            if (it) {
                savePhoto()
                // navigateToEdit(-3, "image text", photoId)
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
                    saveVoice(audiouri, strArr?.joinToString() ?: "", time)

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

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current



    ModalBottomSheet(modalState = modalState) {
        Surface {
            Column(modifier = Modifier.padding(bottom = 36.dp)) {
                NavigationDrawerItem(icon = {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = NoteIcon.Photo),
                        contentDescription = ""
                    )
                }, label = { Text(text = "Take photo") },
                    selected = false, onClick = {
                        snapPictureLauncher.launch(getPhotoUri())
                        coroutineScope.launch { modalState.hide() }
                    })

                NavigationDrawerItem(icon = {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = NoteIcon.Image),
                        contentDescription = ""
                    )
                }, label = { Text(text = "Add image") },
                    selected = false, onClick = {
                        imageLauncher.launch(
                            PickVisualMediaRequest(
                                mediaType = ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                        coroutineScope.launch { modalState.hide() }

                    })
                NavigationDrawerItem(icon = {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = NoteIcon.Brush),
                        contentDescription = ""
                    )
                }, label = { Text(text = "Drawing") },
                    selected = false,
                    onClick = {

                    })
                NavigationDrawerItem(icon = {
                    Icon(
                        imageVector = ImageVector.vectorResource(id = NoteIcon.Voice),
                        contentDescription = ""
                    )
                }, label = { Text(text = "Recording") },
                    selected = false, onClick = {
                        coroutineScope.launch { modalState.hide() }
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
                    })
                if (!isNoteCheck) {
                    NavigationDrawerItem(icon = {
                        Icon(
                            imageVector = ImageVector.vectorResource(id = NoteIcon.Check),
                            contentDescription = ""
                        )
                    }, label = { Text(text = "Checkboxes") },
                        selected = false,
                        onClick = {
                            coroutineScope.launch { modalState.hide() }
                            changeToCheckBoxes()
                        })
                }

            }
        }

    }
}