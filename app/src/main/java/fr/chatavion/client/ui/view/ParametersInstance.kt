package fr.chatavion.client.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import fr.chatavion.client.R
import fr.chatavion.client.communityViewModel
import fr.chatavion.client.db.entity.Community
import fr.chatavion.client.ui.UiText
import fr.chatavion.client.util.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun UserParameter(
    pseudo: String,
    communityId: Int,
    onClose: () -> Unit,
    heightFraction : Float
) {

    val context = LocalContext.current
    var current by remember { mutableStateOf(pseudo) }
    val community by communityViewModel.getById(communityId).observeAsState(Community("","","", 0))

    Column(modifier = Modifier
        .fillMaxSize()
        .fillMaxHeight(heightFraction)
        .background(MaterialTheme.colors.background)
        .padding(8.dp)
    ) {
        Text(
            color = MaterialTheme.colors.onBackground,
            text = UiText.StringResource(R.string.actual_pseudo).asString()
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
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
                    if (it.length <= 35) current = it
                }
            )
            IconButton(
                onClick = {
                    if(current != "") {
                        CoroutineScope(IO).launch {
                            community.pseudo = current
                            communityViewModel.insert(community = community)
                        }
                        onClose()
                        // TODO Toast username changed
                        Utils.showInfoToast(context.getString(R.string.uNameChanged),context)
                    } else {
                        Utils.showInfoToast(context.getString(R.string.newUNameEmpty), context)
                        // TODO Toast username cannot be empty
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
            text = stringResource(id = R.string.explication_pseudo_community)
        )
    }
}