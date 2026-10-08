package com.example.util

import com.example.data.model.CashSessionEntity
import com.example.data.model.CustomerEntity
import com.example.data.model.SaleEntity
import com.example.data.model.SaleItemEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TicketFormatter {
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())

    fun formatSaleTicket(
        sale: SaleEntity,
        items: List<SaleItemEntity>
    ): String {
        val dateStr = dateFormat.format(Date(sale.timestamp))
        val sb = StringBuilder()

        sb.appendLine("================================================")
        sb.appendLine("              FERRETERIA DE LA O                ")
        sb.appendLine("        MATERIALES Y HERRAMIENTAS DE CALIDAD    ")
        sb.appendLine("                 RFC: FLO-140520-9K2            ")
        sb.appendLine("             Av. Las Palmas #248, Centro        ")
        sb.appendLine("                 Tel: (33) 3629-2949            ")
        sb.appendLine("================================================")
        sb.appendLine("FOLIO: ${sale.folio.padEnd(20)} CAJA: 1 (POS-80C)")
        sb.appendLine("FECHA: $dateStr")
        sb.appendLine("CAJERO: ${sale.cashierName.padEnd(19)} ESTACION: DESKTOP-01V")
        sb.appendLine("CLIENTE: ${sale.customerName}")
        sb.appendLine("------------------------------------------------")
        sb.appendLine("CANT  DESCRIPCION                 P.UNIT   TOTAL")
        sb.appendLine("------------------------------------------------")

        for (item in items) {
            val cantStr = String.format(Locale.US, "%.0f %s", item.quantity, item.unit).padEnd(6)
            val desc = if (item.description.length > 20) item.description.take(19) + "." else item.description.padEnd(20)
            val unitStr = String.format(Locale.US, "$%.2f", item.unitPrice).padStart(8)
            val totalStr = String.format(Locale.US, "$%.2f", item.lineTotal).padStart(8)
            sb.appendLine("$cantStr $desc $unitStr $totalStr")
            if (item.description.length > 20) {
                sb.appendLine("      " + item.description.drop(19))
            }
        }

        sb.appendLine("------------------------------------------------")
        sb.appendLine(String.format(Locale.US, "%-32s $%12.2f", "SUBTOTAL (Sin IVA):", sale.subtotal))
        sb.appendLine(String.format(Locale.US, "%-32s $%12.2f", "I.V.A. (16% Trasladado):", sale.taxIva))
        sb.appendLine("================================================")
        sb.appendLine(String.format(Locale.US, "%-28s $%16.2f", "TOTAL A PAGAR:", sale.total))
        sb.appendLine("================================================")
        sb.appendLine("FORMA DE PAGO: ${sale.paymentMethod}")
        if (sale.paymentMethod == "Efectivo") {
            sb.appendLine(String.format(Locale.US, "IMPORTE RECIBIDO:            $%16.2f", sale.amountReceived))
            sb.appendLine(String.format(Locale.US, "CAMBIO:                      $%16.2f", sale.changeGiven))
        } else if (sale.isCredit) {
            sb.appendLine("CONDICION: CREDITO / CUENTA POR COBRAR")
        }
        sb.appendLine("------------------------------------------------")
        sb.appendLine("      ARTICULOS VENDIDOS: ${items.sumOf { it.quantity }.toInt()} PIEZAS")
        sb.appendLine("------------------------------------------------")
        sb.appendLine("   ¡GRACIAS POR SU PREFERENCIA Y CONFIANZA!    ")
        sb.appendLine("   Conserve su comprobante para aclaraciones.   ")
        sb.appendLine("   Garantias válidas con empaque original.      ")
        sb.appendLine("================================================")
        sb.appendLine("        [IMPRESO EN IMPRESORA TERMICA POS-80C]  ")

        return sb.toString()
    }

    fun formatCashCutTicket(
        session: CashSessionEntity,
        isFinalZ: Boolean = true
    ): String {
        val openDateStr = dateFormat.format(Date(session.openTimeMillis))
        val closeDateStr = session.closeTimeMillis?.let { dateFormat.format(Date(it)) } ?: dateFormat.format(Date())
        val cutType = if (isFinalZ) "CORTE DE CAJA 'Z' (CIERRE)" else "CORTE PARCIAL 'X'"

        val sb = StringBuilder()
        sb.appendLine("================================================")
        sb.appendLine("              FERRETERIA DE LA O                ")
        sb.appendLine("            REPORTE DE CORTE DE CAJA            ")
        sb.appendLine("                 $cutType               ")
        sb.appendLine("================================================")
        sb.appendLine("FOLIO SESION : ${session.sessionFolio}")
        sb.appendLine("CAJERO(A)    : ${session.cashierName}")
        sb.appendLine("ESTACION     : ${session.stationName}")
        sb.appendLine("IMPRESORA    : ${session.printerModel}")
        sb.appendLine("APERTURA     : $openDateStr")
        sb.appendLine("CIERRE       : $closeDateStr")
        sb.appendLine("------------------------------------------------")
        sb.appendLine("RESUMEN DE FONDOS Y VENTAS")
        sb.appendLine("------------------------------------------------")
        sb.appendLine(String.format(Locale.US, "%-30s $%14.2f", "(+) FONDO INICIAL DE CAJA:", session.initialFund))
        sb.appendLine(String.format(Locale.US, "%-30s $%14.2f", "(+) VENTAS EN EFECTIVO:", session.totalCashSales))
        sb.appendLine(String.format(Locale.US, "%-30s $%14.2f", "(+) INGRESOS EXTRA / DEPOSITOS:", session.totalDeposits))
        sb.appendLine(String.format(Locale.US, "%-30s $%14.2f", "(-) GASTOS / RETIROS DE CAJA:", session.totalWithdrawals))
        val totalExpectedCash = session.initialFund + session.totalCashSales + session.totalDeposits - session.totalWithdrawals
        sb.appendLine("------------------------------------------------")
        sb.appendLine(String.format(Locale.US, "%-30s $%14.2f", "TOTAL EFECTIVO EN CAJA:", totalExpectedCash))
        sb.appendLine("------------------------------------------------")
        sb.appendLine(String.format(Locale.US, "%-30s $%14.2f", "(+) TARJETAS / CLIP / TRANSF:", session.totalCardSales))
        sb.appendLine(String.format(Locale.US, "%-30s $%14.2f", "(+) VENTAS A CREDITO:", session.totalCreditSales))
        val grandTotal = session.totalCashSales + session.totalCardSales + session.totalCreditSales
        sb.appendLine("================================================")
        sb.appendLine(String.format(Locale.US, "%-26s $%18.2f", "VENTAS TOTALES DEL TURNO:", grandTotal))
        sb.appendLine("================================================")
        if (session.status == "CERRADA" || session.countedCash > 0) {
            sb.appendLine("ARQUEO FISICO (CONTEO REAL)")
            sb.appendLine(String.format(Locale.US, "%-30s $%14.2f", "EFECTIVO CONTADO:", session.countedCash))
            val diff = session.difference
            val diffLabel = when {
                diff > 0 -> "(+) SOBRANTE EN CAJA:"
                diff < 0 -> "(-) FALTANTE EN CAJA:"
                else -> "(=) BALANCE EXACTO:"
            }
            sb.appendLine(String.format(Locale.US, "%-30s $%14.2f", diffLabel, kotlin.math.abs(diff)))
        }
        sb.appendLine("------------------------------------------------")
        sb.appendLine("\n\n")
        sb.appendLine("________________________   _____________________")
        sb.appendLine("     FIRMA CAJERO(A)           FIRMA SUPERVISOR ")
        sb.appendLine("        ${session.cashierName}                     ADMIN")
        sb.appendLine("================================================")

        return sb.toString()
    }

    fun formatPaymentReceipt(
        customer: CustomerEntity,
        amount: Double,
        newBalance: Double,
        cashierName: String,
        folio: String
    ): String {
        val dateStr = dateFormat.format(Date())
        val sb = StringBuilder()
        sb.appendLine("================================================")
        sb.appendLine("              FERRETERIA DE LA O                ")
        sb.appendLine("          RECIBO DE ABONO A CREDITO             ")
        sb.appendLine("================================================")
        sb.appendLine("FOLIO RECIBO : $folio")
        sb.appendLine("FECHA / HORA : $dateStr")
        sb.appendLine("CAJERO(A)    : $cashierName")
        sb.appendLine("CLIENTE      : ${customer.name}")
        sb.appendLine("RFC          : ${customer.rfc}")
        sb.appendLine("------------------------------------------------")
        sb.appendLine(String.format(Locale.US, "%-28s $%16.2f", "SALDO ANTERIOR:", customer.currentBalance))
        sb.appendLine(String.format(Locale.US, "%-28s $%16.2f", "MONTO DEL ABONO:", amount))
        sb.appendLine("================================================")
        sb.appendLine(String.format(Locale.US, "%-28s $%16.2f", "NUEVO SALDO PENDIENTE:", newBalance))
        sb.appendLine("================================================")
        sb.appendLine("LIMITE AUTORIZADO: $" + String.format(Locale.US, "%.2f", customer.creditLimit))
        sb.appendLine("DISPONIBLE: $" + String.format(Locale.US, "%.2f", customer.creditLimit - newBalance))
        sb.appendLine("------------------------------------------------")
        sb.appendLine("           ¡GRACIAS POR SU PUNTUAL PAGO!        ")
        sb.appendLine("================================================")
        return sb.toString()
    }
}
