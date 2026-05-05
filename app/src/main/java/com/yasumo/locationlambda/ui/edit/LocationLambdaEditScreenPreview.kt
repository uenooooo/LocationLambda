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
        name = "\u6e0b\u8c37\u99c5",
        addressLabel = "\u6771\u4eac\u90fd\u6e0b\u8c37\u533a\u9053\u7384\u57421-1-1",
        areaLabel = "\u901a\u77e5\u534a\u5f84150m",
        transitions = listOf(TransitionUi("\u5230\u7740", EnterBlue)),
        actionTypeLabel = "\u30a2\u30d7\u30ea\u3092\u958b\u304f",
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
