package com.example.locationlambda.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.example.locationlambda.ui.model.LocationRuleUi
import com.example.locationlambda.ui.theme.CardSurface
import com.example.locationlambda.ui.theme.Divider

@Composable
internal fun RuleList(
    rules: List<LocationRuleUi?>,
    onEditRule: (LocationRuleUi) -> Unit,
    onEditEmptyRule: (Int) -> Unit,
    onToggleRule: (LocationRuleUi, Boolean) -> Unit
) {
    Surface(
        color = CardSurface,
        shape = RoundedCornerShape(28.dp)
    ) {
        Column {
            rules.forEachIndexed { index, rule ->
                if (rule == null) {
                    EmptyRuleRow(
                        onClick = { onEditEmptyRule(index + 1) }
                    )
                } else {
                    RuleRow(
                        rule = rule,
                        onEditRule = onEditRule,
                        onToggleRule = onToggleRule
                    )
                }
                if (index != rules.lastIndex) {
                    HorizontalDivider(color = Divider)
                }
            }
        }
    }
}
