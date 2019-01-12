package com.template

import net.corda.core.contracts.*
import net.corda.core.transactions.LedgerTransaction

class InvoiceContract: Contract {

    companion object {
        val ID = "com.template.InvoiceContract"
    }

    interface Commands: CommandData{
        class Issue: TypeOnlyCommandData(),Commands
        class Settle: TypeOnlyCommandData(),Commands
    }

    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<Commands>()
        when (command.value){
            is Commands.Issue -> verifyIssue(tx, command)
            is Commands.Settle -> verifySettle(tx, command)
        }
    }

    private fun verifyIssue(tx: LedgerTransaction, command: CommandWithParties<Commands>) {
        requireThat {
            "Transaction should have zero inputs" using (tx.inputs.isEmpty())
            "Transaction should have one output" using (tx.outputs.size == 1)
            val outputState = tx.outputStates.get(0) as InvoiceState
            //val outputState2 = tx.outputsOfType<InvoiceState>().get(0)
            "Invoice should be signed by the seller" using (command.signers.contains(outputState.seller.owningKey))
            "The invoice amount should be positive" using (outputState.amount > 0)
        }
    }

    private fun verifySettle(tx: LedgerTransaction, command: CommandWithParties<InvoiceContract.Commands>) {
        val outputState = tx.outputStates.get(0) as InvoiceState
        requireThat {
            "Transaction should have one input" using (tx.inputs.size == 1)
            "Transaction should have one output" using (tx.outputs.size == 1)
            "Output should be of type InvoiceState" using (tx.outputStates.first() is InvoiceState)
            "Transaction should be signed by both buyer and seller" using
                    (command.signers.containsAll(listOf(outputState.seller.owningKey,outputState.buyer.owningKey)))
        }
    }

}