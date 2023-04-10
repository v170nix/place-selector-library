package net.arwix.placeselectorfeature.ui

import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import net.arwix.placeselectorfeature.parts.edit.location.ui.data.InputTextFieldState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputTextField(
    modifier: Modifier = Modifier,
    textFieldState: InputTextFieldState,
    @StringRes labelId: Int?,
    @StringRes placeholderId: Int? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions()
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TextField(
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            label = {
                if (labelId != null) Text(stringResource(labelId))
            },
            placeholder = { if (placeholderId != null) Text(stringResource(placeholderId)) },
            onValueChange = textFieldState.onValueChange,
            value = textFieldState.value,
            isError = textFieldState.isError,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions
        )

        AnimatedVisibility(visible = textFieldState.isError) {
            Text(
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.error, shape = MaterialTheme.shapes.extraSmall)
                    .padding(4.dp)
                ,
                color = MaterialTheme.colorScheme.onError,
                style = MaterialTheme.typography.labelSmall,
                text = textFieldState.textError
            )
        }
    }

}