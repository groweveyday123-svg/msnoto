package com.example.msnoto

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.msnoto.ui.theme.MsnotoTheme
import java.math.BigDecimal
import java.math.RoundingMode

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MsnotoTheme {
                BillingScreen()
            }
        }
    }
}

@Composable
fun BillingScreen() {
    var customer by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var product by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var price by remember { mutableStateOf("") }
    var discount by remember { mutableStateOf("0") }
    var gst by remember { mutableStateOf("18") }
    var result by remember { mutableStateOf("") }

    fun money(value: BigDecimal): String =
        "₹" + value.setScale(2, RoundingMode.HALF_UP).toPlainString()

    @Composable
    fun Field(
        label: String,
        value: String,
        onValueChange: (String) -> Unit,
        numeric: Boolean = false
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = if (numeric)
                    KeyboardType.Decimal else KeyboardType.Text
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Customer Billing", style = MaterialTheme.typography.headlineMedium)

        Field("Customer name", customer, { customer = it })
        Field("Phone number", phone, { phone = it }, true)
        Field("Address", address, { address = it })
        Field("Product name", product, { product = it })
        Field("Quantity", quantity, { quantity = it }, true)
        Field("Unit price (₹)", price, { price = it }, true)
        Field("Discount (₹)", discount, { discount = it }, true)
        Field("GST (%)", gst, { gst = it }, true)

        Button(
            onClick = {
                try {
                    val q = quantity.toBigDecimal()
                    val p = price.toBigDecimal()
                    val d = discount.toBigDecimalOrNull() ?: BigDecimal.ZERO
                    val g = gst.toBigDecimal()

                    if (customer.isBlank() || product.isBlank()) {
                        result = "Enter customer and product names."
                    } else if (
                        q <= BigDecimal.ZERO ||
                        p < BigDecimal.ZERO ||
                        d < BigDecimal.ZERO ||
                        g < BigDecimal.ZERO ||
                        g > BigDecimal("100")
                    ) {
                        result = "Check quantity, price, discount and GST."
                    } else {
                        val subtotal = q.multiply(p)
                        if (d > subtotal) {
                            result = "Discount cannot exceed subtotal."
                        } else {
                            val taxable = subtotal.subtract(d)
                            val tax = taxable.multiply(g).divide(
                                BigDecimal("100"), 2, RoundingMode.HALF_UP
                            )
                            val total = taxable.add(tax)

                            result = """
                                CUSTOMER BILL
                                Customer: $customer
                                Phone: $phone
                                Address: $address
                                Product: $product
                                Quantity: $q
                                Unit price: ${money(p)}
                                Subtotal: ${money(subtotal)}
                                Discount: ${money(d)}
                                GST: ${money(tax)}
                                GRAND TOTAL: ${money(total)}
                            """.trimIndent()
                        }
                    }
                } catch (_: NumberFormatException) {
                    result = "Enter valid numbers for quantity, price, discount and GST."
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Calculate Bill")
        }

        if (result.isNotEmpty()) {
            Text(result, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
