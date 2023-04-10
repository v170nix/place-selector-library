package net.arwix.placeselectorfeature.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp

@Composable
fun PlaceWizardBottomBarComponent(
    isEnableNextStep: Boolean,
    isShowNextStep: Boolean = true,
    previousName: String,
    nextName: String,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit
) {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .navigationBarsPadding()
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        ) {

            Button(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(48.dp)
                    .semantics { role = Role.Button },
                content = {
                    Text(
                        text = previousName.toUpperCase(Locale.current),
                    )
                },
                onClick = onPreviousClick,
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 6.dp,
                    focusedElevation = 6.dp,
                    hoveredElevation = 8.dp,
                    disabledElevation = 0.dp
                )
            )

            Spacer(modifier = Modifier.width(16.dp))

            if (isShowNextStep) {

                Row(
                    modifier = Modifier
                        .weight(1f),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(48.dp)
                            .semantics { role = Role.Button },
                        content = {
                            Text(
                                text = nextName.toUpperCase(Locale.current),
                            )
                        },
                        onClick = onNextClick,
                        enabled = isEnableNextStep,
                        elevation = ButtonDefaults.buttonElevation(
                            defaultElevation = 6.dp,
                            pressedElevation = 6.dp,
                            focusedElevation = 6.dp,
                            hoveredElevation = 8.dp,
                            disabledElevation = 0.dp
                        )
                    )
                }
            }

        }

    }

}