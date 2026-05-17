package com.yasumo.locationlambda.ui.edit

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.yasumo.locationlambda.ui.model.LocationRuleUi
import com.yasumo.locationlambda.ui.model.TransitionUi
import com.yasumo.locationlambda.ui.theme.EnterBlue
import com.yasumo.locationlambda.ui.theme.LocationLambdaTheme

@Preview(showBackground = true)
@Composable
private fun LocationLambdaEditScreenPreview() {
    val previewRule = LocationRuleUi(
        id = "preview",
        name = "渋谷駅",
        addressLabel = "東京都渋谷区道玄坂1-1-1",
        areaLabel = "通知半径150m",
        transitions = listOf(TransitionUi("到着", EnterBlue)),
        actionTypeLabel = "アプリを開く",
        actionTargetLabel = "Teams",
        actionTargetValue = "com.microsoft.teams",
        enabled = true
    )

    LocationLambdaTheme {
        LocationLambdaEditScreen(
            rule = previewRule,
            onBack = {},
            onRuleChange = {}
        )
    }
}
