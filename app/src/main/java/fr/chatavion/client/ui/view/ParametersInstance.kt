package fr.chatavion.client.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.chatavion.client.R
import fr.chatavion.client.communityViewModel
import fr.chatavion.client.datastore.SettingsRepository
import fr.chatavion.client.db.entity.Community
import fr.chatavion.client.ui.UiText
import fr.chatavion.client.util.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

/**
 * A Composable function that displays a user parameter screen.
 *
 * @param pseudo The current user's pseudo.
 * @param communityId The ID of the current community.
 * @param onClose A function that is called when the screen is closed.
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun UserParameter(
    pseudo: String,
    communityId: Int,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current

    // A state variable that stores the current pseudo value.
    var current by remember { mutableStateOf(pseudo) }

    // Retrieve the community from the ViewModel based on the given ID.
    val community by communityViewModel.getById(communityId)
        .observeAsState(Community("", "", "", 0))

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colors.background)
            .padding(horizontal = 15.dp, vertical = 30.dp)
    ) {
        Text(
            text = UiText.StringResource(R.string.actual_pseudo).asString(context),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            val buttonWeight = 0.2f
            TextField(
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    color = MaterialTheme.colors.onBackground
                ),
                colors = TextFieldDefaults.textFieldColors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent
                ),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(
                    onDone = {keyboardController?.hide()}),
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .fillMaxWidth(1f - buttonWeight)
                    .semantics {
                        testTagsAsResourceId = true
                    }
                    .testTag("pseudoChangeTextField"),
                value = current.replace(
                    "\n",
                    ""
                ),
                onValueChange = {
                    if (it.length <= 20) current = it
                }
            )
            IconButton(
                onClick = {
                    current = current.trim()
                    if (current != "") {
                        CoroutineScope(IO).launch {
                            community.pseudo = current
                            communityViewModel.insert(community = community)
                        }
                        onClose()
                        Utils.showInfoToast(UiText.StringResource(R.string.username_changed).asString(context), context)
                    } else {
                        Utils.showInfoToast(UiText.StringResource(R.string.username_empty).asString(context), context)
                    }
                },
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .semantics {
                        testTagsAsResourceId = true
                    }
                    .testTag("confirmPseudoChange"),
                content = {
                    Icon(
                        Icons.Filled.Done,
                        "Done",
                        tint = MaterialTheme.colors.onBackground
                    )
                }
            )
        }
        Text(
            color = MaterialTheme.colors.onBackground,
            text = UiText.StringResource(R.string.explication_pseudo_community).asString(context) + " ${community.name}",
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SliderParameterRefreshTime(
    value: Long,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    val settingsRepository = SettingsRepository(context = context)

    var sliderPosition by remember { mutableStateOf(value.toFloat()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colors.background)
            .padding(horizontal = 15.dp, vertical = 30.dp)
    ) {
        Text(
            text = UiText.StringResource(R.string.refresh_time).asString(context),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
        )
        Slider(
            value = sliderPosition,
            valueRange = 10f..300f,
            onValueChange = { sliderPosition = it },
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colors.onPrimary,
                activeTrackColor = MaterialTheme.colors.onPrimary
            ))
        Text(
            text = sliderPosition.toLong().toString() + " " + UiText.StringResource(R.string.second).asString(context),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
        )
        IconButton(
            onClick = {
                CoroutineScope(IO).launch {
                    settingsRepository.setRefreshTime(sliderPosition.toLong())
                }
                onClose()
                Utils.showInfoToast(UiText.StringResource(R.string.refresh_time_changed).asString(context), context)
            },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .semantics {
                    testTagsAsResourceId = true
                }
                .testTag("confirmRefreshTimeChange"),
            content = {
                Icon(
                    Icons.Filled.Done,
                    "Done",
                    tint = MaterialTheme.colors.onBackground
                )
            }
        )
        Text(
            text = UiText.StringResource(R.string.delay_slider_parameter).asString(context),
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SliderParameterAmountMessage(
    value: Int,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    val settingsRepository = SettingsRepository(context = context)

    var sliderPosition by remember { mutableStateOf(value.toFloat()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colors.background)
            .padding(horizontal = 15.dp, vertical = 30.dp)
    ) {
        Text(
            text = UiText.StringResource(R.string.history).asString(context),
            modifier = Modifier
                .align(Alignment.CenterHorizontally),
            textAlign = TextAlign.Center
        )
        Slider(
            value = sliderPosition,
            valueRange = 5f..25f,
            onValueChange = { sliderPosition = it },
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colors.onPrimary,
                activeTrackColor = MaterialTheme.colors.onPrimary
            )
        )
        Text(
            text = sliderPosition.toLong().toString() + " " + UiText.StringResource(R.string.history_message)
                .asString(context),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
        )
        IconButton(
            onClick = {
                CoroutineScope(IO).launch {
                    settingsRepository.setHistoryLoading(sliderPosition.toInt())
                }
                onClose()
                Utils.showInfoToast(
                    UiText.StringResource(R.string.history_amount).asString(context), context
                )
            },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .semantics {
                    testTagsAsResourceId = true
                }
                .testTag("confirmHistoryMessageAmountTimeChange"),
            content = {
                Icon(
                    Icons.Filled.Done,
                    "Done",
                    tint = MaterialTheme.colors.onBackground
                )
            }
        )
        Text(
            text = UiText.StringResource(R.string.loading_history_text)
                .asString(context),
            modifier = Modifier.padding(top = 16.dp)
        )
    }
}